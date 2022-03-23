package de.yuga.spacebattle.backend.combat.dto;

import de.yuga.spacebattle.backend.ComparatorsContractTest;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

@Disabled
class DamagePerRangePerAlignmentTest {

    @Test
    void testContract() {
        final List<DamagePerRangePerAlignment> list = List.of(
                new DamagePerRangePerAlignment(new RangeDefinition(BigDecimal.ZERO, BigDecimal.TEN, EDistanceMetric.M), List.of()),
                new DamagePerRangePerAlignment(new RangeDefinition(BigDecimal.ZERO, BigDecimal.ONE, EDistanceMetric.M), List.of()),
                new DamagePerRangePerAlignment(new RangeDefinition(BigDecimal.ZERO, BigDecimal.valueOf(0.0002), EDistanceMetric.AU), List.of())
        );
        ComparatorsContractTest.verifyTransitivity(new DamagePerRangePerAlignment.MaxRangeComparator(), list);
    }
}
