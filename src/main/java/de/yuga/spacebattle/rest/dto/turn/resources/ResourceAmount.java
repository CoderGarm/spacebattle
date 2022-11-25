package de.yuga.spacebattle.rest.dto.turn.resources;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.enums.EResourceType;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class ResourceAmount {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The resource type.")
    private EResourceType resourceType;

    @JsonProperty
    @Schema(required = true, description = "The amount for the resource.")
    private long amount;

    public ResourceAmount() {
    }

    public ResourceAmount(@Nonnull final de.yuga.spacebattle.backend.enums.EResourceType realResourceType, final long amount) {
        Preconditions.checkNotNull(realResourceType, "resourceType shouldn't be null!");

        resourceType = new EResourceType(realResourceType);
        this.amount = amount;
    }

    @Nonnull
    @JsonIgnore
    public de.yuga.spacebattle.backend.enums.EResourceType getRealResourceType() {
        return de.yuga.spacebattle.backend.enums.EResourceType.valueOf(resourceType.getTypeName());
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
