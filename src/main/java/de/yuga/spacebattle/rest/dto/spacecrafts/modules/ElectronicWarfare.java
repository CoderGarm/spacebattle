package de.yuga.spacebattle.rest.dto.spacecrafts.modules;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.WithCosts;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.basics.BaseModule;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class ElectronicWarfare extends WithCosts<ElectronicWarfare> {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The basic values of this module.")
    private BaseModule baseModule;

    public ElectronicWarfare() {
    }

    public ElectronicWarfare(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.modules.ElectronicWarfare electronicWarfare,
                             @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(electronicWarfare, "electronicWarfare shouldn't be null!");

        this.baseModule = new BaseModule(electronicWarfare, languageCode);
    }

    @Nonnull
    public BaseModule getBaseModule() {
        return baseModule;
    }
}
