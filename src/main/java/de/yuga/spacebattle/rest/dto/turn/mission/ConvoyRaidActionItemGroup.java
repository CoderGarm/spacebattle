package de.yuga.spacebattle.rest.dto.turn.mission;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.resources.trade.TradedResource;
import de.yuga.spacebattle.backend.enums.EMissionType;
import de.yuga.spacebattle.rest.dto.turn.resources.trade.TradeContract;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = ".")
public class ConvoyRaidActionItemGroup {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "What happens.")
    private final EMissionType missionType = EMissionType.CONVOY_PROTECTION;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The affected contract.")
    private TradeContract affectedTrade;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The actions to report.")
    private List<ConvoyRaidActionItem> actionItems = new ArrayList<>();


    public ConvoyRaidActionItemGroup(@Nonnull final TradedResource tradedResource) {
        Preconditions.checkNotNull(tradedResource, "tradedResource must not be empty");

        this.affectedTrade = new TradeContract(tradedResource);
        this.actionItems = tradedResource.getConvoyProtectionMissionItems().stream().map(ConvoyRaidActionItem::new).collect(Collectors.toList());
    }
}
