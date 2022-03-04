package de.yuga.spacebattle.rest.dto.spacecrafts.modules;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EHyperBand;
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
    @Nonnull
    @ApiModelProperty(required = true, value = "If this propulsion module if for faster then light.")
    private EHyperBand ftlCapable;

    public Propulsion() {

    }

    public Propulsion(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.modules.Propulsion propulsion) {
        Preconditions.checkNotNull(propulsion, "propulsion shouldn't be null!");

        this.baseModule = new BaseModule(propulsion);
        this.ftlCapable = propulsion.getHyperBand();
    }

    @Nonnull
    public BaseModule getBaseModule() {
        return baseModule;
    }

    @Nonnull
    public EHyperBand getFtlCapable() {
        return ftlCapable;
    }
}
