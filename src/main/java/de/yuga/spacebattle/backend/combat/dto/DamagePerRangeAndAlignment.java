package de.yuga.spacebattle.backend.combat.dto;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;
import de.yuga.spacebattle.backend.enums.EWeaponType;

import javax.annotation.Nonnull;

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

    @Nonnull
    private final EWeaponType weaponType;

    public DamagePerRangeAndAlignment(@Nonnull final RangeDefinition minRange,
                                      final long damageValue,
                                      @Nonnull final EWeaponAlignment weaponAlignment,
                                      @Nonnull final EWeaponType weaponType) {
        Preconditions.checkNotNull(minRange, "minRange shouldn't be null!");
        Preconditions.checkNotNull(weaponAlignment, "weaponAlignment shouldn't be null!");
        Preconditions.checkNotNull(weaponType, "weaponType shouldn't be null!");

        this.rangeDefinition = minRange;
        this.damageValue = damageValue;
        this.weaponAlignment = weaponAlignment;
        this.weaponType = weaponType;
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

    @Nonnull
    public EWeaponType getWeaponType() {
        return weaponType;
    }

    /**
     * States if the given distance is inside these boundaries.
     *
     * @param distance the given range
     * @return <code>true</code> if the distance is inside the boundaries, <code>false</code> otherwise
     */
    public boolean isInRange(@Nonnull final Distance distance) {
        Preconditions.checkNotNull(distance, "distance shouldn't be null!");

        return rangeDefinition.isInRange(distance.getCoordinateInMetric(rangeDefinition.getDistanceMetric()));
    }

    public DamagePerRangeAndAlignment multiplyDamage(final int multiplier) {
        damageValue = damageValue * multiplier;
        return this;
    }
}
