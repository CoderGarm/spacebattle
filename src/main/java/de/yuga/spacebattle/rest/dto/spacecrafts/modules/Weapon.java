package de.yuga.spacebattle.rest.dto.spacecrafts.modules;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;
import de.yuga.spacebattle.backend.enums.EWeaponType;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.basics.BaseModule;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Weapon {

    @Nonnull
    @ApiModelProperty(required = true, value = "The basic values of this module.")
    private BaseModule baseModule;

    /**
     * Defines the range of this weapon.
     */
    @ApiModelProperty(required = true, value = "The effective range of this weapon.")
    private BigDecimal effectiveRange;

    @Nonnull
    @ApiModelProperty(required = true, value = "The way how the damage will be projected.")
    private EWeaponType weaponType;

    /**
     * Holds the information about the alignment ability.
     */
    @Nonnull
    @ApiModelProperty(required = true, value = "The possible mount points of this weapon.")
    private List<EWeaponAlignment> alignmentTypes;

    public Weapon() {
    }

    public Weapon(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.modules.Weapon weapon) {
        Preconditions.checkNotNull(weapon, "weapon shouldn't be null!");

        this.effectiveRange = weapon.getDamageProjectionRange();
        this.weaponType = weapon.getWeaponType();
        this.alignmentTypes = new ArrayList<>(weapon.getAllowedWeaponAlignments());
        this.baseModule = new BaseModule(weapon);
    }

    @Nonnull
    public BaseModule getBaseModule() {
        return baseModule;
    }

    public void setBaseModule(@Nonnull BaseModule baseModule) {
        this.baseModule = baseModule;
    }

    public BigDecimal getEffectiveRange() {
        return effectiveRange;
    }

    public void setEffectiveRange(BigDecimal effectiveRange) {
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
