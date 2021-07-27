package de.yuga.spacebattle.backend.enums;

import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;

import javax.annotation.Nullable;
import java.util.concurrent.ThreadLocalRandom;

public enum EHitArea {
    FITTING_AND_HULL(null),
    ARMOR(FITTING_AND_HULL),
    SIDEWALL(ARMOR),
    PROPULSION(ARMOR),
    ELOKA(ARMOR);

    @Nullable
    private final EHitArea fallback;

    EHitArea(@Nullable final EHitArea fallback) {
        this.fallback = fallback;
    }

    @Nullable
    public EHitArea getFallback() {
        return fallback;
    }

    private static boolean isInside(final int toCheck, final int lowerBound, final int upperBound) {
        return toCheck >= lowerBound && toCheck <= upperBound;
    }

    public static EHitArea getRandomToApplyDamage() {
        final int attackedPart = ThreadLocalRandom.current().nextInt(0, 100);
        if (EHitArea.isInside(attackedPart, 0, 10)) {
            // hit fitting and hull - lowest chance
            return EHitArea.FITTING_AND_HULL;
        }
        if (EHitArea.isInside(attackedPart, 10, 45)) {
            // hit armor - low chance
            return EHitArea.ARMOR;
        }
        if (EHitArea.isInside(attackedPart, 45, 80)) {
            // hit sidewall - normal chance
            return EHitArea.SIDEWALL;
        }
        if (EHitArea.isInside(attackedPart, 80, 90)) {
            // hit propulsion - lower chance
            return EHitArea.PROPULSION;
        }
        if (EHitArea.isInside(attackedPart, 90, 100)) {
            // hit eloka - lowest chance
            return EHitArea.ELOKA;
        }

        throw new NotifyWebUserException("This must not happen.");
    }
}
