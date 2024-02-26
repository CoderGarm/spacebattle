package de.yuga.spacebattle.rest.dto.turn.battle.combat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class CombatRoundKey {

    @JsonProperty
    @Schema(description = "The id of the parent.")
    private Integer id;

    /**
     * The current combat round.<br>
     * A volley of direct weapons will hit in the same weapon.
     */
    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The combat round in which this action happens.")
    private CombatRound combatRound;


    public CombatRoundKey(@Nonnull final de.yuga.spacebattle.backend.combat.round.CombatRound combatRound) {
        this.combatRound = new CombatRound(Preconditions.checkNotNull(combatRound, "combatRound must not be empty"));
    }

    public CombatRoundKey(final int id, @Nonnull final de.yuga.spacebattle.backend.combat.round.CombatRound combatRound) {
        this(combatRound);

        this.id = id;
    }

    public CombatRoundKey() {
    }

    @Nonnull
    public CombatRound getCombatRound() {
        return combatRound;
    }
}
