package de.yuga.spacebattle.backend.entities.spacecrafts;

import de.yuga.spacebattle.backend.calculator.SpacecraftTonnageCalculator;
import de.yuga.spacebattle.backend.dto.physics.Mass;
import de.yuga.spacebattle.backend.enums.physics.EMassMetric;

import javax.annotation.Nullable;
import java.util.Comparator;

/**
 * Sorts the list of ship classes from biggest hull to smallest.
 */
public class ShipClassComparator implements Comparator<ShipClass> {

    @Override
    public int compare(@Nullable final ShipClass o1, @Nullable final ShipClass o2) {
        if (o1 == null || o2 == null) {
            return 1;
        }
        final Mass o1Mass = SpacecraftTonnageCalculator.getFullTonnage(o1);
        final Mass o2Mass = SpacecraftTonnageCalculator.getFullTonnage(o2);
        return o1Mass.getCoordinateInMetric(EMassMetric.KT).compareTo(o2Mass.getCoordinateInMetric(EMassMetric.KT));
    }
}
