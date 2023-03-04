package de.yuga.spacebattle.backend.enums.physics;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator;
import de.yuga.spacebattle.backend.enums.ETechnologyType;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Arrays;

public enum EHyperBand {

    NONE(0, 0.3, 1, 1, 1),
    ALPHA(0.92, 0.6, 62, 37.2, 31.0),
    BETA(0.85, 0.6, 767, 460.2, 383.5),
    GAMMA(0.78, 0.6, 1473, 883.8, 736.5),
    DELTA(0.72, 0.6, 2178, 1306.8, 1089.0),
    EPSILON(0.66, 0.6, 2884, 1730.4, 1442.0),
    ZETA(0.61, 0.6, 3589, 2153.4, 1794.5),
    ETA(0.56, 0.6, 4294, 2576.4, 2147.0),
    THETA(0.52, 0.6, 5000, 3000.0, 2500.0);

    /**
     * The loss of speed when this translation is proceeded.
     */
    private final double translationBleedOff;

    private final double translationUpMaxSpeed;

    private final int velocityMultiplier;

    /**
     * The effective value of the speed of light in hyperspace for a warship.
     */
    private final double effectiveCWarship;

    /**
     * The effective value of the speed of light in hyperspace for a merchants ship.
     */
    private final double effectiveCMerchant;

    EHyperBand(final double translationBleedOff,
               final double translationUpMaxSpeed,
               final int velocityMultiplier,
               final double effectiveCWarship,
               final double effectiveCMerchant) {
        this.translationBleedOff = translationBleedOff;
        this.translationUpMaxSpeed = translationUpMaxSpeed;
        this.velocityMultiplier = velocityMultiplier;
        this.effectiveCWarship = effectiveCWarship;
        this.effectiveCMerchant = effectiveCMerchant;
    }

    public double getTranslationBleedOff() {
        return translationBleedOff;
    }

    public double getTranslationUpMaxSpeed() {
        return translationUpMaxSpeed;
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
                .orElseThrow(() -> new NotifyWebUserException("There was no match for a hyper band definition possible by searching for '" + hyperBand + "'."));
    }

    /**
     * Returns the physical maximum speed for this hyper band.
     *
     * @return the top speed in m/s
     */
    public BigDecimal getPhysicalTopSpeed() {
        return EDistanceMetric.LS.getMeterEquivalent().multiply(BigDecimal.valueOf(effectiveCWarship), DistanceCalculator.MC_HU);
    }

    /**
     * Returns the safe travel top speed.
     *
     * @return the top speed in m/s which is safe to travel
     */
    public BigDecimal getEffectiveTopSpeed(@Nonnull final ETechnologyType technologyType) {
        Preconditions.checkNotNull(technologyType, "technologyType must not be empty");

        return getPhysicalTopSpeed().multiply(BigDecimal.valueOf(technologyType.getMaxVelocitySOL()), DistanceCalculator.MC_HU);
    }
}
