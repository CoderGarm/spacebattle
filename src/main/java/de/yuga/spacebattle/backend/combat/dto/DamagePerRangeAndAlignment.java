package de.yuga.spacebattle.backend.combat.dto;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

public class DamagePerRangeAndAlignment {

    /**
     * The range definition.
     */
    @Nonnull
    private final RangeDefinition rangeDefinition;

    /**
     * The damage per salvo in absolute units.
     */
    private long damageValue;

    /**
     * The damage per salvo relative to the total amount of damage which can be applied over all ranges.
     */
    @Nonnull
    private final EWeaponAlignment weaponAlignment;

    public DamagePerRangeAndAlignment(@Nonnull final BigDecimal minRange,
                                      @Nonnull final BigDecimal maxRange,
                                      final long damageValue,
                                      @Nonnull final EWeaponAlignment weaponAlignment) {
        Preconditions.checkNotNull(minRange, "minRange shouldn't be null!");
        Preconditions.checkNotNull(maxRange, "maxRange shouldn't be null!");
        Preconditions.checkNotNull(weaponAlignment, "weaponAlignment shouldn't be null!");

        this.rangeDefinition = new RangeDefinition(minRange, maxRange);
        this.damageValue = damageValue;
        this.weaponAlignment = weaponAlignment;
    }

    @Nonnull
    public RangeDefinition getRangeDefinition() {
        return rangeDefinition;
    }

    public long getDamageValue() {
        return damageValue;
    }

    @Nonnull
    public EWeaponAlignment getWeaponAlignment() {
        return weaponAlignment;
    }

    /**
     * States if the given distance is inside these boundaries.
     *
     * @param distance the given range
     * @return <code>true</code> if the distance is inside the boundaries, <code>false</code> otherwise
     */
    public boolean isInRange(final BigDecimal distance) {
        return rangeDefinition.isInRange(distance);
    }

    public DamagePerRangeAndAlignment multiplyDamage(final int multiplier) {
        damageValue = damageValue * multiplier;
        return this;
    }
}
