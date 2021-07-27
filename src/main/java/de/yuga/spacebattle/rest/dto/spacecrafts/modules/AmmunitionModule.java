package de.yuga.spacebattle.rest.dto.spacecrafts.modules;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.basics.BaseModule;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;

public class AmmunitionModule {

    @Nonnull
    @ApiModelProperty(required = true, value = "The basic values of this module.")
    private BaseModule baseModule;

    @Nonnull
    @ApiModelProperty(required = true, value = "The basic values of this module.")
    private Missile missile;

    public AmmunitionModule() {
    }

    public AmmunitionModule(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.modules.AmmunitionModule ammunitionModule) {
        Preconditions.checkNotNull(ammunitionModule, "ammunitionModule shouldn't be null!");

        this.baseModule = new BaseModule(ammunitionModule);
        this.missile = new Missile(ammunitionModule.getMissile());
    }

    @Nonnull
    public BaseModule getBaseModule() {
        return baseModule;
    }

    public void setBaseModule(@Nonnull BaseModule baseModule) {
        this.baseModule = baseModule;
    }

    @Nonnull
    public Missile getMissile() {
        return missile;
    }

    public void setMissile(@Nonnull Missile missile) {
        this.missile = missile;
    }
}
