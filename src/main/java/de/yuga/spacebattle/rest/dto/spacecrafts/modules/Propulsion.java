package de.yuga.spacebattle.rest.dto.spacecrafts.modules;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.physics.EHyperBand;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.basics.BaseModule;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class Propulsion {

    @Nonnull
    @Schema(required = true, description = "The basic values of this module.")
    private BaseModule baseModule;

    /**
     * If this propulsion module provides the ability to travel faster than light.
     */
    @Nonnull
    @Schema(required = true, description = "If this propulsion module if for faster then light.")
    private EHyperBand ftlCapable;

    public Propulsion() {

    }

    public Propulsion(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.modules.Propulsion propulsion,
                      @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(propulsion, "propulsion shouldn't be null!");

        this.baseModule = new BaseModule(propulsion, languageCode);
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
