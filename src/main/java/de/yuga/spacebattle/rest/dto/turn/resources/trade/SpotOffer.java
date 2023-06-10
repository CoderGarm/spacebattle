package de.yuga.spacebattle.rest.dto.turn.resources.trade;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.yuga.spacebattle.rest.dto.turn.resources.ResourceAmount;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class SpotOffer {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The resource amount.")
    private ResourceAmount resourceAmount;

    @JsonProperty
    @Schema(required = true, description = "The planet's id.")
    private int idPlanet;

    public SpotOffer() {
    }

    @Nonnull
    public ResourceAmount getResourceAmount() {
        return resourceAmount;
    }

    public int getIdPlanet() {
        return idPlanet;
    }
}
