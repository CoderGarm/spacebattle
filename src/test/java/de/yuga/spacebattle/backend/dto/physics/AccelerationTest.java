package de.yuga.spacebattle.backend.dto.physics;

import de.yuga.spacebattle.backend.enums.physics.EAccelerationMetric;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import de.yuga.spacebattle.backend.enums.physics.ETimeMetric;
import org.hamcrest.Matchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Stream;

import static de.yuga.spacebattle.TestDataProviderUtils.*;
import static de.yuga.spacebattle.backend.enums.physics.EAccelerationMetric.G;
import static de.yuga.spacebattle.backend.enums.physics.EAccelerationMetric.MS2;
import static org.hamcrest.MatcherAssert.assertThat;

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
}
