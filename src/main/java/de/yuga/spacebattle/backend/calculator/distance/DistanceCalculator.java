package de.yuga.spacebattle.backend.calculator.distance;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.NotifyUserException;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.enums.ELengthDefinition;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.backend.enums.EStarClassType;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

public class DistanceCalculator {

    public final static MathContext MATH_CONTEXT_TO_INTEGER_DOWN = new MathContext(0, RoundingMode.DOWN);
    public final static MathContext MATH_CONTEXT_MORE_PRECISION = new MathContext(15, RoundingMode.DOWN);

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

        return calculateTimeToTravel(fleet, fleet.getOrbit(), destination);
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

        BigDecimal duration = BigDecimal.ZERO;


        final StarSystem originSystem = origin.getSystem();
        final StarSystem destinationSystem = destination.getSystem();
        if (originSystem != null && destinationSystem != null) {
            final BigDecimal subLightDurationToHyperLimit = getSubLightDurationToHyperLimit(fleet, origin, destination);
            duration = duration.add(subLightDurationToHyperLimit);
            final BigDecimal ftlDuration = getFTLDuration(fleet, originSystem.getOrbit(), destinationSystem.getOrbit());
            duration = duration.add(ftlDuration);
            final BigDecimal subLightDurationFromHyperLimit = getSubLightDurationFromHyperLimit(fleet, destination);
            duration = duration.add(subLightDurationFromHyperLimit);
        } else if (origin.getOrbit() != null && destination.getOrbit() != null) {
            final BigDecimal subLightDuration = getSubLightDuration(fleet, origin.getOrbit(), destination.getOrbit());
            duration = duration.add(subLightDuration);
        }

        final int ticksToTravel = duration.intValue();
        if (ticksToTravel == 0) {
            return 1;
        } else if (ticksToTravel < 0) {
            throw new NotifyUserException("mathe genius, check that please");
        }
        return ticksToTravel;
    }

    /**
     * Calculates the time to travel from the nearest point at the hyper limit to the given destination.<br>
     *
     * @param fleet       the fleet which wants to travel
     * @param destination the destination of the fleet
     * @return the time to travel from the current position of the fleet to the hyper limit
     */
    @Nonnull
    @VisibleForTesting
    private static BigDecimal getSubLightDurationFromHyperLimit(@Nonnull final Fleet fleet, @Nonnull final FleetOrbit destination) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkNotNull(destination, "destination shouldn't be null!");
        Preconditions.checkArgument(destination.getSystem() != null, "destination system shouldn't be null!");
        Preconditions.checkArgument(destination.getOrbit() != null, "destination orbit shouldn't be null!");
        Preconditions.checkArgument(fleet.getOrbit() != null, "fleet's orbit shouldn't be null here");

        final Quadrant quadrant = Quadrant.getByOrbit(destination.getOrbit());
        final EStarClassType starClassType = destination.getSystem().getStarClassType();
        final double radiusOfHyperLimit = starClassType.getLightMinutesToHyperLimit() * 100;

        final Orbit positionOnHyperLimit = createByRadiusAndQuadrant(new BigDecimal(radiusOfHyperLimit), quadrant);
        // todo currently there are fixed entry points into a system - this must be changed
        return getSubLightDuration(fleet, positionOnHyperLimit, destination.getOrbit());
    }

    /**
     * Calculates the time to travel from the current position of the fleet to the hyper limit of the star.<br>
     * The fleet must be inside the hyper limit of a star.
     *
     * @param fleet the fleet which wants to travel
     * @return the time to travel from the current position of the fleet to the hyper limit
     */
    @Nonnull
    private static BigDecimal getSubLightDurationToHyperLimit(@Nonnull final Fleet fleet,
                                                              @Nonnull final FleetOrbit origin,
                                                              @Nonnull final FleetOrbit destination) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkNotNull(origin, "origin shouldn't be null!");
        Preconditions.checkNotNull(destination, "destination shouldn't be null!");

        final StarSystem originSystem = origin.getSystem();
        final Orbit originOrbit = origin.getOrbit();
        final StarSystem destinationSystem = destination.getSystem();

        final boolean outOfSystem = originSystem == null || destinationSystem == null;
        final boolean noPositionSpecified = originOrbit == null;
        if (outOfSystem || originSystem.equals(destinationSystem) || noPositionSpecified) {
            return BigDecimal.ZERO;
        }

        final Quadrant quadrant = Quadrant.getByOrbit(originOrbit);
        final EStarClassType starClassType = originSystem.getStarClassType();
        final double radiusOfHyperLimit = starClassType.getLightMinutesToHyperLimit() * 100;

        final Orbit positionOnHyperLimit = createByRadiusAndQuadrant(new BigDecimal(radiusOfHyperLimit), quadrant);
        return getSubLightDuration(fleet, originOrbit, positionOnHyperLimit);
    }

    /**
     * Creates an orbit out of the given radius and the quadrant.
     *
     * @param radius   the radius
     * @param quadrant the quadrant
     * @return the orbit
     */
    @Nonnull
    public static Orbit createByRadiusAndQuadrant(final BigDecimal radius, @Nonnull final Quadrant quadrant) {
        Preconditions.checkNotNull(quadrant, "quadrant shouldn't be null!");

        //X=r⋅cos(φ),Y=r⋅sin(φ)
        final double toRadians = Math.toRadians(quadrant.getPhi());
        final double cosPhi = Math.cos(toRadians);
        final double sinPhi = Math.sin(toRadians);
        final BigDecimal xCoord = radius.multiply(new BigDecimal(cosPhi));
        final BigDecimal yCoord = radius.multiply(new BigDecimal(sinPhi));
        return new Orbit(xCoord, yCoord);
    }

    /**
     * Calculates the time to travel for sub-light journeys in ticks for given orbits.
     *
     * @param fleet       the fleet which should travel
     * @param origin      the origin
     * @param destination the destination
     * @return the time to travel in ticks
     */
    @Nonnull
    private static BigDecimal getSubLightDuration(@Nonnull final Fleet fleet,
                                                  @Nonnull final Orbit origin,
                                                  @Nonnull final Orbit destination) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkNotNull(origin, "origin shouldn't be null!");
        Preconditions.checkNotNull(destination, "destination shouldn't be null!");

        final BigDecimal distance = origin.getDistance(destination);
        final BigDecimal subLightRangePerTick = fleet.getRangePerTick(EModuleType.PROPULSION);
        return distance.divide(subLightRangePerTick, 1, RoundingMode.UP);
    }

    /**
     * Calculates the time to travel for faster-than-light journeys in ticks.
     *
     * @param fleet       the fleet which should travel
     * @param origin      the origin
     * @param destination the destination
     * @return the time to travel in ticks
     */
    @Nonnull
    private static BigDecimal getFTLDuration(@Nonnull final Fleet fleet,
                                             @Nonnull final Orbit origin,
                                             @Nonnull final Orbit destination) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkNotNull(origin, "origin shouldn't be null!");
        Preconditions.checkNotNull(destination, "destination shouldn't be null!");

        final BigDecimal distance = origin.getDistance(destination);
        final BigDecimal ftlRangePerTick = fleet.getRangePerTick(EModuleType.FTLPROPULSION);
        return distance.divide(ftlRangePerTick, 1, RoundingMode.UP);
    }

    /**
     * Returns the distance between the two given orbits.
     *
     * @param orbit1 the first orbit
     * @param orbit2 the second orbit
     * @return the distance
     */
    @Nonnull
    public static BigDecimal getOrbitalDistance(@Nonnull final Orbit orbit1, @Nonnull final Orbit orbit2) {
        Preconditions.checkNotNull(orbit1, "orbit1 shouldn't be null!");
        Preconditions.checkNotNull(orbit2, "orbit2 shouldn't be null!");

        final BigDecimal x1 = new BigDecimal(orbit1.getXCoordinate());
        final BigDecimal y1 = new BigDecimal(orbit1.getYCoordinate());

        final BigDecimal x2 = new BigDecimal(orbit2.getXCoordinate());
        final BigDecimal y2 = new BigDecimal(orbit2.getYCoordinate());

        return getDistance(x2.subtract(x1), y2.subtract(y1));
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
        return (x.add(y)).sqrt(MATH_CONTEXT_MORE_PRECISION);
    }

    /**
     * Transforms the given value into a suitable length with unit.
     *
     * @param value the value
     * @return a string like '0.3 lm' for o point three light minutes
     */
    @Nonnull
    public static String getDistanceAsStringWithUnit(@Nonnull final BigDecimal value) {
        Preconditions.checkNotNull(value, "value shouldn't be null!");

        final ELengthDefinition lengthDefinition = ELengthDefinition.getBy(value);
        final BigInteger divisor = lengthDefinition.getDivisor();
        return value.divide(new BigDecimal(divisor), DistanceCalculator.MATH_CONTEXT_MORE_PRECISION) + " " + lengthDefinition.getUnit();
    }

    /**
     * Transforms the given value into a suitable length with unit.
     *
     * @param value the value
     * @return a string like '0.3 lm' for o point three light minutes
     */
    @Nonnull
    public static String getDistanceAsStringWithUnit(@Nonnull final BigInteger value) {
        Preconditions.checkNotNull(value, "value shouldn't be null!");

        final ELengthDefinition lengthDefinition = ELengthDefinition.getBy(value);
        final BigInteger divisor = lengthDefinition.getDivisor();
        return new BigDecimal(value).divide(new BigDecimal(divisor), DistanceCalculator.MATH_CONTEXT_MORE_PRECISION) + " " + lengthDefinition.getUnit();
    }

    /**
     * Calculates the amount of combat rounds to travel by the given range.
     *
     * @param distance            the distance
     * @param rangePerCombatRound the range per round
     * @return the amount of rounds
     */
    public static int getCombatRoundsToTravel(@Nonnull final BigDecimal distance, @Nonnull final BigDecimal rangePerCombatRound) {
        Preconditions.checkNotNull(distance, "distance shouldn't be null!");
        Preconditions.checkNotNull(rangePerCombatRound, "rangePerCombatRound shouldn't be null!");

        return distance.divide(rangePerCombatRound, DistanceCalculator.MATH_CONTEXT_MORE_PRECISION).intValue();
    }
}
