package de.yuga.spacebattle.rest.dto.turn.battle.combat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.ECombatPhase;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Schema(description = ".")
public class CombatRoundKey {

    @JsonProperty
    @Schema(required = true, description = "The id of the parent.")
    private int id;

    /**
     * The current combat round.<br>
     * A volley of direct weapons will hit in the same weapon.
     */
    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The combat round in which this action happens.")
    private CombatRound combatRound;

    /**
     * The current phase.
     */
    @Nullable
    @JsonProperty
    @Schema(required = true, description = "The combat phase in which this action happens.")
    private ECombatPhase.ECombatSubPhase combatPhase;


    public CombatRoundKey(final int id,
                          @Nonnull final de.yuga.spacebattle.backend.combat.round.CombatRound combatRound,
                          @Nonnull final ECombatPhase.ECombatSubPhase combatPhase) {
        Preconditions.checkNotNull(combatRound, "combatRound shouldn't be null!");
        Preconditions.checkNotNull(combatPhase, "combatPhase shouldn't be null!");

        this.id = id;
        this.combatRound = new CombatRound(combatRound);
        this.combatPhase = combatPhase;
    }

    public CombatRoundKey() {
    }

    @Nonnull
    public CombatRound getCombatRound() {
        return combatRound;
    }
}
