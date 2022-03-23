package de.yuga.spacebattle.backend.enums.physics;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;

public enum ETimeMetric {

    SECOND(BigDecimal.ONE, 1, 0, "s"),
    MINUTE(new BigDecimal("60"), 2, 1, "m"),
    HOUR(new BigDecimal("3600"), 4, 3, "h"),
    DAY(new BigDecimal("86400"), 5, 5, "d"),
    WEEK(new BigDecimal("604800"), 6, 6, "w"),
    MONTH(new BigDecimal("2419200"), 7, 7, "m"),
    YEAR(new BigDecimal("29030400"), 8, 8, "y");

    /**
     * The value which is needed to divide the original value to get the result in the basic unit {@link ETimeMetric#SECOND}.
     */
    final BigDecimal secondEquivalent;

    /**
     * The amount of digits which is needed to fill a full length unit.
     */
    final int digitCount;

    /**
     * The amount of decimal places which represents 100 000 km.
     */
    final int scale;

    @Nonnull
    final String unit;

    ETimeMetric(final BigDecimal secondEquivalent, final int digitCount, final int scale, @Nonnull final String unit) {
        Preconditions.checkNotNull(unit, "unit shouldn't be null!");

        this.secondEquivalent = secondEquivalent;
        this.digitCount = digitCount;
        this.scale = scale;
        this.unit = unit;
    }

    public BigDecimal getSecondEquivalent() {
        return secondEquivalent;
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
    public static ETimeMetric getByName(@Nonnull final String metric) {
        Preconditions.checkNotNull(metric, "metric shouldn't be null!");

        return Arrays.stream(ETimeMetric.values()).filter(l -> l.name().equals(metric))
                .findFirst()
                .orElseThrow(() -> new NotifyWebUserException("There was no match for a length definition possible by searching for '" + metric + "'."));
    }

    /**
     * Returns the conversion factor from the length metric to the current metric.
     *
     * @param targetMetric the metric from which should converted
     * @return the factor to convert "from" to the current
     */
    public BigDecimal getConversionFactor(@Nonnull final ETimeMetric targetMetric) {
        Preconditions.checkNotNull(targetMetric, "targetMetric shouldn't be null!");

        final BigDecimal targetMetricSecondEquivalent = targetMetric.getSecondEquivalent();
        final int targetMetricScale = targetMetric.getScale();
        return secondEquivalent.divide(targetMetricSecondEquivalent, new MathContext(targetMetricScale, RoundingMode.HALF_UP));
    }
}
