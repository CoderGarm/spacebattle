package de.yuga.spacebattle.rest.dto.turn.resources;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.enums.EEducationType;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class HumanResourceAmount {

    @Nonnull
    @Schema(required = true, description = "The resource type.")
    private EEducationType resourceType;

    @Schema(required = true, description = "The amount for the resource.")
    private long amount;

    public HumanResourceAmount() {
    }

    public HumanResourceAmount(@Nonnull final de.yuga.spacebattle.backend.enums.EEducationType eEducationType, final long amount) {
        Preconditions.checkNotNull(eEducationType, "eEducationType shouldn't be null!");

        resourceType = new EEducationType(eEducationType);
        this.amount = amount;
    }

    @Nonnull
    public EEducationType getResourceType() {
        return resourceType;
    }

    @Nonnull
    @JsonIgnore
    public de.yuga.spacebattle.backend.enums.EEducationType getRealEducationType() {
        return de.yuga.spacebattle.backend.enums.EEducationType.valueOf(resourceType.getTypeName());
    }

    @Nonnull
    public Long getAmount() {
        return amount;
    }
}
