package de.yuga.spacebattle.rest.dto.spacecrafts.modules;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.ETechnologyType;
import de.yuga.spacebattle.backend.enums.physics.EHyperBand;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.basics.BaseModule;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class Propulsion {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The basic values of this module.")
    private BaseModule baseModule;

    @JsonProperty
    @Schema(required = true, description = "The percentage of the parent's module cost which represents the costs of 'this'.")
    private int costsPercentage;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "If this propulsion module if for faster then light.")
    private EHyperBand hyperBand;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "If this propulsion module is for military or civil purposes.")
    private ETechnologyType technologyType;

    public Propulsion() {
    }

    public Propulsion(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.modules.Propulsion propulsion,
                      @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(propulsion, "propulsion shouldn't be null!");

        this.baseModule = new BaseModule(propulsion, languageCode);
        this.costsPercentage = propulsion.getCostsPercentage();
        this.technologyType = propulsion.getTechnologyType();
        this.hyperBand = propulsion.getHyperBand();
    }

    @JsonIgnore
    public int getIdModule() {
        return baseModule.getIdModule();
    }
}
