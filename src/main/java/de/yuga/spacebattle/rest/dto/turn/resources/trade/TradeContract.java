package de.yuga.spacebattle.rest.dto.turn.resources.trade;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.resources.trade.TradedResource;
import de.yuga.spacebattle.rest.dto.AbstractId;
import de.yuga.spacebattle.rest.dto.turn.resources.ResourceAmount;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class TradeContract {

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

        this.offer = new TradeOffer(trade.getTradeOffer());
        this.buyer = new AbstractId(trade.getBuyer(), trade.getBuyer().getUsername());
        this.destination = new AbstractId(trade.getDestination(), trade.getDestination().getName());
    }

    @Nonnull
    @JsonIgnore
    public TradeOffer getOffer() {
        return offer;
    }

    @JsonIgnore
    public int getIdBuyer() {
        return buyer.getId();
    }

    @JsonIgnore
    public int getIdPlanetDestination() {
        return destination.getId();
    }

    @Nonnull
    @JsonIgnore
    public ResourceAmount getResourceAmount() {
        return offer.getResourceAmount();
    }

    @JsonIgnore
    public long getPrice() {
        return offer.getPricePerUnit();
    }
}
