package de.yuga.spacebattle.backend.combat.maneuver;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator;
import de.yuga.spacebattle.backend.calculator.distance.GeometryCalculator;
import de.yuga.spacebattle.backend.calculator.geometry.CubicBezier;
import de.yuga.spacebattle.backend.calculator.geometry.KinematicInfo;
import de.yuga.spacebattle.backend.calculator.resource.CourseOrderElement;
import de.yuga.spacebattle.backend.combat.dto.MissileSalvo;
import de.yuga.spacebattle.backend.combat.main.Cage;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.combat.round.FleetRoundState;
import de.yuga.spacebattle.backend.dto.physics.*;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import de.yuga.spacebattle.backend.enums.physics.ETimeMetric;

import javax.annotation.Nonnull;

public class ManeuverFactory {

    @Nonnull
    private final Cage cage;

    public ManeuverFactory(@Nonnull final Cage cage) {
        this.cage = Preconditions.checkNotNull(cage, "cage must not be empty");
    }

    /*
     * fixme
     *      1. course plotting by control points needs better control point generation
     */

    @Nonnull
    public Maneuver createMissileTrail(@Nonnull final MissileSalvo volley) {
        Preconditions.checkNotNull(volley, "volley must not be empty");

        final Fleet agent = volley.getActor();
        final Fleet target = volley.getTarget();

        final Acceleration accelerationMax = volley.getAcceleration();

        final FleetRoundState agentState = cage.getCurrentStateByFleet(agent);
        final FleetRoundState targetState = cage.getCurrentStateByFleet(target);

        Orbit targetPosition = cage.getCurrentStateByFleet(target).getPosition().clone();
        final Distance initialDistance = agentState.getPosition().getDistance(targetPosition);
        final Time time = initialDistance.calculateTimeToPass(accelerationMax, Velocity.SOL);
        final int roundsToHit = time.getCoordinateInMetric(ETimeMetric.SECOND)
                .divide(CombatRound.COMBAT_ROUND.getCoordinateInMetric(ETimeMetric.SECOND), DistanceCalculator.MC_HU).intValue();

        final CombatRound hitTime = cage.getCurrentCombatRound().add(roundsToHit);

        final CourseOrderElement courseElement = targetState.getCoursePlot().getCourseElement(hitTime);
        if (courseElement == null) {
            // set expected future position if known
            targetPosition = cage.getCurrentStateByFleet(target).getPosition().clone();
        }

        return new SimpleCourse(
                cage,
                cage.getCurrentCombatRound(),
                agent,
                KinematicInfo.getFrom(agentState).with(volley.getAcceleration()),
                KinematicInfo.getFrom(targetState).with(targetPosition).with(volley.getAcceleration()).with(Velocity.SOL),
                volley,
                target
        ).createCoursePlot();
    }

    @Nonnull
    public Maneuver createInitial() {

        final Fleet agent = cage.getAggressor();
        final Fleet target = cage.getDefender();

        final FleetRoundState agentState = cage.getCurrentStateByFleet(agent);
        final FleetRoundState targetState = cage.getCurrentStateByFleet(target);

        final KinematicInfo agentsKinematicDesignation = KinematicInfo.getFrom(targetState)
                .with(Acceleration.ZERO)
                .with(Velocity.ZERO);

        final KinematicInfo agentsKinematicInitial = KinematicInfo.getFrom(agentState)
                // shifts the position ot of the planets center
                .shiftInPlanetaryOrbit(agentsKinematicDesignation.getPosition());

        return new TimeOptimizedCourse(
                cage,
                cage.getCurrentCombatRound(),
                agent,
                agentsKinematicInitial,
                agentsKinematicDesignation,
                target
        ).createCoursePlot();
    }

    @Nonnull
    public Maneuver createInitialResponseManeuver(@Nonnull final Maneuver oppositionManeuver) {
        Preconditions.checkNotNull(oppositionManeuver, "oppositionManeuver must not be empty");

        // needed information
        final Fleet agent = cage.getDefender();
        final Fleet target = oppositionManeuver.getAgent();

        final FleetRoundState agentState = cage.getCurrentStateByFleet(agent);

        final Orbit agentsPosition = KinematicInfo.getFrom(agentState).getPosition();

        final Orbit targetsInitialPos = oppositionManeuver.getAgentsKinematicInitial().getPosition();
        final Orbit targetsDesignatedPos = oppositionManeuver.getAgentsKinematicDesignated().getPosition();
        final Direction targetsCourseDirection = new Direction(targetsInitialPos, targetsDesignatedPos);
        final Distance targetsTravelDistance = targetsInitialPos.getDistance(targetsDesignatedPos);

        // course calculations
        final CubicBezier combatElement = oppositionManeuver.getCombatElement();
        final Orbit cp1 = new Orbit(combatElement.getCp1(), EDistanceMetric.KM);
        final Orbit cp2 = new Orbit(combatElement.getCp2(), EDistanceMetric.KM);
        final Orbit closerControlPoint = agentsPosition.getDistance(cp1).compareTo(agentsPosition.getDistance(cp2)) < 0 ? cp1 : cp2;
        final Distance halfDistance = targetsTravelDistance.divide(2);

        final Distance aThird = halfDistance.divide(3);
        // state the closer control point of the opponents course as pivotal point for own maneuver
        final Orbit agentsManeuverEnd = closerControlPoint.moveAboutAndGet(aThird.multiply(2), targetsCourseDirection.negate());

        // formulate unmodified maneuver to calculate the modifications which are needed for the executed maneuver to hit the target in a good range
        final Maneuver maneuver = new CrossingTheT(
                cage,
                cage.getCurrentCombatRound(),
                agent,
                KinematicInfo.getFrom(agentState),
                KinematicInfo.getFrom(agentState).with(agentsManeuverEnd),
                target
        ).createCoursePlot();

        // calculate the intersection point to find the arrival time difference in both maneuvers
        long start = System.currentTimeMillis();
        final Orbit intersectionPoint = GeometryCalculator.calculateClosestPoint(maneuver.getCombatElement(), oppositionManeuver.getCombatElement());
        Preconditions.checkNotNull(intersectionPoint, "intersectionPoint must not be empty");
        cage.logMessage("intersect curves", start, System.currentTimeMillis());

        final CombatRound oppositionIntersectionRound = oppositionManeuver.getIntersectionTimeFor(intersectionPoint);
        final CombatRound intersectionRound = maneuver.getIntersectionTimeFor(intersectionPoint);
        Preconditions.checkNotNull(oppositionIntersectionRound, "oppositionIntersectionRound must not be empty");
        Preconditions.checkNotNull(intersectionRound, "intersectionRound must not be empty");

        final int combatRoundDifferenceAtPoint = oppositionIntersectionRound.getNo() - intersectionRound.getNo();
        final int designatedEnd = maneuver.getDesignatedEnd().getNo();
        // calculate the acceleration modifier for this reacting maneuver to arrive nearly at the same round
        final double accelerationModifier = ((double) combatRoundDifferenceAtPoint / designatedEnd);

        final Maneuver result = new CrossingTheT(
                cage,
                cage.getCurrentCombatRound(),
                agent,
                KinematicInfo.getFrom(agentState).withAccelerationModifier(accelerationModifier),
                KinematicInfo.getFrom(agentState).with(agentsManeuverEnd),
                target
        ).createCoursePlot();

        result.setIntersectionPoint(intersectionPoint);
        return result;
    }

    @Nonnull
    public Maneuver createAggressiveResponseManeuver(@Nonnull final Maneuver oppositionManeuver) {
        Preconditions.checkNotNull(oppositionManeuver, "oppositionManeuver must not be empty");

        final Fleet agent = oppositionManeuver.getTarget();
        final Fleet target = oppositionManeuver.getAgent();
        final FleetRoundState agentState = cage.getCurrentStateByFleet(agent);

        final KinematicInfo targetsKinematicDesignated = oppositionManeuver.getAgentsKinematicDesignated();

        final Maneuver result = new CrossingTheT(
                cage,
                cage.getCurrentCombatRound(),
                agent,
                KinematicInfo.getFrom(agentState).with(agentState.getMaxSubLightVelocity()).with(agentState.getAccelerationFor(EModuleType.PROPULSION)),
                targetsKinematicDesignated,
                target
        ).createCoursePlot();


        return result;
    }
}
