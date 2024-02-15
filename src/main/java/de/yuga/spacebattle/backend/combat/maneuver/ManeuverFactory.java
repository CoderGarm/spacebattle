package de.yuga.spacebattle.backend.combat.maneuver;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.geometry.CubicBezier;
import de.yuga.spacebattle.backend.calculator.geometry.KinematicInfo;
import de.yuga.spacebattle.backend.combat.main.Cage;
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
    public Maneuver createInitialResponseManeuver(@Nonnull final Maneuver maneuver) {
        Preconditions.checkNotNull(maneuver, "maneuver must not be empty");

        final Fleet agent = cage.getDefender();
        final Fleet target = maneuver.getAgent();

        final FleetRoundState agentState = cage.getCurrentStateByFleet(agent);

        final Orbit agentsPosition = KinematicInfo.getFrom(agentState).getPosition();

        final Orbit targetsInitialPos = maneuver.getAgentsKinematicInitial().getPosition();
        final Orbit targetsDesignatedPos = maneuver.getAgentsKinematicDesignated().getPosition();
        final Direction targetsCourseDirection = new Direction(targetsInitialPos, targetsDesignatedPos);
        final Distance targetsTravelDistance = targetsInitialPos.getDistance(targetsDesignatedPos);

        /*
            create curves and add them as start or end curve
            gegnerisches manöver bekannt, eigener plan bekannt -> ausgangspunkt für eigenen plan aus gegnermanöver berechnen

            - take closer control point of time-optimized-course as geometric center
            - calc p1, p2 based on distance enemy-CP to own P1 by setting enemy-CP in 2/3 of broadside passing baseline
         */

        /*
            fixme react to aggressive course

            - state intersection point
            - state intersection time
            - calc acceleration to reach both
            - proceed normally
         */

        final CubicBezier combatElement = maneuver.getCombatElement();
        final Orbit cp1 = new Orbit(combatElement.getCp1(), EDistanceMetric.KM);
        final Orbit cp2 = new Orbit(combatElement.getCp2(), EDistanceMetric.KM);
        final Orbit closerControlPoint = agentsPosition.getDistance(cp1).compareTo(agentsPosition.getDistance(cp2)) < 0 ? cp1 : cp2;
        final Distance halfDistance = targetsTravelDistance.divide(2);

        final Distance aThird = halfDistance.divide(3);
        final Orbit agentsManeuverStart = closerControlPoint.moveAboutAndGet(aThird, targetsCourseDirection);
        final Orbit agentsManeuverEnd = closerControlPoint.moveAboutAndGet(aThird.multiply(2), targetsCourseDirection.negate());


        /*
            create curve from origin to beginning of the actual maneuver

            - p1 is known -> calc time optimized to p1
         */

        final Maneuver transferCourse = new SimpleCourse(
                cage,
                cage.getCurrentCombatRound(),
                agent,
                KinematicInfo.getFrom(agentState),
                KinematicInfo.getFrom(agentState).with(agentsManeuverStart),
                target
        );


        return new CrossingTheT(
                cage,
                cage.getCurrentCombatRound(),
                agent,
                KinematicInfo.getFrom(agentState)/*.with(agentsManeuverStart)*/,
                KinematicInfo.getFrom(agentState).with(agentsManeuverEnd),
                target
        )/*.withTransferCourse(transferCourse.getCombatElement())*/
                .createCoursePlot();
    }
}
