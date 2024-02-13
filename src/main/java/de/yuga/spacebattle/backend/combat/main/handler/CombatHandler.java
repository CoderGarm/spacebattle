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
import de.yuga.spacebattle.backend.combat.maneuver.Maneuver;
import de.yuga.spacebattle.backend.combat.maneuver.ManeuverElement;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.combat.round.FleetRoundState;
import de.yuga.spacebattle.backend.dto.physics.Direction;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
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

        // state and execute movement
        if (cage.getCurrentCombatRound().getNo() == 1) {
            createInitialCoursePlot();
        }

        // fixme add check for "will we have a fight on the current course"
        extendAggressiveCoursePlot(cage.getAggressor(), cage.getDefender());
        // fixme same for defender

        // execute movement
        executeMovement(cage.getAggressor());
        executeMovement(cage.getDefender());

        endCombatByExceededPlot();

        final Orbit aggroPos = cage.getCurrentStateByFleet(cage.getAggressor()).getPosition();
        final Orbit defPos = cage.getCurrentStateByFleet(cage.getDefender()).getPosition();
        cage.logMessage("#" + cage.getCurrentCombatRound().getNo() + "\t\t - " + aggroPos.getDistance(defPos).getCoordinateInMetric(LS) + " LS");
    }

    private void endCombatByExceededPlot() {
        final boolean isExceeded = cage.getParticipatingFleets().stream()
                .map(cage::getCurrentStateByFleet)
                .map(FleetRoundState::getCoursePlot)
                .anyMatch(CoursePlot::hasPlotExceeded);

        if (isExceeded) {
            final List<CoursePlot> notExceededPlots = cage.getParticipatingFleets().stream()
                    .map(cage::getCurrentStateByFleet)
                    .map(FleetRoundState::getCoursePlot)
                    .filter(c -> !c.hasPlotExceeded())
                    .collect(Collectors.toList());
            for (final CoursePlot notExceededPlot : notExceededPlots) {
                notExceededPlot.clearFutureCourseElements();
                notExceededPlot.getManeuver().setEnd(cage.getCurrentCombatRound().clone());
            }
        }
    }

    private void createInitialCoursePlot() {

        final FleetRoundState aggressorsState = cage.getCurrentStateByFleet(cage.getAggressor());
        final FleetRoundState defendersState = cage.getCurrentStateByFleet(cage.getDefender());

        final CoursePlot aggressorsCoursePlot = aggressorsState.getCoursePlot();
        final CoursePlot defendersCoursePlot = defendersState.getCoursePlot();
        final boolean creatingCoursePlotNeeded = aggressorsCoursePlot.isFreshPlotWithoutAnyMovement();
        final boolean hasPlotToBeRepainted = aggressorsCoursePlot.hasPlotExceeded();
        final boolean ableToAttack = aggressorsState.isAbleToAttack();
        if (!ableToAttack && (creatingCoursePlotNeeded || hasPlotToBeRepainted || cage.isActionHappened())) { // fixme auslagern und voran stellen
            // actor has no weapons
            aggressorsCoursePlot.createEscapeCourse();
            return;
        }
        // plot the course to attack the target
        aggressorsCoursePlot.createAggressiveCourse();
        defendersCoursePlot.createDefensiveCourse();
    }

    private void extendAggressiveCoursePlot(@Nonnull final Fleet agent, @Nonnull final Fleet target) {
        Preconditions.checkNotNull(agent, "agent must not be empty");
        Preconditions.checkNotNull(target, "target must not be empty");

        final FleetRoundState aggressorsState = cage.getCurrentStateByFleet(agent);
        final CoursePlot aggressorsCoursePlot = aggressorsState.getCoursePlot();
        final boolean hasPlotToBeRepainted = aggressorsCoursePlot.hasPlotExceeded();

        if (hasPlotToBeRepainted || cage.isActionHappened()) {
            // plot the next round's course for the upcoming round
            // fixme weiter hier aggressorsCoursePlot.createNextAggressiveCourseElement();
            cage.logWarning("Course of '" + agent.getOwner().getUsername() + "' will be refreshed.");
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

        final Orbit interimDestination = courseElement.getPosition().clone();
        final EMovementType movementType = courseElement.getMovementType();
        final Maneuver maneuver = courseElement.getManeuver();
        final ManeuverElement maneuverElement = courseElement.getManeuverElement();
        final Distance lengthOnTrack = courseElement.getLengthOnTrack();
        final MovementAction movementAction = new MovementAction(cage, agent, maneuver, maneuverElement, lengthOnTrack, movementType);
        agentsState.getPosition().moveTo(interimDestination);

        coursePlot.executeLatestPendingOrder();
        if (coursePlot.hasPlotExceeded()) {
            // stop condition
            coursePlot.clearFutureCourseElements();
            maneuver.setEnd(currentCombatRound.clone());
            cage.logMessage("Combat ended by exceeded course from '" + agent.getOwner().getUsername() + "' in round '" + currentCombatRound + "'");
        }

        movementAction.historize();
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

            inRangeWhilePassing = distance.subtract(agentsMobility).subtract(targetsMobility).compareTo(maximumBeamRangeOne) <= 0;
        }

        final Set<EWeaponAlignment> applicableAlignments = EWeaponAlignment.getApplicableAlignments(agentsPos, agentsDirection, targetPos);
        final boolean isAlignedToFire = agentsState.hasWeaponsForAlignment(applicableAlignments, EWeaponType.BEAM);
        if ((isInRange || inRangeWhilePassing) && isAlignedToFire) {
            final String complement = isInRange ? "directly" : "while passing";
            cage.logMessage(agent.getOwner().getUsername() + " tries to fire beams for '" + applicableAlignments.stream().map(Enum::name).collect(Collectors.joining(", ")) + "' "
                    + complement + " at range of " + distance);
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
        final FleetRoundState targetsState = cage.getCurrentStateByFleet(target);

        final Orbit actorPos = actorsState.getPosition();
        final Direction actorsDirection = actorsState.getDirection();
        final Orbit targetPos = targetsState.getPosition();
        final Distance distance = actorPos.getDistance(targetPos);

        // todo real distance-to-chance-to-hit calculation
        final Set<EWeaponAlignment> applicableAlignments = EWeaponAlignment.getApplicableAlignments(actorPos, actorsDirection, targetPos);

        final Set<Missile> applicableMissiles = actorsState.getApplicableMissiles(applicableAlignments).stream()
                .filter(m -> distance.compareTo(m.getMaximumMissileRange(actorsState, targetsState)) <= 0)
                .collect(Collectors.toSet());

        final boolean hasShotsLeft = actorsState.getFleetHealthState().hasShotsLeft(EWeaponType.MISSILE);
        if (!applicableMissiles.isEmpty() && hasShotsLeft) {
            cage.logMessage(actor.getOwner().getUsername() + " tries to fire missiles for " + applicableAlignments.stream().map(Enum::name).collect(Collectors.joining(", "))
                    + " at range of " + distance);
            cage.addToFlyingMissileSalvos(new MissileSalvo(cage, actor, target, applicableAlignments, applicableMissiles));
            cage.setActionHappened(true);
        }
    }

}
