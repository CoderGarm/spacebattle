package de.yuga.spacebattle.rest.dto.turn.resources;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.enums.EResourceType;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;

public class ResourceAmount {

    @Nonnull
    @JsonIgnore
    private final de.yuga.spacebattle.backend.enums.EResourceType realResourceType;

    @Nonnull
    @ApiModelProperty(required = true, value = "The resource type.")
    private final EResourceType resourceType;

    @ApiModelProperty(required = true, value = "The amount for the resource.")
    private final long amount;

    public ResourceAmount(@Nonnull final de.yuga.spacebattle.backend.enums.EResourceType realResourceType, final long amount) {
        Preconditions.checkNotNull(realResourceType, "resourceType shouldn't be null!");

        this.realResourceType = realResourceType;
        resourceType = new EResourceType(realResourceType);
        this.amount = amount;
    }

    @Nonnull
    @JsonIgnore
    public de.yuga.spacebattle.backend.enums.EResourceType getRealResourceType() {
        return realResourceType;
    }

    @Nonnull
    public EResourceType getResourceType() {
        return resourceType;
    }

    @Nonnull
    public Long getAmount() {
        return amount;
    }
}
