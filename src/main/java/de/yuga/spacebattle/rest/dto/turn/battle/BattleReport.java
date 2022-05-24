package de.yuga.spacebattle.rest.dto.turn.battle;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.account.UserJson;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.rest.dto.orbitals.FleetOrbit;
import de.yuga.spacebattle.rest.dto.turn.Tick;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Schema(description = ".")
public class BattleReport {

    @Schema(required = true, description = "The database id of the report.")
    private final int idBattleReport;

    @Nonnull
    @Schema(required = true, description = "The tick where the action happened.")
    private final Tick tick;

    @Nonnull
    @Schema(required = true, description = "The last round in this battle.")
    private final CombatRound lastRound;

    /**
     * The place to be.
     */
    @Nonnull
    @Schema(required = true, description = "The place where the action happened.")
    private final FleetOrbit orbit;

    /**
     * The users which has played a role in this battle.
     */
    @Nonnull
    @Schema(required = true, description = "The participating users.")
    private final Set<UserJson> participatingUsers = new HashSet<>();

    @Nonnull
    @Schema(required = true, description = "The losses of this battle.")
    private final List<LossRole> lossRole = new ArrayList<>();

    @Nonnull
    @Schema(required = true, description = "The protagonists - and the antagonists.")
    private final Set<Fleet> participatingFleets = new HashSet<>();

    @Nonnull
    @Schema(required = true, description = "The movements which were done in this clash.")
    private final Set<MovementAction> movementActions = new HashSet<>();

    @Nonnull
    @Schema(required = true, description = "The hits against missile salvos.")
    private final Set<CounterMissileHit> counterMissileHits = new HashSet<>();

    @Nonnull
    @Schema(required = true, description = "All loose off weapon action.")
    private final Set<ReleasedVolley> releasedVolleys = new HashSet<>();

    @Nonnull
    @Schema(required = true, description = "The missile movements during this combat.")
    private final Set<MissileMovement> missileMovements = new HashSet<>();

    @Nonnull
    @Schema(required = true, description = "All hits of ship killer weapons during this combat.")
    private final Set<ShipKillerHit> shipKillerHits = new HashSet<>();

    public BattleReport(@Nonnull final de.yuga.spacebattle.backend.entities.turn.battle.BattleReport battleReport) {
        Preconditions.checkNotNull(battleReport, "battleReport shouldn't be null!");

        this.idBattleReport = battleReport.getId();
        this.lastRound = new CombatRound(battleReport.getLastRound());
        this.tick = new de.yuga.spacebattle.rest.dto.turn.Tick(battleReport.getTick());
        this.orbit = new FleetOrbit(battleReport.getVenue());
        this.lossRole.addAll(battleReport.getLossRole().stream().map(LossRole::new).collect(Collectors.toList()));
        this.participatingUsers.addAll(battleReport.getParticipatingUsers().stream().map(UserJson::new).collect(Collectors.toSet()));
        this.participatingFleets.addAll(battleReport.getParticipatingFleets().stream().map(Fleet::new).collect(Collectors.toList()));
        this.movementActions.addAll(battleReport.getMovementActions().stream().map(MovementAction::new).collect(Collectors.toList()));
        this.counterMissileHits.addAll(battleReport.getCounterMissileHits().stream().map(CounterMissileHit::new).collect(Collectors.toList()));
        this.releasedVolleys.addAll(battleReport.getReleasedVolleys().stream().map(ReleasedVolley::new).collect(Collectors.toList()));
        this.missileMovements.addAll(battleReport.getMissileMovements().stream().map(MissileMovement::new).collect(Collectors.toList()));
        this.shipKillerHits.addAll(battleReport.getShipKillerHits().stream().map(ShipKillerHit::new).collect(Collectors.toList()));
    }

    public int getIdBattleReport() {
        return idBattleReport;
    }

    @Nonnull
    public CombatRound getLastRound() {
        return lastRound;
    }

    @Nonnull
    public Tick getTick() {
        return tick;
    }

    @Nonnull
    public FleetOrbit getOrbit() {
        return orbit;
    }

    @Nonnull
    public Set<UserJson> getParticipatingUsers() {
        return participatingUsers;
    }

    @Nonnull
    public List<LossRole> getLossRole() {
        return lossRole;
    }

    @Nonnull
    public Set<Fleet> getParticipatingFleets() {
        return participatingFleets;
    }

    @Nonnull
    public Set<MovementAction> getMovementActions() {
        return movementActions;
    }

    @Nonnull
    public Set<CounterMissileHit> getCounterMissileHits() {
        return counterMissileHits;
    }

    @Nonnull
    public Set<ReleasedVolley> getReleasedVolleys() {
        return releasedVolleys;
    }

    @Nonnull
    public Set<MissileMovement> getMissileMovements() {
        return missileMovements;
    }

    @Nonnull
    public Set<ShipKillerHit> getShipKillerHits() {
        return shipKillerHits;
    }
}
