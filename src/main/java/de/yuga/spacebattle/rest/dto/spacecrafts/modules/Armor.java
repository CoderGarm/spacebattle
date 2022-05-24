package de.yuga.spacebattle.rest.dto.spacecrafts.modules;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.basics.BaseModule;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class Armor {

    @Nonnull
    @Schema(required = true, description = "The basic values of this module.")
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
