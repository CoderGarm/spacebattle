package de.yuga.spacebattle.backend.calculator.distance;

import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.enums.EDistanceMetric;
import org.hamcrest.Matchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.stream.Stream;

import static de.yuga.spacebattle.TestDataProviderUtils.*;
import static de.yuga.spacebattle.backend.calculator.distance.Quadrant.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DistanceCalculatorTest {

    @ParameterizedTest
    @MethodSource("testCalculateDistanceDataProvider")
    public void testCalculateDistance(final Fleet fleet, final Planet planet, final int expectation) {
        final FleetOrbit destination = new FleetOrbit(planet.getOrbit(), planet.getSystem());
        final int calculateDistance = DistanceCalculator.calculateTimeToTravel(fleet, destination);
        assertEquals(calculateDistance, expectation);
    }

    private static Stream<Arguments> testCalculateDistanceDataProvider() {
        final Planet planetStart = planet(0, 0);
        final Planet planetTarget1 = planet(10, 10);
        final Planet planetTarget2 = planet(20, 20);
        final Planet planetTarget3 = planet(30, 30);

        return Stream.of(
                Arguments.of(fleet(10, planetStart), planetTarget1, 414),
                Arguments.of(fleet(15, planetStart), planetTarget1, 276),
                Arguments.of(fleet(10, planetStart), planetTarget2, 416),
                Arguments.of(fleet(14, planetStart), planetTarget2, 297),
                Arguments.of(fleet(11, planetStart), planetTarget3, 379),
                Arguments.of(fleet(14, planetStart), planetTarget3, 298)
        );
    }

    private static Stream<Arguments> testGetDigitCountBigDecimal() {
        return Stream.of(
                Arguments.of(1D, 1),
                Arguments.of(11D, 2),
                Arguments.of(1111111111D, 10),
                Arguments.of(11111.1111D, 5),
                Arguments.of(1.1111D, 1)
        );
    }

    @ParameterizedTest
    @MethodSource("testGetDigitCountBigDecimal")
    void testGetDigitCountBigDecimal(final double value, final int expectation) {
        final int result = DistanceCalculator.getDigitCount(BigDecimal.valueOf(value));
        assertEquals(expectation, result);
    }

    private static Stream<Arguments> testGetDigitCountBigInteger() {
        return Stream.of(
                Arguments.of(1L, 1),
                Arguments.of(11L, 2),
                Arguments.of(1111111111L, 10),
                Arguments.of(11111L, 5)
        );
    }

    @ParameterizedTest
    @MethodSource("testGetDigitCountBigInteger")
    void testGetDigitCountBigInteger(final long value, final int expectation) {
        final int result = DistanceCalculator.getDigitCount(BigInteger.valueOf(value));
        assertEquals(expectation, result);
    }

    private static Stream<Arguments> testCalculateTimeToTravelWithDefinedOrigin() {
        final Planet planet = planet(0, 0);
        return Stream.of(
                Arguments.of(fleet(500, planet),
                        new FleetOrbit(orbit(1, 2000, Planet.PLANET_STANDARD_METRIC), system(2000, 1)),
                        new FleetOrbit(Orbit.getCenterOrbit(), system(25, 25)),
                        11),
                Arguments.of(fleet(500, planet),
                        new FleetOrbit(orbit(666, 666, Planet.PLANET_STANDARD_METRIC), system(222, 222)),
                        new FleetOrbit(Orbit.getCenterOrbit(), system(25, 25)),
                        9),
                Arguments.of(fleet(500, planet),
                        new FleetOrbit(orbit(666, 666, Planet.PLANET_STANDARD_METRIC), system(-666, -666)),
                        new FleetOrbit(Orbit.getCenterOrbit(), system(1125, 1130)),
                        13)
        );
    }

    @ParameterizedTest
    @MethodSource("testCalculateTimeToTravelWithDefinedOrigin")
    void testCalculateTimeToTravelWithDefinedOrigin(final Fleet fleet,
                                                    final FleetOrbit origin,
                                                    final FleetOrbit destination,
                                                    final Integer expectation) {

        final int result = DistanceCalculator.calculateTimeToTravel(fleet, origin, destination);
        assertEquals(expectation, result);
    }

    private static Stream<Arguments> testCalculateTimeToTravel() {
        final Planet planet = planet(0, 0);
        return Stream.of(
                Arguments.of(fleet(500, planet), new FleetOrbit(Orbit.getCenterOrbit(), system(25, 25)), 2),
                Arguments.of(fleet(500, planet), new FleetOrbit(Orbit.getCenterOrbit(), system(50, 50)), 4),
                Arguments.of(fleet(500, planet), new FleetOrbit(Orbit.getCenterOrbit(), system(1125, 1130)), 85)
        );
    }

    @ParameterizedTest
    @MethodSource("testCalculateTimeToTravel")
    void testCalculateTimeToTravel(final Fleet fleet, final FleetOrbit destination, final int expectation) {
        final int result = DistanceCalculator.calculateTimeToTravel(fleet, destination);
        assertEquals(expectation, result);
    }

    private static Stream<Arguments> testCreateByRadiusAndQuadrant() {
        return Stream.of(
                Arguments.of(bd(1), Q1, Orbit.getCenterOrbit()),
                Arguments.of(bd(19.23), Q2, new Orbit(new Distance(-13, EDistanceMetric.M), new Distance(13, EDistanceMetric.M))),
                Arguments.of(bd(24.11), Q3, new Orbit(new Distance(-17, EDistanceMetric.M), new Distance(-17, EDistanceMetric.M))),
                Arguments.of(bd(99999221), Q4, new Orbit(new Distance(70710127, EDistanceMetric.M), new Distance(-70710127, EDistanceMetric.M)))
        );
    }

    @ParameterizedTest
    @MethodSource("testCreateByRadiusAndQuadrant")
    void testCreateByRadiusAndQuadrant(final BigDecimal radius, final Quadrant quadrant, final Orbit expectation) {
        final Orbit result = DistanceCalculator.createByRadiusAndQuadrant(radius, quadrant, EDistanceMetric.M);
        assertEquals(expectation, result);
    }

    private static Stream<Arguments> testGetOrbitalDistanceProvider() {
        return Stream.of(
                Arguments.of(Orbit.getCenterOrbit(), Orbit.getCenterOrbit(), Distance.ZERO),
                Arguments.of(Orbit.getCenterOrbit(), Orbit.getCenterOrbit(), Distance.ZERO),
                Arguments.of(new Orbit(new Distance(1, EDistanceMetric.M), new Distance(1, EDistanceMetric.M)),
                        new Orbit(new Distance(-1, EDistanceMetric.M), new Distance(-1, EDistanceMetric.M)),
                        new Distance(bd(2.828), EDistanceMetric.M)),
                Arguments.of(new Orbit(new Distance(2, EDistanceMetric.M), new Distance(2, EDistanceMetric.M)),
                        new Orbit(new Distance(2, EDistanceMetric.M), new Distance(2, EDistanceMetric.M)),
                        new Distance(bd(0), EDistanceMetric.M)),
                Arguments.of(new Orbit(new Distance(-13, EDistanceMetric.M), new Distance(13, EDistanceMetric.M)),
                        new Orbit(new Distance(1, EDistanceMetric.M), new Distance(-17, EDistanceMetric.M)),
                        new Distance(bd(33.1), EDistanceMetric.M)),
                Arguments.of(new Orbit(new Distance(bd("-643634643513"), EDistanceMetric.M), new Distance(bd("675765667"), EDistanceMetric.M)),
                        new Orbit(new Distance(-17, EDistanceMetric.M), new Distance(-17, EDistanceMetric.M)),
                        new Distance(bd("6.436349982E+11"), EDistanceMetric.M))
        );
    }

    @ParameterizedTest
    @MethodSource("testGetOrbitalDistanceProvider")
    void testGetOrbitalDistance(final Orbit one, final Orbit two, final Distance expectation) {
        final Distance result = DistanceCalculator.getOrbitalDistance(one, two);
        assertThat(expectation, Matchers.comparesEqualTo(result));
    }

    private static Stream<Arguments> testGetDistanceProvider() {
        return Stream.of(
                Arguments.of(bd(2), bd(3), bd(3.605551275)),
                Arguments.of(bd(9), bd(25), bd(26.57066051)),
                Arguments.of(bd(21), bd(31), bd(37.44329045)),
                Arguments.of(bd(221), bd(113), bd(248.2136176)),
                Arguments.of(bd(6578), bd(1234), bd(6692.745326)),
                Arguments.of(bd(1), bd(1548), bd(1548.000322))
        );
    }


    @ParameterizedTest
    @MethodSource("testGetDistanceProvider")
    void testGetDistance(final BigDecimal a, final BigDecimal b, final BigDecimal expectation) {
        final BigDecimal result = DistanceCalculator.getDistance(a, b);
        assertThat(expectation, Matchers.comparesEqualTo(result));
    }

    private static Stream<Arguments> testGetDistanceAsStringWithUnitBigDecimalProvider() {
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
    @MethodSource("testGetDistanceAsStringWithUnitBigDecimalProvider")
    void testGetDistanceAsStringWithUnitBigDecimal(final BigDecimal a, final String expectation) {
        final String result = DistanceCalculator.getDistanceAsStringWithUnit(a);
        assertEquals(expectation, result);
    }

    private static Stream<Arguments> testGetDistanceAsStringWithUnitBigIntegerProvider() {
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
    @MethodSource("testGetDistanceAsStringWithUnitBigIntegerProvider")
    void testGetDistanceAsStringWithUnitBigInteger(final BigDecimal a, final String expectation) {
        final String result = a.toString();
        assertEquals(expectation, result);
    }

    private static Stream<Arguments> scalingProvider() {
        return Stream.of(
                Arguments.of(new Distance(BigDecimal.valueOf(1300), EDistanceMetric.LS), Distance.valueOf("1300 LS")),
                Arguments.of(new Distance(BigDecimal.valueOf(-1300), EDistanceMetric.LS), Distance.valueOf("-1300 LS")),
                Arguments.of(new Distance(BigDecimal.valueOf(2.605185), EDistanceMetric.AU), Distance.valueOf("389730127 KM")),
                Arguments.of(Distance.valueOf("47.414849487074682006498619613044098741738707758486270904541015625 LS"), Distance.valueOf("14214614 KM")),
                Arguments.of(Distance.valueOf("0.455467960302086778612619613044098741738707758486270904541015625 LS"), Distance.valueOf("136545860 M")),
                Arguments.of(Distance.valueOf("0.4554679603020867786126196 LS"), Distance.valueOf("136545860 M"))
        );
    }

    @ParameterizedTest
    @MethodSource("scalingProvider")
    void testConvertToScape(final Distance distance, final Distance expectation) {

        final Distance result = DistanceCalculator.convertToScale(distance);
        assertNotNull(result);
        assertEquals(expectation, result);
    }
}
