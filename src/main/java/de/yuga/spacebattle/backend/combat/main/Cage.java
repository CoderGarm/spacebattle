package de.yuga.spacebattle.backend.combat.main;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.CombatAllowanceCalculator;
import de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator;
import de.yuga.spacebattle.backend.calculator.distance.NavigationCalculator;
import de.yuga.spacebattle.backend.calculator.geometry.CubicBezier;
import de.yuga.spacebattle.backend.calculator.resource.BezierCoursePlot;
import de.yuga.spacebattle.backend.combat.BattleLogger;
import de.yuga.spacebattle.backend.combat.dto.*;
import de.yuga.spacebattle.backend.combat.main.handler.CombatHandler;
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

    /**
     * The multiplier for defining the initial cage diameter by the maximum fleet's weapon ranges.
     */
    private static final BigDecimal INITIAL_CAGE_DIAMETER_MULTIPLIER = BigDecimal.valueOf(2);

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

    @Nonnull
    private final List<FleetRoundState> historyOfRounds = new ArrayList<>();

    @Nonnull
    private final List<MovementAction> historyMovement = new ArrayList<>();

    @Nonnull
    private final List<MissileSalvo> historyOfMissileSalvos = new ArrayList<>();

    @Nonnull
    private final List<BeamVolley> historyOfBeamSalvos = new ArrayList<>();

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

        final Set<Owner> users = participatingFleets.stream().map(Fleet::getOwner).collect(Collectors.toSet());
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
        this.combatHandler = new CombatHandler(this);
        this.battleLogger.createChart(aggressor.getOwner(), defender.getOwner(), fleetClash.getOrbit().getInterplanetaryResultingOrbit());
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
                final BezierCoursePlot coursePlot = roundState.getCoursePlot();
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
        battleLogger.logMessage("#" + currentCombatRound + " new round " + Calendar.getInstance(Locale.GERMANY).getTime());
        long start = System.currentTimeMillis();
        combatHandler.handleMovementPhase();
        long end = System.currentTimeMillis();
        battleLogger.logMessage("movement", start, end);
        start = System.currentTimeMillis();
        combatHandler.handleMissilePhase();
        end = System.currentTimeMillis();
        battleLogger.logMessage("missile", start, end);
        start = System.currentTimeMillis();
        combatHandler.handleIncomingWeaponFirePhase();
        end = System.currentTimeMillis();
        battleLogger.logMessage("incoming fire", start, end);
        start = System.currentTimeMillis();
        combatHandler.handleFireWeaponPhase();
        end = System.currentTimeMillis();
        battleLogger.logMessage("fire weapon", start, end);

        start = System.currentTimeMillis();

        /* fixme implement forced battle end at condition
         */
        final int no = currentCombatRound.getNo();
        if (no >= 1000) {
            logMessage("#" + no + " BATTLE FORCED DONE");
            forceDone = true;
        }

        if (!isDone()) {
            prepareNextCombatRound(currentCombatRound);
        } else {
            // state the last round results
            participatingFleets.forEach(fleet -> getCurrentStateByFleet(fleet).historize());
            // todo create the last combat round with the resulting setup
        }
        end = System.currentTimeMillis();
        battleLogger.logMessage("tidy up", start, end);
        battleLogger.closeRound();
    }

    /**
     * All collection elements which are older than 10 rounds to the older history which is used only for documentation.<br>
     * All not active missile salvos, no matter whether destroyed or detonated, were transferred, too.
     */
    private void transferToLongHistory() {
        final CombatRound currentCombatRound = getCurrentCombatRound();
        final int noOfNoReturn = currentCombatRound.getNo() - 2;

        final List<FleetRoundState> fleetRoundStatesToArchive = roundStates.stream()
                .filter(ma -> ma.getCombatRound().getNo() < noOfNoReturn)
                .collect(Collectors.toList());
        historyOfRounds.addAll(fleetRoundStatesToArchive);
        roundStates.removeAll(fleetRoundStatesToArchive);

        final List<MissileSalvo> missileSalvosToArchive2 = flyingMissileSalvos.stream()
                .filter(ma -> !ma.isActive() || ma.getResult() != null)
                .collect(Collectors.toList());
        flyingMissileSalvos.removeAll(missileSalvosToArchive2);

        final List<BeamVolley> beamVolleysToArchive = flyingBeamVolleys.stream()
                .filter(ma -> ma.getCombatRound().getNo() < noOfNoReturn || ma.getResult() != null)
                .collect(Collectors.toList());
        flyingBeamVolleys.removeAll(beamVolleysToArchive);
    }

    /**
     * Prepares the next combat round.
     *
     * @param currentCombatRound the current round
     */
    @VisibleForTesting
    protected void prepareNextCombatRound(@Nonnull final CombatRound currentCombatRound) {
        Preconditions.checkNotNull(currentCombatRound, "currentCombatRound shouldn't be null!");

        final FleetRoundState stateOne = getCurrentStateByFleet(fleetOne);
        final FleetRoundState stateTwo = getCurrentStateByFleet(fleetTwo);
        currentCombatRound.next();
        // just historizing the state
        new FleetRoundState(this, stateOne);
        new FleetRoundState(this, stateTwo);

        transferToLongHistory();
    }

    /**
     * Creates the first combat round for all participating fleet.
     */
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

    /**
     * Returns the current state for the given fleet.
     *
     * @param fleet the fleet
     * @return the state for the fleet
     */
    @Nonnull
    public FleetRoundState getCurrentStateByFleet(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        final CombatRound currentCombatRound = getCurrentCombatRound();
        final FleetRoundState roundState = roundStates.stream()
                .filter(fleetRoundState -> fleetRoundState.isEqualsByFleetAndRound(currentCombatRound, fleet))
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
            final Distance currentDistance = actorPosition.getDistance(s.getPosition());
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

    @SuppressWarnings("rawtypes")
    public void addHistorizable(@Nonnull final Historizable historizable) {
        Preconditions.checkNotNull(historizable, "historizable shouldn't be null!");

        if (historizable instanceof MovementAction) {
            historyMovement.add(((MovementAction) historizable).clone());
        } else if (historizable instanceof MissileSalvo) {
            historyOfMissileSalvos.add(((MissileSalvo) historizable).clone());
        } else if (historizable instanceof BeamVolley) {
            historyOfBeamSalvos.add(((BeamVolley) historizable).clone());
        } else if (historizable instanceof FleetRoundState) {
            historyOfRounds.add(((FleetRoundState) historizable).clone());
        }
    }

    @Nonnull
    public List<MovementAction> getHistoryMovement() {
        return historyMovement;
    }

    @Nonnull
    public List<MissileSalvo> getHistoryOfMissileSalvos() {
        return historyOfMissileSalvos;
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
    public List<BeamVolley> getHistoryOfBeamSalvos() {
        return historyOfBeamSalvos;
    }

    @Nonnull
    public List<FleetRoundState> getHistoryOfRounds() {
        return historyOfRounds;
    }

    @Nonnull
    public FleetClash getFleetClash() {
        return fleetClash;
    }

    @Nonnull
    public BattleResult getBattleResult() {
        return new BattleResult(this);
    }

    public void logMessage(@Nonnull final String msg) {
        Preconditions.checkNotNull(msg, "msg must not be empty");

        battleLogger.logMessage(msg);
    }

    public void logWarning(@Nonnull final String msg) {
        Preconditions.checkNotNull(msg, "msg must not be empty");

        battleLogger.logWarning(msg);
    }

    public void attachToChart(@Nonnull final Owner owner, @Nonnull final CubicBezier curve) {
        Preconditions.checkNotNull(owner, "owner must not be empty");
        Preconditions.checkNotNull(curve, "curve must not be empty");

        battleLogger.attachToChart(owner, curve);
    }
}
