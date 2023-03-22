package de.yuga.spacebattle.backend.dto.physics;

import de.yuga.spacebattle.backend.enums.physics.EAccelerationMetric;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import de.yuga.spacebattle.backend.enums.physics.ETimeMetric;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Stream;

import static de.yuga.spacebattle.TestDataProviderUtils.*;
import static de.yuga.spacebattle.backend.enums.physics.EAccelerationMetric.G;
import static de.yuga.spacebattle.backend.enums.physics.EAccelerationMetric.MS2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AccelerationTest {

    @ParameterizedTest
    @MethodSource("testConversionProvider")
    public void testConversion(final Acceleration base, final EAccelerationMetric metric, final BigDecimal expectation) {

        final BigDecimal result = base.convertToMetric(metric).setScale(2, RoundingMode.HALF_UP);
        assertThat(result, Matchers.comparesEqualTo(expectation));
    }

    private static Stream<Arguments> testConversionProvider() {
        return Stream.of(
                Arguments.of(acc(9.81, MS2), G, BigDecimal.ONE),
                Arguments.of(acc(1, G), MS2, BigDecimal.valueOf(9.81)),
                Arguments.of(acc(500, G), MS2, BigDecimal.valueOf(4905))
        );
    }

    private static Stream<Arguments> testGetDistanceByTimeProvider() {
        final EDistanceMetric meter = EDistanceMetric.M;
        final ETimeMetric second = ETimeMetric.SECOND;
        return Stream.of(
                Arguments.of(acc(0, MS2), time(1, second), vel(1, meter, second), meter, dis(1, meter)),
                Arguments.of(acc(0, MS2), time(1, second), vel(10, meter, second), meter, dis(10, meter)),
                Arguments.of(acc(10, MS2), time(1, second), vel(10, meter, second), meter, dis(15, meter)),
                Arguments.of(acc(10, MS2), time(1, second), vel(0, meter, second), meter, dis(5, meter)),
                Arguments.of(acc(500, G), time(60, second), vel(0, meter, second), meter, dis(8829000, meter))
        );
    }

    @ParameterizedTest
    @MethodSource("testGetDistanceByTimeProvider")
    void testGetDistanceByTime(final Acceleration acceleration,
                               final Time duration,
                               final Velocity velocity,
                               final EDistanceMetric targetMetric,
                               final Distance expectation) {
        final Distance result = acceleration.getDistanceByTime(duration, velocity, targetMetric);
        assertThat(result, Matchers.comparesEqualTo(expectation));
    }

    private static final MathContext MATH_CONTEXT = new MathContext(8, RoundingMode.DOWN);

    @Test
    void testAccelerationCalculationByMass() {
        final int[] civilAcceleration = {190, 207, 215, 230, 240, 253};

        final int[] tons = {8500000, 7000000, 4500000, 1500000, 500000, 80000};
        final int[] militaryAcceleration = {420, 450, 470, 500, 520, 550};

        final BigDecimal a = BigDecimal.valueOf(558); // Antriebswert

        final List<BigDecimal> paramList = List.of(
                BigDecimal.valueOf(-0.0001075032),
                BigDecimal.valueOf(7.261618).scaleByPowerOfTen(-11),
                BigDecimal.valueOf(-2.175344).scaleByPowerOfTen(-17),
                BigDecimal.valueOf(2.786797).scaleByPowerOfTen(-24),
                BigDecimal.valueOf(-1.275354).scaleByPowerOfTen(-31)
        );

        //y = 558.1465 - 0.0001075032*x + 7.261618e-11*x^2 - 2.1753440000000002e-17*x^3 + 2.786797e-24*x^4 - 1.275354e-31*x^5

        for (int j = 0; j < tons.length; j++) {
            final BigDecimal x = BigDecimal.valueOf(tons[j]);
            BigDecimal result = a;
            for (int i = 0; i < paramList.size(); i++) {
                final BigDecimal coefficient = paramList.get(i);
                final BigDecimal inBetween = coefficient.multiply(x.pow(i + 1), MATH_CONTEXT);
                result = result.add(inBetween);
            }
            assertEquals(result.setScale(0, RoundingMode.HALF_EVEN).intValue(), militaryAcceleration[j]);
        }
    }
}
