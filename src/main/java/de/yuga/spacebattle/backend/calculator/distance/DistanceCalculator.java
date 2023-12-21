package de.yuga.spacebattle.backend.calculator.distance;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.enums.EMovementType;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.backend.enums.EStarClassType;
import de.yuga.spacebattle.backend.enums.ETechnologyType;
import de.yuga.spacebattle.backend.enums.physics.EAccelerationMetric;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import de.yuga.spacebattle.backend.enums.physics.EHyperBand;
import de.yuga.spacebattle.backend.enums.space.EWormhole;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DistanceCalculator {

    public final static MathContext MC_HU = new MathContext(8, RoundingMode.HALF_UP);

    /**
     * 154 g at gamma is so fucking slow
     */
    @Nonnull
    public static final Acceleration PUBLIC_TRANSPORT_ACCELERATION = new Acceleration(50000, EAccelerationMetric.G, EHyperBand.EPSILON);

    /**
     * Returns the amount of digits.
     *
     * @param number the number
     * @return the amount of digits
     */
    public static int getDigitCount(@Nonnull final BigInteger number) {
        Preconditions.checkNotNull(number, "number shouldn't be null!");

        double factor = Math.log(2) / Math.log(10);
        int digitCount = (int) (factor * number.bitLength() + 1);
        if (BigInteger.TEN.pow(digitCount - 1).compareTo(number) > 0) {
            return digitCount - 1;
        }
        return digitCount;
    }

    /**
     * Returns the amount of digits.
     *
     * @param number the number
     * @return the amount of digits
     */
    public static int getDigitCount(@Nonnull final BigDecimal number) {
        Preconditions.checkNotNull(number, "number shouldn't be null!");

        return DistanceCalculator.getDigitCount(number.toBigInteger());
    }

    /**
     * Calculates the time-to-travel from the actual sojourn to the given target.<br>
     * Part of the calculations are:<br>
     * - flight from current position to hyper limit<br>
     * - faster-than-light travelling to destination system<br>
     * - flight from hyper limit to destination
     *
     * @param fleet       the fleet
     * @param destination the destination
     * @return the time-to-travel in ticks, not below one
     */
    public static int calculateTimeToTravel(@Nonnull final Fleet fleet,
                                            @Nonnull final FleetOrbit destination) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkNotNull(destination, "destination shouldn't be null!");
        Preconditions.checkArgument(fleet.getOrbit() != null, "fleet should be placed at a location!");

        final ETechnologyType restrictingTechnologyType = fleet.getRestrictingTechnologyType();
        final Acceleration acceleration = fleet.getAccelerationFor(EModuleType.FTLPROPULSION);
        return calculateTimeToTravel(restrictingTechnologyType, acceleration, fleet.getOrbit(), destination);
    }

    /**
     * Calculates the time-to-travel from the actual sojourn to the given target.<br>
     * Part of the calculations are:<br>
     * - flight from current position to hyper limit<br>
     * - faster-than-light travelling to destination system<br>
     * - flight from hyper limit to destination
     *
     * @param fleet       the fleet
     * @param origin      the specific origin besides the current fleet position
     * @param destination the destination
     * @return the time-to-travel in ticks, not below one
     */
    public static int calculateTimeToTravel(@Nonnull final Fleet fleet,
                                            @Nonnull final FleetOrbit origin,
                                            @Nonnull final FleetOrbit destination) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkNotNull(origin, "origin shouldn't be null!");
        Preconditions.checkNotNull(destination, "destination shouldn't be null!");

        final ETechnologyType restrictingTechnologyType = fleet.getRestrictingTechnologyType();
        final Acceleration acceleration = fleet.getAccelerationFor(EModuleType.FTLPROPULSION);
        return calculateTimeToTravel(restrictingTechnologyType, acceleration, origin, destination);
    }


    public static int calculateTimeToTravel(@Nonnull final ETechnologyType restrictingTechnologyType,
                                            @Nonnull final Acceleration acceleration,
                                            @Nonnull final FleetOrbit origin,
                                            @Nonnull final FleetOrbit destination) {
        Preconditions.checkNotNull(restrictingTechnologyType, "restrictingTechnologyType must not be empty");
        Preconditions.checkNotNull(acceleration, "acceleration must not be empty");
        Preconditions.checkNotNull(origin, "origin shouldn't be null!");
        Preconditions.checkNotNull(destination, "destination shouldn't be null!");

        double ticksToTravel = 0;
        final StarSystem originSystem = origin.getSystem();
        final StarSystem destinationSystem = destination.getSystem();

        if (originSystem != null && destinationSystem != null && !originSystem.equals(destinationSystem)) {
            // interstellar traveling
            final boolean systemsConnected = EWormhole.areSystemsConnected(originSystem, destinationSystem);
            ticksToTravel += getSubLightDurationToHyperLimit(restrictingTechnologyType, acceleration, origin);
            ticksToTravel += systemsConnected ? 0 : getDuration(EModuleType.FTLPROPULSION, restrictingTechnologyType, acceleration, originSystem.getOrbit(), destinationSystem.getOrbit());
            ticksToTravel += getSubLightDurationFromHyperLimit(restrictingTechnologyType, acceleration, destination);
        } else if (origin.getInterplanetaryResultingOrbit() != null && destination.getInterplanetaryResultingOrbit() != null) {
            // interplanetary traveling
            ticksToTravel += getDuration(EModuleType.PROPULSION, restrictingTechnologyType, acceleration, origin.getInterplanetaryResultingOrbit(), destination.getInterplanetaryResultingOrbit());
        }

        final int rounded = BigDecimal.valueOf(ticksToTravel).setScale(0, RoundingMode.UP).intValue();
        return Math.max(rounded, 1);
    }

    /**
     * Calculates the time to travel from the nearest point at the hyper limit to the given destination.<br>
     *
     * @return the time to travel from the current position of the fleet to the hyper limit
     */
    @VisibleForTesting
    private static double getSubLightDurationFromHyperLimit(@Nonnull final ETechnologyType restrictingTechnologyType,
                                                            @Nonnull final Acceleration acceleration,
                                                            @Nonnull final FleetOrbit destination) {
        Preconditions.checkNotNull(restrictingTechnologyType, "restrictingTechnologyType must not be empty");
        Preconditions.checkNotNull(acceleration, "acceleration must not be empty");
        Preconditions.checkNotNull(destination, "destination shouldn't be null!");
        Preconditions.checkArgument(destination.getInterplanetaryResultingOrbit() != null, "destination resulting orbit shouldn't be null!");

        final Orbit positionOnHyperLimit = NavigationCalculator.getPositionOnHyperlimit(destination);
        // todo currently there are fixed entry points into a system - this must be changed
        return getDuration(EModuleType.PROPULSION, restrictingTechnologyType, acceleration, positionOnHyperLimit, destination.getInterplanetaryResultingOrbit());
    }


    /**
     * Calculates the time to travel from the current position of the fleet to the hyper limit of the star.<br>
     * The fleet must be inside the hyper limit of a star.
     *
     * @return the time to travel from the current position of the fleet to the hyper limit
     */
    public static double getSubLightDurationToHyperLimit(@Nonnull final ETechnologyType restrictingTechnologyType,
                                                         @Nonnull final Acceleration acceleration,
                                                         @Nonnull final FleetOrbit origin) {
        Preconditions.checkNotNull(restrictingTechnologyType, "restrictingTechnologyType must not be empty");
        Preconditions.checkNotNull(acceleration, "acceleration must not be empty");
        Preconditions.checkNotNull(origin, "origin shouldn't be null!");

        final StarSystem originSystem = origin.getSystem();
        final Orbit originOrbit = origin.getInterplanetaryResultingOrbit();
        if (originOrbit == null || originSystem == null) {
            throw new NotifyWebUserException("You must be in a system to travel to the hyper limit from the inwards.");
        }

        final Quadrant quadrant = Quadrant.getByOrbit(originOrbit);
        final EStarClassType starClassType = originSystem.getStarClassType();
        final double radiusOfHyperLimit = starClassType.getLightMinutesToHyperLimit();

        final Orbit positionOnHyperLimit = createByRadiusAndQuadrant(new Distance(radiusOfHyperLimit, EDistanceMetric.LM), quadrant, Planet.PLANET_STANDARD_METRIC);
        return getDuration(EModuleType.PROPULSION, restrictingTechnologyType, acceleration, originOrbit, positionOnHyperLimit);
    }

    /**
     * Creates an orbit out of the given radius and the quadrant.
     *
     * @param radius   the radius
     * @param quadrant the quadrant
     * @return the orbit
     */
    @Nonnull
    public static Orbit createByRadiusAndQuadrant(@Nonnull final Distance radius,
                                                  @Nonnull final Quadrant quadrant,
                                                  @Nonnull final EDistanceMetric metric) {
        Preconditions.checkNotNull(radius, "radius shouldn't be null!");
        Preconditions.checkNotNull(quadrant, "quadrant shouldn't be null!");
        Preconditions.checkNotNull(metric, "metric shouldn't be null!");

        //X=r⋅cos(φ),Y=r⋅sin(φ)
        final double toRadians = Math.toRadians(quadrant.getPhi());
        final double cosPhi = Math.cos(toRadians);
        final double sinPhi = Math.sin(toRadians);
        final BigDecimal xCoord = radius.getCoordinateInMetric(metric).multiply(new BigDecimal(cosPhi));
        final BigDecimal yCoord = radius.getCoordinateInMetric(metric).multiply(new BigDecimal(sinPhi));
        return new Orbit(new Distance(xCoord, metric), new Distance(yCoord, metric));
    }

    /**
     * Calculates the time to travel for journeys in ticks for given orbits and the given propulsion type.
     *
     * @param propulsionType if the travel is sub light or faster than light
     * @param origin         the origin
     * @param destination    the destination
     * @return the time to travel in ticks
     */
    public static double getDuration(@Nonnull final EModuleType propulsionType,
                                     @Nonnull final ETechnologyType restrictingTechnologyType,
                                     @Nonnull final Acceleration acceleration,
                                     @Nonnull final Orbit origin,
                                     @Nonnull final Orbit destination) {
        Preconditions.checkNotNull(propulsionType, "propulsionType shouldn't be null!");
        Preconditions.checkNotNull(restrictingTechnologyType, "restrictingTechnologyType must not be empty");
        Preconditions.checkNotNull(acceleration, "acceleration must not be empty");
        Preconditions.checkNotNull(origin, "origin shouldn't be null!");
        Preconditions.checkNotNull(destination, "destination shouldn't be null!");

        final Distance distance = origin.getDistance(destination);
        final int targetedPercentageOfTopSpeed = propulsionType == EModuleType.FTLPROPULSION ? 100 : 8;
        final int duration = NavigationCalculator.getDurationForTargetedEndSpeed(propulsionType, targetedPercentageOfTopSpeed, restrictingTechnologyType, acceleration, distance);

        return (double) duration / Tick.TICK_DURATION_IN_SECONDS;
    }

    /**
     * Returns the distance between the two given orbits.
     *
     * @param orbit1 the first orbit
     * @param orbit2 the second orbit
     * @return the distance
     */
    @Nonnull
    public static Distance getOrbitalDistance(@Nonnull final Orbit orbit1, @Nonnull final Orbit orbit2) {
        Preconditions.checkNotNull(orbit1, "orbit1 shouldn't be null!");
        Preconditions.checkNotNull(orbit2, "orbit2 shouldn't be null!");

        final BigDecimal x1 = orbit1.getXCoordinate().getCoordinate();
        final BigDecimal y1 = orbit1.getYCoordinate().getCoordinate();

        final BigDecimal x2 = orbit2.getXCoordinate().getCoordinateInMetric(orbit1.getXCoordinate().getDistanceMetric());
        final BigDecimal y2 = orbit2.getYCoordinate().getCoordinateInMetric(orbit1.getYCoordinate().getDistanceMetric());

        return new Distance(getDistance(x2.subtract(x1), y2.subtract(y1)), orbit1.getXCoordinate().getDistanceMetric());
    }

    /**
     * Calculates the distance between thw two given coordinates.
     *
     * @param firstCoord  the first digit
     * @param secondCoord the second digit
     * @return the distance
     */
    @Nonnull
    public static BigDecimal getDistance(final BigDecimal firstCoord, final BigDecimal secondCoord) {
        final BigDecimal x = firstCoord.pow(2);
        final BigDecimal y = secondCoord.pow(2);
        return (x.add(y)).sqrt(MC_HU);
    }

    /**
     * Calculates the amount of combat rounds to travel by the given range.
     *
     * @param distance            the distance
     * @param rangePerCombatRound the range per round
     * @return the amount of rounds
     */
    public static int getCombatRoundsToTravel(@Nonnull final Distance distance, @Nonnull final Distance rangePerCombatRound) {
        Preconditions.checkNotNull(distance, "distance shouldn't be null!");
        Preconditions.checkNotNull(rangePerCombatRound, "rangePerCombatRound shouldn't be null!");

        final BigDecimal distanceInMetric = distance.getCoordinateInMetric(EDistanceMetric.M);
        final BigDecimal rangeInMetric = rangePerCombatRound.getCoordinateInMetric(EDistanceMetric.M);
        return distanceInMetric.divide(rangeInMetric, MC_HU).intValue();
    }

    /**
     * Transforms the given value into a suitable length with unit.
     *
     * @param value the value
     * @return a string like '0.3 lm' for o point three light minutes
     */
    @Nonnull
    public static Distance convertToScale(@Nonnull final Distance value) {
        Preconditions.checkNotNull(value, "value shouldn't be null!");

        final Distance clone = value.clone();
        final BigDecimal coordinate = value.getCoordinate();
        final int scale = coordinate.scale();
        if (scale == 0) {
            return clone;
        }
        final Map<EDistanceMetric, BigDecimal> collect = Arrays.stream(EDistanceMetric.values())
                .filter(metric -> metric.getDigitCount() <= value.getDistanceMetric().getDigitCount())
                .collect(Collectors.toMap(Function.identity(), value::getCoordinateInMetric));

        collect.entrySet().stream().min((o1, o2) -> {
            final int scale1 = o1.getValue().scale();
            final int scale2 = o2.getValue().scale();
            if (scale < scale1 && scale < scale2 || scale1 < 0 || scale2 < 0) {
                return -1;
            }
            return Integer.compare(scale1, scale2);
        }).ifPresent(e -> clone.convertToMetricWithScale(e.getKey()));

        return clone;
    }

    public static List<Orbit> getWaypointsFromCourse(@Nonnull final EModuleType propulsionType,
                                                     @Nonnull final ETechnologyType restrictingTechnologyType,
                                                     @Nonnull final Acceleration acceleration,
                                                     @Nonnull final Orbit origin,
                                                     @Nonnull final Orbit destination,
                                                     final int steps) {
        Preconditions.checkNotNull(propulsionType, "propulsionType must not be empty");
        Preconditions.checkNotNull(restrictingTechnologyType, "restrictingTechnologyType must not be empty");
        Preconditions.checkNotNull(acceleration, "acceleration must not be empty");
        Preconditions.checkNotNull(origin, "origin must not be empty");
        Preconditions.checkNotNull(destination, "destination must not be empty");

        final List<Orbit> result = new ArrayList<>();
        final Distance distance = origin.getDistance(destination);
        final Distance stepWith = new Distance(distance.getCoordinate().divide(BigDecimal.valueOf(steps), MC_HU), distance.getDistanceMetric());
        for (int i = 1; i <= steps; i++) {
            result.add(origin.move(EMovementType.REDUCE_DISTANCE, stepWith.multiply(i), destination));
        }
        return result;
    }
}
