package de.yuga.spacebattle.rest.dto.spacecrafts.modules;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.basics.BaseModule;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.basics.HasCostsByParent;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class Armor {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The basic values of this module.")
    private BaseModule baseModule;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "Some relevant info about the cost of this module.")
    private HasCostsByParent hasCostsByParent;

    public Armor() {
    }

    public Armor(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.modules.Armor armor,
                 @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(armor, "armor shouldn't be null!");

        this.baseModule = new BaseModule(armor, languageCode);
        this.hasCostsByParent = new HasCostsByParent(armor);
    }

    @JsonIgnore
    public int getIdModule() {
        return baseModule.getIdModule();
    }
}
