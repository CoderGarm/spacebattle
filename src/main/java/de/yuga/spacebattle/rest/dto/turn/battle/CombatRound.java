package de.yuga.spacebattle.rest.dto.turn.battle;

import com.google.common.base.Preconditions;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;

public class CombatRound {

    /**
     * The battle round number.
     */
    @ApiModelProperty(required = true, value = "The combat round number.")
    private int no;

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
