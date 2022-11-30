package de.yuga.spacebattle.rest.dto.turn.resources;


import com.fasterxml.jackson.annotation.JsonProperty;
import de.yuga.spacebattle.backend.enums.ETransportType;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.List;

@Schema(description = ".")
public class ResourceTransfer {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "Designates the type of the from and the to.")
    private ETransportType transportType;

    @JsonProperty
    @Schema(required = true, description = "The from ID.")
    private int fromId;

    @JsonProperty
    @Schema(required = true, description = "The to ID.")
    private int toId;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The amount of stored resources by their type.")
    private List<ResourceAmount> resources;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The amount of human resources by their type.")
    private List<HumanResourceAmount> humanResources;

    public ResourceTransfer() {
    }

    @Nonnull
    public ETransportType getTransportType() {
        return transportType;
    }

    public int getFromId() {
        return fromId;
    }

    public int getToId() {
        return toId;
    }

    @Nonnull
    public List<ResourceAmount> getResources() {
        return resources;
    }

    @Nonnull
    public List<HumanResourceAmount> getHumanResources() {
        return humanResources;
    }
}
