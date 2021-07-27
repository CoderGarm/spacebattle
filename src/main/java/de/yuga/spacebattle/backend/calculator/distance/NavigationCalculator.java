package de.yuga.spacebattle.backend.calculator.distance;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.enums.EMovementType;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
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
     * @param time         the endurance of the acceleration in s
     * @param acceleration the acceleration in m/s²
     */
    public static BigDecimal getRangeByTimeAndAcceleration(final int time, final int acceleration) {
        final double squaredTime = Math.pow(time, 2);
        return new BigDecimal("0.5").multiply(new BigDecimal(acceleration)).multiply(new BigDecimal(squaredTime), MATH_CONTEXT_MORE_PRECISION);
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
        //final BigDecimal rangePerCombatRound = agent.getRangePerCombatRound();
        final BigDecimal accelerationInGravityEarth = agent.getRangePerTick(EModuleType.PROPULSION);
        final int meterPerSecondSquaredFromG = NavigationCalculator.getMeterPerSecondSquaredFromG(accelerationInGravityEarth.intValue());
        final BigDecimal rangeByTimeAndAcceleration = NavigationCalculator.getRangeByTimeAndAcceleration(CombatRound.COMBAT_ROUND_DURATION, meterPerSecondSquaredFromG);
        return agentsPosition.move(movementType, rangeByTimeAndAcceleration, direction);
    }
}
