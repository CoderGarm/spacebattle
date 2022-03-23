package de.yuga.spacebattle.backend.entities.orbitals;

import de.yuga.spacebattle.backend.dto.physics.Direction;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static de.yuga.spacebattle.TestDataProviderUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OrbitTest {

    private static Stream<Arguments> testGetDestinationByProvider() {
        final int distanceValue = 5;
        final Distance distance = dis(distanceValue, EDistanceMetric.M);
        final Orbit baseOrbit = Orbit.getCenterOrbit();
        return Stream.of(
                Arguments.of(baseOrbit, dir("up"), distance, orbit(0, distanceValue, EDistanceMetric.M)),
                Arguments.of(baseOrbit, dir("down"), distance, orbit(0, -distanceValue, EDistanceMetric.M)),
                Arguments.of(baseOrbit, dir("right"), distance, orbit(distanceValue, 0, EDistanceMetric.M)),
                Arguments.of(baseOrbit, dir("left"), distance, orbit(-distanceValue, 0, EDistanceMetric.M)),
                Arguments.of(baseOrbit, dir("upper right"), dis(new BigDecimal("7.0710676"), EDistanceMetric.M), orbit(distanceValue, distanceValue, EDistanceMetric.M)),
                Arguments.of(baseOrbit, dir("upper left"), dis(new BigDecimal("7.0710676"), EDistanceMetric.M), orbit(-distanceValue, distanceValue, EDistanceMetric.M)),
                // live data from special case but valid
                Arguments.of(orbit(new BigDecimal("-17.54901749158640793321427508999477140605449676513671875"),
                                new BigDecimal("-17.54901749158640793321427508999477140605449676513671875"),
                                EDistanceMetric.LS),
                        new Direction(new BigDecimal("0.70710678"), new BigDecimal("0.70710678")),
                        dis(new BigDecimal("32.300"), EDistanceMetric.LS),
                        orbit(new BigDecimal("5.29053150841359206678572491000522859394550323486328125"),
                                new BigDecimal("5.29053150841359206678572491000522859394550323486328125"),
                                EDistanceMetric.LS)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testGetDestinationByProvider")
    void testGetDestinationBy(final Orbit baseOrbit, final Direction dir, final Distance distance, final Orbit expectation) {

        final Orbit result = baseOrbit.getDestinationBy(distance, dir);
        final Distance resultDistance = baseOrbit.getDistance(result);

        assertEquals(expectation, result);
        assertEquals(distance, resultDistance);
    }
}
