package de.yuga.spacebattle.rest.dto.spacecrafts.modules;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.basics.BaseModule;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;

public class Armor {

    @Nonnull
    @ApiModelProperty(required = true, value = "The basic values of this module.")
    private BaseModule baseModule;

    public Armor() {
    }

    public Armor(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.modules.Armor armor) {
        Preconditions.checkNotNull(armor, "armor shouldn't be null!");

        this.baseModule = new BaseModule(armor);
    }

    @Nonnull
    public BaseModule getBaseModule() {
        return baseModule;
    }
}
