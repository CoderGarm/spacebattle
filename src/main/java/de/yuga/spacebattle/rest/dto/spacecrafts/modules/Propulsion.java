package de.yuga.spacebattle.rest.dto.spacecrafts.modules;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.ETechLevel;
import de.yuga.spacebattle.backend.enums.ETechnologyType;
import de.yuga.spacebattle.backend.enums.physics.EHyperBand;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.basics.HasCostsByParent;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.basics.HasIdModule;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Schema(description = ".")
public class Propulsion extends HasIdModule {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "Some relevant info about the cost of this module.")
    private HasCostsByParent hasCostsByParent;

    @Nullable
    @JsonProperty
    @Schema(required = true, description = "The base effect value of this module.")
    private Integer effectValue;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The level of this module.")
    private ETechLevel techLevel;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "If this propulsion module if for faster then light.")
    private EHyperBand hyperBand;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "If this propulsion module is for military or civil purposes.")
    private ETechnologyType technologyType;

    public Propulsion() {
        super();
    }

    public Propulsion(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.modules.Propulsion propulsion,
                      @Nonnull final String languageCode) {
        super(propulsion);
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(propulsion, "propulsion shouldn't be null!");

        this.hasCostsByParent = new HasCostsByParent(propulsion, languageCode);
        this.techLevel = propulsion.getTechLevel();
        this.technologyType = propulsion.getTechnologyType();
        this.hyperBand = propulsion.getHyperBand();
    }
}
