package de.yuga.spacebattle.rest.dto.spacecrafts.modules.basics;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.ETechLevel;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class HasNamedTechLevel {

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

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The level of this module.")
    private ETechLevel techLevel;

    protected HasNamedTechLevel() {
    }

    public HasNamedTechLevel(@Nonnull final de.yuga.spacebattle.backend.entities.misc.HasNamedTechLevel hasNamedTechLevel,
                             @Nonnull final String languageCode) {
        Preconditions.checkNotNull(hasNamedTechLevel, "hasNamedTechLevel shouldn't be null!");
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");

        this.name = hasNamedTechLevel.getName(languageCode);
        this.technicalTypeName = hasNamedTechLevel.getTechnicalTypeName();
        this.description = hasNamedTechLevel.getDescription(languageCode);
        this.techLevel = hasNamedTechLevel.getTechLevel();
    }
}
