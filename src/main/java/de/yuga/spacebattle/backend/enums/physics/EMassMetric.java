package de.yuga.spacebattle.backend.enums.physics;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;

public enum EMassMetric {

    KG(BigDecimal.ONE, 1, 0, "kg"),
    T(new BigDecimal("1000"), 4, 4, "t"),
    KT(new BigDecimal("1000000"), 7, 7, "kt"),
    MT(new BigDecimal("1000000000"), 10, 10, "Mt");

    /**
     * The value which is needed to divide the original value to get the result in the basic unit {@link EMassMetric#SECOND}.
     */
    final BigDecimal kilogramEquivalent;

    /**
     * The amount of digits which is needed to fill a full length unit.
     */
    final int digitCount;

    /**
     * The amount of decimal places which represents 1 kg.
     */
    final int scale;

    @Nonnull
    final String unit;

    EMassMetric(final BigDecimal kilogramEquivalent, final int digitCount, final int scale, @Nonnull final String unit) {
        Preconditions.checkNotNull(unit, "unit shouldn't be null!");

        this.kilogramEquivalent = kilogramEquivalent;
        this.digitCount = digitCount;
        this.scale = scale;
        this.unit = unit;
    }

    public BigDecimal getKilogramEquivalent() {
        return kilogramEquivalent;
    }

    @Nonnull
    public String getUnit() {
        return unit;
    }

    public int getDigitCount() {
        return digitCount;
    }

    public int getScale() {
        return scale;
    }

    @Nonnull
    public static EMassMetric getByName(@Nonnull final String metric) {
        Preconditions.checkNotNull(metric, "metric shouldn't be null!");

        return Arrays.stream(EMassMetric.values()).filter(l -> l.name().equals(metric))
                .findFirst()
                .orElseThrow(() -> new NotifyWebUserException("There was no match for a length definition possible by searching for '" + metric + "'."));
    }

    /**
     * Returns the conversion factor from the length metric to the current metric.
     *
     * @param targetMetric the metric from which should converted
     * @return the factor to convert "from" to the current
     */
    public BigDecimal getConversionFactor(@Nonnull final EMassMetric targetMetric) {
        Preconditions.checkNotNull(targetMetric, "targetMetric shouldn't be null!");

        final BigDecimal targetMetricSecondEquivalent = targetMetric.getKilogramEquivalent();
        final int targetMetricScale = targetMetric.getScale();
        return kilogramEquivalent.divide(targetMetricSecondEquivalent, new MathContext(targetMetricScale, RoundingMode.HALF_UP));
    }
}
