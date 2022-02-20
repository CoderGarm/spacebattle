package de.yuga.spacebattle.backend.entities.turn.battle.combat;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.converter.CombatRoundConverter;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.enums.ECombatPhase;

import javax.annotation.Nonnull;
import javax.persistence.Convert;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

@MappedSuperclass
public class CombatRoundKey extends AbstractEntityKey {

    /**
     * The current combat round.<br>
     * A volley of direct weapons will hit in the same weapon.
     */
    @NotNull
    @Nonnull
    @Convert(converter = CombatRoundConverter.class)
    private CombatRound combatRound;

    /**
     * The current phase.
     */
    @NotNull
    @Nonnull
    @Enumerated(EnumType.STRING)
    private ECombatPhase.ECombatSubPhase combatPhase;

    public CombatRoundKey(@Nonnull final CombatRound combatRound,
                          @Nonnull final ECombatPhase.ECombatSubPhase combatPhase) {
        Preconditions.checkNotNull(combatRound, "combatRound shouldn't be null!");
        Preconditions.checkNotNull(combatPhase, "combatPhase shouldn't be null!");

        this.combatRound = combatRound;
        this.combatPhase = combatPhase;
    }

    public CombatRoundKey() {
    }

    @Nonnull
    public CombatRound getCombatRound() {
        return combatRound;
    }

    @Nonnull
    public ECombatPhase.ECombatSubPhase getCombatPhase() {
        return combatPhase;
    }
}
