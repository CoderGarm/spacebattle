package de.yuga.spacebattle.rest.dto.spacecrafts.modules;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;
import de.yuga.spacebattle.backend.enums.EWeaponType;
import de.yuga.spacebattle.rest.dto.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.basics.BaseModule;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Launcher {

    @Nonnull
    @ApiModelProperty(required = true, value = "The basic values of this module.")
    private BaseModule baseModule;

    @Nonnull
    @ApiModelProperty(required = true, value = "The way how the damage will be projected.")
    private EWeaponType weaponType;

    /**
     * Holds the information about the alignment ability.
     */
    @Nonnull
    @ApiModelProperty(required = true, value = "The possible mount points of this weapon.")
    private List<EWeaponAlignment> alignmentTypes;

    @Nonnull
    @ApiModelProperty(required = true, value = "The bunch of allowed missiles for this launcher.")
    private List<de.yuga.spacebattle.rest.dto.spacecrafts.ammunition.Missile> allowedMissiles = new ArrayList<>();

    /**
     * An empty ammunition module means that the weapon needs no ammunition.
     */
    @Nonnull
    @ApiModelProperty(required = true, value = "The ammunition for this launcher.")
    private AmmunitionModule ammunitionModule;

    public Launcher() {
    }

    public Launcher(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.modules.Launcher launcher) {
        Preconditions.checkNotNull(launcher, "launcher shouldn't be null!");

        this.baseModule = new BaseModule(launcher);
        this.weaponType = launcher.getWeaponType();
        this.ammunitionModule = new AmmunitionModule(launcher.getAmmunitionModule());
        this.alignmentTypes = new ArrayList<>(launcher.getAllowedWeaponAlignments());
        this.allowedMissiles = launcher.getAllowedMissiles().stream().map(de.yuga.spacebattle.rest.dto.spacecrafts.ammunition.Missile::new).collect(Collectors.toList());
    }

    @Nonnull
    public BaseModule getBaseModule() {
        return baseModule;
    }

    @Nonnull
    public EWeaponType getWeaponType() {
        return weaponType;
    }

    @Nonnull
    public List<EWeaponAlignment> getAlignmentTypes() {
        return alignmentTypes;
    }

    @Nonnull
    public List<Missile> getAllowedMissiles() {
        return allowedMissiles;
    }

    @Nonnull
    public AmmunitionModule getAmmunitionModule() {
        return ammunitionModule;
    }

    public void setBaseModule(@Nonnull BaseModule baseModule) {
        this.baseModule = baseModule;
    }

    public void setWeaponType(@Nonnull EWeaponType weaponType) {
        this.weaponType = weaponType;
    }

    public void setAlignmentTypes(@Nonnull List<EWeaponAlignment> alignmentTypes) {
        this.alignmentTypes = alignmentTypes;
    }

    public void setAllowedMissiles(@Nonnull List<Missile> allowedMissiles) {
        this.allowedMissiles = allowedMissiles;
    }

    public void setAmmunitionModule(@Nonnull AmmunitionModule ammunitionModule) {
        this.ammunitionModule = ammunitionModule;
    }
}
