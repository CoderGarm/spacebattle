package de.yuga.spacebattle.backend.dto.physics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.physics.EAccelerationMetric;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import de.yuga.spacebattle.backend.enums.physics.EHyperBand;
import de.yuga.spacebattle.backend.enums.physics.ETimeMetric;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

@Schema(description = ".")
public class Acceleration implements Cloneable, Comparable<Acceleration> {

    public static final Acceleration ZERO = new Acceleration(0, EAccelerationMetric.MS2);

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The value of this acceleration.")
    private BigDecimal value;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The metric of this acceleration.")
    private EAccelerationMetric accelerationMetric;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The hyper band which can be reached.")
    private EHyperBand hyperBand;

    public Acceleration() {
    }

    public Acceleration(final int value, @Nonnull final EAccelerationMetric accelerationMetric) {
        Preconditions.checkNotNull(accelerationMetric, "lengthDefinition shouldn't be null!");

        this.value = BigDecimal.valueOf(value);
        this.accelerationMetric = accelerationMetric;
        this.hyperBand = EHyperBand.NONE;
    }

    public Acceleration(@Nonnull final BigDecimal value,
                        @Nonnull final EAccelerationMetric accelerationMetric,
                        @Nonnull final EHyperBand hyperBand) {
        Preconditions.checkNotNull(value, "coordinate shouldn't be null!");
        Preconditions.checkNotNull(accelerationMetric, "lengthDefinition shouldn't be null!");
        Preconditions.checkNotNull(hyperBand, "hyperBand shouldn't be null!");

        this.value = value;
        this.accelerationMetric = accelerationMetric;
        this.hyperBand = hyperBand;
    }

    public Acceleration(final double value, @Nonnull final EAccelerationMetric accelerationMetric) {
        Preconditions.checkNotNull(accelerationMetric, "lengthDefinition shouldn't be null!");

        this.value = BigDecimal.valueOf(value);
        this.accelerationMetric = accelerationMetric;
        this.hyperBand = EHyperBand.NONE;
    }

    @Nonnull
    @JsonIgnore
    public static Acceleration getFromString(@Nonnull final String fromDb) {
        Preconditions.checkNotNull(fromDb, "fromDb shouldn't be null!");

        final String[] split = fromDb.trim().split("\\s");
        return new Acceleration(new BigDecimal(split[0]), EAccelerationMetric.getByName(split[1]), EHyperBand.getByName(split[2]));
    }

    @Nonnull
    @JsonIgnore
    public EHyperBand getHyperBand() {
        return hyperBand;
    }

    @Nonnull
    @JsonIgnore
    public BigDecimal getValue() {
        return value;
    }

    @Nonnull
    @JsonIgnore
    public BigDecimal getCoordinateInMetric(@Nonnull final EAccelerationMetric targetMetric) {
        Preconditions.checkNotNull(targetMetric, "targetMetric shouldn't be null!");

        return convertToMetric(targetMetric);
    }

    @Nonnull
    @JsonIgnore
    public EAccelerationMetric getAccelerationMetric() {
        return accelerationMetric;
    }

    @Nonnull
    @JsonIgnore
    public String asString() {
        return value + " " + accelerationMetric + " " + hyperBand;
    }

    @Override
    public String toString() {
        return asString();
    }

    @Nonnull
    @JsonIgnore
    public BigDecimal convertToMetric(@Nonnull final EAccelerationMetric targetMetric) {
        Preconditions.checkNotNull(targetMetric, "targetMetric shouldn't be null!");

        if (accelerationMetric == targetMetric) {
            return value;
        }
        final BigDecimal factor = accelerationMetric.getConversionFactor(targetMetric);
        return value.multiply(factor);
    }

    /**
     * Calculates the distance which is laid back at the given acceleration by time and the starting velocity.
     *
     * @param duration     the acceleration endurance
     * @param targetMetric the scale for the result
     * @return the laid back distance
     */
    @Nonnull
    @JsonIgnore
    public Distance getDistanceByTime(@Nonnull final Time duration,
                                      @Nonnull final Velocity velocity,
                                      @Nonnull final EDistanceMetric targetMetric) {
        Preconditions.checkNotNull(targetMetric, "targetMetric shouldn't be null!");
        Preconditions.checkNotNull(velocity, "velocity shouldn't be null!");

        // s = 0,5 · a · t² + v · t
        final BigDecimal time = duration.getCoordinateInMetric(ETimeMetric.SECOND);
        final BigDecimal distanceFromAccelerationValue = BigDecimal.valueOf(0.5)
                .multiply(getCoordinateInMetric(EAccelerationMetric.MS2))
                .multiply(time.pow(2));
        final BigDecimal distanceFromVelocity = velocity.getCoordinateInMetric(EDistanceMetric.M, ETimeMetric.SECOND).multiply(time);
        final BigDecimal sum = distanceFromAccelerationValue.add(distanceFromVelocity);
        return new Distance(sum, EDistanceMetric.M).convertToMetric(targetMetric);
    }

    @Nonnull
    @JsonIgnore
    public Acceleration negate() {
        return new Acceleration(value.negate(), accelerationMetric, hyperBand);
    }

    @Override
    @JsonIgnore
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (!(o instanceof Acceleration)) return false;

        final Acceleration distance = (Acceleration) o;
        final int compareTo = compareTo(distance);
        return new EqualsBuilder().append(compareTo, 0).isEquals();
    }

    @Override
    @JsonIgnore
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(value).append(accelerationMetric).toHashCode();
    }

    @Override
    @JsonIgnore
    public Acceleration clone() {
        try {
            return (Acceleration) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    @JsonIgnore
    public int compareTo(@Nonnull final Acceleration o) {
        Preconditions.checkNotNull(o, "o shouldn't be null!");

        return convertToMetric(EAccelerationMetric.MS2).compareTo(o.convertToMetric(EAccelerationMetric.MS2));
    }
}
