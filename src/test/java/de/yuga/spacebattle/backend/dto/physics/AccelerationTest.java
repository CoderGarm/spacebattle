package de.yuga.spacebattle.backend.dto.physics;

import de.yuga.spacebattle.backend.enums.EAccelerationMetric;
import org.hamcrest.Matchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Stream;

import static de.yuga.spacebattle.TestDataProviderUtils.acc;
import static de.yuga.spacebattle.backend.enums.EAccelerationMetric.G;
import static de.yuga.spacebattle.backend.enums.EAccelerationMetric.MS2;
import static org.hamcrest.MatcherAssert.assertThat;

class AccelerationTest {

    @ParameterizedTest
    @MethodSource("testConversionProvider")
    public void testConversion(final Acceleration base, final EAccelerationMetric metric, final BigDecimal expectation) {

        final BigDecimal result = base.convertToMetric(metric).setScale(2, RoundingMode.HALF_UP);
        assertThat(expectation, Matchers.comparesEqualTo(result));
    }

    private static Stream<Arguments> testConversionProvider() {
        return Stream.of(
                Arguments.of(acc(9.81, MS2), G, BigDecimal.ONE),
                Arguments.of(acc(1, G), MS2, BigDecimal.valueOf(9.81))
        );
    }
}
