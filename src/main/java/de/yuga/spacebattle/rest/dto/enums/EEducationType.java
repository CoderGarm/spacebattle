package de.yuga.spacebattle.rest.dto.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Describes the level of education.
 */
@Schema(description = ".")
public class EEducationType extends HasIcon {

    /**
     * Defines if the education level is part of the working people.
     */
    @JsonProperty
    @Schema(required = true, description = "If this education type is part of the workforce.")
    private final boolean isWorkforce;

    @JsonProperty
    @Schema(required = true, description = "If this education type is part of the military.")
    private final boolean isMilitary;

    /**
     * The requirement of an educational level which must be fulfilled to reach *this* level.
     */
    @Nullable
    @JsonProperty
    private final EEducationType requirement;

    public EEducationType() {
        super();

        isWorkforce = false;
        isMilitary = false;
        requirement = null;
    }

    public EEducationType(@Nonnull final de.yuga.spacebattle.backend.enums.EEducationType educationType) {
        super(educationType);

        isWorkforce = educationType.isWorkforce();
        isMilitary = educationType.isMilitary();
        requirement = educationType.getRequirement() != null ? new EEducationType(educationType.getRequirement()) : null;
    }
}
