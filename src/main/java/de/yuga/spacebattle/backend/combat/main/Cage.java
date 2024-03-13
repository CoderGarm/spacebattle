package de.yuga.spacebattle.backend.combat.main;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.CombatAllowanceCalculator;
import de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator;
import de.yuga.spacebattle.backend.calculator.distance.NavigationCalculator;
import de.yuga.spacebattle.backend.calculator.resource.CoursePlot;
import de.yuga.spacebattle.backend.combat.BattleLogger;
import de.yuga.spacebattle.backend.combat.dto.BattleResult;
import de.yuga.spacebattle.backend.combat.dto.BeamVolley;
import de.yuga.spacebattle.backend.combat.dto.FleetClash;
import de.yuga.spacebattle.backend.combat.dto.MissileSalvo;
import de.yuga.spacebattle.backend.combat.main.handler.CombatHandler;
import de.yuga.spacebattle.backend.combat.maneuver.Maneuver;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.combat.round.FleetHealthState;
import de.yuga.spacebattle.backend.combat.round.FleetRoundState;
import de.yuga.spacebattle.backend.combat.round.WarshipHealthState;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.backend.enums.physics.EDistanceMetric.LS;

/**
 * The cage is the fighting area for a {@link FleetClash} and will handle the complete combat phase on its own.
 */
public class Cage implements Future<Cage> {

    @Nonnull
    private final BattleLogger battleLogger;

    @Nonnull
    private final List<CombatRound> rounds = new ArrayList<>();

    /**
     * The current combat round.
     */
    @Nonnull
    private final CombatRound currentCombatRound;

    /**
     * This indicates if any weapon damage were applied or if something important happened and makes it necessary to recalculating combat stuff.
     */
    private boolean actionHappened = false;

    /**
     * The clash.
     */
    @Nonnull
    private final FleetClash fleetClash;

    @Nonnull
    private final CombatHandler combatHandler;

    /**
     * The first acting fleet.
     */
    @Nonnull
    private final Fleet fleetOne;

    /**
     * The second acting fleet.
     */
    @Nonnull
    private final Fleet fleetTwo;

    /**
     * The intruder.
     */
    @Nonnull
    private final Fleet aggressor;

    /**
     * Yeah, well, the defender...
     */
    @Nonnull
    private final Fleet defender;

    /**
     * The destination of the aggressor. Currentlyn the only acceptable option for a fight.
     */
    @Nonnull
    private final Planet target;

    /**
     * The involved fleets.
     */
    @Nonnull
    private final List<Fleet> participatingFleets;

    @Nonnull
    private final List<MissileSalvo> flyingMissileSalvos = new ArrayList<>();

    @Nonnull
    private final List<BeamVolley> flyingBeamVolleys = new ArrayList<>();

    @Nonnull
    private final List<FleetRoundState> roundStates = new ArrayList<>();

    /**
     * If <code>true</code> the next combat round will not start.
     */
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    private boolean forceDone = false;

    public Cage(@Nonnull final FleetClash fleetClash, @Nonnull final BattleLogger battleLogger) {
        Preconditions.checkNotNull(fleetClash, "fleetClash shouldn't be null!");
        Preconditions.checkNotNull(battleLogger, "battleLogger must not be empty");

        this.fleetClash = fleetClash;
        this.battleLogger = battleLogger;
        this.participatingFleets = fleetClash.getParticipatingFleets();

        final Set<Owner> users = getParticipatingUsers();
        if (!CombatAllowanceCalculator.isCombatAllowed(users)) {
            // todo implement 3-way combat anyhow
            throw new NotifyWebUserException("Yeah probably you couldn't harm yourself!");
        }

        // todo guessing that there are only two participants which are foes
        this.fleetOne = participatingFleets.get(0);
        this.fleetTwo = participatingFleets.get(1);

        this.target = Objects.requireNonNull(fleetClash.getOrbit().getPlanet());
        this.aggressor = fleetOne.isInPlanetaryOrbit() && Objects.equals(Objects.requireNonNull(fleetOne.getOrbit()).getPlanet(), target) ? fleetOne : fleetTwo;
        this.defender = fleetOne.equals(this.aggressor) ? fleetTwo : fleetOne;

        this.currentCombatRound = new CombatRound();

        initiateCombat();
        this.battleLogger.createChart(aggressor.getOwner(), defender.getOwner(), Objects.requireNonNull(fleetClash.getOrbit().getInterplanetaryResultingOrbit()));
        this.combatHandler = new CombatHandler(this);
    }

    @Nonnull
    public Set<Owner> getParticipatingUsers() {
        return participatingFleets.stream().map(Fleet::getOwner).collect(Collectors.toSet());
    }

    @Nonnull
    public Fleet getOpponent(@Nonnull final Fleet agent) {
        Preconditions.checkNotNull(agent, "agent must not be empty");

        return getParticipatingFleets().stream().filter(u -> !u.equals(agent)).findFirst().orElseThrow(() -> new NotifyWebUserException("Nope."));
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        forceDone = true;
        return true;
    }

    @Override
    public boolean isCancelled() {
        return forceDone;
    }

    @Override
    public boolean isDone() {

        if (getCurrentCombatRound().getNo() == 1) {
            // first round - just proceed
            return false;
        }

        final boolean isDone;
        if (forceDone) {
            battleLogger.logMessagePlain("forced battle end");
            isDone = true;
        } else {

            final List<FleetRoundState> currentRoundStates = participatingFleets.stream().map(this::getCurrentStateByFleet).collect(Collectors.toList());
            final List<Boolean> isFightingCapableStates = currentRoundStates.stream().map(FleetRoundState::getFleetHealthState).map(FleetHealthState::isFightingCapable).collect(Collectors.toList());
            final long fightingCount = isFightingCapableStates.stream().filter(aBoolean -> aBoolean).count();
            final boolean noActiveWeapons = flyingMissileSalvos.isEmpty() && flyingBeamVolleys.isEmpty();
            isDone = fightingCount <= 1 && noActiveWeapons;
            if (isDone) {
                battleLogger.logMessage("fighting count <= 1");
                currentRoundStates.forEach(fleetRoundState -> {
                    final String username = fleetRoundState.getFleet().getOwner().getUsername();
                    final Map<WarShip, WarshipHealthState> losses = fleetRoundState.getFleetHealthState().getLosses();
                    final Set<WarshipHealthState> leftOver = fleetRoundState.getFightingWarShips().collect(Collectors.toSet());
                    battleLogger.logMessage(username + " has lost " + losses.size() + " ships and " + leftOver.size() + " ships left.");
                });
            }

            final CombatRound nextRound = currentCombatRound.clone();
            nextRound.next();
            final boolean nextRoundNotPresent = currentRoundStates.stream().anyMatch(roundState -> {
                final CoursePlot coursePlot = roundState.getCoursePlot();
                return !coursePlot.isFreshPlotWithoutAnyMovement() && coursePlot.hasPlotExceeded();
            });
            if (nextRoundNotPresent) {
                battleLogger.logMessagePlain("\nbattle proudly presents by counting stuff\n");
                return true;
            }
        }
        return isDone;
    }

    /**
     * Runs a combat round.<br>
     * ReadMe: <a href="kampfsystem.md">Combat system</a>.<br>
     * <br>
     * <p>
     * fleet movement<br>
     * missile movement<br>
     * incoming weapon fire<br>
     * fire weapons<br>
     * </p>
     */
    @Override
    @Nonnull
    public Cage get() throws InterruptedException, ExecutionException {
        return this;
    }

    @Override
    public Cage get(final long timeout, @Nonnull final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        throw new InterruptedException("Not implemented - use the other call.");
    }

    /**
     * Runs a complete combat phase.<br>
     * ReadMe: <a href="kampfsystem.md">Combat system</a>.<br>
     */
    public void handleCombatPhases() {
        while (!isDone()) {
            executeCombatRound();
        }
    }

    /**
     * Runs the combat round completely.
     */
    @VisibleForTesting
    protected void executeCombatRound() {
        final CombatRound currentCombatRound = getCurrentCombatRound();

        battleLogger.init(currentCombatRound);
        logMessage("#" + currentCombatRound + " new round " + Calendar.getInstance(Locale.GERMANY).getTime());

        long start = System.currentTimeMillis();
        combatHandler.handleMovementPhase();
        logMessage("movement", start, start);

        start = System.currentTimeMillis();
        combatHandler.handleMissilePhase();
        logMessage("missile", start, start);

        start = System.currentTimeMillis();
        combatHandler.handleIncomingWeaponFirePhase();
        logMessage("incoming fire", start, start);

        start = System.currentTimeMillis();
        combatHandler.handleFireWeaponPhase();
        logMessage("fire weapon", start, start);

        /* fixme implement forced battle end at condition
         */
        final int no = currentCombatRound.getNo();
        if (no >= 1000) {
            logMessage("#" + no + " BATTLE FORCED DONE");
            forceDone = true;
        }

        start = System.currentTimeMillis();
        prepareNextCombatRound();

        logMessage("tidy up", start, start);
        battleLogger.closeRound();
    }

    protected void prepareNextCombatRound() {
        Preconditions.checkNotNull(currentCombatRound, "currentCombatRound shouldn't be null!");

        roundStates.forEach(FleetRoundState::createCurrentAuraState);
        rounds.add(currentCombatRound.clone());
        currentCombatRound.next();
    }

    private void initiateCombat() {
        final Orbit positionOnHyperlimit = NavigationCalculator.getPositionOnHyperlimit(Objects.requireNonNull(aggressor.getOrbit()));
        final Orbit defendersPos = Objects.requireNonNull(defender.getOrbit()).getInterplanetaryResultingOrbit();
        Preconditions.checkNotNull(positionOnHyperlimit, "aggressorsPos must not be empty");
        Preconditions.checkNotNull(defendersPos, "defendersPos must not be empty");

        final BigDecimal initialCageRadius = positionOnHyperlimit.getDistance(defendersPos).getCoordinateInMetric(LS);
        battleLogger.logMessagePlain("Initial cage radius: " + initialCageRadius + " LS");

        roundStates.add(new FleetRoundState(this, aggressor, positionOnHyperlimit));
        roundStates.add(new FleetRoundState(this, defender, defendersPos));
    }

    /**
     * Returns the current combat round.
     *
     * @return the current combat round
     */
    @Nonnull
    public CombatRound getCurrentCombatRound() {
        return currentCombatRound;
    }

    /**
     * Returns if something important happened and makes it necessary to recalculating combat stuff.
     *
     * @return if the recalculation of combat stuff is necessary
     */
    public boolean isActionHappened() {
        return actionHappened;
    }

    public void setActionHappened(final boolean actionHappened) {
        this.actionHappened = actionHappened;
    }

    @Nonnull
    public FleetRoundState getAggressorsState() {
        return getCurrentStateByFleet(aggressor);
    }

    @Nonnull
    public FleetRoundState getDefendersState() {
        return getCurrentStateByFleet(defender);
    }

    @Nonnull
    public FleetRoundState getCurrentStateByFleet(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        final FleetRoundState roundState = roundStates.stream()
                .filter(fleetRoundState -> fleetRoundState.isEqualsByFleet(fleet))
                .findFirst()
                .orElse(null);
        if (roundState != null) {
            return roundState;
        }

        battleLogger.logMessage("There is no fleet state for idFleet '" + fleet.getId() + "'.");
        throw new NotifyWebUserException("No state present - please call the administrator.");
    }

    /**
     * Returns all salvos which will hit this round against the given target.
     *
     * @param target the target
     * @return all salvos which will hit this round
     */
    @Nonnull
    public List<MissileSalvo> getFlyingMissileSalvosAgainst(@Nonnull final Fleet target) {
        Preconditions.checkNotNull(target, "target shouldn't be null!");

        final Orbit actorPosition = getCurrentStateByFleet(target).getPosition();
        return flyingMissileSalvos.stream().filter(s -> s.getTarget().equals(target)).filter(s -> {
            final Distance currentDistance = actorPosition.getDistance(s.getCurrentPosition());
            final Distance rangePerCombatRound = s.getRangePerCombatRound();
            final int toTravel = DistanceCalculator.getCombatRoundsToTravel(currentDistance, rangePerCombatRound);
            return toTravel <= 0;
        }).collect(Collectors.toList());
    }

    /**
     * Returns all volleys which will hit this round against the given target.
     *
     * @param target the target
     * @return all volleys which will hit this round
     */
    @Nonnull
    public List<BeamVolley> getFlyingBeamVolleysAgainst(@Nonnull final Fleet target) {
        Preconditions.checkNotNull(target, "target shouldn't be null!");

        return flyingBeamVolleys.stream().filter(s -> s.getTarget().equals(target)).collect(Collectors.toList());
    }

    /**
     * Returns a randomly selected warship of the targeted fleet.
     *
     * @param target the fleet to select from
     * @return the chosen one
     */
    @Nullable
    public WarShip getRandomActiveWarShipOfFleet(@Nonnull final Fleet target) {
        Preconditions.checkNotNull(target, "target shouldn't be null!");

        final Map<WarShip, WarshipHealthState> warshipHealthStates = getCurrentStateByFleet(target).getFleetHealthState().getWarshipHealthStates();

        if (warshipHealthStates.isEmpty()) {
            return null;
        }
        int numberOfAttackedShip = 0;
        if (warshipHealthStates.size() > 1) {
            numberOfAttackedShip = ThreadLocalRandom.current().nextInt(0, warshipHealthStates.size() - 1);
        }
        return new ArrayList<>(warshipHealthStates.keySet()).get(numberOfAttackedShip);
    }

    /**
     * Returns the already sorted rounds.
     */
    @Nonnull
    public List<CombatRound> getRounds() {
        return rounds.stream().sorted(CombatRound::compareTo).collect(Collectors.toList());
    }

    @Nonnull
    public Fleet getFleetOne() {
        return fleetOne;
    }

    @Nonnull
    public Fleet getFleetTwo() {
        return fleetTwo;
    }

    @Nonnull
    public Fleet getAggressor() {
        return aggressor;
    }

    @Nonnull
    public Fleet getDefender() {
        return defender;
    }

    @Nonnull
    public Planet getTarget() {
        return target;
    }

    @Nonnull
    public List<Fleet> getParticipatingFleets() {
        return participatingFleets;
    }

    @Nonnull
    public List<MissileSalvo> getFlyingMissileSalvos() {
        return flyingMissileSalvos;
    }

    @Nonnull
    public List<BeamVolley> getFlyingBeamVolleys() {
        return flyingBeamVolleys;
    }

    public void addToFlyingMissileSalvos(@Nonnull final MissileSalvo missileSalvo) {
        Preconditions.checkNotNull(missileSalvo, "missileSalvo shouldn't be null!");

        flyingMissileSalvos.add(missileSalvo);
    }

    public void addToFlyingBeamVolleys(@Nonnull final BeamVolley beamVolley) {
        Preconditions.checkNotNull(beamVolley, "beamVolley shouldn't be null!");

        flyingBeamVolleys.add(beamVolley);
    }

    @Nonnull
    public List<FleetRoundState> getRoundStates() {
        return roundStates;
    }

    @Nonnull
    public FleetClash getFleetClash() {
        return fleetClash;
    }

    @Nonnull
    public BattleResult getBattleResult() {
        return new BattleResult(this);
    }

    public void logMessage(@Nonnull final String msg, @Nullable final Long start, @Nullable final Long end) {
        Preconditions.checkNotNull(msg, "msg must not be empty");

        battleLogger.logMessage(msg, start, end);
    }

    public void logMessage(@Nonnull final String msg) {
        Preconditions.checkNotNull(msg, "msg must not be empty");

        battleLogger.logMessage(msg);
    }

    public void logWarning(@Nonnull final String msg) {
        Preconditions.checkNotNull(msg, "msg must not be empty");

        battleLogger.logWarning(msg);
    }

    public void logMessagePlain(@Nonnull final String msg) {
        Preconditions.checkNotNull(msg, "msg must not be empty");

        battleLogger.logMessagePlain(msg);
    }

    public void attachToChart(@Nonnull final Owner owner, @Nonnull final Maneuver maneuver) {
        Preconditions.checkNotNull(owner, "owner must not be empty");
        Preconditions.checkNotNull(maneuver, "maneuver must not be empty");

        battleLogger.attachToChart(owner, maneuver);
    }
}
