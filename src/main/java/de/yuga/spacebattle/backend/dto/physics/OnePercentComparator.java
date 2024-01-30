package de.yuga.spacebattle.backend.dto.physics;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Comparator;

/**
 * Compares two values and returns the equality if the values are at max 1% different.
 */
public class OnePercentComparator implements Comparator<BigDecimal> {

    @Override
    public int compare(@Nonnull final BigDecimal o1, @Nonnull final BigDecimal o2) {
        Preconditions.checkNotNull(o1, "o1 shouldn't be null!");
        Preconditions.checkNotNull(o2, "o2 shouldn't be null!");


        if (o1.compareTo(BigDecimal.ZERO) == 0 || o2.compareTo(BigDecimal.ZERO) == 0) {
            // if one value is zero than just compare the values
            return o1.compareTo(o2);
        }

        final BigDecimal absolutDifference = o1.subtract(o2).abs();
        final BigDecimal absPercentageDifference = absolutDifference.divide(o1.min(o2), DistanceCalculator.MC_HU).multiply(BigDecimal.valueOf(100)).abs();
        return absPercentageDifference.compareTo(BigDecimal.valueOf(0.01)) <= 0 ? 0 : o1.compareTo(o2);
    }
}
