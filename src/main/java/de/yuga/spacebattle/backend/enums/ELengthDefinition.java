package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

public enum ELengthDefinition {
    M(BigInteger.ONE, 1, "m"),
    KM(new BigInteger("1000"), 4, "km"),
    LS(new BigInteger("299792458"), 9, "ls"),
    LM(new BigInteger("17987547480"), 11, "lm"),
    AU(new BigInteger("149597870700"), 12, "AU"),
    LH(new BigInteger("1079252848800"), 13, "lh"),
    LD(new BigInteger("25902068371200"), 14, "ld"),
    LY(new BigInteger("9454254955488000"), 13, "ly"),
    PC(new BigInteger("30856776000000000"), 17, "pc"),
    ;

    final BigInteger divisor;

    final int digitCount;

    @Nonnull
    final String unit;

    ELengthDefinition(final BigInteger divisor, final int digitCount, @Nonnull final String unit) {
        Preconditions.checkNotNull(unit, "unit shouldn't be null!");

        this.divisor = divisor;
        this.digitCount = digitCount;
        this.unit = unit;
    }

    public BigInteger getDivisor() {
        return divisor;
    }

    @Nonnull
    public String getUnit() {
        return unit;
    }

    public int getDigitCount() {
        return digitCount;
    }

    public static ELengthDefinition getBy(@Nonnull final BigInteger value) {
        Preconditions.checkNotNull(value, "value shouldn't be null!");

        final int digitCount = DistanceCalculator.getDigitCount(value);
        return Arrays.stream(ELengthDefinition.values()).filter(l -> detectMatch(l, digitCount)).findFirst().orElse(ELengthDefinition.PC);
    }

    private static boolean detectMatch(final ELengthDefinition l, final int digitCount) {
        return l.getDigitCount() == digitCount || l.getDigitCount() > digitCount;
    }

    public static ELengthDefinition getBy(@Nonnull final BigDecimal value) {
        Preconditions.checkNotNull(value, "value shouldn't be null!");

        final int digitCount = DistanceCalculator.getDigitCount(value);
        return Arrays.stream(ELengthDefinition.values()).filter(l -> detectMatch(l, digitCount)).findFirst().orElse(ELengthDefinition.PC);
    }
}
