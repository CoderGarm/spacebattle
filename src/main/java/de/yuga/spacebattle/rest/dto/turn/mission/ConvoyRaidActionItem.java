package de.yuga.spacebattle.rest.dto.turn.mission;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.mission.ConvoyProtectionMissionItem;
import de.yuga.spacebattle.backend.enums.EMissionAction;
import de.yuga.spacebattle.rest.dto.turn.Tick;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class ConvoyRaidActionItem {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "When the action happened.")
    private Tick happenedAt;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "In which phase of the mission the action happened.")
    private EMissionAction affectedPhase;

    @JsonProperty
    @Schema(required = true, description = "How much was stolen from the left over trade.")
    private int percentOfCargoLost = 0;

    @JsonProperty
    @Schema(required = true, description = "If ransom was payed or if the loss happened by destruction.")
    private boolean isRansomPayment = false;

    @JsonProperty
    @Schema(required = true, description = "If the pirates withdrawn early.")
    private boolean piratedWithdraw = false;

    @JsonProperty
    @Schema(required = true, description = "If the pirates withdrawn after scanning the guards.")
    private boolean piratedWithdrawAfterApproach = false;

    public ConvoyRaidActionItem(@Nonnull final ConvoyProtectionMissionItem item) {
        Preconditions.checkNotNull(item, "item must not be empty");

        this.happenedAt = new Tick(item.getCreatedAt());
        this.affectedPhase = item.getPhase();
        this.percentOfCargoLost = item.getPercentOfCargoLost();
        this.affectedPhase = item.getPhase();
        this.isRansomPayment = item.isRansomPayment();
        this.piratedWithdraw = item.isPiratedWithdraw();
        this.piratedWithdrawAfterApproach = item.isPiratedWithdrawAfterApproach();
    }
}
