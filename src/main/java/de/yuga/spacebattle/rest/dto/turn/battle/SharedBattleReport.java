package de.yuga.spacebattle.rest.dto.turn.battle;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.AbstractId;
import de.yuga.spacebattle.rest.dto.combined.account.Alliance;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Schema(description = ".")
public class SharedBattleReport {

    @JsonProperty
    @Schema(required = true, description = ".")
    private int idBattleReport;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = ".")
    private final Set<AbstractId> participatingUsers = new HashSet<>();

    @Nullable
    @JsonProperty
    @Schema(required = true, description = ".")
    private final Set<de.yuga.spacebattle.rest.dto.combined.account.Alliance> sharedWithAlliance = new HashSet<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = ".")
    private final Set<AbstractId> sharedWithUsers = new HashSet<>();

    @JsonProperty
    @Schema(required = true, description = ".")
    private boolean shareWithEveryone = false;

    public SharedBattleReport() {
    }


    public SharedBattleReport(@Nonnull final de.yuga.spacebattle.backend.entities.turn.battle.SharedBattleReport sharedReport, final int idBattleReport) {
        Preconditions.checkNotNull(sharedReport, "sharedReport must not be empty");

        this.idBattleReport = idBattleReport;
        this.participatingUsers.addAll(sharedReport.getParticipatingUsers().stream().map(u -> new AbstractId(u, u.getUsername())).collect(Collectors.toSet()));
        this.sharedWithAlliance.addAll(sharedReport.getSharedWithAlliances().stream().map(Alliance::new).collect(Collectors.toSet()));
        this.sharedWithUsers.addAll(sharedReport.getSharedWithUsers().stream().map(u -> new AbstractId(u, u.getUsername())).collect(Collectors.toSet()));
        this.shareWithEveryone = sharedReport.isShareWithEveryone();
    }
}
