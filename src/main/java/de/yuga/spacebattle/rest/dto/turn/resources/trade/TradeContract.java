package de.yuga.spacebattle.rest.dto.turn.resources.trade;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.resources.trade.TradedResource;
import de.yuga.spacebattle.rest.dto.AbstractId;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class TradeContract {

    @JsonProperty
    @Schema(required = true, description = "The database id.")
    private int idTradedResource;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The accepted offer.")
    private TradeOffer offer;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The buyer's id.")
    private AbstractId buyer;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The destination planet of the good.")
    private AbstractId destination;

    public TradeContract() {
    }

    public TradeContract(@Nonnull final TradedResource trade) {
        Preconditions.checkNotNull(trade, "trade must not be empty");

        this.idTradedResource = trade.getId();
        this.offer = new TradeOffer(trade.getTradeOffer());
        this.buyer = new AbstractId(trade.getBuyer(), trade.getBuyer().getUsername());
        this.destination = new AbstractId(trade.getDestination(), trade.getDestination().getName());
    }
}
