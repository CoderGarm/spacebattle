package de.yuga.spacebattle.backend.dto.physics;

import de.yuga.spacebattle.backend.enums.EDistanceMetric;
import org.hamcrest.Matchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static de.yuga.spacebattle.TestDataProviderUtils.bd;
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


    private static Stream<Arguments> testGetDistanceAsStringWithUnitBigDecimal() {
        return Stream.of(
                Arguments.of(bd(1), "1.0 m"),
                Arguments.of(bd(2), "2.0 m"),
                Arguments.of(bd(1000), "1.0 km"),
                Arguments.of(bd(299792458), "1 ls"),
                Arguments.of(bd("17987547480"), "1 lm"),
                Arguments.of(bd("149597870700"), "1 AU"),
                Arguments.of(bd("1079252848800"), "1 lh"),
                Arguments.of(bd("25902068371200"), "1 ld"),
                Arguments.of(bd("9454254955488000"), "0.3063 pc"),
                Arguments.of(bd("30856776000000000"), "1 pc"),
                Arguments.of(bd("1495978707000"), "1.386 lh"),
                Arguments.of(bd("10792528488000"), "0.4166 ld"),
                Arguments.of(bd("259020683712000"), "0.008394 pc"),
                Arguments.of(bd("308567760000000000"), "10 pc")
        );
    }

    @ParameterizedTest
    @MethodSource("testGetDistanceAsStringWithUnitBigDecimal")
    void testGetDistanceAsStringWithUnitBigDecimal(ArgumentsAccessor accessor) {
        final Object[] args = accessor.toArray();
        final BigDecimal a = (BigDecimal) args[0];
        final String expectation = (String) args[1];
        final String result = a.toString();
        assertEquals(expectation, result);
    }

    private static Stream<Arguments> testGetDistanceAsStringWithUnitBigInteger() {
        return Stream.of(
                Arguments.of(bd(1), "1 m"),
                Arguments.of(bd(2), "2 m"),
                Arguments.of(bd(1000), "1 km"),
                Arguments.of(bd(299792458), "1 ls"),
                Arguments.of(bd("17987547480"), "1 lm"),
                Arguments.of(bd("149597870700"), "1 AU"),
                Arguments.of(bd("1079252848800"), "1 lh"),
                Arguments.of(bd("25902068371200"), "1 ld"),
                Arguments.of(bd("9454254955488000"), "0.3063 pc"),
                Arguments.of(bd("30856776000000000"), "1 pc"),
                Arguments.of(bd("1495978707000"), "1.386 lh"),
                Arguments.of(bd("10792528488000"), "0.4166 ld"),
                Arguments.of(bd("259020683712000"), "0.008394 pc"),
                Arguments.of(bd("308567760000000000"), "10 pc")
        );
    }

    @ParameterizedTest
    @MethodSource("testGetDistanceAsStringWithUnitBigInteger")
    void testGetDistanceAsStringWithUnitBigInteger(ArgumentsAccessor accessor) {
        final Object[] args = accessor.toArray();
        final BigDecimal a = (BigDecimal) args[0];
        final String expectation = (String) args[1];
        final String result = a.toString();
        assertEquals(expectation, result);
    }
}
