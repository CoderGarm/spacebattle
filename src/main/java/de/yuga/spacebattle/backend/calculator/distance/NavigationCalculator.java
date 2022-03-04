package de.yuga.spacebattle.backend.calculator.distance;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.enums.EMovementType;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.enums.EAccelerationMetric;
import de.yuga.spacebattle.backend.enums.EDistanceMetric;
import de.yuga.spacebattle.backend.enums.EModuleType;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class NavigationCalculator {

    private final static MathContext MATH_CONTEXT_MORE_PRECISION = new MathContext(4, RoundingMode.DOWN);

    /**
     * The gravitation earth constant value
     */
    private static final BigDecimal GRAVITATION_EARTH = new BigDecimal("0.98");

    /**
     * The maximum percentage of the speed of light which can be reached by a star ship.
     */
    private static final BigDecimal MAX_PERCENTAGE_SPEED_OF_LIGHT = BigDecimal.valueOf(0.75);

    private NavigationCalculator() {
    }

    /**
     * Converts the amount of g (gravitation earth) to meter per square second.
     *
     * @param g times gravitation earth
     * @return the value in meter per square second
     */
    public static int getMeterPerSecondSquaredFromG(final int g) {
        return new BigDecimal(g).multiply(GRAVITATION_EARTH, MATH_CONTEXT_MORE_PRECISION).intValue();
    }

    /**
     * Calculates the distance for the given time and acceleration.
     *
     * @param endurance    the endurance of the acceleration in s
     * @param acceleration the acceleration
     */
    @Nonnull
    public static Distance getRangeByTimeAndAcceleration(final int endurance, @Nonnull final Acceleration acceleration) {
        Preconditions.checkNotNull(acceleration, "acceleration shouldn't be null!");

        final double effectiveCWarship = acceleration.getHyperBand().getEffectiveCWarship();
        return getRange(endurance, acceleration, EDistanceMetric.LS.getMeterEquivalent().multiply(BigDecimal.valueOf(effectiveCWarship), DistanceCalculator.MATH_CONTEXT_REALISTIC_PRECISION));
    }

    @Nonnull
    private static Distance getRange(final int endurance, final @Nonnull Acceleration acceleration, @Nonnull final BigDecimal speedOfLightInMeterPerSecond) {
        Preconditions.checkNotNull(acceleration, "acceleration shouldn't be null!");
        Preconditions.checkNotNull(speedOfLightInMeterPerSecond, "speedOfLightInMeterPerSecond shouldn't be null!");

        //v = s / t
        //s = 0,5 · a · t²
        //v = a · t
        //s = 0,5 · v · t
        final BigDecimal maxSpeedInMpS = speedOfLightInMeterPerSecond.multiply(MAX_PERCENTAGE_SPEED_OF_LIGHT, DistanceCalculator.MATH_CONTEXT_REALISTIC_PRECISION);
        final BigDecimal accelerationInMpSSquared = acceleration.convertToMetric(EAccelerationMetric.MS2);
        final int timeToMaxSpeed = maxSpeedInMpS.divide(accelerationInMpSSquared, DistanceCalculator.MATH_CONTEXT_REALISTIC_PRECISION).intValue();
        final int timeOfFullSpeed = endurance - timeToMaxSpeed * 2;
        // acceleration to max speed
        final Distance accelerationDistance = getDistanceByTimeAndAcceleration(timeToMaxSpeed, acceleration);
        // travel with max speed
        Distance fullThrottleDistance = Distance.ZERO;
        if (timeOfFullSpeed >= 0) {
            final BigDecimal distanceOfFullThrottle = BigDecimal.valueOf(timeOfFullSpeed).multiply(maxSpeedInMpS, DistanceCalculator.MATH_CONTEXT_MORE_PRECISION);
            fullThrottleDistance = new Distance(distanceOfFullThrottle, EDistanceMetric.M);
        }
        // slow down to destination "end of time"
        final Distance slowDownDistance = getDistanceByTimeAndAcceleration(timeToMaxSpeed, acceleration);

        return accelerationDistance.add(fullThrottleDistance).add(slowDownDistance);
    }

    /**
     * Calculates the distance for the given time and acceleration.
     *
     * @param endurance    the endurance of the acceleration in s
     * @param acceleration the acceleration
     */
    @Nonnull
    private static Distance getDistanceByTimeAndAcceleration(final int endurance, @Nonnull final Acceleration acceleration) {
        Preconditions.checkNotNull(acceleration, "acceleration shouldn't be null!");

        //s = 0,5 · v · t
        final double squaredTime = Math.pow(endurance, 2);
        final BigDecimal range = new BigDecimal("0.5")
                .multiply(acceleration.convertToMetric(EAccelerationMetric.MS2))
                .multiply(new BigDecimal(squaredTime), MATH_CONTEXT_MORE_PRECISION);
        return new Distance(range, EDistanceMetric.M);
    }

    /**
     * Calculates the destination orbit of the fleet to move towards the direction.<br>
     * <b>This does not include any physical laws.</b><br>
     *
     * @param agent          the fleet which is on the way
     * @param movementType   the plan, go towards the direction, or stay away from it
     * @param agentsPosition the current position of the agent
     * @param direction      the direction
     * @return the calculated destination of this turn
     */
    public static Orbit getDestinationOrbitOfFleetForTargetAtSubLightSpeed(@Nonnull final Fleet agent,
                                                                           @Nonnull final EMovementType movementType,
                                                                           @Nonnull final Orbit agentsPosition,
                                                                           @Nonnull final Orbit direction) {
        Preconditions.checkNotNull(agent, "agent shouldn't be null!");
        Preconditions.checkNotNull(movementType, "movementType shouldn't be null!");
        Preconditions.checkNotNull(agentsPosition, "agentsPosition shouldn't be null!");
        Preconditions.checkNotNull(direction, "direction shouldn't be null!");

        // todo change to physical reliability
        final Acceleration accelerationInGravityEarth = agent.getAccelerationFor(EModuleType.PROPULSION);
        final Distance rangeByTimeAndAcceleration = NavigationCalculator.getRangeByTimeAndAcceleration(CombatRound.COMBAT_ROUND_DURATION, accelerationInGravityEarth);
        return agentsPosition.move(movementType, rangeByTimeAndAcceleration, direction);
    }
}
