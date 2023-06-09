package de.yuga.spacebattle.rest.dto.turn.resources.trade;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.yuga.spacebattle.rest.dto.turn.resources.ResourceAmount;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class TakeSpotOffer {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The traded resource amount.")
    private ResourceAmount resourceAmount;

    @JsonProperty
    @Schema(required = true, description = "The destination's planet id.")
    private int idDestination;

    public TakeSpotOffer() {
    }

    @Nonnull
    public ResourceAmount getResourceAmount() {
        return resourceAmount;
    }

    public int getIdDestination() {
        return idDestination;
    }
}
