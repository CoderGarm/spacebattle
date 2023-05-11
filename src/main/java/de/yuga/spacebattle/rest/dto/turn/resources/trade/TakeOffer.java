package de.yuga.spacebattle.rest.dto.turn.resources.trade;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = ".")
public class TakeOffer {

    @JsonProperty
    @Schema(required = true, description = "The offer's id.")
    private int idTradeOffer;

    @JsonProperty
    @Schema(required = true, description = "The destination's planet id.")
    private int idDestination;

    public TakeOffer() {
    }

    public Integer getIdTradeOffer() {
        return idTradeOffer;
    }

    public int getIdDestination() {
        return idDestination;
    }
}
