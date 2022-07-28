package de.yuga.spacebattle.rest.dto.spacecrafts.modules;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;
import de.yuga.spacebattle.backend.enums.EWeaponType;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.basics.BaseModule;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@Schema(description = ".")
public class Weapon {

    @Nonnull
    @Schema(required = true, description = "The basic values of this module.")
    private BaseModule baseModule;

    /**
     * Defines the range of this weapon.
     */
    @Nonnull
    @Schema(required = true, description = "The effective range of this weapon.")
    private Distance effectiveRange;

    @Nonnull
    @Schema(required = true, description = "The way how the damage will be projected.")
    private EWeaponType weaponType;

    /**
     * Holds the information about the alignment ability.
     */
    @Nonnull
    @Schema(required = true, description = "The possible mount points of this weapon.")
    private List<EWeaponAlignment> alignmentTypes;

    public Weapon() {
    }

    public Weapon(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.modules.Weapon weapon,
                  @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(weapon, "weapon shouldn't be null!");

        this.effectiveRange = weapon.getDamageProjectionRange();
        this.weaponType = weapon.getWeaponType();
        this.alignmentTypes = new ArrayList<>(weapon.getAllowedWeaponAlignments());
        this.baseModule = new BaseModule(weapon, languageCode);
    }

    @Nonnull
    public BaseModule getBaseModule() {
        return baseModule;
    }

    public void setBaseModule(@Nonnull BaseModule baseModule) {
        this.baseModule = baseModule;
    }

    public Distance getEffectiveRange() {
        return effectiveRange;
    }

    public void setEffectiveRange(Distance effectiveRange) {
        this.effectiveRange = effectiveRange;
    }

    @Nonnull
    public EWeaponType getWeaponType() {
        return weaponType;
    }

    public void setWeaponType(@Nonnull EWeaponType weaponType) {
        this.weaponType = weaponType;
    }

    @Nonnull
    public List<EWeaponAlignment> getAlignmentTypes() {
        return alignmentTypes;
    }

    public void setAlignmentTypes(@Nonnull List<EWeaponAlignment> alignmentTypes) {
        this.alignmentTypes = alignmentTypes;
    }
}
