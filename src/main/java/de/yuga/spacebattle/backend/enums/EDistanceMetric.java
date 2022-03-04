package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.NotifyUserException;
import de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

public enum EDistanceMetric {

    M(BigDecimal.ONE, 1, 0, "m"),
    KM(new BigDecimal("1000"), 4, 0, "km"),
    LS(new BigDecimal("299792458"), 9, 3, "ls"),
    LM(new BigDecimal("17987547480"), 11, 5, "lm"),
    AU(new BigDecimal("149597870700"), 12, 6, "au"),
    LH(new BigDecimal("1079252848800"), 13, 7, "lh"),
    LD(new BigDecimal("25902068371200"), 14, 8, "ld"),
    LY(new BigDecimal("9454254955488000"), 16, 10, "ly"),
    PC(new BigDecimal("30856776000000000"), 17, 11, "pc"),
    ;

    /**
     * The value which is needed to divide the original value to get the result in the basic unit {@link EDistanceMetric#M}.
     */
    final BigDecimal meterEquivalent;

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

    EDistanceMetric(final BigDecimal meterEquivalent, final int digitCount, final int scale, @Nonnull final String unit) {
        Preconditions.checkNotNull(unit, "unit shouldn't be null!");

        this.meterEquivalent = meterEquivalent;
        this.digitCount = digitCount;
        this.scale = scale;
        this.unit = unit;
    }

    public BigDecimal getMeterEquivalent() {
        return meterEquivalent;
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
    public static EDistanceMetric getByName(@Nonnull final String metric) {
        Preconditions.checkNotNull(metric, "metric shouldn't be null!");

        return Arrays.stream(EDistanceMetric.values()).filter(l -> l.name().equals(metric))
                .findFirst()
                .orElseThrow(() -> new NotifyUserException("There was no match for a length definition possible by searching for '" + metric + "'."));
    }

    @Nonnull
    public static EDistanceMetric getBy(@Nonnull final BigInteger value) {
        Preconditions.checkNotNull(value, "value shouldn't be null!");

        final int digitCount = DistanceCalculator.getDigitCount(value);
        return Arrays.stream(EDistanceMetric.values()).filter(l -> detectMatch(l, digitCount))
                .findFirst()
                .orElse(EDistanceMetric.PC);
    }

    private static boolean detectMatch(@Nonnull final EDistanceMetric l, final int digitCount) {
        Preconditions.checkNotNull(l, "l shouldn't be null!");

        return l.getDigitCount() == digitCount || l.getDigitCount() > digitCount;
    }

    @Nonnull
    public static EDistanceMetric getBy(@Nonnull final BigDecimal value) {
        Preconditions.checkNotNull(value, "value shouldn't be null!");

        final int digitCount = DistanceCalculator.getDigitCount(value);
        return Arrays.stream(EDistanceMetric.values()).filter(l -> detectMatch(l, digitCount))
                .findFirst()
                .orElse(EDistanceMetric.PC);
    }

    /**
     * Returns the conversion factor from the length metric to the current metric.
     *
     * @param targetMetric the metric from which should converted
     * @return the factor to convert "from" to the current
     */
    public BigDecimal getConversionFactor(@Nonnull final EDistanceMetric targetMetric) {
        Preconditions.checkNotNull(targetMetric, "targetMetric shouldn't be null!");

        return meterEquivalent.divide(targetMetric.getMeterEquivalent(), DistanceCalculator.MATH_CONTEXT_REALISTIC_PRECISION);
    }
}
