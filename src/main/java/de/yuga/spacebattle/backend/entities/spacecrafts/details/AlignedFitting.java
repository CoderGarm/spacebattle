package de.yuga.spacebattle.backend.entities.spacecrafts.details;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.dto.DamagePerRangeAndAlignment;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Launcher;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Weapon;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;
import de.yuga.spacebattle.backend.enums.EWeaponType;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * The aligned fitting represents a weapon system and the alignment, where the weapon is placed.
 */
@Embeddable
public class AlignedFitting {

    /**
     * The placement.
     */
    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private EWeaponAlignment weaponAlignment;

    /**
     * The weapon.
     */
    @Nullable
    @ManyToOne
    @JoinColumn(name = "idWeapon")
    private Weapon weapon;

    /**
     * The weapon.
     */
    @Nullable
    @ManyToOne
    @JoinColumn(name = "idLauncher")
    private Launcher launcher;

    /**
     * The amount of this weapon with the given {@link EWeaponAlignment}.
     * <p>
     * Note that a {@link EWeaponAlignment#BROADSIDE} aligned fit needs to be doubled.
     */
    @Min(0)
    private int amount;

    public AlignedFitting() {
    }

    public AlignedFitting(@Nonnull final EWeaponAlignment weaponAlignment,
                          @Nonnull final Weapon weapon,
                          final int amount) {
        Preconditions.checkNotNull(weaponAlignment, "weaponAlignment shouldn't be null!");
        Preconditions.checkNotNull(weapon, "weapon shouldn't be null!");

        this.weaponAlignment = weaponAlignment;
        this.weapon = weapon;
        this.amount = amount;
    }

    public AlignedFitting(@Nonnull final EWeaponAlignment weaponAlignment,
                          @Nonnull final Launcher launcher,
                          final int amount) {
        Preconditions.checkNotNull(weaponAlignment, "weaponAlignment shouldn't be null!");
        Preconditions.checkNotNull(launcher, "launcher shouldn't be null!");

        this.weaponAlignment = weaponAlignment;
        this.launcher = launcher;
        this.amount = amount;
    }

    @Nonnull
    public EWeaponType getWeaponType() {
        if (weapon != null) {
            return weapon.getWeaponType();
        }
        if (launcher != null) {
            return launcher.getWeaponType();
        }
        throw new NotifyWebUserException("An aligned fittings needs a weapon system!");
    }

    @Nonnull
    public EWeaponAlignment getWeaponAlignment() {
        return weaponAlignment;
    }

    @Nullable
    public Weapon getWeapon() {
        return weapon;
    }

    @Nullable
    public Launcher getLauncher() {
        return launcher;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    /**
     * Returns the damage which can be projected by this fitting to the given range in meter.
     *
     * @param lowerBound the lower boundary
     * @param upperBound the upper boundary
     * @return the damage value
     */
    @Nullable
    public DamagePerRangeAndAlignment getDamagePerRange(final BigDecimal lowerBound, final BigDecimal upperBound) {

        BigDecimal damageProjectionRange;
        long damageValue = 0;
        EWeaponType weaponType = null;
        if (weapon != null) {
            damageProjectionRange = weapon.getDamageProjectionRange();
            final int compareToLower = damageProjectionRange.compareTo(lowerBound);
            final int compareToUpper = upperBound.compareTo(damageProjectionRange);
            if (compareToLower >= 0 && compareToUpper <= 0) {
                damageValue = (long) weapon.getEffectValue() * amount;
                weaponType = EWeaponType.BEAM;
            }
        }
        if (launcher != null) {
            final Missile missile = launcher.getAmmunitionModule().getMissile();
            damageProjectionRange = missile.getMissileRange();
            final int compareToLower = damageProjectionRange.compareTo(lowerBound);
            final int compareToUpper = upperBound.compareTo(damageProjectionRange);
            if (compareToLower >= 0 && compareToUpper <= 0) {
                damageValue = missile.getWarhead().getDamageValue() * amount;
                weaponType = EWeaponType.MISSILE;
            }
        }
        if (weaponType == null || damageValue == 0) {
            return null;
        }
        return new DamagePerRangeAndAlignment(lowerBound, upperBound, damageValue, weaponAlignment, weaponType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof AlignedFitting)) return false;

        AlignedFitting that = (AlignedFitting) o;

        return new EqualsBuilder().append(weaponAlignment, that.weaponAlignment).append(weapon, that.weapon).append(launcher, that.launcher).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(weaponAlignment).append(weapon).append(launcher).toHashCode();
    }

    /**
     * Returns the range.
     *
     * @return the range
     */
    @Nonnull
    public BigDecimal getRange() {
        BigDecimal range = BigDecimal.ZERO;
        if (weapon != null) {
            range = weapon.getDamageProjectionRange();
        }
        if (launcher != null) {
            range = launcher.getAmmunitionModule().getMissile().getMissileRange();
        }
        return range;
    }
}
