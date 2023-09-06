package de.yuga.spacebattle.rest.dto.turn.resources.trade;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.resources.trade.TradedResource;
import de.yuga.spacebattle.rest.dto.AbstractId;
import de.yuga.spacebattle.rest.dto.turn.mission.ConvoyRaidActionItem;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = ".")
public class TradeContract {

    @JsonProperty
    @Schema(required = true, description = "The database id.")
    private int idTradedResource;

    @JsonProperty
    @Schema(required = true, description = "If the contract has finished.")
    private boolean isFinished;

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

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The actions to report.")
    private List<ConvoyRaidActionItem> actionItems = new ArrayList<>();

    public TradeContract() {
    }

    public TradeContract(@Nonnull final TradedResource trade) {
        Preconditions.checkNotNull(trade, "trade must not be empty");

        this.idTradedResource = trade.getId();
        this.isFinished = trade.isDeleted();
        if (this.isFinished) {
            this.actionItems = trade.getConvoyProtectionMissionItems().stream().map(ConvoyRaidActionItem::new).collect(Collectors.toList());
        }
        this.offer = new TradeOffer(trade.getTradeOffer());
        this.buyer = new AbstractId(trade.getBuyer(), trade.getBuyer().getUsername());
        this.destination = new AbstractId(trade.getDestination(), trade.getDestination().getName());
    }
}
