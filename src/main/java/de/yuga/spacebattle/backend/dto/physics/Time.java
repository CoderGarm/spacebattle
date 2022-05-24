package de.yuga.spacebattle.backend.dto.physics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.physics.ETimeMetric;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class Time implements Cloneable, Comparable<Time> {

    public static final Time ZERO = new Time(0, ETimeMetric.SECOND);

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The value of this distance.")
    private BigDecimal coordinate;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The metric of this time.")
    private ETimeMetric timeMetric;

    public Time() {
    }

    public Time(final int coordinate, @Nonnull final ETimeMetric timeMetric) {
        Preconditions.checkNotNull(timeMetric, "timeMetric shouldn't be null!");

        this.coordinate = BigDecimal.valueOf(coordinate);
        this.timeMetric = timeMetric;
    }

    public Time(final double coordinate, @Nonnull final ETimeMetric timeMetric) {
        Preconditions.checkNotNull(timeMetric, "timeMetric shouldn't be null!");

        this.coordinate = BigDecimal.valueOf(coordinate);
        this.timeMetric = timeMetric;
    }

    public Time(@Nonnull final BigDecimal coordinate, @Nonnull final ETimeMetric timeMetric) {
        Preconditions.checkNotNull(coordinate, "coordinate shouldn't be null!");
        Preconditions.checkNotNull(timeMetric, "timeMetric shouldn't be null!");

        this.coordinate = coordinate;
        this.timeMetric = timeMetric;
    }

    @Nonnull
    @JsonIgnore
    public BigDecimal getCoordinate() {
        return coordinate;
    }

    @Nonnull
    @JsonIgnore
    public ETimeMetric getTimeMetric() {
        return timeMetric;
    }

    @Nonnull
    @JsonIgnore
    public BigDecimal getCoordinateInMetric(@Nonnull final ETimeMetric targetMetric) {
        Preconditions.checkNotNull(targetMetric, "targetMetric shouldn't be null!");

        if (timeMetric == targetMetric) {
            return coordinate;
        }
        final BigDecimal factor = timeMetric.getConversionFactor(targetMetric);
        return coordinate.multiply(factor, new MathContext(targetMetric.getScale(), RoundingMode.HALF_UP));
    }

    @Nonnull
    @JsonIgnore
    public Time convertToMetric(@Nonnull final ETimeMetric targetMetric) {
        Preconditions.checkNotNull(targetMetric, "targetMetric shouldn't be null!");

        this.coordinate = getCoordinateInMetric(targetMetric);
        this.timeMetric = targetMetric;
        return this;
    }

    @Nonnull
    @JsonIgnore
    public Time convertToMetricWithScale(@Nonnull final ETimeMetric targetMetric) {
        Preconditions.checkNotNull(targetMetric, "targetMetric shouldn't be null!");

        this.coordinate = getCoordinateInMetric(targetMetric).setScale(targetMetric.getScale(), RoundingMode.HALF_UP);
        this.timeMetric = targetMetric;
        return this;
    }

    @Nonnull
    @JsonIgnore
    public Time multiply(final int factor) {
        final BigDecimal result = getCoordinate().multiply(BigDecimal.valueOf(factor));
        return new Time(result, timeMetric);
    }

    @Nonnull
    @JsonIgnore
    public Time getInMetricWithScale(@Nonnull final ETimeMetric targetMetric) {
        Preconditions.checkNotNull(targetMetric, "targetMetric shouldn't be null!");

        final BigDecimal coordinate = getCoordinateInMetric(targetMetric)
                .setScale(targetMetric.getScale(), RoundingMode.HALF_UP);
        return new Time(coordinate, targetMetric);
    }

    @Override
    @JsonIgnore
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (!(o instanceof Time)) return false;

        final Time distance = (Time) o;

        final int compareTo = compareTo(distance);
        return new EqualsBuilder().append(compareTo, 0).isEquals();
    }

    @Override
    @JsonIgnore
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(coordinate).append(timeMetric).toHashCode();
    }

    @Override
    @JsonIgnore
    public Time clone() {
        try {
            final Time clone = (Time) super.clone();
            clone.coordinate = new BigDecimal(coordinate.toString());
            clone.timeMetric = ETimeMetric.getByName(timeMetric.name());
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
    public int compareTo(@Nonnull final Time that) {
        Preconditions.checkNotNull(that, "that shouldn't be null!");

        if (getCoordinate().compareTo(BigDecimal.ZERO) == 0 || that.getCoordinate().compareTo(BigDecimal.ZERO) == 0) {
            // if one value is zero than just compare the values
            return getCoordinate().compareTo(that.getCoordinate());
        }

        final BigDecimal thisValue;
        final BigDecimal thatValue;
        if (getTimeMetric() != that.getTimeMetric()) {
            // convert values to the same scale if different
            final ETimeMetric metric = ETimeMetric.SECOND;
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
    public Time add(@Nonnull final Time o) {
        Preconditions.checkNotNull(o, "o shouldn't be null!");

        final BigDecimal additional = o.getCoordinateInMetric(timeMetric);
        return new Time(coordinate.add(additional), timeMetric);
    }

    @Nonnull
    @JsonIgnore
    public Time subtract(@Nonnull final Time o) {
        Preconditions.checkNotNull(o, "o shouldn't be null!");

        final BigDecimal subtrahend = o.getCoordinateInMetric(timeMetric);
        return new Time(coordinate.subtract(subtrahend), timeMetric);
    }

    /**
     * Returns the lowest of both.
     *
     * @param o the one
     * @return the lowest
     */
    @Nonnull
    @JsonIgnore
    public Time min(@Nonnull final Time o) {
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
    public Time max(@Nonnull final Time o) {
        Preconditions.checkNotNull(o, "o shouldn't be null!");

        return compareTo(o) > 0 ? this : o;
    }
}
