package de.yuga.spacebattle.backend.entities.constructables.spacecrafts.details;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Weapon;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

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
    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idWeapon")
    private Weapon weapon;

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

    @Nonnull
    public EWeaponAlignment getWeaponAlignment() {
        return weaponAlignment;
    }

    @Nonnull
    public Weapon getWeapon() {
        return weapon;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AlignedFitting)) return false;

        AlignedFitting that = (AlignedFitting) o;

        if (weaponAlignment != that.weaponAlignment) return false;
        return weapon.equals(that.weapon);
    }

    @Override
    public int hashCode() {
        int result = weaponAlignment.hashCode();
        result = 31 * result + weapon.hashCode();
        return result;
    }
}
