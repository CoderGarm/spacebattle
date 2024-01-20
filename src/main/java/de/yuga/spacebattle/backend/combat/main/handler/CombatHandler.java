package de.yuga.spacebattle.backend.combat.main.handler;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.resource.CourseOrderElement;
import de.yuga.spacebattle.backend.calculator.resource.CoursePlot;
import de.yuga.spacebattle.backend.combat.dto.BeamVolley;
import de.yuga.spacebattle.backend.combat.dto.MissileSalvo;
import de.yuga.spacebattle.backend.combat.dto.MovementAction;
import de.yuga.spacebattle.backend.combat.enums.EMovementType;
import de.yuga.spacebattle.backend.combat.main.Cage;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.combat.round.FleetRoundState;
import de.yuga.spacebattle.backend.dto.physics.Direction;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.enums.ECombatPhase;
import de.yuga.spacebattle.backend.enums.ECombatPhase.ECombatSubPhase;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;
import de.yuga.spacebattle.backend.enums.EWeaponType;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.backend.enums.physics.EDistanceMetric.LS;

/**
 * The combat handler will handle every combat related round for the {@link #cage}.<br>
 * <br>
 * Compare: @see <a href="kampfsystem.md#combat system">Combat System</a>
 */
public class CombatHandler {

    @Nonnull
    private final Cage cage;

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

        // todo state and execute movement by initiative
        // state and execute movement
        createCoursePlot(fleetOne, fleetTwo);
        createCoursePlot(fleetTwo, fleetOne);

        // execute movement
        executeMovement(fleetOne);
        executeMovement(fleetTwo);

        final Distance distance = cage.getCurrentStateByFleet(fleetOne).getPosition().getDistance(cage.getCurrentStateByFleet(fleetTwo).getPosition());
        cage.logMessage("2 #" + cage.getCurrentCombatRound().getNo() + "\t\t - " + distance.getCoordinateInMetric(LS) + " LS");
    }

    private void createCoursePlot(@Nonnull final Fleet agent, @Nonnull final Fleet target) {
        Preconditions.checkNotNull(agent, "agent shouldn't be null!");
        Preconditions.checkNotNull(target, "target shouldn't be null!");

        final FleetRoundState agentsState = cage.getCurrentStateByFleet(agent);
        final FleetRoundState targetsState = cage.getCurrentStateByFleet(target);

        final Distance agentsMissileRange = agentsState.getMaximumWeaponRangePerType(EWeaponType.MISSILE);
        final Distance targetsMissileRange = targetsState.getMaximumWeaponRangePerType(EWeaponType.MISSILE);
        if (agentsMissileRange.compareTo(Distance.ZERO) == 0 && targetsMissileRange.compareTo(Distance.ZERO) == 0) {
            cage.logMessage("Out of ammo");
        }

        final CoursePlot agentsCoursePlot = agentsState.getCoursePlot();
        final boolean creatingCoursePlotNeeded = agentsCoursePlot.isFreshPlotWithoutAnyMovement();
        final boolean hasPlotToBeRepainted = agentsCoursePlot.hasPlotExceeded();
        final boolean ableToAttack = agentsState.isAbleToAttack();
        if (!ableToAttack && (creatingCoursePlotNeeded || hasPlotToBeRepainted || cage.isActionHappened())) {
            // actor has no weapons
            agentsCoursePlot.createEscapeCourse(target);
            return;
        }
        // the current plan is to reach the best attack distance and then create the course round by round
        if (creatingCoursePlotNeeded) {
            // plot the course to attack the target
            agentsCoursePlot.createAggressiveCourse(target);
        } else if (hasPlotToBeRepainted) {
            // plot the next round's course for the upcoming round
            agentsCoursePlot.createNextAggressiveCourseElement(target);
        } else if (cage.isActionHappened()) {
            // plot the next round's course for the upcoming round
            agentsCoursePlot.clearFutureCourseElements();
            agentsCoursePlot.createNextAggressiveCourseElement(target);
        }
    }

    private void executeMovement(@Nonnull final Fleet agent) {
        Preconditions.checkNotNull(agent, "agent shouldn't be null!");

        final FleetRoundState agentsState = cage.getCurrentStateByFleet(agent);
        final CoursePlot coursePlot = agentsState.getCoursePlot();
        final CombatRound currentCombatRound = cage.getCurrentCombatRound();
        final CourseOrderElement courseElement = coursePlot.getCourseElement(currentCombatRound);
        if (courseElement == null) {
            throw new NotifyWebUserException("There should be a course element at round '" + currentCombatRound + "'.");
        }
        final Orbit position;
        if (currentCombatRound.getNo() > 1) {
            final CombatRound previousCombatRound = currentCombatRound.clone();
            previousCombatRound.previous();
            final CourseOrderElement previousCourseElement = coursePlot.getCourseElement(previousCombatRound);
            assert previousCourseElement != null : "The previous element must be present at this place.";
            position = previousCourseElement.getPosition();
        } else {
            position = coursePlot.getOrigin();
        }
        final Orbit interimDestination = courseElement.getPosition().clone();
        final Orbit destination = coursePlot.getDestination();
        assert destination != null : "There should be a destination.";
        final EMovementType movementType = courseElement.getMovementType();
        new MovementAction(cage, agent, movementType, position, interimDestination, destination);
        agentsState.getPosition().moveTo(interimDestination);

        coursePlot.executeLatestPendingOrder();
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
        new ArrayList<>(flyingBeamVolleys)
                .forEach(BeamVolley::applyDamage);
        if (!flyingBeamVolleys.isEmpty()) {
            cage.setActionHappened(true);
        }
    }

    /**
     * Handles the {@link ECombatSubPhase#MISSILE_FIRE_INCOMING_PHASE}.<br>
     * Applies the damage which is transported by the missile salvo.
     */
    @VisibleForTesting
    protected void handleMissileDamage() {
        final List<MissileSalvo> flyingMissileSalvos = cage.getFlyingMissileSalvos();
        new ArrayList<>(flyingMissileSalvos).stream()
                .filter(MissileSalvo::isInDetonationRange)
                .forEach(MissileSalvo::detonate);
        if (!flyingMissileSalvos.isEmpty()) {
            cage.setActionHappened(true);
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
     * @param agent  the beam weapon platform
     * @param target the salvo's target
     */
    @VisibleForTesting
    protected void fireBeams(@Nonnull final Fleet agent, @Nonnull final Fleet target) {
        Preconditions.checkNotNull(agent, "agent shouldn't be null!");
        Preconditions.checkNotNull(target, "target shouldn't be null!");

        final FleetRoundState agentsState = cage.getCurrentStateByFleet(agent);
        final Orbit agentsPos = agentsState.getPosition();
        final Direction agentsDirection = agentsState.getDirection();
        final FleetRoundState targetsState = cage.getCurrentStateByFleet(target);
        final Orbit targetPos = targetsState.getPosition();
        final Direction targetDirection = targetsState.getDirection();
        final Distance distance = agentsPos.getDistance(targetPos);

        final Distance maximumBeamRangeOne = agent.getMaximumWeaponRangePerType(EWeaponType.BEAM);
        final boolean isInRange = distance.compareTo(maximumBeamRangeOne) <= 0;

        // both fleets could reach them with their movement in one round - beam weapon range is too small for the endurance of a combat round
        boolean inRangeWhilePassing = false;
        if (!isInRange) {
            final Distance agentsMobility = agentsState.getMobilityForDirection(agentsDirection);
            final Distance targetsMobility = targetsState.getMobilityForDirection(targetDirection);

            final Orbit designatedAgentsPosition = agentsPos.clone().move(EMovementType.REDUCE_DISTANCE, agentsMobility, targetPos);
            final Orbit designatedTargetsPosition = targetPos.clone().move(EMovementType.REDUCE_DISTANCE, targetsMobility, agentsPos);

            final Orbit agentsMove = agentsPos.clone();
            final Orbit targetsMove = targetPos.clone();
            final Distance steps = agentsMobility.divide(maximumBeamRangeOne);

            while (designatedAgentsPosition.getDistance(agentsMove).compareTo(maximumBeamRangeOne) <= 0) {

                final boolean inRange = agentsMove.getDistance(targetsMove).compareTo(maximumBeamRangeOne) <= 0;
                if (inRange) {
                    inRangeWhilePassing = true;
                    break;
                }

                agentsMove.move(EMovementType.REDUCE_DISTANCE, steps, designatedTargetsPosition);
                targetsMove.move(EMovementType.REDUCE_DISTANCE, steps, designatedAgentsPosition);
            }
        }

        final Set<EWeaponAlignment> applicableAlignments = EWeaponAlignment.getApplicableAlignments(agentsPos, agentsDirection, targetPos);
        final boolean isAlignedToFire = agentsState.hasWeaponsForAlignment(applicableAlignments, EWeaponType.BEAM);
        if ((isInRange || inRangeWhilePassing) && isAlignedToFire) {
            cage.logMessage(agent.getOwner().getUsername() + " tries to fire beams for " + applicableAlignments.stream().map(Enum::name).collect(Collectors.joining(", "))
                    + " at range of " + distance);
            cage.addToFlyingBeamVolleys(new BeamVolley(cage, agent, target));
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
        final Direction actorsDirection = actorsState.getDirection();
        final Orbit targetPos = cage.getCurrentStateByFleet(target).getPosition();
        final Distance distance = actorPos.getDistance(targetPos);
        final Distance actorsMaximumMissileRange = actor.getMaximumWeaponRangePerType(EWeaponType.MISSILE);
        // todo real distance-to-chance-to-hit calculation
        final boolean isInRange = distance.compareTo(actorsMaximumMissileRange) <= 0;
        final Set<EWeaponAlignment> applicableAlignments = EWeaponAlignment.getApplicableAlignments(actorPos, actorsDirection, targetPos);
        final boolean isAlignedToFire = actorsState.hasWeaponsForAlignment(applicableAlignments, EWeaponType.MISSILE);
        if (isInRange && isAlignedToFire) {
            final boolean hasShotsLeft = actorsState.getFleetHealthState().hasShotsLeft(EWeaponType.MISSILE);
            if (hasShotsLeft) {
                cage.logMessage(actor.getOwner().getUsername() + " tries to fire missiles for " + applicableAlignments.stream().map(Enum::name).collect(Collectors.joining(", "))
                        + " at range of " + distance);
                cage.addToFlyingMissileSalvos(new MissileSalvo(cage, actor, target, applicableAlignments));
                cage.setActionHappened(true);
            }
        }
    }

}
