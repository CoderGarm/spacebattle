package de.yuga.spacebattle.rest.dto.spacecrafts.details;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.Launcher;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.Weapon;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The aligned fitting represents a weapon system and the alignment, where the weapon is placed.
 */
@Schema(description = ".")
public class AlignedFitting {

    /**
     * The placement.
     */
    @Nonnull
    @Schema(required = true, description = "The used mount point of this fitting.")
    private EWeaponAlignment weaponAlignment;

    /**
     * The possible direct weapon.
     */
    @Nullable
    @Schema(description = "The mounted direct weapon.")
    private Weapon weapon;

    /**
     * The possible missile launcher.
     */
    @Nullable
    @Schema(description = "The mounted launcher.")
    private Launcher launcher;

    /**
     * The amount of this weapon with the given {@link EWeaponAlignment}.
     * <p>
     * Note that a {@link EWeaponAlignment#BROADSIDE} aligned fit needs to be doubled.
     */
    @Schema(required = true, description = "The amount of mounted weapons.")
    private int amount;

    public AlignedFitting() {
    }

    public AlignedFitting(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.details.AlignedFitting alignedFitting) {
        Preconditions.checkNotNull(alignedFitting, "alignedFitting shouldn't be null!");

        this.weaponAlignment = alignedFitting.getWeaponAlignment();
        this.weapon = alignedFitting.getWeapon() != null ? new Weapon(alignedFitting.getWeapon()) : null;
        this.launcher = alignedFitting.getLauncher() != null ? new Launcher(alignedFitting.getLauncher()) : null;
        this.amount = alignedFitting.getAmount();
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

    public void setWeaponAlignment(@Nonnull EWeaponAlignment weaponAlignment) {
        this.weaponAlignment = weaponAlignment;
    }

    public void setWeapon(@Nullable Weapon weapon) {
        this.weapon = weapon;
    }

    public void setLauncher(@Nullable Launcher launcher) {
        this.launcher = launcher;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
