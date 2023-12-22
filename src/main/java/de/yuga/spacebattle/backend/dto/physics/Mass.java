package de.yuga.spacebattle.backend.dto.physics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.physics.EMassMetric;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@Schema(description = ".")
public class Mass implements Cloneable, Comparable<Mass> {

    private static final MathContext MC = new MathContext(8, RoundingMode.HALF_UP);

    public static final Mass ZERO = new Mass(0, EMassMetric.T);

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The value of this distance.")
    private BigDecimal coordinate;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The metric of this time.")
    private EMassMetric massMetric;

    public Mass() {
        this.coordinate = BigDecimal.ZERO;
        this.massMetric = EMassMetric.T;
    }

    public Mass(final int coordinate, @Nonnull final EMassMetric massMetric) {
        Preconditions.checkNotNull(massMetric, "timeMetric shouldn't be null!");

        this.coordinate = BigDecimal.valueOf(coordinate);
        this.massMetric = massMetric;
    }

    public Mass(final double coordinate, @Nonnull final EMassMetric massMetric) {
        Preconditions.checkNotNull(massMetric, "timeMetric shouldn't be null!");

        this.coordinate = BigDecimal.valueOf(coordinate);
        this.massMetric = massMetric;
    }

    public Mass(@Nonnull final BigDecimal coordinate, @Nonnull final EMassMetric massMetric) {
        Preconditions.checkNotNull(coordinate, "coordinate shouldn't be null!");
        Preconditions.checkNotNull(massMetric, "timeMetric shouldn't be null!");

        this.coordinate = coordinate;
        this.massMetric = massMetric;
    }

    @Nonnull
    @JsonIgnore
    public BigDecimal getCoordinate() {
        return coordinate;
    }

    @Nonnull
    @JsonIgnore
    public EMassMetric getMassMetric() {
        return massMetric;
    }

    @Nonnull
    @JsonIgnore
    public BigDecimal getCoordinateInMetric(@Nonnull final EMassMetric targetMetric) {
        Preconditions.checkNotNull(targetMetric, "targetMetric shouldn't be null!");

        if (massMetric == targetMetric) {
            return coordinate;
        }
        final BigDecimal factor = massMetric.getConversionFactor(targetMetric);
        return coordinate.multiply(factor, new MathContext(targetMetric.getScale(), RoundingMode.HALF_UP));
    }

    @Nonnull
    @JsonIgnore
    public Mass convertToMetric(@Nonnull final EMassMetric targetMetric) {
        Preconditions.checkNotNull(targetMetric, "targetMetric shouldn't be null!");

        this.coordinate = getCoordinateInMetric(targetMetric);
        this.massMetric = targetMetric;
        return this;
    }

    @Nonnull
    @JsonIgnore
    public Mass convertToMetricWithScale(@Nonnull final EMassMetric targetMetric) {
        Preconditions.checkNotNull(targetMetric, "targetMetric shouldn't be null!");

        this.coordinate = getCoordinateInMetric(targetMetric).setScale(targetMetric.getScale(), RoundingMode.HALF_UP);
        this.massMetric = targetMetric;
        return this;
    }

    @Nonnull
    @JsonIgnore
    public Mass multiply(final int factor) {
        return multiply(BigDecimal.valueOf(factor));
    }

    @Nonnull
    @JsonIgnore
    public Mass multiply(@Nonnull final BigDecimal factor) {
        Preconditions.checkNotNull(factor, "factor must not be empty");

        final BigDecimal result = getCoordinate().multiply(factor, MC);
        return new Mass(result, massMetric);
    }

    @Nonnull
    @JsonIgnore
    public Mass divide(@Nonnull final BigDecimal dividend) {
        Preconditions.checkNotNull(dividend, "dividend must not be empty");

        final BigDecimal result = getCoordinate().divide(dividend, MC);
        return new Mass(result, massMetric);
    }

    @Nonnull
    @JsonIgnore
    public Mass getInMetricWithScale(@Nonnull final EMassMetric targetMetric) {
        Preconditions.checkNotNull(targetMetric, "targetMetric shouldn't be null!");

        final BigDecimal coordinate = getCoordinateInMetric(targetMetric)
                .setScale(targetMetric.getScale(), RoundingMode.HALF_UP);
        return new Mass(coordinate, targetMetric);
    }

    @Override
    @JsonIgnore
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (!(o instanceof Mass)) return false;

        final Mass distance = (Mass) o;

        final int compareTo = compareTo(distance);
        return new EqualsBuilder().append(compareTo, 0).isEquals();
    }

    @Override
    @JsonIgnore
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(coordinate).append(massMetric).toHashCode();
    }

    @Override
    @JsonIgnore
    public Mass clone() {
        try {
            final Mass clone = (Mass) super.clone();
            clone.coordinate = new BigDecimal(coordinate.toString());
            clone.massMetric = EMassMetric.getByName(massMetric.name());
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    /**
     * This is funny.<br>
     * Normally it's a simple comparison.<br>
     * But it has to deal with different length units and an inaccuracy by the given scales and conversions.<br>
     * So there will be a tolerance of 0.1 percent to deal with.
     *
     * @param that the object to be compared
     * @return if this is smaller (-1), equals (0) or bigger (1) than that
     */
    @Override
    @JsonIgnore
    public int compareTo(@Nonnull final Mass that) {
        Preconditions.checkNotNull(that, "that shouldn't be null!");

        if (getCoordinate().compareTo(BigDecimal.ZERO) == 0 || that.getCoordinate().compareTo(BigDecimal.ZERO) == 0) {
            // if one value is zero than just compare the values
            return getCoordinate().compareTo(that.getCoordinate());
        }

        final BigDecimal thisValue;
        final BigDecimal thatValue;
        if (getMassMetric() != that.getMassMetric()) {
            // convert values to the same scale if different
            final EMassMetric metric = EMassMetric.KG;
            thisValue = getInMetricWithScale(metric).getCoordinate();
            thatValue = that.getInMetricWithScale(metric).getCoordinate();
        } else {
            thisValue = getCoordinate();
            thatValue = that.getCoordinate();
        }
        return new OnePercentComparator().compare(thisValue, thatValue);
    }

    @Nonnull
    @JsonIgnore
    public Mass add(@Nonnull final Mass o) {
        Preconditions.checkNotNull(o, "o shouldn't be null!");

        final BigDecimal additional = o.getCoordinateInMetric(massMetric);
        return new Mass(coordinate.add(additional), massMetric);
    }

    @Nonnull
    @JsonIgnore
    public Mass subtract(@Nonnull final Mass o) {
        Preconditions.checkNotNull(o, "o shouldn't be null!");

        final BigDecimal subtrahend = o.getCoordinateInMetric(massMetric);
        return new Mass(coordinate.subtract(subtrahend), massMetric);
    }

    /**
     * Returns the lowest of both.
     *
     * @param o the one
     * @return the lowest
     */
    @Nonnull
    @JsonIgnore
    public Mass min(@Nonnull final Mass o) {
        Preconditions.checkNotNull(o, "o shouldn't be null!");

        return compareTo(o) < 0 ? this : o;
    }

    /**
     * Returns the bigger of both.
     *
     * @param o the one
     * @return the bigger
     */
    @Nonnull
    @JsonIgnore
    public Mass max(@Nonnull final Mass o) {
        Preconditions.checkNotNull(o, "o shouldn't be null!");

        return compareTo(o) > 0 ? this : o;
    }

    @Nonnull
    @JsonIgnore
    public static Mass valueOf(@Nonnull final String fromDb) {
        Preconditions.checkNotNull(fromDb, "fromDb shouldn't be null!");

        final String[] split = fromDb.trim().split("\\s");
        return new Mass(new BigDecimal(split[0]), EMassMetric.getByName(split[1]));
    }

    @JsonIgnore
    @Override
    public String toString() {
        return coordinate + " " + massMetric.name();
    }
}
