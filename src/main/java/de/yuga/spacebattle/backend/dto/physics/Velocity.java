package de.yuga.spacebattle.backend.dto.physics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.physics.EAccelerationMetric;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import de.yuga.spacebattle.backend.enums.physics.ETimeMetric;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class Velocity implements Cloneable, Comparable<Velocity> {

    public static final Velocity ZERO = new Velocity(0, EDistanceMetric.M, ETimeMetric.SECOND);

    /**
     * The speed of light.
     */
    public static final Velocity SOL = new Velocity(EDistanceMetric.LS.getMeterEquivalent(), EDistanceMetric.M, ETimeMetric.SECOND);

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The value of this velocity.")
    private BigDecimal value;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The metric of the distance unit of this velocity.")
    private EDistanceMetric distanceMetric;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The metric of the time unit of this velocity.")
    private ETimeMetric timeMetric;

    public Velocity() {
    }

    public Velocity(final int value,
                    @Nonnull final EDistanceMetric distanceMetric,
                    @Nonnull final ETimeMetric timeMetric) {
        Preconditions.checkNotNull(distanceMetric, "distanceMetric shouldn't be null!");
        Preconditions.checkNotNull(timeMetric, "timeMetric shouldn't be null!");

        this.value = BigDecimal.valueOf(value);
        this.distanceMetric = distanceMetric;
        this.timeMetric = timeMetric;
    }

    public Velocity(final double value,
                    @Nonnull final EDistanceMetric distanceMetric,
                    @Nonnull final ETimeMetric timeMetric) {
        Preconditions.checkNotNull(distanceMetric, "distanceMetric shouldn't be null!");
        Preconditions.checkNotNull(timeMetric, "timeMetric shouldn't be null!");

        this.value = BigDecimal.valueOf(value);
        this.distanceMetric = distanceMetric;
        this.timeMetric = timeMetric;
    }

    public Velocity(@Nonnull final BigDecimal value,
                    @Nonnull final EDistanceMetric distanceMetric,
                    @Nonnull final ETimeMetric timeMetric) {
        Preconditions.checkNotNull(value, "coordinate shouldn't be null!");
        Preconditions.checkNotNull(distanceMetric, "distanceMetric shouldn't be null!");
        Preconditions.checkNotNull(timeMetric, "timeMetric shouldn't be null!");

        this.value = value;
        this.distanceMetric = distanceMetric;
        this.timeMetric = timeMetric;
    }

    @Nonnull
    @JsonIgnore
    public BigDecimal getValue() {
        return value;
    }

    @Nonnull
    @JsonIgnore
    public EDistanceMetric getDistanceMetric() {
        return distanceMetric;
    }

    @Nonnull
    @JsonIgnore
    public ETimeMetric getTimeMetric() {
        return timeMetric;
    }

    @Nonnull
    @JsonIgnore
    public BigDecimal getCoordinateInMetric(@Nonnull final EDistanceMetric targetDistanceMetric,
                                            @Nonnull final ETimeMetric targetTimeMetric) {
        Preconditions.checkNotNull(targetDistanceMetric, "targetDistanceMetric shouldn't be null!");
        Preconditions.checkNotNull(targetTimeMetric, "targetTimeMetric shouldn't be null!");

        if (distanceMetric == targetDistanceMetric && timeMetric == targetTimeMetric) {
            return value;
        }
        final int scale = value.scale();
        final BigDecimal distanceMetricConversionFactor = distanceMetric.getConversionFactor(targetDistanceMetric);
        final BigDecimal timeMetricConversionFactor = timeMetric.getConversionFactor(targetTimeMetric);
        return value.multiply(distanceMetricConversionFactor)
                .multiply(timeMetricConversionFactor, new MathContext(scale, RoundingMode.HALF_UP));
    }

    @Nonnull
    @JsonIgnore
    public Velocity convertToMetric(@Nonnull final EDistanceMetric targetDistanceMetric,
                                    @Nonnull final ETimeMetric targetTimeMetric) {
        Preconditions.checkNotNull(targetDistanceMetric, "targetDistanceMetric shouldn't be null!");
        Preconditions.checkNotNull(targetTimeMetric, "targetTimeMetric shouldn't be null!");

        this.value = getCoordinateInMetric(targetDistanceMetric, targetTimeMetric);
        this.distanceMetric = targetDistanceMetric;
        this.timeMetric = targetTimeMetric;
        return this;
    }

    @Nonnull
    @JsonIgnore
    public Velocity getInMetricWithScale(@Nonnull final EDistanceMetric targetDistanceMetric,
                                         @Nonnull final ETimeMetric targetTimeMetric) {
        Preconditions.checkNotNull(targetDistanceMetric, "targetDistanceMetric shouldn't be null!");
        Preconditions.checkNotNull(targetTimeMetric, "targetTimeMetric shouldn't be null!");

        final BigDecimal coordinate = getCoordinateInMetric(targetDistanceMetric, targetTimeMetric);
        return new Velocity(coordinate, targetDistanceMetric, targetTimeMetric);
    }


    @Nonnull
    @Override
    @JsonIgnore
    public String toString() {
        return value + " " + distanceMetric + "/" + timeMetric;
    }

    @Override
    @JsonIgnore
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (!(o instanceof Velocity)) return false;

        final Velocity distance = (Velocity) o;

        final int compareTo = compareTo(distance);
        return new EqualsBuilder().append(compareTo, 0).isEquals();
    }

    @Override
    @JsonIgnore
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(value).append(distanceMetric).toHashCode();
    }

    @Override
    @JsonIgnore
    public Velocity clone() {
        try {
            final Velocity clone = (Velocity) super.clone();
            clone.value = new BigDecimal(value.toString());
            clone.distanceMetric = EDistanceMetric.getByName(distanceMetric.name());
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
    public int compareTo(@Nonnull final Velocity that) {
        Preconditions.checkNotNull(that, "that shouldn't be null!");

        if (getValue().compareTo(BigDecimal.ZERO) == 0 || that.getValue().compareTo(BigDecimal.ZERO) == 0) {
            // if one value is zero than just compare the values
            return getValue().compareTo(that.getValue());
        }

        final BigDecimal thisValue;
        final BigDecimal thatValue;
        if (getDistanceMetric() != that.getDistanceMetric()) {
            // convert values to the same scale if different
            final EDistanceMetric distanceMetric = EDistanceMetric.M;
            final ETimeMetric timeMetric = ETimeMetric.SECOND;
            thisValue = getInMetricWithScale(distanceMetric, timeMetric).getValue();
            thatValue = that.getInMetricWithScale(distanceMetric, timeMetric).getValue();
        } else {
            thisValue = getValue();
            thatValue = that.getValue();
        }
        return new OnePercentComparator().compare(thisValue, thatValue);
    }

    @Nonnull
    @JsonIgnore
    public Velocity add(@Nonnull final Velocity o) {
        Preconditions.checkNotNull(o, "o shouldn't be null!");

        final BigDecimal additional = o.getCoordinateInMetric(distanceMetric, timeMetric);
        return new Velocity(value.add(additional), distanceMetric, timeMetric);
    }

    @Nonnull
    @JsonIgnore
    public Velocity subtract(@Nonnull final Velocity o) {
        Preconditions.checkNotNull(o, "o shouldn't be null!");

        final BigDecimal subtrahend = o.getCoordinateInMetric(distanceMetric, timeMetric);
        return new Velocity(value.subtract(subtrahend), distanceMetric, timeMetric);
    }

    /**
     * Returns the lowest of both.
     *
     * @param o the one
     * @return the lowest
     */
    @Nonnull
    @JsonIgnore
    public Velocity min(@Nonnull final Velocity o) {
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
    public Velocity max(@Nonnull final Velocity o) {
        Preconditions.checkNotNull(o, "o shouldn't be null!");

        return compareTo(o) > 0 ? this : o;
    }

    /**
     * Calculates the resulting velocity by this and the given parameters.
     *
     * @param acceleration the acceleration
     * @param duration     the duration
     * @return the resulting velocity
     */
    @Nonnull
    @JsonIgnore
    public Velocity getVelocityByAcceleration(@Nonnull final Acceleration acceleration, @Nonnull final Time duration) {
        Preconditions.checkNotNull(acceleration, "acceleration shouldn't be null!");
        Preconditions.checkNotNull(duration, "duration shouldn't be null!");

        // v = a · t + v0
        final BigDecimal velocityFromAcceleration = acceleration.getCoordinateInMetric(EAccelerationMetric.MS2).multiply(duration.getCoordinateInMetric(ETimeMetric.SECOND));
        return new Velocity(velocityFromAcceleration, distanceMetric, timeMetric).add(this);
    }

    @Nonnull
    @JsonIgnore
    public Velocity multiply(@Nonnull final BigDecimal multiplier) {
        Preconditions.checkNotNull(multiplier, "multiplier shouldn't be null!");

        return new Velocity(value.multiply(multiplier), distanceMetric, timeMetric);
    }

    /**
     * Returns the resulting velocity by the given alignment factor.
     *
     * @param alignmentFactor the alignment factor which represents the angle between two courses
     * @return the result, can be negative in case of an opposite direction
     */
    @Nonnull
    @JsonIgnore
    public Velocity getByAlignmentFactor(@Nonnull final BigDecimal alignmentFactor) {
        Preconditions.checkNotNull(alignmentFactor, "alignmentFactor shouldn't be null!");

        return this.multiply(alignmentFactor);
    }
}
