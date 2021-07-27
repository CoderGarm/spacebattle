package de.yuga.spacebattle.rest.dto.spacecrafts.modules;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.basics.BaseModule;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;

public class Propulsion {

    @Nonnull
    @ApiModelProperty(required = true, value = "The basic values of this module.")
    private BaseModule baseModule;

    /**
     * If this propulsion module provides the ability to travel faster than light.
     */
    @ApiModelProperty(required = true, value = "If this propulsion module if for faster then light.")
    private boolean ftlCapable = false;

    public Propulsion() {

    }

    public Propulsion(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.modules.Propulsion propulsion) {
        Preconditions.checkNotNull(propulsion, "propulsion shouldn't be null!");

        this.baseModule = new BaseModule(propulsion);
        this.ftlCapable = propulsion.isFtlCapable();
    }

    @Nonnull
    public BaseModule getBaseModule() {
        return baseModule;
    }

    public boolean isFtlCapable() {
        return ftlCapable;
    }
}
