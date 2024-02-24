package de.yuga.spacebattle.backend.combat.maneuver;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.distance.GeometryCalculator;
import de.yuga.spacebattle.backend.calculator.geometry.CubicBezier;
import de.yuga.spacebattle.backend.calculator.geometry.KinematicInfo;
import de.yuga.spacebattle.backend.combat.main.Cage;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.combat.round.FleetRoundState;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import de.yuga.spacebattle.backend.dto.physics.Direction;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.dto.physics.Velocity;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;

import javax.annotation.Nonnull;

public class ManeuverFactory {

    @Nonnull
    private final Cage cage;

    public ManeuverFactory(@Nonnull final Cage cage) {
        this.cage = Preconditions.checkNotNull(cage, "cage must not be empty");
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

        final Fleet agent = cage.getDefender();
        final Fleet target = oppositionManeuver.getAgent();

        final FleetRoundState agentState = cage.getCurrentStateByFleet(agent);

        final Orbit agentsPosition = KinematicInfo.getFrom(agentState).getPosition();

        final Orbit targetsInitialPos = oppositionManeuver.getAgentsKinematicInitial().getPosition();
        final Orbit targetsDesignatedPos = oppositionManeuver.getAgentsKinematicDesignated().getPosition();
        final Direction targetsCourseDirection = new Direction(targetsInitialPos, targetsDesignatedPos);
        final Distance targetsTravelDistance = targetsInitialPos.getDistance(targetsDesignatedPos);

        /*
            fixme react to aggressive course

            - state intersection point
            - state intersection time
            - calc acceleration to reach both
            - proceed normally

                1. intersect point of courses by intersection of control points
                2. intersect time by maneuver plot "get combat round for position" for both
                3. time difference base for adapting and recalc second course with higher/lower acceleration (das ist schummeln!)
                4. zweiten kurs mit neuer beschleunigung neu bestimmen

         */

        final CubicBezier combatElement = oppositionManeuver.getCombatElement();
        final Orbit cp1 = new Orbit(combatElement.getCp1(), EDistanceMetric.KM);
        final Orbit cp2 = new Orbit(combatElement.getCp2(), EDistanceMetric.KM);
        final Orbit closerControlPoint = agentsPosition.getDistance(cp1).compareTo(agentsPosition.getDistance(cp2)) < 0 ? cp1 : cp2;
        final Distance halfDistance = targetsTravelDistance.divide(2);

        final Distance aThird = halfDistance.divide(3);
        final Orbit agentsManeuverEnd = closerControlPoint.moveAboutAndGet(aThird.multiply(2), targetsCourseDirection.negate());

        final Maneuver maneuver = new CrossingTheT(
                cage,
                cage.getCurrentCombatRound(),
                agent,
                KinematicInfo.getFrom(agentState),
                KinematicInfo.getFrom(agentState).with(agentsManeuverEnd),
                target
        ).createCoursePlot();

        long start = System.currentTimeMillis();
        final Orbit intersectionPoint = GeometryCalculator.calculateClosestPoint(maneuver.getCombatElement(), oppositionManeuver.getCombatElement());
        cage.logMessage("intersect curves", start, System.currentTimeMillis());

        final CombatRound oppositionIntersectionRound = oppositionManeuver.getIntersectionTimeFor(intersectionPoint);
        final CombatRound intersectionRound = maneuver.getIntersectionTimeFor(intersectionPoint);

        final int combatRoundDifferenceAtPoint = oppositionIntersectionRound.getNo() - intersectionRound.getNo();
        final int designatedEnd = maneuver.getDesignatedEnd().getNo();
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
}
