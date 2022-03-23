package de.yuga.spacebattle.backend.combat.dto;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Comparator;

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

    @Nonnull
    private final EDistanceMetric distanceMetric;

    public RangeDefinition(@Nonnull final BigDecimal minRange, @Nonnull final BigDecimal maxRange, @Nonnull final EDistanceMetric distanceMetric) {
        Preconditions.checkNotNull(minRange, "minRange shouldn't be null!");
        Preconditions.checkNotNull(maxRange, "maxRange shouldn't be null!");
        Preconditions.checkNotNull(distanceMetric, "distanceMetric shouldn't be null!");

        this.minRange = minRange;
        this.maxRange = maxRange;
        this.distanceMetric = distanceMetric;
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

        final Distance minRangeThat = rangeDefinition.getMinRange();
        final boolean insideMin = new Distance(minRange, distanceMetric).compareTo(minRangeThat) <= 0;

        final Distance maxRangeThat = rangeDefinition.getMaxRange();
        final boolean insideMax = new Distance(maxRange, distanceMetric).compareTo(maxRangeThat) >= 0;

        return insideMin && insideMax;
    }

    /**
     * States if the given distance is bigger or equal the max range.
     *
     * @param distance the given range
     * @return <code>true</code> if the distance is bigger or equal the max range, <code>false</code> otherwise
     */
    public boolean isInRange(@Nonnull final Distance distance) {
        Preconditions.checkNotNull(distance, "distance shouldn't be null!");

        return maxRange.compareTo(distance.getCoordinateInMetric(distanceMetric)) <= 0;
    }

    /**
     * States that the given range def is adjusting or overlapping to this.
     *
     * @param rangeDefinition the range def to check
     * @return <code>true</code> if the given min or max range is inside the boundaries of this, <code>false</code> otherwise
     */
    public boolean isChainingRange(@Nonnull final RangeDefinition rangeDefinition) {
        Preconditions.checkNotNull(rangeDefinition, "rangeDefinition shouldn't be null!");

        final Distance minRangeThat = rangeDefinition.getMinRange();
        final Distance maxRangeThat = rangeDefinition.getMaxRange();

        return isInRange(minRangeThat) || isInRange(maxRangeThat);
    }

    @Nonnull
    public Distance getMinRange() {
        return new Distance(minRange, distanceMetric);
    }

    @Nonnull
    public Distance getMaxRange() {
        return new Distance(maxRange, distanceMetric);
    }

    @Nonnull
    public EDistanceMetric getDistanceMetric() {
        return distanceMetric;
    }

    @Override
    public String toString() {
        return "minRange: " + minRange + ", maxRange: " + maxRange + ", metric: " + distanceMetric;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (!(o instanceof RangeDefinition)) return false;

        final RangeDefinition that = (RangeDefinition) o;

        return new EqualsBuilder().append(minRange, that.minRange).append(maxRange, that.maxRange).append(distanceMetric, that.distanceMetric).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(minRange).append(maxRange).append(distanceMetric).toHashCode();
    }

    public static class MaxRangeComparator implements Comparator<RangeDefinition> {

        @Override
        public int compare(@Nonnull final RangeDefinition o1, @Nonnull final RangeDefinition o2) {
            Preconditions.checkNotNull(o1, "o1 shouldn't be null!");
            Preconditions.checkNotNull(o2, "o2 shouldn't be null!");

            return o1.getMaxRange().compareTo(o2.getMaxRange());
        }
    }
}
