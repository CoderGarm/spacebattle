package de.yuga.spacebattle.backend.calculator.resource;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.dto.*;
import de.yuga.spacebattle.backend.combat.enums.EDamageImpact;
import de.yuga.spacebattle.backend.combat.enums.EMovementMotivation;
import de.yuga.spacebattle.backend.combat.enums.EMovementType;
import de.yuga.spacebattle.backend.combat.main.Cage;
import de.yuga.spacebattle.backend.combat.maneuver.Maneuver;
import de.yuga.spacebattle.backend.combat.maneuver.ManeuverFactory;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.combat.round.FleetHealthState;
import de.yuga.spacebattle.backend.combat.round.FleetRoundState;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import de.yuga.spacebattle.backend.dto.physics.Direction;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.dto.physics.Velocity;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator.MC_HU;
import static de.yuga.spacebattle.backend.combat.enums.EMovementType.*;
import static de.yuga.spacebattle.backend.combat.round.CombatRound.COMBAT_ROUND;
import static de.yuga.spacebattle.backend.enums.EWeaponAlignment.BROADSIDE;

public class CoursePlot extends Historizable<CoursePlot> implements Cloneable {

    @Nonnull
    private final Cage cage;

    @Nonnull
    private final Fleet agent;

    @Nullable
    private Fleet target;

    @Nonnull
    private CombatRound startingRound;

    @Nonnull
    private Velocity agentsVelocity;

    @Nonnull
    private Direction agentsDirection;

    @Nonnull
    private EMovementMotivation movementMotivation = EMovementMotivation.ESCAPE_MOVEMENT;

    @Nonnull
    private Orbit origin;

    @Nullable
    private Orbit destination;

    @Nullable
    private Direction courseDirection;

    @Nonnull
    private List<CourseOrderElement> courseOrderElements = new ArrayList<>();

    @Nonnull
    private final Map<Fleet, FleetDamageProjectionPerRange> fleetDamages = new HashMap<>();

    @Nullable
    private Maneuver maneuver;

    public CoursePlot(@Nonnull final Cage cage,
                      @Nonnull final Fleet agent,
                      @Nonnull final Orbit position) {
        Preconditions.checkNotNull(cage, "cage shouldn't be null!");
        Preconditions.checkNotNull(agent, "agent shouldn't be null!");
        Preconditions.checkNotNull(position, "position shouldn't be null!");

        this.cage = cage;
        this.agent = agent;
        this.startingRound = cage.getCurrentCombatRound().clone();
        this.agentsVelocity = Velocity.ZERO;
        this.agentsDirection = Direction.ZERO;
        this.origin = position.clone();
    }

    private void setInformationForCreatingPlot(@Nonnull final Orbit agentsOrigin,
                                               @Nonnull final Fleet target,
                                               @Nonnull final Orbit destination,
                                               @Nonnull final EMovementMotivation movementMotivation) {
        Preconditions.checkNotNull(agentsOrigin, "agentsOrigin must not be empty");
        Preconditions.checkNotNull(target, "target shouldn't be null!");
        Preconditions.checkNotNull(destination, "destination shouldn't be null!");
        Preconditions.checkNotNull(movementMotivation, "movementMotivation shouldn't be null!");

        this.target = target;
        this.agentsVelocity = getCurrentVelocity();
        this.agentsDirection = getCurrentDirection();
        this.origin = agentsOrigin;
        this.destination = destination;
        this.courseDirection = new Direction(agentsOrigin, destination);
        this.movementMotivation = movementMotivation;
    }

    public void createEscapeCourse(@Nonnull final Fleet target) {
        Preconditions.checkNotNull(target, "target shouldn't be null!");

        // fixme check if fleet is fast enough to escape sub-light and check if can enter hyperspace

        this.target = target;
        final FleetRoundState agentsState = cage.getCurrentStateByFleet(agent);
        final Orbit agentsPosition = agentsState.getPosition();

        final FleetRoundState targetsState = cage.getCurrentStateByFleet(target);
        final Orbit targetsPosition = targetsState.getPosition();

        final Velocity velocity = agentsState.getVelocity();
        final Acceleration acceleration = agentsState.getAccelerationFor(EModuleType.PROPULSION);

        final Direction direction = new Direction(targetsPosition, agentsPosition);
        final Acceleration negate = acceleration.negate();
        final Velocity resultingVelocity = velocity.getVelocityByAcceleration(negate, COMBAT_ROUND);


        final Distance distanceByTime = acceleration.getDistanceByTime(COMBAT_ROUND, resultingVelocity, EDistanceMetric.LS);
        final Orbit destination = agentsPosition.getDestinationBy(distanceByTime, direction);

        this.agentsVelocity = resultingVelocity;
        this.agentsDirection = direction;
        this.origin = agentsPosition;
        this.destination = targetsPosition;
        this.courseDirection = direction;

        // flee for 50 rounds
        setCourseOrderElements(agentsPosition, destination, INCREASE_DISTANCE, velocity, acceleration, COMBAT_ROUND.multiply(50).getCoordinate());
        // fixme enter hyperspace
    }

    public void createNextAggressiveCourseElement(@Nonnull final Fleet target) {
        Preconditions.checkNotNull(target, "target shouldn't be null!");

        this.target = target;
        final FleetRoundState agentsState = cage.getCurrentStateByFleet(agent);
        final Orbit agentsPosition = agentsState.getPosition();

        final FleetRoundState targetsState = cage.getCurrentStateByFleet(target);
        final Orbit targetsPosition = targetsState.getPosition();

        final Distance distance = agentsPosition.getDistance(targetsPosition);

        final EMovementType movementType = detectMovementType(distance);
        final Velocity velocity = agentsState.getVelocity();
        final Acceleration acceleration = agentsState.getAccelerationFor(EModuleType.PROPULSION);

        Velocity resultingVelocity;
        switch (movementType) {
            case EVASION_MOVEMENT:
            case INCREASE_DISTANCE:
                this.agentsDirection = new Direction(targetsPosition, agentsPosition);
                final Acceleration negate = acceleration.negate();
                resultingVelocity = velocity.getVelocityByAcceleration(negate, COMBAT_ROUND); // fixme unify course plotting and make a reliable speed calculation
                break;
            case IMPELLER_WEDGE_PROTECTION:
            case OFFENSIVE_ROLL:
            case HOLD_DISTANCE:
                this.agentsDirection = Direction.clockWiseHoldRadius(agentsPosition, targetsPosition);
                resultingVelocity = velocity;
                break;
            default:
            case REDUCE_DISTANCE:
                this.agentsDirection = new Direction(agentsPosition, targetsPosition);
                resultingVelocity = velocity.getVelocityByAcceleration(acceleration, COMBAT_ROUND);
                break;
        }
        if (hasViolatedTopSpeed(resultingVelocity)) {
            resultingVelocity = getAgentsTopSpeed();
        }

        final Distance distanceByTime = acceleration.getDistanceByTime(COMBAT_ROUND, resultingVelocity, EDistanceMetric.LS);
        final Orbit destination = agentsPosition.getDestinationBy(distanceByTime, this.agentsDirection);

        this.agentsVelocity = resultingVelocity;
        this.origin = agentsPosition;
        this.destination = targetsPosition;
        this.courseDirection = this.agentsDirection;

        final CombatRound cc = cage.getCurrentCombatRound().clone();
        cc.next();
        addCourseOrder(cc, movementType, resultingVelocity, destination);
    }

    public void createAggressiveCourse() {
        final Orbit origin = cage.getCurrentStateByFleet(cage.getAggressor()).getPosition();
        final Orbit destination = cage.getTarget().getOrbit();

        setInformationForCreatingPlot(origin, cage.getDefender(), destination, EMovementMotivation.INITIATE_COMBAT);
        maneuver = new ManeuverFactory(cage).createInitial();
        cage.attachToChart(cage.getAggressor().getOwner(), maneuver.getCubicBezier());
        courseOrderElements.addAll(maneuver.getCourseOrderElements());
    }

    public void createDefensiveCourse() {
        final Orbit origin = cage.getTarget().getOrbit();
        final Orbit destination = cage.getCurrentStateByFleet(cage.getAggressor()).getPosition();

        setInformationForCreatingPlot(origin, cage.getAggressor(), destination, EMovementMotivation.INITIATE_COMBAT);
        final Maneuver aggressiveManeuver = cage.getCurrentStateByFleet(cage.getAggressor()).getCoursePlot().getManeuver();
        Preconditions.checkNotNull(aggressiveManeuver, "aggressiveManeuver must not be empty");
        maneuver = new ManeuverFactory(cage).createInitialResponseManeuver(aggressiveManeuver);
        cage.attachToChart(cage.getDefender().getOwner(), maneuver.getCubicBezier());
        courseOrderElements.addAll(maneuver.getCourseOrderElements());
    }

    /**
     * Returns the position where the agent is onto a course towards the target and has the best range for damage projection.
     *
     * @param target the target
     * @return the orbit
     */
    @Nullable
    public Orbit getDestinationForBestDamageAtFirstApproach(@Nonnull final Fleet target) {
        Preconditions.checkNotNull(target, "target shouldn't be null!");

        final FleetDamageProjectionPerRange actorInfo = getFleetDamageProjectionPerRange(agent);
        final FleetDamageProjectionPerRange targetInfo = getFleetDamageProjectionPerRange(target);

        final DamagePerRangePerAlignment bestDamagePotential = actorInfo.getDistanceWithBestDamageAgainst(targetInfo);
        if (bestDamagePotential == null) {
            return null;
        }
        final RangeDefinition bestDamagePotentialRangeDefinition = bestDamagePotential.getRangeDefinition();
        final Distance minRange = bestDamagePotentialRangeDefinition.getMinRange();
        final Distance maxRange = bestDamagePotentialRangeDefinition.getMaxRange();
        final Distance difference = maxRange.subtract(minRange);
        final BigDecimal divide = difference.getCoordinate().divide(BigDecimal.valueOf(2), MC_HU);
        // state distance from target
        final Distance bestDistanceToTarget = new Distance(divide, difference.getDistanceMetric());

        final FleetRoundState agentsState = cage.getCurrentStateByFleet(agent);
        final FleetRoundState targetsState = cage.getCurrentStateByFleet(target);

        final Orbit agentsPosition = agentsState.getPosition().clone();
        final Orbit targetsPosition = targetsState.getPosition().clone();

        // assume enemies movement and set up destination accordingly!
        final Orbit destination = getFirstIntersection(agentsPosition, agentsState, targetsPosition, targetsState, 1);

        // state direction from target
        final Direction direction = new Direction(destination, agentsPosition);
        return destination.getDestinationBy(bestDistanceToTarget, direction);
    }

    @Nonnull
    private Orbit getFirstIntersection(@Nonnull final Orbit agentsPosition,
                                       @Nonnull final FleetRoundState agentsState,
                                       @Nonnull final Orbit targetsPosition,
                                       @Nonnull final FleetRoundState targetsState,
                                       final int amountOfRounds) {
        Preconditions.checkNotNull(agentsPosition, "agentsPosition shouldn't be null!");
        Preconditions.checkNotNull(agentsState, "agentsState shouldn't be null!");
        Preconditions.checkNotNull(targetsPosition, "targetsPosition shouldn't be null!");
        Preconditions.checkNotNull(targetsState, "targetsState shouldn't be null!");


        final Distance agentsMobility = agentsState.getMobilityForDirection(new Direction(agentsPosition, targetsPosition), amountOfRounds);
        final Distance targetsMobility = agentsState.getMobilityForDirection(new Direction(targetsPosition, agentsPosition), amountOfRounds);

        final List<Orbit> circleIntersectionPoints = getCircleIntersectionPoints(agentsPosition, agentsMobility, targetsPosition, targetsMobility);
        if (circleIntersectionPoints.isEmpty()) {
            return getFirstIntersection(agentsPosition, agentsState, targetsPosition, targetsState, amountOfRounds + 1);
        }
        final Orbit one = circleIntersectionPoints.get(0);
        if (circleIntersectionPoints.size() == 1) {
            return one;
        }
        final Orbit two = circleIntersectionPoints.get(1);

        final Orbit locationVector = two.subtract(one);
        return locationVector.divide(2);
    }

    /**
     * Calculates the intersection points of two circles.
     *
     * @param centerA center of circle a
     * @param radiusA the radius of circle a
     * @param centerB center of circle b
     * @param radiusB the radius of circle b
     * @return all existing intersection coordinates
     */
    @Nonnull
    private List<Orbit> getCircleIntersectionPoints(@Nonnull final Orbit centerA, @Nonnull final Distance radiusA, @Nonnull final Orbit centerB, @Nonnull final Distance radiusB) {
        Preconditions.checkNotNull(centerA, "centerA shouldn't be null!");
        Preconditions.checkNotNull(radiusA, "radiusA shouldn't be null!");
        Preconditions.checkNotNull(centerB, "centerB shouldn't be null!");
        Preconditions.checkNotNull(radiusB, "radiusB shouldn't be null!");

        final Orbit locationVectorAB = centerB.subtract(centerA);
        final Distance AB0 = locationVectorAB.getXCoordinate();
        final Distance AB1 = locationVectorAB.getYCoordinate();
        final Distance distance = centerA.getDistance(centerB);
        final List<Orbit> intersectionCoordinates = new ArrayList<>();
        if (distance.compareTo(Distance.ZERO) == 0) {
            // no distance between centers
            return intersectionCoordinates;
        }
        final Distance x = (radiusA.pow(2).add(distance.pow(2)).subtract(radiusB.pow(2))).divide(distance.add(distance));
        Distance y = radiusA.pow(2).subtract(x.pow(2));
        if (y.compareTo(Distance.ZERO) < 0) {
            // no intersection
            return intersectionCoordinates;
        }
        if (y.compareTo(Distance.ZERO) > 0) {
            y = y.sqrt();
        }
        // compute unit vectors ex and ey
        final Distance ex0 = AB0.divide(distance);
        final Distance ex1 = AB1.divide(distance);
        final Distance ey0 = ex1.negate();
        final Distance ey1 = ex0.clone();
        Distance Q1x = centerA.getXCoordinate().add(x.multiply(ex0));
        Distance Q1y = centerA.getYCoordinate().add(x.multiply(ex1));
        if (y.compareTo(Distance.ZERO) == 0) {
            // one touch point
            intersectionCoordinates.add(new Orbit(Q1x, Q1y));
            return intersectionCoordinates;
        }
        // two intersections
        final Distance Q2x = Q1x.subtract(y.multiply(ey0));
        final Distance Q2y = Q1y.subtract(y.multiply(ey1));
        Q1x = Q1x.add(y.multiply(ey0));
        Q1y = Q1y.add(y.multiply(ey1));
        intersectionCoordinates.add(new Orbit(Q1x, Q1y));
        intersectionCoordinates.add(new Orbit(Q2x, Q2y));
        return intersectionCoordinates;
    }

    private void setCourseOrderElements(@Nonnull final Orbit origin,
                                        @Nonnull final Orbit destination,
                                        @Nonnull final EMovementType movementType,
                                        @Nonnull final Velocity agentsCurrentVelocity,
                                        @Nonnull final Acceleration acceleration,
                                        @Nonnull final BigDecimal travelDuration) {
        Preconditions.checkState(this.target != null, "target shouldn't be null!");
        Preconditions.checkNotNull(agentsCurrentVelocity, "agentsCurrentVelocity shouldn't be null!");
        Preconditions.checkNotNull(origin, "origin shouldn't be null!");
        Preconditions.checkNotNull(destination, "destination shouldn't be null!");
        Preconditions.checkNotNull(movementType, "movementType shouldn't be null!");
        Preconditions.checkNotNull(acceleration, "acceleration shouldn't be null!");
        Preconditions.checkNotNull(travelDuration, "travelDuration shouldn't be null!");


        final int roundsToTravel = travelDuration.intValue() / CombatRound.COMBAT_ROUND_DURATION;
        // calculate desired direction by origin and destination
        final Direction courseDirection = new Direction(origin, destination);
        // calculate difference from current direction to desired direction
        final BigDecimal alignmentFactor = getCurrentDirection().getAlignmentFactor(courseDirection);
        // calculate vector velocity by difference in directions -> change initialVelocity
        final CombatRound combatRound = getUpcomingRound();
        Velocity initialVelocity = agentsCurrentVelocity.getByAlignmentFactor(alignmentFactor);
        cage.logMessage("rounds to travel '" + roundsToTravel + "' for " + getAgent().getName());
        for (int i = 1; i <= roundsToTravel; i++) {
            final CourseOrderElement latestCourseElement = getLatestCourseElement(); // fixme the speed must be decelerated - is done by alignment factor?
            final Orbit position = latestCourseElement != null ? latestCourseElement.getPosition() : origin;
            final Distance distanceByTime = acceleration.getDistanceByTime(COMBAT_ROUND, initialVelocity, EDistanceMetric.LS);
            Velocity resultingVelocity = initialVelocity.getVelocityByAcceleration(acceleration, COMBAT_ROUND);
            final Velocity topSpeed = getAgentsTopSpeed();
            if (hasViolatedTopSpeed(resultingVelocity)) {
                resultingVelocity = topSpeed;
            }
            // calculate resulting orbit
            final Orbit stepDestination = position.getDestinationBy(distanceByTime, courseDirection);
            addCourseOrder(combatRound.clone(), movementType, resultingVelocity, stepDestination);
            initialVelocity = resultingVelocity;
            combatRound.next();
        }
    }

    private boolean hasViolatedTopSpeed(@Nonnull final Velocity velocity) {
        Preconditions.checkNotNull(velocity, "velocity must not be empty");

        final Velocity topSpeed = getAgentsTopSpeed();
        return velocity.compareTo(topSpeed) > 0;
    }

    @Nonnull
    private Velocity getAgentsTopSpeed() {
        return Velocity.SOL.multiply(BigDecimal.valueOf(agent.getRestrictingTechnologyType().getMaxVelocitySOL()));
    }

    public void addCourseOrder(@Nonnull final CombatRound combatRound,
                               @Nonnull final EMovementType movementType,
                               @Nonnull final Velocity velocity,
                               @Nonnull final Orbit destination) {
        Preconditions.checkNotNull(combatRound, "combatRound shouldn't be null!");
        Preconditions.checkNotNull(movementType, "movementType shouldn't be null!");
        Preconditions.checkNotNull(velocity, "velocity shouldn't be null!");
        Preconditions.checkNotNull(destination, "destination shouldn't be null!");

        if (hasViolatedTopSpeed(velocity)) {
            cage.logWarning("VELOCITY VIOLATED from fleet " + agent.getId());
        }

        courseOrderElements.add(new CourseOrderElement(combatRound, movementType, velocity, destination));
    }


    /**
     * Returns or creates the damage projection for the fleet depending on their current state.<br>
     * If no damage were applied in the last round, the state hasn't changed and can be reused.
     *
     * @param fleet the fleet to get the damage projection for
     * @return the damage projection
     */
    @Nonnull
    private FleetDamageProjectionPerRange getFleetDamageProjectionPerRange(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        final FleetRoundState roundState = cage.getCurrentStateByFleet(fleet);
        FleetDamageProjectionPerRange projectionPerRange = fleetDamages.get(fleet);
        if (projectionPerRange == null || cage.isActionHappened()) {
            projectionPerRange = new FleetDamageProjectionPerRange(roundState);
            fleetDamages.put(fleet, projectionPerRange);
        }
        return projectionPerRange;
    }

    /**
     * Calculates the battle weight and the range of the fleets to detect which movement ist the best for the agent.
     *
     * @return the movement type for the next round for fleet one
     */
    @Nonnull
    @VisibleForTesting
    protected EMovementType detectMovementType(@Nonnull final Distance distance) {
        Preconditions.checkNotNull(distance, "distance shouldn't be null!");
        Preconditions.checkNotNull(target, "target shouldn't be null!");

        final FleetDamageProjectionPerRange actorInfo = getFleetDamageProjectionPerRange(agent);
        final FleetDamageProjectionPerRange targetInfo = getFleetDamageProjectionPerRange(target);

        final DamagePerRangePerAlignment bestDamagePotential = actorInfo.getDistanceWithBestDamageAgainst(targetInfo);
        if (bestDamagePotential == null) {
            // if nothing is returned, the fleet should evade until hyperspace
            return EVASION_MOVEMENT;
        }

        EMovementType actorsMovementType = null;
        // calculate offensive movement
        final boolean inBestDamageRange = bestDamagePotential.isInRange(distance);
        if (inBestDamageRange) {
            // if in range, just fire
            actorsMovementType = HOLD_DISTANCE; // todo specify best alignment more precisely - by movement plan?

        } else {
            // calculate movement
            final RangeDefinition rangeDefinition = bestDamagePotential.getRangeDefinition();
            final Distance minRange = rangeDefinition.getMinRange();
            final int minRangeCompare = minRange.compareTo(distance);
            if (minRangeCompare > 0) {
                actorsMovementType = INCREASE_DISTANCE;
            }

            final Distance maxRange = rangeDefinition.getMaxRange();
            final int maxRangeCompare = maxRange.compareTo(distance);
            if (maxRangeCompare < 0) {
                actorsMovementType = REDUCE_DISTANCE;
            }

            final DamageProjectionPerRange maximumPotentialDamage = bestDamagePotential.getMaximumPotentialDamage();
            final EWeaponAlignment alignmentWithBestDamageForRange = actorInfo.getAlignmentWithBestDamageForRange(distance);
            if (alignmentWithBestDamageForRange != null) {
                // damaging the enemy possible
                final DamageProjectionPerRange damageAtRange = actorInfo.getDamageProjectionAtRangeAndAlignment(distance, alignmentWithBestDamageForRange);
                if (damageAtRange != null) {
                    final long dmgQuote = maximumPotentialDamage.getDamageValue() / damageAtRange.getDamageValue();
                    if (dmgQuote > 0.5) { // todo why should roll and fight? fire salvo to test counter measures?
                        // if the agents damage potential is bigger than half the max - just use your weapons
                        final boolean movementMatchesDamageApplication = alignmentWithBestDamageForRange.isAssignableFromMovementType(actorsMovementType);
                        if (!movementMatchesDamageApplication) {
                            actorsMovementType = HOLD_DISTANCE; // todo specify best alignment more precisely - by movement plan?
                        }
                    }
                }
            }
        }

        // calculate defensive movement
        final List<BeamVolley> volleysHittingThisRound = cage.getFlyingBeamVolleysAgainst(agent);
        final List<MissileSalvo> salvosHittingThisRound = cage.getFlyingMissileSalvosAgainst(agent);
        if (!salvosHittingThisRound.isEmpty() || !volleysHittingThisRound.isEmpty()) {
            final FleetRoundState actorsState = cage.getCurrentStateByFleet(agent);
            final FleetHealthState actorsHealthState = actorsState.getFleetHealthState();
            final EDamageImpact lossEstimation = actorsHealthState.estimateLosses(salvosHittingThisRound, volleysHittingThisRound);
            switch (lossEstimation) {
                case NONE:
                case LIGHT:
                    break;
                case DAMAGING:
                    final boolean canAttackWithBroadside = actorInfo.canAttackAtRangeOnSide(distance, BROADSIDE);
                    // on incoming fire - do not show the skirt or let crossing the T
                    actorsMovementType = !canAttackWithBroadside ? IMPELLER_WEDGE_PROTECTION : OFFENSIVE_ROLL;
                    break;
                case HEAVY:
                case BRUTAL:
                case VIOLATING:
                case DEVASTATING:
                    actorsMovementType = IMPELLER_WEDGE_PROTECTION;
                    break;
            }
        }

        if (actorsMovementType == null) {
            throw new NotifyWebUserException("The shit hits the fan and everything is broken!");
        }
        return actorsMovementType;
    }

    /**
     * Returns the round for the next course element.
     *
     * @return the upcoming round
     */
    @Nonnull
    public CombatRound getUpcomingRound() {
        final CourseOrderElement lastElement = getLatestCourseElement();
        if (lastElement == null) {
            return startingRound.clone();
        }
        final CombatRound combatRound = lastElement.getCombatRound().clone();
        combatRound.next();
        return combatRound;
    }

    @Nullable
    public CourseOrderElement getLatestCourseElement() {
        return courseOrderElements.stream().max(Comparator.comparing(CourseOrderElement::getCombatRound)).orElse(null);
    }

    @Nullable
    public CourseOrderElement getCourseElement(@Nonnull final CombatRound combatRound) {
        Preconditions.checkNotNull(combatRound, "combatRound shouldn't be null!");

        return courseOrderElements.stream().filter(elem -> elem.getCombatRound().equals(combatRound)).findFirst().orElse(null);
    }

    @Nonnull
    public Velocity getCurrentVelocity() {
        final CourseOrderElement latestCourseElement = getLatestCourseElement();
        return latestCourseElement != null ? latestCourseElement.getVelocity() : agentsVelocity;
    }

    @Nonnull
    public Direction getCurrentDirection() {
        if (courseOrderElements.size() < 2) {
            return agentsDirection;
        }
        assert courseDirection != null : "If there are course elements then there is a direction, too";
        return courseDirection;
    }

    @Nonnull
    public Fleet getAgent() {
        return agent;
    }

    @Nonnull
    public CombatRound getStartingRound() {
        return startingRound;
    }

    @Nonnull
    public Velocity getAgentsVelocity() {
        return agentsVelocity;
    }

    @Nonnull
    public Direction getAgentsDirection() {
        return agentsDirection;
    }

    @Nonnull
    public EMovementMotivation getMovementMotivation() {
        return movementMotivation;
    }

    @Nonnull
    public Orbit getOrigin() {
        return origin;
    }

    @Nullable
    public Orbit getDestination() {
        return destination;
    }

    @Nonnull
    public List<CourseOrderElement> getCourseOrderElements() {
        return courseOrderElements;
    }


    @Nullable
    public Maneuver getManeuver() {
        return maneuver;
    }

    public void setManeuver(@Nullable final Maneuver maneuver) {
        this.maneuver = maneuver;
    }

    @Override
    public CoursePlot clone() {
        final CoursePlot clone = (CoursePlot) super.clone();
        clone.startingRound = startingRound.clone();
        clone.agentsVelocity = agentsVelocity.clone();
        clone.agentsDirection = agentsDirection.clone();
        clone.origin = origin.clone();
        clone.destination = destination != null ? destination.clone() : null;
        clone.courseDirection = courseDirection != null ? courseDirection.clone() : null;
        clone.courseOrderElements = courseOrderElements.stream().map(CourseOrderElement::clone).collect(Collectors.toList());
        return clone;
    }

    /**
     * Returns whether the course plot has consumed all of its course elements or not.
     *
     * @return <code>true</code> if no course element 'for the future' is left and all of them are already used,<br>
     * <code>false</code> if there are at least one order left for the next round
     */
    public boolean hasPlotExceeded() {
        final boolean courseElementsLeftForFuture = courseOrderElements.stream().anyMatch(el -> !el.isCourseOrderExecuted());
        return !courseElementsLeftForFuture;
    }

    /**
     * Returns whether the plot is completely new or not.
     *
     * @return <code>true</code> if there are no course elements known, <code>false</code> otherwise
     */
    public boolean isFreshPlotWithoutAnyMovement() {
        return courseOrderElements.isEmpty();
    }

    /**
     * Marks the course element as executed and sets some useful information.
     */
    public void executeLatestPendingOrder() {
        final CombatRound currentCombatRound = cage.getCurrentCombatRound();
        final CourseOrderElement courseElement = getCourseElement(currentCombatRound);
        Preconditions.checkState(courseElement != null, "courseElement shouldn't be null!");
        courseElement.executeOrder();
        final FleetRoundState currentStateByFleet = cage.getCurrentStateByFleet(agent);
        final EMovementType movementType = courseElement.getMovementType();
        final Velocity currentVelocity = getCurrentVelocity();
        cage.logMessage(agent.getOwner().getUsername() + " moves " + movementType + " with velocity of " + currentVelocity);
        currentStateByFleet.setMovementType(movementType);
        currentStateByFleet.setDirection(getCurrentDirection());
        currentStateByFleet.setVelocity(currentVelocity);
    }

    public void clearFutureCourseElements() {
        final CombatRound currentCombatRound = cage.getCurrentCombatRound();
        courseOrderElements.removeIf(e -> !e.isCourseOrderExecuted() && e.getCombatRound().compareTo(currentCombatRound) > 0);
    }
}
