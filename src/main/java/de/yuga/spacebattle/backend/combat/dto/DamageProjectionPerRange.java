package de.yuga.spacebattle.backend.combat.dto;

import java.math.BigDecimal;

/**
 * Holds the information about a range and the applicable damage
 * for that range absolutely and relative to the complete projectable damage to a fleet.
 */
public class DamageProjectionPerRange {

    /**
     * The minimal range from where the damage can be projected.
     */
    private final BigDecimal minRange;

    /**
     * The maximal range from where the damage can be projected.
     */
    private final BigDecimal maxRange;

    /**
     * The damage per salvo in absolute units.
     */
    private final long absoluteEffectiveDamage;

    /**
     * The damage per salvo relative to the total amount of damage which can be applied over all ranges.
     */
    private final long relativeEffectiveDamage;

    public DamageProjectionPerRange(final BigDecimal minRange,
                                    final BigDecimal maxRange,
                                    final long absoluteEffectiveDamage,
                                    final long relativeEffectiveDamage) {
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.absoluteEffectiveDamage = absoluteEffectiveDamage;
        this.relativeEffectiveDamage = relativeEffectiveDamage;
    }

    /**
     * States if the given distance is inside these boundaries.
     *
     * @param distance the given range
     * @return <code>true</code> if the distance is inside the boundaries, <code>false</code> otherwise
     */
    public boolean isInRange(final BigDecimal distance) {
        final int compareToMin = minRange.compareTo(distance);
        final int compareToMax = maxRange.compareTo(distance);
        return compareToMin <= 0 && compareToMax >= 0;
    }

    public BigDecimal getMinRange() {
        return minRange;
    }

    public BigDecimal getMaxRange() {
        return maxRange;
    }

    public long getAbsoluteEffectiveDamage() {
        return absoluteEffectiveDamage;
    }

    public long getRelativeEffectiveDamage() {
        return relativeEffectiveDamage;
    }
}
