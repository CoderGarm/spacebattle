package de.yuga.spacebattle.backend.calculator.distance;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.geometry.CubicBezier;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeometryCalculator {

    private GeometryCalculator() {
    }

    /**
     * Returns null if no intersection is possible.
     */
    @Nullable
    public static Orbit calculateIntersectionPoint(@Nonnull final List<Orbit> o1, @Nonnull final List<Orbit> o2) {
        Preconditions.checkNotNull(o1, "o1 must not be empty");
        Preconditions.checkNotNull(o2, "o2 must not be empty");
        Preconditions.checkState(o1.size() == 2, "o1.size() == 2 must be true");
        Preconditions.checkState(o2.size() == 2, "o2.size() == 2 must be true");


        final double[] eq1 = getLineEquation(
                new double[]{
                        o1.get(0).getXCoordinate().getCoordinateInMetric(EDistanceMetric.M).doubleValue(),
                        o1.get(0).getYCoordinate().getCoordinateInMetric(EDistanceMetric.M).doubleValue()
                },
                new double[]{
                        o1.get(1).getXCoordinate().getCoordinateInMetric(EDistanceMetric.M).doubleValue(),
                        o1.get(1).getYCoordinate().getCoordinateInMetric(EDistanceMetric.M).doubleValue()
                }
        );

        final double[] eq2 = getLineEquation(
                new double[]{
                        o2.get(0).getXCoordinate().getCoordinateInMetric(EDistanceMetric.M).doubleValue(),
                        o2.get(0).getYCoordinate().getCoordinateInMetric(EDistanceMetric.M).doubleValue()
                },
                new double[]{
                        o2.get(1).getXCoordinate().getCoordinateInMetric(EDistanceMetric.M).doubleValue(),
                        o2.get(1).getYCoordinate().getCoordinateInMetric(EDistanceMetric.M).doubleValue()
                }
        );

        final double[] intersectionPoint = calculateIntersectionPoint(eq1[0], eq1[1], eq2[0], eq2[1]);
        return intersectionPoint != null ? new Orbit(intersectionPoint, EDistanceMetric.M) : null;
    }

    /**
     * Calculated the intersection point of two lines given by the line equation coefficients and shits.
     *
     * @return the intersection point
     */
    @Nullable
    private static double[] calculateIntersectionPoint(double m1, double b1, double m2, double b2) {

        if (m1 == m2) {
            return null;
        }

        double x = (b2 - b1) / (m1 - m2);
        double y = m1 * x + b1;

        return new double[]{x, y};
    }

    /**
     * Given two points {(x1,y1),(x2,y2)} returns {m,b} from the equation y = mx + b for a line
     * which passes through both points.
     *
     * @return The slope and y intercept of the line passing through the points provided
     */
    @Nonnull
    private static double[] getLineEquation(final double[] p1, final double[] p2) {

        final double x1 = p1[0];
        final double y1 = p1[1];
        final double x2 = p2[0];
        final double y2 = p2[1];

        double coefficient = (y2 - y1) / (x2 - x1);
        double shift = -(coefficient * x1) + y1;
        return new double[]{coefficient, shift};
    }

    @Nullable
    public static Orbit calculateClosestPoint(@Nonnull final CubicBezier c1, @Nonnull final CubicBezier c2) {
        Preconditions.checkNotNull(c1, "c1 must not be empty");
        Preconditions.checkNotNull(c2, "c2 must not be empty");

        final Map<Double, Orbit> pointsC1 = new HashMap<>();
        final Map<Double, Orbit> pointsC2 = new HashMap<>();


        for (double t = 0; t <= 1; t += 0.001) {
            pointsC1.put(t, new Orbit(c1.getPointAtParameter(t), EDistanceMetric.KM));
            pointsC2.put(t, new Orbit(c2.getPointAtParameter(t), EDistanceMetric.KM));
        }

        /* fixme cam be improved by newtons approach - start in the middle and go out. following code is other ways around: start an both ends and go inwards - but buggy somehow
        final double step = 0.001;
        double t = 0.5;
        double iterator = t / step;
        while (iterator > 0) {

            final double upParam = t + (iterator * step);
            final double downParam = t - (iterator * step);

            pointsC1.put(upParam, new Orbit(c1.getPointAtParameter(upParam), EDistanceMetric.KM));
            pointsC2.put(upParam, new Orbit(c2.getPointAtParameter(upParam), EDistanceMetric.KM));

            pointsC1.put(downParam, new Orbit(c1.getPointAtParameter(downParam), EDistanceMetric.KM));
            pointsC2.put(downParam, new Orbit(c2.getPointAtParameter(downParam), EDistanceMetric.KM));

            iterator--;
        }
        */

        Distance referenceDistance = null;
        Orbit closest = null;

        for (final Map.Entry<Double, Orbit> e1 : pointsC1.entrySet()) {

            final double t1 = e1.getKey();
            final Orbit p1 = e1.getValue();

            for (final Map.Entry<Double, Orbit> e2 : pointsC2.entrySet()) {

                final double t2 = e2.getKey();
                final Orbit p2 = e2.getValue();

                final Distance diff = p1.getDistance(p2);
                if (referenceDistance == null || diff.compareTo(referenceDistance) < 0) {
                    closest = p1;
                    referenceDistance = diff;
                }
            }
        }

        return closest;
    }

}
