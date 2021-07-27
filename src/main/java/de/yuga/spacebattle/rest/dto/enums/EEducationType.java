package de.yuga.spacebattle.rest.dto.enums;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Describes the level of education.
 */
@ApiModel(parent = HasIcon.class)
public class EEducationType extends HasIcon {

    /**
     * Defines if the education level is part of the working people.
     */
    @JsonProperty
    private final boolean isWorkforce;

    /**
     * The requirement of an educational level which must be fulfilled to reach *this* level.
     */
    @Nullable
    private final EEducationType requirement;

    public EEducationType() {
        super();
        isWorkforce = false;
        requirement = null;
    }

    public EEducationType(@Nonnull final de.yuga.spacebattle.backend.enums.EEducationType educationType) {
        super(educationType);

        isWorkforce = educationType.isWorkforce();
        requirement = educationType.getRequirement() != null ? new EEducationType(educationType.getRequirement()) : null;
    }

    @JsonIgnore
    public boolean isWorkforce() {
        return isWorkforce;
    }

    @Nullable
    public EEducationType getRequirement() {
        return requirement;
    }
}
