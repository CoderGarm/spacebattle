package de.yuga.spacebattle.rest.dto.turn.battle;


import com.fasterxml.jackson.annotation.JsonProperty;
import de.yuga.spacebattle.backend.enums.ECalculationType;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Schema(description = ".")
public class ChangeSharedBattleReport {

    @JsonProperty
    @Schema(required = true, description = ".")
    private int idBattleReport;

    @Nullable
    @JsonProperty
    @Schema(description = ".")
    private Integer sharedWithAlliance;

    @Nullable
    @JsonProperty
    @Schema(description = ".")
    private Integer sharedWithUser;

    @Nullable
    @JsonProperty
    @Schema(description = ".")
    private Boolean shareWithEveryone;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "What should be done")
    private ECalculationType calculationType;

    public ChangeSharedBattleReport() {
    }


    public int getIdBattleReport() {
        return idBattleReport;
    }

    @Nullable
    public Integer getSharedWithAlliance() {
        return sharedWithAlliance;
    }

    @Nullable
    public Integer getSharedWithUser() {
        return sharedWithUser;
    }

    @Nullable
    public Boolean getShareWithEveryone() {
        return shareWithEveryone;
    }

    @Nonnull
    public ECalculationType getCalculationType() {
        return calculationType;
    }
}
