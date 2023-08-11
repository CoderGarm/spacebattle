package de.yuga.spacebattle.rest.dto.turn.resources.trade;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.AbstractId;
import de.yuga.spacebattle.rest.dto.orbitals.Orbit;
import de.yuga.spacebattle.rest.dto.turn.resources.ResourceAmount;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Schema(description = ".")
public class TradeOffer {

    @JsonProperty
    @Schema(description = "The offer's id.")
    private Integer idTradeOffer;

    @Nullable
    @JsonProperty
    @Schema(description = "The seller.")
    private AbstractId seller;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The origin planet of the goods.")
    private AbstractId origin;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The origin planets orbit at the universe map.")
    private Orbit originOrbit;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The traded resource amount.")
    private Trade trade;

    public TradeOffer() {
    }

    public TradeOffer(@Nonnull final de.yuga.spacebattle.backend.entities.turn.resources.trade.TradeOffer offer) {
        Preconditions.checkNotNull(offer, "offer must not be empty");

        this.idTradeOffer = offer.getId();
        this.seller = new AbstractId(offer.getSeller(), offer.getSeller().getUsername());
        this.origin = new AbstractId(offer.getOrigin(), offer.getOrigin().getName());
        this.originOrbit = new Orbit(offer.getOrigin().getOrbit());
        this.trade = new Trade(offer);
    }

    public Integer getIdTradeOffer() {
        return idTradeOffer;
    }

    @JsonIgnore
    public int getIdPlanetOrigin() {
        return origin.getId();
    }

    @Nonnull
    @JsonIgnore
    public ResourceAmount getResourceAmount() {
        return trade.getResourceAmount();
    }

    @JsonIgnore
    public long getPricePerUnit() {
        return trade.getPricePerUnit();
    }
}
