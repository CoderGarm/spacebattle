package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.NotifyUserException;
import de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Arrays;

public enum EAccelerationMetric {
    MS2(BigDecimal.ONE, "m/s²"),
    G(new BigDecimal("9.81"), "g"),
    C(new BigDecimal("299792458"), "c");

    /**
     * The value which is needed to divide the original value to get the result in the basic unit {@link EAccelerationMetric#MS2}.
     */
    final BigDecimal meterEquivalent;

    @Nonnull
    final String unit;

    EAccelerationMetric(final BigDecimal meterEquivalent, @Nonnull final String unit) {
        Preconditions.checkNotNull(unit, "unit shouldn't be null!");

        this.meterEquivalent = meterEquivalent;
        this.unit = unit;
    }

    public BigDecimal getMeterEquivalent() {
        return meterEquivalent;
    }

    @Nonnull
    public String getUnit() {
        return unit;
    }

    @Nonnull
    public static EAccelerationMetric getByName(@Nonnull final String metric) {
        Preconditions.checkNotNull(metric, "metric shouldn't be null!");

        return Arrays.stream(EAccelerationMetric.values()).filter(l -> l.name().equals(metric))
                .findFirst()
                .orElseThrow(() -> new NotifyUserException("There was no match for a acceleration definition possible by searching for '" + metric + "'."));
    }

    /**
     * Returns the conversion factor from the length metric to the current metric.
     *
     * @param targetMetric the metric from which should converted
     * @return the factor to convert "from" to the current
     */
    public BigDecimal getConversionFactor(@Nonnull final EAccelerationMetric targetMetric) {
        Preconditions.checkNotNull(targetMetric, "targetMetric shouldn't be null!");

        return meterEquivalent.divide(targetMetric.getMeterEquivalent(), DistanceCalculator.MATH_CONTEXT_REALISTIC_PRECISION);
    }
}
