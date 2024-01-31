package de.yuga.spacebattle.backend.combat.maneuver;


import de.yuga.spacebattle.backend.calculator.geometry.CubicBezier;
import de.yuga.spacebattle.backend.calculator.geometry.KinematicInfo;
import de.yuga.spacebattle.backend.combat.main.Cage;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;

import javax.annotation.Nonnull;

/**
 * fixme change target detection to individual ships by angle
 * <br>
 * Tries to take the others fleet's course orthogonal and pass them in the middle.
 * Compare<br>
 * <img src="data/bezier-curves/crossing-the-t.png"> in respect to the time-optimized-course as opponent.
 */
public class CrossingTheT extends Maneuver {

    protected CrossingTheT(@Nonnull final Cage cage,
                           @Nonnull final CombatRound start,
                           @Nonnull final Fleet agent,
                           @Nonnull final KinematicInfo agentsKinematicInitial,
                           @Nonnull final KinematicInfo agentsKinematicDesignated,
                           @Nonnull final Fleet target) {
        super(cage, start, agent, agentsKinematicInitial, agentsKinematicDesignated, target);

    }

    @Override
    public ManeuverElements calculateCourse() {

        final Orbit origin = getAgentsKinematicInitial().getPosition();

        final long originX = origin.getXCoordinate().getCoordinateInMetric(EDistanceMetric.KM).longValue();
        final long originY = origin.getYCoordinate().getCoordinateInMetric(EDistanceMetric.KM).longValue();

        final Orbit destination = getAgentsKinematicDesignated().getPosition();
        final long destinationX = destination.getXCoordinate().getCoordinateInMetric(EDistanceMetric.KM).longValue();
        final long destinationY = destination.getYCoordinate().getCoordinateInMetric(EDistanceMetric.KM).longValue();

        final long diffX = (Math.abs(originX) + Math.abs(destinationX)) / 2;
        final long diffY = (Math.abs(originY) + Math.abs(destinationY)) / 2;
        final double[] c0 = {originX, originY};
        final double[] c1 = {originX, originY + (diffY * 2)};
        final double[] c2 = {destinationX - (diffX * 2), destinationY};
        final double[] c3 = {destinationX, destinationY};
        final CubicBezier cubicBezier = new CubicBezier(new double[][]{c0, c1, c2, c3});

        return getAsList(cubicBezier);
    }
}
