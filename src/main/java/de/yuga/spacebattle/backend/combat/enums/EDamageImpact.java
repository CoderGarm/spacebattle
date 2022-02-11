package de.yuga.spacebattle.backend.combat.enums;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.math.Fraction;

import javax.annotation.Nonnull;
import java.util.Arrays;

public enum EDamageImpact {

    NONE(Fraction.ZERO),
    LIGHT(Fraction.getFraction(1, 20)),
    DAMAGING(Fraction.getFraction(1, 10)),
    HEAVY(Fraction.ONE_QUARTER),
    BRUTAL(Fraction.ONE_HALF),
    VIOLATING(Fraction.TWO_THIRDS),
    DEVASTATING(Fraction.THREE_QUARTERS);

    @Nonnull
    final Fraction fraction;

    EDamageImpact(@Nonnull final Fraction fraction) {
        Preconditions.checkNotNull(fraction, "fraction shouldn't be null!");

        this.fraction = fraction;
    }

    @Nonnull
    public Fraction getFraction() {
        return fraction;
    }

    /**
     * Returns the impact of the losses.
     *
     * @param losses    the amount of losses
     * @param reference the reference amount of ships
     * @return the impact
     */
    @Nonnull
    public static EDamageImpact getImpactByLossRatio(final int losses, final int reference) {
        final Fraction fraction = Fraction.getReducedFraction(losses, reference);
        return Arrays.stream(EDamageImpact.values()).filter(d -> d.fraction.compareTo(fraction) >= 0).findFirst().orElse(DEVASTATING);
    }
}
