package de.yuga.spacebattle.backend.combat.dto;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

public class RangeDefinition {

    /**
     * The minimal range from where the damage can be projected.
     */
    @Nonnull
    private final BigDecimal minRange;

    /**
     * The maximal range from where the damage can be projected.
     */
    @Nonnull
    private final BigDecimal maxRange;

    public RangeDefinition(@Nonnull final BigDecimal minRange, @Nonnull final BigDecimal maxRange) {
        Preconditions.checkNotNull(minRange, "minRange shouldn't be null!");
        Preconditions.checkNotNull(maxRange, "maxRange shouldn't be null!");

        this.minRange = minRange;
        this.maxRange = maxRange;
    }

    /**
     * States if the given distance is inside these boundaries.
     *
     * @param distance the given range
     * @return <code>true</code> if the distance is inside the boundaries, <code>false</code> otherwise
     */
    public boolean isInRange(final BigDecimal distance) {
        final int compareToMin = minRange.compareTo(distance);
        final int compareToMax = maxRange.compareTo(distance);
        return compareToMin <= 0 && compareToMax >= 0;
    }

    @Nonnull
    public BigDecimal getMinRange() {
        return minRange;
    }

    @Nonnull
    public BigDecimal getMaxRange() {
        return maxRange;
    }
}
