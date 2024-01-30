package de.yuga.spacebattle.backend.combat.maneuver;


import de.yuga.spacebattle.backend.calculator.geometry.CubicBezier;
import de.yuga.spacebattle.backend.calculator.geometry.KinematicInfo;
import de.yuga.spacebattle.backend.combat.main.Cage;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.dto.physics.Velocity;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;

import javax.annotation.Nonnull;

import static de.yuga.spacebattle.backend.combat.enums.EMovementType.REDUCE_DISTANCE;

/**
 * Two lines of ships are passing themselves in opposite directions and engaging with their broadsides.
 */
public class TimeOptimizedCourse extends Maneuver {

    public TimeOptimizedCourse(@Nonnull final Cage cage,
                               @Nonnull final CombatRound start,
                               @Nonnull final Fleet agent,
                               @Nonnull final KinematicInfo agentsKinematicInitial,
                               @Nonnull final KinematicInfo agentsKinematicDesignated,
                               @Nonnull final Fleet target,
                               @Nonnull final KinematicInfo targetsKinematicInitial,
                               @Nonnull final KinematicInfo targetsKinematicDesignated) {
        super(cage, start, agent, agentsKinematicInitial, agentsKinematicDesignated, target, targetsKinematicInitial, targetsKinematicDesignated);

    }

    @Override
    public CubicBezier calculateCourse() {

        final Orbit origin = getAgentsKinematicInitial().getPosition();

        final long originX = origin.getXCoordinate().getCoordinateInMetric(EDistanceMetric.KM).longValue();
        final long originY = origin.getYCoordinate().getCoordinateInMetric(EDistanceMetric.KM).longValue();

        final Orbit destination = getAgentsKinematicDesignated().getPosition();
        final long destinationX = destination.getXCoordinate().getCoordinateInMetric(EDistanceMetric.KM).longValue();
        final long destinationY = destination.getYCoordinate().getCoordinateInMetric(EDistanceMetric.KM).longValue();

        final Velocity maxVelocity = getAgentsTopSpeed();

        final long diffX = (Math.abs(originX) + Math.abs(destinationX)) / 2;
        final double[] c0 = {originX, originY};
        final double[] c1 = {originX + diffX, originY};
        final double[] c2 = {destinationX - diffX, destinationY};
        final double[] c3 = {destinationX, destinationY};
        final CubicBezier cubicBezier = new CubicBezier(new double[][]{c0, c1, c2, c3});

        final CombatRound combatRound = getStart().clone();

        double length = cubicBezier.getLength();
        for (double i = 0; i <= 1; ) {
            double j = i + 0.01;

            final double[] start = cubicBezier.getPointAtLength(length * j);
            final double[] end = cubicBezier.getPointAtLength(length * j);

            addCourseOrder(
                    combatRound.clone(),
                    REDUCE_DISTANCE,
                    maxVelocity.multiply(0.05).clone(),
                    new Orbit(end[0], end[1], EDistanceMetric.KM)
            );

            i = j;
            combatRound.next();
        }
        setDesignatedEnd(combatRound);
        return cubicBezier;
    }
}
