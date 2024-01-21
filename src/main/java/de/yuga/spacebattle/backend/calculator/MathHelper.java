package de.yuga.spacebattle.backend.calculator;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

public class MathHelper {

    private MathHelper() {
    }

    @Nonnull
    public static BigDecimal getOrEpsilon(@Nonnull final BigDecimal value) {
        Preconditions.checkNotNull(value, "value must not be empty");

        return value.compareTo(BigDecimal.ZERO) == 0 ? new BigDecimal("1e-5") : value;
    }

    @Nonnull
    public static BigDecimal divideWithEpsilon(@Nonnull final BigDecimal dividend, @Nonnull final BigDecimal divisor) {
        Preconditions.checkNotNull(dividend, "dividend must not be empty");
        Preconditions.checkNotNull(divisor, "divisor must not be empty");

        final BigDecimal realDivisor = divisor.compareTo(BigDecimal.ZERO) == 0 ? new BigDecimal("1e-5") : divisor;
        return dividend.divide(realDivisor, DistanceCalculator.MC_HU);

    }
}
