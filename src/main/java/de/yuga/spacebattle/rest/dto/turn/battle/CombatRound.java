package de.yuga.spacebattle.rest.dto.turn.battle;

import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class CombatRound {

    /**
     * The battle round number.
     */
    @Schema(required = true, description = "The combat round number.")
    private int no;

    public CombatRound() {
    }

    public CombatRound(@Nonnull final de.yuga.spacebattle.backend.combat.round.CombatRound combatRound) {
        Preconditions.checkNotNull(combatRound, "combatRound shouldn't be null!");

        this.no = combatRound.getNo();
    }

    public int getNo() {
        return no;
    }

    public void setNo(final int no) {
        this.no = no;
    }
}
