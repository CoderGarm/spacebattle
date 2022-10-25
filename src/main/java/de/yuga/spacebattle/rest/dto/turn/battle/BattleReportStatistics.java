package de.yuga.spacebattle.rest.dto.turn.battle;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.orbitals.FleetOrbit;
import de.yuga.spacebattle.rest.dto.turn.Tick;
import de.yuga.spacebattle.rest.dto.turn.battle.combat.CombatRound;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class BattleReportStatistics {

    @JsonProperty
    @Schema(required = true, description = "The database id of the report.")
    private int idBattleReport;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The tick where the action happened.")
    private Tick tick;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The last round in this battle.")
    private CombatRound lastRound;

    /**
     * The place to be.
     */
    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The place where the action happened.")
    private FleetOrbit orbit;

    public BattleReportStatistics() {
    }

    public BattleReportStatistics(@Nonnull final de.yuga.spacebattle.backend.entities.turn.battle.BattleReport battleReport) {
        Preconditions.checkNotNull(battleReport, "battleReport shouldn't be null!");

        this.idBattleReport = battleReport.getId();
        this.lastRound = new CombatRound(battleReport.getLastRound());
        this.tick = new Tick(battleReport.getTick());
        this.orbit = new FleetOrbit(battleReport.getVenue());
    }

    public BattleReportStatistics(final int idBattleReport, @Nonnull final Tick tick, @Nonnull final CombatRound lastRound, @Nonnull final FleetOrbit orbit) {
        this.idBattleReport = idBattleReport;
        this.tick = tick;
        this.lastRound = lastRound;
        this.orbit = orbit;
    }
}
