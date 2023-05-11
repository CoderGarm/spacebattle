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
    @Schema(required = true, description = "The price for the traded amount.")
    private long price;

    public Trade() {
    }

    public Trade(final long price, @Nonnull final de.yuga.spacebattle.backend.enums.EResourceType realResourceType, final long amount) {
        Preconditions.checkNotNull(realResourceType, "resourceType shouldn't be null!");

        this.resourceAmount = new ResourceAmount(realResourceType, amount);
        this.price = price;
    }

    public Trade(@Nonnull final TradeOffer offer) {
        Preconditions.checkNotNull(offer, "offer must not be empty");

        this.resourceAmount = new ResourceAmount(offer.getResourceType(), offer.getAmount());
        this.price = offer.getPrice();
    }

    public Trade(@Nonnull final TradedResource tradedResource) {
        Preconditions.checkNotNull(tradedResource, "tradedResource must not be empty");

        this.resourceAmount = new ResourceAmount(tradedResource.getResourceType(), tradedResource.getAmount());
        this.price = tradedResource.getPrice();
    }

    @Nonnull
    public ResourceAmount getResourceAmount() {
        return resourceAmount;
    }

    public long getPrice() {
        return price;
    }
}
