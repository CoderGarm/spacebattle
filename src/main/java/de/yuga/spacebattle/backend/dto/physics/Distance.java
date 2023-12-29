package de.yuga.spacebattle.backend.dto.physics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import static de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator.MC_HU;

@Schema(description = ".")
public class Distance implements Cloneable, Comparable<Distance> {

    public static final Distance ZERO = new Distance(0, EDistanceMetric.LS);

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The value of this distance.")
    private BigDecimal coordinate;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The metric of this distance.")
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
        return distance.coordinate.stripTrailingZeros() + " " + distance.getDistanceMetric();
    }

    @Nonnull
    @JsonIgnore
    public BigDecimal getCoordinateInMetric(@Nonnull final EDistanceMetric targetMetric) {
        Preconditions.checkNotNull(targetMetric, "targetMetric shouldn't be null!");

        if (distanceMetric == targetMetric) {
            return coordinate;
        }
        final BigDecimal factor = distanceMetric.getConversionFactor(targetMetric);
        return coordinate.multiply(factor, new MathContext(targetMetric.getScale(), RoundingMode.HALF_UP));
    }

    @Nonnull
    @JsonIgnore
    public Distance convertToMetric(@Nonnull final EDistanceMetric targetMetric) {
        Preconditions.checkNotNull(targetMetric, "targetMetric shouldn't be null!");

        this.coordinate = getCoordinateInMetric(targetMetric);
        this.distanceMetric = targetMetric;
        return this;
    }

    @Nonnull
    @JsonIgnore
    public Distance convertToMetricWithScale(@Nonnull final EDistanceMetric targetMetric) {
        Preconditions.checkNotNull(targetMetric, "targetMetric shouldn't be null!");

        this.coordinate = getCoordinateInMetric(targetMetric).setScale(targetMetric.getScale(), RoundingMode.HALF_UP);
        this.distanceMetric = targetMetric;
        return this;
    }

    @Nonnull
    @JsonIgnore
    public Distance getInMetricWithScale(@Nonnull final EDistanceMetric targetMetric) {
        Preconditions.checkNotNull(targetMetric, "targetMetric shouldn't be null!");

        final BigDecimal coordinate = getCoordinateInMetric(targetMetric)
                .setScale(targetMetric.getScale(), RoundingMode.HALF_UP);
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
    public int compareTo(@Nonnull final Distance that) {
        Preconditions.checkNotNull(that, "that shouldn't be null!");

        if (getCoordinate().compareTo(BigDecimal.ZERO) == 0 || that.getCoordinate().compareTo(BigDecimal.ZERO) == 0) {
            // if one value is zero than just compare the values
            return getCoordinate().compareTo(that.getCoordinate());
        }

        final BigDecimal thisValue;
        final BigDecimal thatValue;
        if (getDistanceMetric() != that.getDistanceMetric()) {
            // convert values to the same scale if different
            final EDistanceMetric metric = EDistanceMetric.M;
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

    @Nonnull
    @JsonIgnore
    public Distance multiply(@Nonnull final Distance o) {
        Preconditions.checkNotNull(o, "o shouldn't be null!");

        final BigDecimal factor = o.getCoordinateInMetric(distanceMetric);
        return new Distance(coordinate.multiply(factor), distanceMetric);
    }

    @Nonnull
    public Distance multiply(final int multiplier) {
        return multiply(new Distance(multiplier, distanceMetric));
    }

    @Nonnull
    public Distance multiply(final double multiplier) {
        return multiply(new Distance(multiplier, distanceMetric));
    }

    @Nonnull
    @JsonIgnore
    public Distance pow(final int o) {
        return new Distance(coordinate.pow(o), distanceMetric);
    }

    @Nonnull
    @JsonIgnore
    public Distance sqrt() {
        return new Distance(coordinate.sqrt(MC_HU), distanceMetric);
    }

    @Nonnull
    @JsonIgnore
    public Distance divide(@Nonnull final Distance o) {
        Preconditions.checkNotNull(o, "o shouldn't be null!");

        final BigDecimal factor = o.getCoordinateInMetric(distanceMetric);
        return new Distance(coordinate.divide(factor, MC_HU), distanceMetric);
    }

    @Nonnull
    @JsonIgnore
    public Distance negate() {
        return new Distance(coordinate.negate(), distanceMetric);
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
