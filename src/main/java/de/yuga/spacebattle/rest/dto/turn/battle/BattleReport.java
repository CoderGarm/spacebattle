package de.yuga.spacebattle.rest.dto.turn.battle;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.account.Player;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.rest.dto.turn.battle.combat.*;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

@Schema(description = ".")
public class BattleReport {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The basic info about the battle.")
    private BattleReportStatistics battleReportStatistics;

    /**
     * The users which has played a role in this battle.
     */
    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The participating users.")
    private final Set<Player> participatingUsers = new HashSet<>();

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
    private final List<MovementAction> movementActions = new ArrayList<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The maneuvers which were done in this clash.")
    private final List<Maneuver> maneuvers = new ArrayList<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The hits against missile salvos.")
    private final List<CounterMissileHit> counterMissileHits = new ArrayList<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "All loose off weapon action.")
    private final List<ReleasedVolley> releasedVolleys = new ArrayList<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The missile movements during this combat.")
    private final List<MissileMovement> missileMovements = new ArrayList<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "All hits of ship killer weapons during this combat.")
    private final List<ShipKillerHit> shipKillerHits = new ArrayList<>();

    public BattleReport() {
    }

    public BattleReport(@Nonnull final de.yuga.spacebattle.backend.entities.turn.battle.BattleReport battleReport,
                        @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(battleReport, "battleReport shouldn't be null!");

        this.battleReportStatistics = new BattleReportStatistics(battleReport);
        this.participatingUsers.addAll(battleReport.getParticipatingUsers().stream().map(Player::new).collect(Collectors.toSet()));
        this.participatingFleets.addAll(battleReport.getParticipatingFleets().stream().map(f -> new Fleet(f, languageCode)).collect(Collectors.toList()));
        this.lossRole.addAll(battleReport.getLossRole().stream().map(l -> new LossRole(l, languageCode)).collect(Collectors.toList()));
        this.movementActions.addAll(battleReport.getMovementActions().stream().map(m -> new MovementAction(m, languageCode, battleReport.getParticipatingFleets()))
                .sorted(Comparator.comparingInt(o -> o.getCombatRoundKey().getCombatRound().getNo()))
                .collect(Collectors.toList()));
        this.maneuvers.addAll(battleReport.getManeuvers().stream().map(m -> new Maneuver(m, battleReport.getParticipatingFleets())).collect(Collectors.toList()));
        this.counterMissileHits.addAll(battleReport.getCounterMissileHits().stream().map(c -> new CounterMissileHit(c, languageCode)).collect(Collectors.toList()));
        this.releasedVolleys.addAll(battleReport.getReleasedVolleys().stream().map(r -> new ReleasedVolley(r, languageCode)).collect(Collectors.toList()));
        this.missileMovements.addAll(battleReport.getMissileMovements().stream().map(m -> new MissileMovement(m, languageCode)).collect(Collectors.toList()));
        this.shipKillerHits.addAll(battleReport.getShipKillerHits().stream().map(s -> new ShipKillerHit(s, languageCode)).collect(Collectors.toList()));
    }
}
