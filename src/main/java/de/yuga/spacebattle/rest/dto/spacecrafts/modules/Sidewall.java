package de.yuga.spacebattle.rest.dto.spacecrafts.modules;

import de.yuga.spacebattle.rest.dto.spacecrafts.modules.basics.BaseModule;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class Sidewall {

    @Nonnull
    @Schema(required = true, description = "The basic values of this module.")
    private BaseModule baseModule;

    public Sidewall() {
    }

    public Sidewall(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.modules.Sidewall sidewall) {
        this.baseModule = new BaseModule(sidewall);
    }

    @Nonnull
    public BaseModule getBaseModule() {
        return baseModule;
    }
}
