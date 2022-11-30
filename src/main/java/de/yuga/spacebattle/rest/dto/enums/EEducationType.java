package de.yuga.spacebattle.rest.dto.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

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

    public EEducationType() {
        super();

        isWorkforce = false;
        isMilitary = false;
    }

    public EEducationType(@Nonnull final de.yuga.spacebattle.backend.enums.EEducationType educationType) {
        super(educationType);

        isWorkforce = educationType.isWorkforce();
        isMilitary = educationType.isMilitary();
    }
}
