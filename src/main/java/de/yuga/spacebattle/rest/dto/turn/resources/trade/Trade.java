package de.yuga.spacebattle.rest.dto.turn.resources.trade;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.resources.trade.TradeOffer;
import de.yuga.spacebattle.backend.entities.turn.resources.trade.TradedResource;
import de.yuga.spacebattle.rest.dto.turn.resources.ResourceAmount;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class Trade {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The traded resource amount.")
    private ResourceAmount resourceAmount;

    @JsonProperty
    @Schema(required = true, description = "The price per unit of the traded resource.")
    private long pricePerUnit;

    public Trade() {
    }

    public Trade(final long pricePerUnit, @Nonnull final de.yuga.spacebattle.backend.enums.EResourceType realResourceType, final long amount) {
        Preconditions.checkNotNull(realResourceType, "resourceType shouldn't be null!");

        this.resourceAmount = new ResourceAmount(realResourceType, amount);
        this.pricePerUnit = pricePerUnit;
    }

    public Trade(@Nonnull final TradeOffer offer) {
        Preconditions.checkNotNull(offer, "offer must not be empty");

        this.resourceAmount = new ResourceAmount(offer.getResourceType(), offer.getAmount());
        this.pricePerUnit = offer.getUnitPrice();
    }

    public Trade(@Nonnull final TradedResource tradedResource) {
        Preconditions.checkNotNull(tradedResource, "tradedResource must not be empty");

        this.resourceAmount = new ResourceAmount(tradedResource.getTradeOffer().getResourceType(), tradedResource.getTradeOffer().getAmount());
        this.pricePerUnit = tradedResource.getTradeOffer().getUnitPrice();
    }

    @Nonnull
    public ResourceAmount getResourceAmount() {
        return resourceAmount;
    }

    public long getPricePerUnit() {
        return pricePerUnit;
    }
}
