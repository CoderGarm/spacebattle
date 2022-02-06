package de.yuga.spacebattle.backend.combat.main.handler;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator;
import de.yuga.spacebattle.backend.calculator.distance.NavigationCalculator;
import de.yuga.spacebattle.backend.combat.dto.BeamVolley;
import de.yuga.spacebattle.backend.combat.dto.FleetDamageProjectionPerRange;
import de.yuga.spacebattle.backend.combat.dto.MissileSalvo;
import de.yuga.spacebattle.backend.combat.dto.MovementAction;
import de.yuga.spacebattle.backend.combat.enums.EMovementType;
import de.yuga.spacebattle.backend.combat.main.Cage;
import de.yuga.spacebattle.backend.combat.round.FleetRoundState;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.enums.ECombatPhase;
import de.yuga.spacebattle.backend.enums.ECombatPhase.ECombatSubPhase;
import de.yuga.spacebattle.backend.enums.EWeaponType;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.backend.combat.enums.EMovementType.*;

/**
 * The combat handler will handle every combat related round for the {@link #cage}.<br>
 * <br>
 * Compare: @see <a href="kampfsystem.md#combat system">Combat System</a>
 */
public class CombatHandler {

    @Nonnull
    private final Cage cage;

    /**
     * This indicates if any weapon damage were applied.
     */
    private boolean actionHappened = false;

    private final Map<Fleet, FleetDamageProjectionPerRange> fleetDamages = new HashMap<>();

    public CombatHandler(@Nonnull final Cage cage) {
        Preconditions.checkNotNull(cage, "cage shouldn't be null!");

        this.cage = cage;
    }

    /**
     * Handles the movement of all participating fleets in the {@link ECombatPhase#MOVEMENT_PHASE}.<br>
     * Will result in the new positions for the participating fleets and completes the movement stage.<br>
     * <br>
     * Compare: @see <a href="kampfsystem.md#movement types">Combat System - Movement</a>
     */
    public void handleMovementPhase() {
        final Fleet fleetOne = cage.getFleetOne();
        final Fleet fleetTwo = cage.getFleetTwo();

        final FleetRoundState currentStateOne = cage.getCurrentStateByFleet(fleetOne);
        final FleetRoundState currentStateTwo = cage.getCurrentStateByFleet(fleetTwo);

        final Orbit positionOne = currentStateOne.getPosition();
        final Orbit positionTwo = currentStateTwo.getPosition();

        // detect movement motivation
        final EMovementType movementTypeOne = detectMovementType(fleetOne, fleetTwo);
        final EMovementType movementTypeTwo = detectMovementType(fleetTwo, fleetOne);

        // calculate movements destinations
        final Orbit destinationOne = NavigationCalculator.getDestinationOrbitOfFleetForTargetAtSubLightSpeed(fleetOne, movementTypeOne, positionOne, positionTwo);
        final Orbit destinationTwo = NavigationCalculator.getDestinationOrbitOfFleetForTargetAtSubLightSpeed(fleetTwo, movementTypeTwo, positionTwo, positionOne);

        // execute movement by initiative
        currentStateOne.determineMovementInitiative();
        currentStateTwo.determineMovementInitiative();
        // here should the magic happen: if the first acting fleet yaws to fire the latter acting fleet can yaw the sidewall between both or something like that
        new MovementAction(cage, fleetOne, movementTypeOne, positionOne, destinationOne, positionTwo);
        new MovementAction(cage, fleetTwo, movementTypeTwo, positionTwo, destinationTwo, positionOne);
        // execute movement
        // todo currently, the first fleet will act first
        positionOne.moveTo(destinationOne);
        positionTwo.moveTo(destinationTwo);
    }

    /**
     * Returns or creates the damage projection for the fleet depending on their current state.<br>
     * If no damage were applied in the last round, the state hasn't changed and can be reused.
     *
     * @param fleet      the fleet to get the damage projection for
     * @param roundState the current fleets state
     * @return the damage projection
     */
    @Nonnull
    private FleetDamageProjectionPerRange getFleetDamageProjectionPerRange(@Nonnull final Fleet fleet, @Nonnull final FleetRoundState roundState) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkNotNull(roundState, "roundState shouldn't be null!");

        FleetDamageProjectionPerRange projectionPerRange = fleetDamages.get(fleet);
        if (projectionPerRange == null) {
            projectionPerRange = new FleetDamageProjectionPerRange(roundState);
            fleetDamages.put(fleet, projectionPerRange);
        } else if (actionHappened) {
            projectionPerRange = new FleetDamageProjectionPerRange(roundState);
            fleetDamages.put(fleet, projectionPerRange);
        }
        return projectionPerRange;
    }

    /**
     * Calculates the battle weight and the range of the fleets to detect which movement ist the best for fleet one.
     *
     * @return the movement type for the next round for fleet one
     */
    @VisibleForTesting
    protected EMovementType detectMovementType(@Nonnull final Fleet actor,
                                               @Nonnull final Fleet target) {
        Preconditions.checkNotNull(actor, "actor shouldn't be null!");
        Preconditions.checkNotNull(target, "target shouldn't be null!");

        final Fleet fleetOne = cage.getFleetOne();
        final Fleet fleetTwo = cage.getFleetTwo();

        final FleetRoundState actorsState = cage.getCurrentStateByFleet(fleetOne);
        final FleetRoundState targetsState = cage.getCurrentStateByFleet(fleetTwo);

        final Orbit actorPosition = actorsState.getPosition();
        final Orbit targetPosition = targetsState.getPosition();

        final FleetDamageProjectionPerRange actorInfo = getFleetDamageProjectionPerRange(fleetOne, actorsState);
        final FleetDamageProjectionPerRange targetInfo = getFleetDamageProjectionPerRange(fleetTwo, targetsState);

        final BigDecimal distance = DistanceCalculator.getOrbitalDistance(actorPosition, targetPosition);
        final BigDecimal bestDamageRangeAgentOnResponder = actorInfo.getDistanceWithBestDamageAgainst(targetInfo);

        EMovementType actorsMovementType;
        final int offensiveCompare = bestDamageRangeAgentOnResponder.compareTo(distance);
        if (offensiveCompare < 0) {
            actorsMovementType = REDUCE_DISTANCE;
        } else if (offensiveCompare > 0) {
            actorsMovementType = INCREASE_DISTANCE;
        } else {
            actorsMovementType = HOLD_DISTANCE;
        }

        final List<BeamVolley> volleysHittingThisRound = cage.getFlyingBeamVolleysAgainst(actor);
        final List<MissileSalvo> salvosHittingThisRound = cage.getFlyingMissileSalvosAgainst(actor).stream().filter(s -> {
            final BigDecimal currentDistance = actorPosition.getDistance(s.getPosition());
            final BigDecimal rangePerCombatRound = s.getRangePerCombatRound();
            final int toTravel = DistanceCalculator.getCombatRoundsToTravel(currentDistance, rangePerCombatRound);
            return toTravel <= 0;
        }).collect(Collectors.toList());
        if (!salvosHittingThisRound.isEmpty() || !volleysHittingThisRound.isEmpty()) {
            actorsMovementType = SIDEWALL_PROTECTION;
        }

        /*
        todo
            weapons for movement type by damage potential
            check distance and decide by damage potential per movement type and per incoming damage
         */

        final boolean isInGoodRangeAndNoDefensiveMovement = actorsMovementType == INCREASE_DISTANCE || actorsMovementType == HOLD_DISTANCE;
        if (isInGoodRangeAndNoDefensiveMovement) {
            actorsMovementType = OFFENSIVE_ROLL;
        }

        actorsState.setMovementType(actorsMovementType);
        return actorsMovementType;
    }

    /**
     * Handles the {@link ECombatPhase#MISSILE_PHASE}.<br>
     */
    public void handleMissilePhase() {
        final List<MissileSalvo> flyingMissileSalvos = cage.getFlyingMissileSalvos();
        new ArrayList<>(flyingMissileSalvos).forEach(MissileSalvo::handleMissilePhase);
    }

    /**
     * Handles the {@link ECombatPhase#INCOMING_WEAPON_FIRE_PHASE}.<br>
     * Handles the incoming weapon fire phase.
     */
    public void handleIncomingWeaponFirePhase() {
        actionHappened = false;
        handleMissileDamage();
        handleDirectWeaponDamage();
    }

    /**
     * Handles the {@link ECombatSubPhase#BEAM_FIRE_INCOMING_PHASE}.<br>
     * Applies the damage which is fired directly by the fleets board weapons.
     */
    @VisibleForTesting
    protected void handleDirectWeaponDamage() {
        final List<BeamVolley> flyingBeamVolleys = cage.getFlyingBeamVolleys();
        new ArrayList<>(flyingBeamVolleys).forEach(BeamVolley::applyDamage);
        if (!flyingBeamVolleys.isEmpty()) {
            actionHappened = true;
        }
    }

    /**
     * Handles the {@link ECombatSubPhase#MISSILE_FIRE_INCOMING_PHASE}.<br>
     * Applies the damage which is transported by the missile salvo.
     */
    @VisibleForTesting
    protected void handleMissileDamage() {
        final List<MissileSalvo> flyingMissileSalvos = cage.getFlyingMissileSalvos();
        new ArrayList<>(flyingMissileSalvos).stream().filter(MissileSalvo::isInDetonationRange).forEach(MissileSalvo::detonate);
        if (!flyingMissileSalvos.isEmpty()) {
            actionHappened = true;
        }
    }

    /**
     * Handles the {@link ECombatPhase#FIRE_WEAPONS_PHASE}.<br>
     * Releases all weapons towards the foe.
     */
    public void handleFireWeaponPhase() {
        final Fleet fleetOne = cage.getFleetOne();
        final Fleet fleetTwo = cage.getFleetTwo();

        fireBeams(fleetOne, fleetTwo);
        fireBeams(fleetTwo, fleetOne);
        fireMissiles(fleetOne, fleetTwo);
        fireMissiles(fleetTwo, fleetOne);
    }

    /**
     * Handles the {@link ECombatSubPhase#BEAM_FIRE_PHASE} for the weapon platform.
     *
     * @param actor  the beam weapon platform
     * @param target the salvo's target
     */
    @VisibleForTesting
    protected void fireBeams(@Nonnull final Fleet actor, @Nonnull final Fleet target) {
        Preconditions.checkNotNull(actor, "actor shouldn't be null!");
        Preconditions.checkNotNull(target, "target shouldn't be null!");

        final FleetRoundState actorsState = cage.getCurrentStateByFleet(actor);
        final Orbit actorPos = actorsState.getPosition();
        final Orbit targetPos = cage.getCurrentStateByFleet(target).getPosition();
        final BigDecimal distance = actorPos.getDistance(targetPos);

        final EMovementType actorsMovementType = actorsState.getMovementType();
        final BigDecimal maximumBeamRangeOne = actor.getMaximumWeaponRangePerType(EWeaponType.BEAM);
        final boolean isInRange = distance.compareTo(maximumBeamRangeOne) <= 0;
        // todo check that movement matches to weapons - also in missiles and in movement type detection
        if (isInRange && actorsState.hasWeaponsForAlignment(EWeaponType.BEAM)) {
            cage.addToFlyingBeamVolleys(new BeamVolley(cage, actor, target));
        }
    }

    /**
     * Handles the {@link ECombatSubPhase#MISSILE_FIRE_PHASE} for the actor.
     *
     * @param actor  the missile platform
     * @param target the salvo's target
     */
    @VisibleForTesting
    protected void fireMissiles(@Nonnull final Fleet actor, @Nonnull final Fleet target) {
        Preconditions.checkNotNull(actor, "actor shouldn't be null!");
        Preconditions.checkNotNull(target, "target shouldn't be null!");

        final FleetRoundState actorsState = cage.getCurrentStateByFleet(actor);
        final Orbit actorPos = actorsState.getPosition();
        final Orbit targetPos = cage.getCurrentStateByFleet(target).getPosition();
        final BigDecimal distance = actorPos.getDistance(targetPos);
        final BigDecimal maximumMissileRangeOne = actor.getMaximumWeaponRangePerType(EWeaponType.MISSILE);
        // todo real distance-to-chance-to-hit calculation
        final boolean isInRange = distance.compareTo(maximumMissileRangeOne) <= 0;
        if (isInRange && actorsState.hasWeaponsForAlignment(EWeaponType.MISSILE)) {
            final boolean hasShotsLeft = actorsState.getFleetHealthState().hasShotsLeft();
            if (hasShotsLeft) {
                cage.addToFlyingMissileSalvos(new MissileSalvo(cage, actor, target));
            }
        }
    }
}
