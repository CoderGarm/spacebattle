package de.yuga.spacebattle.rest.dto.spacecrafts.modules;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.ETechLevel;
import de.yuga.spacebattle.backend.enums.ETechnologyType;
import de.yuga.spacebattle.backend.enums.physics.EHyperBand;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Schema(description = ".")
public class Propulsion {

    @JsonProperty
    @Schema(required = true, description = "The id of this module.")
    private int idModule;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The name of this module.")
    private String name;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The technical type name of this module.")
    private String technicalTypeName;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The description of this module.")
    private String description;

    @JsonProperty
    @Schema(required = true, description = "The percentage of the parent's module cost which represents the costs of 'this'.")
    private int costsPercentage;

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

    }

    public Propulsion(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.modules.Propulsion propulsion,
                      @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(propulsion, "propulsion shouldn't be null!");

        this.idModule = propulsion.getId();
        this.name = propulsion.getName(languageCode);
        this.technicalTypeName = propulsion.getTechnicalTypeName();
        this.description = propulsion.getDescription(languageCode);
        this.costsPercentage = propulsion.getCostsPercentage();
        this.techLevel = propulsion.getTechLevel();
        this.technologyType = propulsion.getTechnologyType();
        this.hyperBand = propulsion.getHyperBand();
    }

    public int getIdModule() {
        return idModule;
    }
}
