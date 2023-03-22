package de.yuga.spacebattle.backend.combat.main;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator;
import de.yuga.spacebattle.backend.calculator.distance.Quadrant;
import de.yuga.spacebattle.backend.calculator.resource.CoursePlot;
import de.yuga.spacebattle.backend.combat.BattleLogger;
import de.yuga.spacebattle.backend.combat.dto.*;
import de.yuga.spacebattle.backend.combat.main.handler.CombatHandler;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.combat.round.FleetHealthState;
import de.yuga.spacebattle.backend.combat.round.FleetRoundState;
import de.yuga.spacebattle.backend.combat.round.WarshipHealthState;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(Cage.class);

    @Nonnull
    private final BattleLogger battleLogger;

    /**
     * The multiplier for defining the initial cage diameter by the maximum fleet's weapon ranges.
     */
    private static final BigDecimal INITIAL_CAGE_DIAMETER_MULTIPLIER = BigDecimal.valueOf(2);

    /**
     * If zero, then the multiplier will be chosen for calculation.
     */
    private static final BigDecimal INITIAL_CAGE_DIAMETER = BigDecimal.valueOf(0);

    /**
     * The current combat round.
     */
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

        final Set<User> users = participatingFleets.stream().map(Fleet::getOwner).collect(Collectors.toSet());
        if (users.size() != 2) {
            // todo implement 3-way combat anyhow
            throw new NotifyWebUserException("Yeah probably you couldn't harm yourself!");
        }

        // todo guessing that there are only two participants which are foes
        fleetOne = participatingFleets.get(0);
        fleetTwo = participatingFleets.get(1);

        currentCombatRound = new CombatRound();
        initiateCombat();
        combatHandler = new CombatHandler(this);
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
            System.out.println("forced battle end");
            isDone = true;
        } else {
            final List<FleetRoundState> currentRoundStates = participatingFleets.stream().map(this::getCurrentStateByFleet).collect(Collectors.toList());
            final List<Boolean> isFightingCapableStates = currentRoundStates.stream().map(FleetRoundState::getFleetHealthState).map(FleetHealthState::isFightingCapable).collect(Collectors.toList());
            final long fightingCount = isFightingCapableStates.stream().filter(aBoolean -> aBoolean).count();
            isDone = fightingCount <= 1;
            if (isDone) {
                System.out.println("fighting count <= 1");
            }

            // todo remove this
            final CombatRound nextRound = currentCombatRound.clone();
            nextRound.next();
            final boolean nextRoundNotPresent = currentRoundStates.stream().anyMatch(roundState -> {
                final CoursePlot coursePlot = roundState.getCoursePlot();
                return !coursePlot.isFreshPlotWithoutAnyMovement() && coursePlot.hasPlotExceeded();
            });
            if (nextRoundNotPresent) {
                System.out.println("\nbattle proudly presents by counting stuff\n");
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

    private void log(String msg, final Long start, final Long end) {
        battleLogger.logMessage(msg, start, end);
    }


    /**
     * Runs the combat round completely.
     */
    @VisibleForTesting
    protected void executeCombatRound() {
        final CombatRound currentCombatRound = getCurrentCombatRound();
        log("#" + currentCombatRound + " new round " + Calendar.getInstance(Locale.GERMANY).getTime(), null, null);
        long start = System.currentTimeMillis();
        combatHandler.handleMovementPhase();
        long end = System.currentTimeMillis();
        log("movement", start, end);
        start = System.currentTimeMillis();
        combatHandler.handleMissilePhase();
        end = System.currentTimeMillis();
        log("missile", start, end);
        start = System.currentTimeMillis();
        combatHandler.handleIncomingWeaponFirePhase();
        end = System.currentTimeMillis();
        log("incoming fire", start, end);
        start = System.currentTimeMillis();
        combatHandler.handleFireWeaponPhase();
        end = System.currentTimeMillis();
        log("fire weapon", start, end);

        start = System.currentTimeMillis();
        final int no = currentCombatRound.getNo();
        if (no > 2000) {
            //logIt = true;
        }

        /* todo implement forced battle end at condition
         */
        if (no >= 1000) {
            System.out.println("#" + no + " BATTLE FORCED DONE");
            forceDone = true;
        }

        if (!isDone()) {
            prepareNextCombatRound(currentCombatRound);
        } else {
            // state the last round results
            participatingFleets.forEach(fleet -> getCurrentStateByFleet(fleet).historize());
            // todo run combat until every missile is gone
            // todo create the last combat round with the resulting setup
        }
        end = System.currentTimeMillis();
        log("tidy up", start, end);
        if (no % 10 == 0) {
            final Distance distance = getCurrentStateByFleet(fleetOne).getPosition().getDistance(getCurrentStateByFleet(fleetTwo).getPosition());
            System.out.println("#" + no + "\t\t - " + distance.getCoordinateInMetric(LS) + " LS");
        }
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
     * Creates the first combat round for all participating fleet.<br>
     * Places the participating fleets in opposite corners of the cage.
     */
    private void initiateCombat() {
        final BigDecimal initialCageDiameter = getInitialCageDiameter();
        final BigDecimal initialCageRadius = initialCageDiameter.divide(BigDecimal.valueOf(2), DistanceCalculator.MC_HU);
        System.out.println("Initial cage radius: " + initialCageRadius + " LS");
        final Orbit fleetOneStartingOrbit = DistanceCalculator.createByRadiusAndQuadrant(initialCageRadius, Quadrant.Q1, Planet.PLANET_STANDARD_METRIC);
        final Orbit fleetTwoStartingOrbit = DistanceCalculator.createByRadiusAndQuadrant(initialCageRadius, Quadrant.Q3, Planet.PLANET_STANDARD_METRIC);

        roundStates.add(new FleetRoundState(this, fleetOne, fleetOneStartingOrbit));
        roundStates.add(new FleetRoundState(this, fleetTwo, fleetTwoStartingOrbit));
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
     * Returns the initial diameter of the cage.
     *
     * @return the diameter
     */
    private BigDecimal getInitialCageDiameter() {
        final Distance f1 = fleetOne.getMaximumWeaponRange();
        final Distance f2 = fleetTwo.getMaximumWeaponRange();
        final BigDecimal initialCageDiameter;
        if (INITIAL_CAGE_DIAMETER.compareTo(BigDecimal.ZERO) == 0) {
            final Distance min = f1.min(f2);
            System.out.println("Weapon range: " + min.getCoordinateInMetric(LS) + " LS");
            final BigDecimal coordinateInMetric = min.getCoordinateInMetric(Planet.PLANET_STANDARD_METRIC);
            initialCageDiameter = coordinateInMetric.multiply(INITIAL_CAGE_DIAMETER_MULTIPLIER, DistanceCalculator.MC_HU);
        } else {
            initialCageDiameter = INITIAL_CAGE_DIAMETER;
        }
        return initialCageDiameter;
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
        return roundStates.stream()
                .filter(fleetRoundState -> fleetRoundState.isEqualsByFleetAndRound(currentCombatRound, fleet))
                .findFirst()
                .orElseThrow(() -> {
                    LOGGER.info("There is no fleet state for idFleet '" + fleet.getId() + "'.");
                    return new NotifyWebUserException("No state present - please call the administrator.");
                });
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
}
