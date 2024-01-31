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
 * Tries to pass another fleet in opposite direction and engage with the broadsides.<br>
 * Compare<br>
 * <img src="data/bezier-curves/broadside-passing.png"> in respect to the time-optimized-course as opponent.
 */
public class BroadsidePassing extends Maneuver {

    protected BroadsidePassing(@Nonnull final Cage cage,
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

        final long halfXLength = (Math.abs(originX) + Math.abs(destinationX)) / 2;

        final double[] c0 = {originX, originY};
        final double[] c1 = {originX - halfXLength, originY};
        final double[] c2 = {destinationX - halfXLength, destinationY};
        final double[] c3 = {destinationX, destinationY};
        final CubicBezier cubicBezier = new CubicBezier(new double[][]{c0, c1, c2, c3});

        return getAsList(cubicBezier);
    }

}
