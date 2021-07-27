package de.yuga.spacebattle.rest.dto.spacecrafts.modules;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.basics.BaseModule;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;

public class ElectronicWarfare {

    @Nonnull
    @ApiModelProperty(required = true, value = "The basic values of this module.")
    private BaseModule baseModule;

    public ElectronicWarfare() {
    }

    public ElectronicWarfare(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.modules.ElectronicWarfare electronicWarfare) {
        Preconditions.checkNotNull(electronicWarfare, "electronicWarfare shouldn't be null!");

        this.baseModule = new BaseModule(electronicWarfare);
    }

    @Nonnull
    public BaseModule getBaseModule() {
        return baseModule;
    }
}
