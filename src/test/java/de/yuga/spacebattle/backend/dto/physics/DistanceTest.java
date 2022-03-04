package de.yuga.spacebattle.backend.dto.physics;

import de.yuga.spacebattle.backend.enums.EDistanceMetric;
import org.hamcrest.Matchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static de.yuga.spacebattle.TestDataProviderUtils.dis;
import static de.yuga.spacebattle.backend.enums.EDistanceMetric.KM;
import static de.yuga.spacebattle.backend.enums.EDistanceMetric.M;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DistanceTest {

    private static Stream<Arguments> testConversionProvider() {
        return Stream.of(
                Arguments.of(dis(1000, M), KM, BigDecimal.ONE),
                Arguments.of(dis(1, KM), M, BigDecimal.valueOf(1000))
        );
    }

    @ParameterizedTest
    @MethodSource("testConversionProvider")
    public void testConversion(final Distance base, final EDistanceMetric metric, final BigDecimal expectation) {

        final BigDecimal result = base.getCoordinateInMetric(metric);
        assertThat(expectation, Matchers.comparesEqualTo(result));
    }

    private static Stream<Arguments> testEqualsProvider() {
        return Stream.of(
                Arguments.of(Distance.valueOf("1 M"), Distance.valueOf("1.00 M")),
                Arguments.of(Distance.valueOf("1.0000 M"), Distance.valueOf("1 M"))
        );
    }

    @ParameterizedTest
    @MethodSource("testEqualsProvider")
    void testEquals(final Distance first, final Distance second) {
        assertNotNull(first);
        assertNotNull(second);
        assertEquals(first, second);
    }

    private static Stream<Arguments> testGetDistanceAsStringWithUnitBigInteger() {
        return Stream.of(
                Arguments.of(dis(1, M), dis("1 M")),
                Arguments.of(dis(2, M), dis("2 M")),
                Arguments.of(dis(1000, M), dis("1 KM")),
                Arguments.of(dis(299792458, M), dis("1 LS")),
                Arguments.of(dis("17987547480 M"), dis("1 LM")),
                Arguments.of(dis("149597870700 M"), dis("1 AU")),
                Arguments.of(dis("1079252848800 M"), dis("1 LH")),
                Arguments.of(dis("25902068371200 M"), dis("1 LD")),
                Arguments.of(dis("9454254955488000 M"), dis("0.3063915365867315 PC")),
                Arguments.of(dis("30856776000000000 M"), dis("1 PC")),
                Arguments.of(dis("1495978707000 M"), dis("1.386 LH")),
                Arguments.of(dis("10792528488000 M"), dis("0.41667 LD")),
                Arguments.of(dis("259020683712000 M"), dis("0.008394 PC")),
                Arguments.of(dis("308567760000000000 M"), dis("10 PC"))
        );
    }

    @ParameterizedTest
    @MethodSource("testGetDistanceAsStringWithUnitBigInteger")
    void testGetDistanceAsStringWithUnitBigInteger(final Distance result, final Distance expectation) {
        assertThat(expectation, Matchers.comparesEqualTo(result));
    }
}
