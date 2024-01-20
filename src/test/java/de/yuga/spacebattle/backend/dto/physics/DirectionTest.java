package de.yuga.spacebattle.backend.dto.physics;

import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DirectionTest {


    private static Stream<Arguments> testCalculateDirectionAngle() {
        return Stream.of(
                Arguments.of(1, 1, 45),
                Arguments.of(0, 1, 90),
                Arguments.of(-1, 0, 180),
                Arguments.of(0, -1, 270),
                Arguments.of(1, 0, 360)
        );
    }

    /**
     * Think about the flipped y-axis which disrespects the human brain's necessities a lot!
     */
    @ParameterizedTest
    @MethodSource("testCalculateDirectionAngle")
    void calculateDirectionAngle(final int x, final int y, final double expectation) {
        final double resultingAngleByFlippedYAxis = 360 - expectation;
        final Orbit pointToLookAt = new Orbit(BigDecimal.valueOf(x), BigDecimal.valueOf(y), EDistanceMetric.M);

        final Orbit origin = new Orbit(BigDecimal.valueOf(0), BigDecimal.valueOf(0), EDistanceMetric.M);
        final Direction originsDirection = new Direction(BigDecimal.valueOf(1), BigDecimal.valueOf(0));

        final double angleBetween = Direction.getAngleLineOfSight(origin, originsDirection, pointToLookAt);
        assertEquals(resultingAngleByFlippedYAxis, angleBetween);
    }

}
