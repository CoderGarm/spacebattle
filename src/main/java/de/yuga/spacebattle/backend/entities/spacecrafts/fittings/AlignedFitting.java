package de.yuga.spacebattle.backend.entities.spacecrafts.fittings;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.dto.DamagePerRangeAndAlignment;
import de.yuga.spacebattle.backend.combat.dto.RangeDefinition;
import de.yuga.spacebattle.backend.dto.physics.Distance;
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
import java.util.ArrayList;

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
    @Min(1)
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
     * @param boundaries the boundaries
     * @return the damage value
     */
    @Nullable
    public DamagePerRangeAndAlignment getDamagePerRange(@Nonnull final RangeDefinition boundaries) {
        Preconditions.checkNotNull(boundaries, "boundaries shouldn't be null!");

        Distance damageProjectionRange;
        long damageValue = 0;
        EWeaponType weaponType = null;
        boolean isInRange = false;
        if (weapon != null) {
            damageProjectionRange = weapon.getDamageProjectionRange();
            isInRange = boundaries.isInRange(damageProjectionRange);

            damageValue = (long) weapon.getEffectValue() * amount;
            weaponType = EWeaponType.BEAM;

        }
        if (launcher != null) {
            final Missile missile = new ArrayList<>(launcher.getAllowedMissiles()).get(0); // todo fix missile selection
            damageProjectionRange = missile.getMaximumMissileRange();
            isInRange = boundaries.isInRange(damageProjectionRange);

            damageValue = missile.getWarhead().getDamageValue() * amount;
            weaponType = EWeaponType.MISSILE;
        }
        if (!isInRange || damageValue == 0) {
            return null;
        }
        return new DamagePerRangeAndAlignment(boundaries, damageValue, weaponAlignment, weaponType);
    }

    @Nonnull
    public DamagePerRangeAndAlignment getDamagePerRange() {
        Distance damageProjectionRange = null;
        long damageValue = 0;
        EWeaponType weaponType = null;
        if (weapon != null) {
            damageProjectionRange = weapon.getDamageProjectionRange();

            damageValue = (long) weapon.getEffectValue() * amount;
            weaponType = EWeaponType.BEAM;

        }
        if (launcher != null) {
            final Missile missile = new ArrayList<>(launcher.getAllowedMissiles()).get(0); // todo fix missile selection
            damageProjectionRange = missile.getMaximumMissileRange();

            damageValue = missile.getWarhead().getDamageValue() * amount;
            weaponType = EWeaponType.MISSILE;
        }
        assert weaponType != null : "If the weapon type is null, then the item wasn't configured properly";
        return new DamagePerRangeAndAlignment(
                new RangeDefinition(BigDecimal.ZERO, damageProjectionRange.getCoordinate(), damageProjectionRange.getDistanceMetric()),
                damageValue,
                weaponAlignment,
                weaponType);
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
    public Distance getRange() {
        Distance range = Distance.ZERO;
        if (weapon != null) {
            range = weapon.getDamageProjectionRange();
        }
        if (launcher != null) {
            range = new ArrayList<>(launcher.getAllowedMissiles()).get(0).getMaximumMissileRange(); // todo fix missile selection
        }
        return range;
    }

    public int calculateUsedCapacity() {
        if (launcher != null) {
            return amount * launcher.getUseCapacity();
        }
        if (weapon != null) {
            return amount * weapon.getUseCapacity();
        }
        return 0;
    }
}
