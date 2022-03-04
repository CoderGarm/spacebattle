package de.yuga.spacebattle.backend.dto.physics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EAccelerationMetric;
import de.yuga.spacebattle.backend.enums.EHyperBand;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

public class Acceleration implements Cloneable, Comparable<Acceleration> {

    public static final Acceleration ZERO = new Acceleration(0, EAccelerationMetric.MS2);

    @Nonnull
    @JsonProperty
    @ApiModelProperty(required = true, value = "The value of this acceleration.")
    private final BigDecimal accelerationValue;

    @Nonnull
    @JsonProperty
    @ApiModelProperty(required = true, value = "The metric of this acceleration.")
    private final EAccelerationMetric accelerationMetric;

    @Nonnull
    @JsonProperty
    @ApiModelProperty(required = true, value = "The hyper band which can be reached.")
    private final EHyperBand hyperBand;

    public Acceleration(final int accelerationValue, @Nonnull final EAccelerationMetric accelerationMetric) {
        Preconditions.checkNotNull(accelerationMetric, "lengthDefinition shouldn't be null!");

        this.accelerationValue = BigDecimal.valueOf(accelerationValue);
        this.accelerationMetric = accelerationMetric;
        this.hyperBand = EHyperBand.NONE;
    }

    public Acceleration(@Nonnull final BigDecimal accelerationValue,
                        @Nonnull final EAccelerationMetric accelerationMetric,
                        @Nonnull final EHyperBand hyperBand) {
        Preconditions.checkNotNull(accelerationValue, "coordinate shouldn't be null!");
        Preconditions.checkNotNull(accelerationMetric, "lengthDefinition shouldn't be null!");
        Preconditions.checkNotNull(hyperBand, "hyperBand shouldn't be null!");

        this.accelerationValue = accelerationValue;
        this.accelerationMetric = accelerationMetric;
        this.hyperBand = hyperBand;
    }

    public Acceleration(final double accelerationValue, @Nonnull final EAccelerationMetric accelerationMetric) {
        Preconditions.checkNotNull(accelerationMetric, "lengthDefinition shouldn't be null!");

        this.accelerationValue = BigDecimal.valueOf(accelerationValue);
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
    public BigDecimal getAccelerationValue() {
        return accelerationValue;
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
        return accelerationValue + " " + accelerationMetric + " " + hyperBand;
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
            return accelerationValue;
        }
        final BigDecimal factor = accelerationMetric.getConversionFactor(targetMetric);
        return accelerationValue.multiply(factor);
    }

    @Override
    @JsonIgnore
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (!(o instanceof Acceleration)) return false;

        final Acceleration distance = (Acceleration) o;

        return new EqualsBuilder().append(accelerationValue, distance.accelerationValue).append(accelerationMetric, distance.accelerationMetric).isEquals();
    }

    @Override
    @JsonIgnore
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(accelerationValue).append(accelerationMetric).toHashCode();
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
