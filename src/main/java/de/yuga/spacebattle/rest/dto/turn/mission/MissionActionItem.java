package de.yuga.spacebattle.rest.dto.turn.mission;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.turn.mission.MissionItem;
import de.yuga.spacebattle.backend.enums.EMissionAction;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.Fleet;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Schema(description = ".")
public class MissionActionItem {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The other guy.")
    private Fleet opponent;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The action to report.")
    private EMissionAction eMissionAction;

    @JsonProperty
    @Schema(required = true, description = "If the user was defeated by the other.")
    private boolean userDefeated = false;

    @Nullable
    @JsonProperty
    @Schema(required = true, description = "In case of a combat the result is noted in:")
    private Integer idBattleReport;

    public MissionActionItem(@Nonnull final MissionItem item, @Nonnull final String preferredLanguage) {
        Preconditions.checkNotNull(item, "item must not be empty");
        Preconditions.checkNotNull(preferredLanguage, "preferredLanguage must not be empty");

        this.opponent = new Fleet(item.getPirateFleet(), preferredLanguage);
        this.eMissionAction = item.getEMissionAction();
        this.userDefeated = item.isUserDefeated();
        this.idBattleReport = item.getBattleReport() != null ? item.getBattleReport().getId() : null;
    }
}
