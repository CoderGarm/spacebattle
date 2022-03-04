package de.yuga.spacebattle.backend.dto.physics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator;
import de.yuga.spacebattle.backend.enums.EDistanceMetric;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Distance implements Cloneable, Comparable<Distance> {

    public static final Distance ZERO = new Distance(0, EDistanceMetric.LS);

    @Nonnull
    @JsonProperty
    @ApiModelProperty(required = true, value = "The value of this distance.")
    private BigDecimal coordinate;

    @Nonnull
    @JsonProperty
    @ApiModelProperty(required = true, value = "The metric of this distance.")
    private EDistanceMetric distanceMetric;

    public Distance() {
    }

    public Distance(final int coordinate, @Nonnull final EDistanceMetric distanceMetric) {
        Preconditions.checkNotNull(distanceMetric, "lengthDefinition shouldn't be null!");

        this.coordinate = BigDecimal.valueOf(coordinate);
        this.distanceMetric = distanceMetric;
    }

    public Distance(final double coordinate, @Nonnull final EDistanceMetric distanceMetric) {
        Preconditions.checkNotNull(distanceMetric, "lengthDefinition shouldn't be null!");

        this.coordinate = BigDecimal.valueOf(coordinate);
        this.distanceMetric = distanceMetric;
    }

    public Distance(@Nonnull final BigDecimal coordinate, @Nonnull final EDistanceMetric distanceMetric) {
        Preconditions.checkNotNull(coordinate, "coordinate shouldn't be null!");
        Preconditions.checkNotNull(distanceMetric, "lengthDefinition shouldn't be null!");

        this.coordinate = coordinate;
        this.distanceMetric = distanceMetric;
    }

    @Nonnull
    @JsonIgnore
    public BigDecimal getCoordinate() {
        return coordinate;
    }

    @Nonnull
    @JsonIgnore
    public EDistanceMetric getDistanceMetric() {
        return distanceMetric;
    }

    @Nonnull
    @JsonIgnore
    public static Distance valueOf(@Nonnull final String fromDb) {
        Preconditions.checkNotNull(fromDb, "fromDb shouldn't be null!");

        final String[] split = fromDb.trim().split("\\s");
        return new Distance(new BigDecimal(split[0]), EDistanceMetric.getByName(split[1]));
    }

    @JsonIgnore
    @Override
    public String toString() {
        final Distance distance = DistanceCalculator.convertToScale(this);
        return distance.coordinate + " " + distance.getDistanceMetric();
    }

    @Nonnull
    @JsonIgnore
    public BigDecimal getCoordinateInMetric(@Nonnull final EDistanceMetric targetMetric) {
        Preconditions.checkNotNull(targetMetric, "targetMetric shouldn't be null!");

        if (distanceMetric == targetMetric) {
            return coordinate;
        }
        final BigDecimal factor = distanceMetric.getConversionFactor(targetMetric);
        return coordinate.multiply(factor, DistanceCalculator.MATH_CONTEXT_MORE_PRECISION);
    }

    @JsonIgnore
    public Distance convertToMetric(@Nonnull final EDistanceMetric targetMetric) {
        Preconditions.checkNotNull(targetMetric, "targetMetric shouldn't be null!");

        this.coordinate = getCoordinateInMetric(targetMetric);
        this.distanceMetric = targetMetric;
        return this;
    }

    @JsonIgnore
    public Distance convertToMetricWithScale(@Nonnull final EDistanceMetric targetMetric) {
        Preconditions.checkNotNull(targetMetric, "targetMetric shouldn't be null!");

        this.coordinate = getCoordinateInMetric(targetMetric).setScale(targetMetric.getScale(), RoundingMode.HALF_UP);
        this.distanceMetric = targetMetric;
        return this;
    }

    @JsonIgnore
    public Distance getInMetricWithScale(@Nonnull final EDistanceMetric targetMetric) {
        Preconditions.checkNotNull(targetMetric, "targetMetric shouldn't be null!");

        final BigDecimal coordinate = getCoordinateInMetric(targetMetric).setScale(targetMetric.getScale(), RoundingMode.HALF_UP);
        return new Distance(coordinate, targetMetric);
    }

    @Override
    @JsonIgnore
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (!(o instanceof Distance)) return false;

        final Distance distance = (Distance) o;

        final int compareTo = compareTo(distance);
        return new EqualsBuilder().append(compareTo, 0).isEquals();
    }

    @Override
    @JsonIgnore
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(coordinate).append(distanceMetric).toHashCode();
    }

    @Override
    @JsonIgnore
    public Distance clone() {
        try {
            final Distance clone = (Distance) super.clone();
            clone.coordinate = new BigDecimal(coordinate.toString());
            clone.distanceMetric = EDistanceMetric.getByName(distanceMetric.name());
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    @JsonIgnore
    public int compareTo(@Nonnull final Distance that) {
        Preconditions.checkNotNull(that, "that shouldn't be null!");

        final Distance thisDistance = getInMetricWithScale(distanceMetric);
        final Distance thatDistance = that.getInMetricWithScale(distanceMetric);

        final Integer thisRelevantScale = getRelevantDecimalPlaces(thisDistance.getCoordinate());
        final Integer thatRelevantScale = getRelevantDecimalPlaces(thatDistance.getCoordinate());
        Integer relevantScale = null;
        if (thisRelevantScale != null && thatRelevantScale != null) {
            relevantScale = Math.max(thisRelevantScale, thatRelevantScale);
        } else if (thisRelevantScale != null) {
            relevantScale = thisRelevantScale;
        } else if (thatRelevantScale != null) {
            relevantScale = thatRelevantScale;
        }

        if (relevantScale != null) {
            final BigDecimal thisC = thisDistance.getCoordinate().setScale(relevantScale, RoundingMode.HALF_UP);
            final BigDecimal thatC = thatDistance.getCoordinate().setScale(relevantScale, RoundingMode.HALF_UP);
            return thisC.compareTo(thatC);
        }
        return thisDistance.getCoordinate().compareTo(thatDistance.getCoordinate());
    }

    /**
     * Returns the index of the first zero after the decimal point.
     *
     * @param value the value to check
     * @return the index of the first zero after the decimal point - or null if all places are relevant
     */
    @Nullable
    private Integer getRelevantDecimalPlaces(@Nonnull final BigDecimal value) {
        Integer relevantScale = null;
        final String thisToString = value.toString();
        if (thisToString.contains(".")) {
            final String[] thisCoord = thisToString.split("\\.");
            final String decimalPlaces = thisCoord[1];
            if (decimalPlaces.contains("0")) {
                relevantScale = decimalPlaces.indexOf("0");
            }
        }
        return relevantScale;
    }

    @Nonnull
    @JsonIgnore
    public Distance add(@Nonnull final Distance o) {
        Preconditions.checkNotNull(o, "o shouldn't be null!");

        final BigDecimal additional = o.getCoordinateInMetric(distanceMetric);
        return new Distance(coordinate.add(additional), distanceMetric);
    }

    @Nonnull
    @JsonIgnore
    public Distance subtract(@Nonnull final Distance o) {
        Preconditions.checkNotNull(o, "o shouldn't be null!");

        final BigDecimal subtrahend = o.getCoordinateInMetric(distanceMetric);
        return new Distance(coordinate.subtract(subtrahend), distanceMetric);
    }

    /**
     * Returns the lowest of both.
     *
     * @param o the one
     * @return the lowest
     */
    @Nonnull
    @JsonIgnore
    public Distance min(@Nonnull final Distance o) {
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
    public Distance max(@Nonnull final Distance o) {
        Preconditions.checkNotNull(o, "o shouldn't be null!");

        return compareTo(o) > 0 ? this : o;
    }
}
