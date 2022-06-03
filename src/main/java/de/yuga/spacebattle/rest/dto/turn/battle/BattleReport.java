package de.yuga.spacebattle.rest.dto.turn.battle;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.account.UserJson;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.Fleet;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Schema(description = ".")
public class BattleReport {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The basic info about the battle.")
    private final BattleReportStatistics battleReportStatistics;

    /**
     * The users which has played a role in this battle.
     */
    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The participating users.")
    private final Set<UserJson> participatingUsers = new HashSet<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The protagonists - and the antagonists.")
    private final Set<Fleet> participatingFleets = new HashSet<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The losses of this battle.")
    private final List<LossRole> lossRole = new ArrayList<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The movements which were done in this clash.")
    private final Set<MovementAction> movementActions = new HashSet<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The hits against missile salvos.")
    private final Set<CounterMissileHit> counterMissileHits = new HashSet<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "All loose off weapon action.")
    private final Set<ReleasedVolley> releasedVolleys = new HashSet<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The missile movements during this combat.")
    private final Set<MissileMovement> missileMovements = new HashSet<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "All hits of ship killer weapons during this combat.")
    private final Set<ShipKillerHit> shipKillerHits = new HashSet<>();

    public BattleReport(@Nonnull final de.yuga.spacebattle.backend.entities.turn.battle.BattleReport battleReport) {
        Preconditions.checkNotNull(battleReport, "battleReport shouldn't be null!");

        this.battleReportStatistics = new BattleReportStatistics(battleReport);
        this.participatingUsers.addAll(battleReport.getParticipatingUsers().stream().map(UserJson::new).collect(Collectors.toSet()));
        this.participatingFleets.addAll(battleReport.getParticipatingFleets().stream().map(Fleet::new).collect(Collectors.toList()));
        this.lossRole.addAll(battleReport.getLossRole().stream().map(LossRole::new).collect(Collectors.toList()));
        this.movementActions.addAll(battleReport.getMovementActions().stream().map(MovementAction::new).collect(Collectors.toList()));
        this.counterMissileHits.addAll(battleReport.getCounterMissileHits().stream().map(CounterMissileHit::new).collect(Collectors.toList()));
        this.releasedVolleys.addAll(battleReport.getReleasedVolleys().stream().map(ReleasedVolley::new).collect(Collectors.toList()));
        this.missileMovements.addAll(battleReport.getMissileMovements().stream().map(MissileMovement::new).collect(Collectors.toList()));
        this.shipKillerHits.addAll(battleReport.getShipKillerHits().stream().map(ShipKillerHit::new).collect(Collectors.toList()));
    }
}
