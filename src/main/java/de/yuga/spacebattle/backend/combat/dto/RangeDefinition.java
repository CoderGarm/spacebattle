package de.yuga.spacebattle.backend.combat.dto;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

public class RangeDefinition implements Comparable<RangeDefinition> {

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
     * @param distance the given distance
     * @return <code>true</code> if the distance is inside the boundaries, <code>false</code> otherwise
     */
    public boolean isInRange(@Nonnull final BigDecimal distance) {
        Preconditions.checkNotNull(distance, "distance shouldn't be null!");

        final int compareToMin = minRange.compareTo(distance);
        final int compareToMax = maxRange.compareTo(distance);
        return compareToMin <= 0 && compareToMax >= 0;
    }

    /**
     * States if the given distance is inside these boundaries.
     *
     * @param rangeDefinition the given range
     * @return <code>true</code> if the distance is inside the boundaries, <code>false</code> otherwise
     */
    public boolean isInRange(@Nonnull final RangeDefinition rangeDefinition) {
        Preconditions.checkNotNull(rangeDefinition, "rangeDefinition shouldn't be null!");

        return isInRange(rangeDefinition.getMinRange()) && isInRange(rangeDefinition.getMaxRange());
    }

    /**
     * States that the given range def is adjusting or overlapping to this.
     *
     * @param rangeDefinition the range def to check
     * @return <code>true</code> if the given min or max range is inside the boundaries of this, <code>false</code> otherwise
     */
    public boolean isChainingRange(@Nonnull final RangeDefinition rangeDefinition) {
        Preconditions.checkNotNull(rangeDefinition, "rangeDefinition shouldn't be null!");

        final BigDecimal minRangeThat = rangeDefinition.getMinRange();
        final BigDecimal maxRangeThat = rangeDefinition.getMaxRange();

        return isInRange(minRangeThat) || isInRange(maxRangeThat);
    }

    @Nonnull
    public BigDecimal getMinRange() {
        return minRange;
    }

    @Nonnull
    public BigDecimal getMaxRange() {
        return maxRange;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (!(o instanceof RangeDefinition)) return false;

        final RangeDefinition that = (RangeDefinition) o;

        return new EqualsBuilder().append(minRange, that.minRange).append(maxRange, that.maxRange).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(minRange).append(maxRange).toHashCode();
    }

    @Override
    public int compareTo(@Nonnull final RangeDefinition o) {
        Preconditions.checkNotNull(o, "o shouldn't be null!");

        return maxRange.compareTo(o.getMaxRange());
    }
}
