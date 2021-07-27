package de.yuga.spacebattle.backend.calculator.distance;

import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.stream.Stream;

import static de.yuga.spacebattle.TestDataProviderUtils.*;
import static de.yuga.spacebattle.backend.calculator.distance.Quadrant.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DistanceCalculatorTest {

    @ParameterizedTest
    @MethodSource("testCalculateDistanceDataProvider")
    public void testCalculateDistance(ArgumentsAccessor accessor) {
        Object[] args = accessor.toArray();

        final Fleet fleet = (Fleet) args[0];
        final Planet planet = (Planet) args[1];
        final int expectation = (int) args[2];
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
    void testGetDigitCountBigDecimal(ArgumentsAccessor accessor) {
        final Object[] args = accessor.toArray();
        final double value = (double) args[0];
        final int expectation = (int) args[1];
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
    void testGetDigitCountBigInteger(ArgumentsAccessor accessor) {
        final Object[] args = accessor.toArray();
        final long value = (long) args[0];
        final int expectation = (int) args[1];
        final int result = DistanceCalculator.getDigitCount(BigInteger.valueOf(value));
        assertEquals(expectation, result);
    }

    private static Stream<Arguments> testCalculateTimeToTravelWithDefinedOrigin() {
        final Planet planet = planet(0, 0);
        return Stream.of(
                Arguments.of(fleet(500, planet),
                        new FleetOrbit(orbit(1, 2000), system(2000, 1)),
                        new FleetOrbit(Orbit.getCenterOrbit(), system(25, 25)),
                        11),
                Arguments.of(fleet(500, planet),
                        new FleetOrbit(orbit(666, 666), system(222, 222)),
                        new FleetOrbit(Orbit.getCenterOrbit(), system(25, 25)),
                        9),
                Arguments.of(fleet(500, planet),
                        new FleetOrbit(orbit(666, 666), system(-666, -666)),
                        new FleetOrbit(Orbit.getCenterOrbit(), system(1125, 1130)),
                        13)
        );
    }

    @ParameterizedTest
    @MethodSource("testCalculateTimeToTravelWithDefinedOrigin")
    void testCalculateTimeToTravelWithDefinedOrigin(ArgumentsAccessor accessor) {
        final Fleet fleet = accessor.get(0, Fleet.class);
        final FleetOrbit origin = accessor.get(1, FleetOrbit.class);
        final FleetOrbit destination = accessor.get(2, FleetOrbit.class);
        final Integer expectation = accessor.get(3, Integer.class);
        final int result = DistanceCalculator.calculateTimeToTravel(fleet, origin, destination);
        assertEquals(expectation, result);
    }

    private static Stream<Arguments> testCalculateTimeToTravel() {
        final Planet planet = planet(0, 0);
        return Stream.of(
                Arguments.of(fleet(500, planet),
                        new FleetOrbit(Orbit.getCenterOrbit(), system(25, 25)),
                        8),
                Arguments.of(fleet(500, planet),
                        new FleetOrbit(Orbit.getCenterOrbit(), system(25, 25)),
                        8),
                Arguments.of(fleet(500, planet),
                        new FleetOrbit(Orbit.getCenterOrbit(), system(1125, 1130)),
                        11)
        );
    }

    @ParameterizedTest
    @MethodSource("testCalculateTimeToTravel")
    void testCalculateTimeToTravel(ArgumentsAccessor accessor) {
        final Fleet fleet = accessor.get(0, Fleet.class);
        final FleetOrbit destination = accessor.get(1, FleetOrbit.class);
        final Integer expectation = accessor.get(2, Integer.class);
        final int result = DistanceCalculator.calculateTimeToTravel(fleet, destination);
        assertEquals(expectation, result);
    }

    private static Stream<Arguments> testCreateByRadiusAndQuadrant() {
        return Stream.of(
                Arguments.of(bd(1), Q1, Orbit.getCenterOrbit()),
                Arguments.of(bd(19.23), Q2, new Orbit(-13, 13)),
                Arguments.of(bd(24.11), Q3, new Orbit(-17, -17)),
                Arguments.of(bd(99999221), Q4, new Orbit(70710127, -70710127))
        );
    }

    @ParameterizedTest
    @MethodSource("testCreateByRadiusAndQuadrant")
    void testCreateByRadiusAndQuadrant(ArgumentsAccessor accessor) {
        final Object[] args = accessor.toArray();
        final BigDecimal radius = (BigDecimal) args[0];
        final Quadrant quadrant = (Quadrant) args[1];
        final Orbit expectation = (Orbit) args[2];
        final Orbit result = DistanceCalculator.createByRadiusAndQuadrant(radius, quadrant);
        assertEquals(expectation, result);
    }

    private static Stream<Arguments> testGetOrbitalDistance() {
        return Stream.of(
                Arguments.of(Orbit.getCenterOrbit(), Orbit.getCenterOrbit(), bd(0)),
                Arguments.of(new Orbit(0, 0), new Orbit(0, 0), bd(0)),
                Arguments.of(new Orbit(1, 1), new Orbit(-1, -1), bd(2.828)),
                Arguments.of(new Orbit(2, 2), new Orbit(2, 2), bd(0)),
                Arguments.of(new Orbit(-13, 13), new Orbit(bi(1), bi(-17)), bd(33.1)),
                Arguments.of(new Orbit(bd("-643634643513"), bd("675765667")), new Orbit(-17, -17), bd("6.436E+11"))
        );
    }

    @ParameterizedTest
    @MethodSource("testGetOrbitalDistance")
    void testGetOrbitalDistance(ArgumentsAccessor accessor) {
        final Object[] args = accessor.toArray();
        final Orbit one = (Orbit) args[0];
        final Orbit two = (Orbit) args[1];
        final BigDecimal expectation = (BigDecimal) args[2];
        final BigDecimal result = DistanceCalculator.getOrbitalDistance(one, two);
        assertEquals(0, expectation.compareTo(result));
    }

    private static Stream<Arguments> testGetDistance() {
        return Stream.of(
                Arguments.of(bd(2), bd(3), bd(3.605)),
                Arguments.of(bd(9), bd(25), bd(26.57)),
                Arguments.of(bd(21), bd(31), bd(37.44)),
                Arguments.of(bd(221), bd(113), bd(248.2)),
                Arguments.of(bd(6578), bd(1234), bd(6692)),
                Arguments.of(bd(1), bd(1548), bd(1548))
        );
    }

    private static BigDecimal bd(final double x) {
        return BigDecimal.valueOf(x);
    }

    private static BigDecimal bd(final String x) {
        return new BigDecimal(x);
    }

    @ParameterizedTest
    @MethodSource("testGetDistance")
    void testGetDistance(ArgumentsAccessor accessor) {
        final Object[] args = accessor.toArray();
        final BigDecimal a = (BigDecimal) args[0];
        final BigDecimal b = (BigDecimal) args[1];
        final BigDecimal expectation = (BigDecimal) args[2];

        final BigDecimal result = DistanceCalculator.getDistance(a, b);
        assertEquals(0, expectation.compareTo(result));
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
        final String result = DistanceCalculator.getDistanceAsStringWithUnit(a);
        assertEquals(expectation, result);
    }

    private static BigInteger bi(final long x) {
        return BigInteger.valueOf(x);
    }

    private static BigInteger bi(final String x) {
        return new BigInteger(x);
    }

    private static Stream<Arguments> testGetDistanceAsStringWithUnitBigInteger() {
        return Stream.of(
                Arguments.of(bi(1), "1 m"),
                Arguments.of(bi(2), "2 m"),
                Arguments.of(bi(1000), "1 km"),
                Arguments.of(bi(299792458), "1 ls"),
                Arguments.of(bi("17987547480"), "1 lm"),
                Arguments.of(bi("149597870700"), "1 AU"),
                Arguments.of(bi("1079252848800"), "1 lh"),
                Arguments.of(bi("25902068371200"), "1 ld"),
                Arguments.of(bi("9454254955488000"), "0.3063 pc"),
                Arguments.of(bi("30856776000000000"), "1 pc"),
                Arguments.of(bi("1495978707000"), "1.386 lh"),
                Arguments.of(bi("10792528488000"), "0.4166 ld"),
                Arguments.of(bi("259020683712000"), "0.008394 pc"),
                Arguments.of(bi("308567760000000000"), "10 pc")
        );
    }

    @ParameterizedTest
    @MethodSource("testGetDistanceAsStringWithUnitBigInteger")
    void testGetDistanceAsStringWithUnitBigInteger(ArgumentsAccessor accessor) {
        final Object[] args = accessor.toArray();
        final BigInteger a = (BigInteger) args[0];
        final String expectation = (String) args[1];
        final String result = DistanceCalculator.getDistanceAsStringWithUnit(a);
        assertEquals(expectation, result);
    }
}
