package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.NotifyUserException;

import javax.annotation.Nonnull;
import java.util.Arrays;

public enum EHyperBand {

    NONE(0, 1, 1, 1),
    Alpha(0.92, 62, 37.2, 31.0),
    Beta(0.85, 767, 460.2, 383.5),
    Gamma(0.78, 1473, 883.8, 736.5),
    Delta(0.72, 2178, 1306.8, 1089.0),
    Epsilon(0.66, 2884, 1730.4, 1442.0),
    Zeta(0.61, 3589, 2153.4, 1794.5),
    Eta(0.56, 4294, 2576.4, 2147.0),
    Theta(0.52, 5000, 3000.0, 2500.0);

    /**
     * The loss of speed when this translation is proceeded.
     */
    final double translationBleedOff;

    final int velocityMultiplier;

    /**
     * The effective value of the speed of light in hyperspace for a warship.
     */
    final double effectiveCWarship;

    /**
     * The effective value of the speed of light in hyperspace for a merchants ship.
     */
    final double effectiveCMerchant;

    EHyperBand(final double translationBleedOff,
               final int velocityMultiplier,
               final double effectiveCWarship,
               final double effectiveCMerchant) {
        this.translationBleedOff = translationBleedOff;
        this.velocityMultiplier = velocityMultiplier;
        this.effectiveCWarship = effectiveCWarship;
        this.effectiveCMerchant = effectiveCMerchant;
    }

    public double getTranslationBleedOff() {
        return translationBleedOff;
    }

    public int getVelocityMultiplier() {
        return velocityMultiplier;
    }

    public double getEffectiveCWarship() {
        return effectiveCWarship;
    }

    public double getEffectiveCMerchant() {
        return effectiveCMerchant;
    }

    @Nonnull
    public static EHyperBand getByName(@Nonnull final String hyperBand) {
        Preconditions.checkNotNull(hyperBand, "hyperBand shouldn't be null!");

        return Arrays.stream(EHyperBand.values()).filter(l -> l.name().equals(hyperBand))
                .findFirst()
                .orElseThrow(() -> new NotifyUserException("There was no match for a hyper band definition possible by searching for '" + hyperBand + "'."));
    }
}
