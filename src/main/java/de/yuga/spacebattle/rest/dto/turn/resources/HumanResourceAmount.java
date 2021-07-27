package de.yuga.spacebattle.rest.dto.turn.resources;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.enums.EEducationType;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;

public class HumanResourceAmount {

    @Nonnull
    @ApiModelProperty(required = true, value = "The resource type.")
    private final EEducationType resourceType;

    @ApiModelProperty(required = true, value = "The amount for the resource.")
    private final long amount;

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
    public Long getAmount() {
        return amount;
    }
}
