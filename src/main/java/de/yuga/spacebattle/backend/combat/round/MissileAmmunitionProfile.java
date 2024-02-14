package de.yuga.spacebattle.backend.combat.round;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.ECombatPhase;

import javax.annotation.Nonnull;

public class MissileAmmunitionProfile implements Comparable<MissileAmmunitionProfile> {

    @Nonnull
    private final CombatRound combatRound;

    @Nonnull
    private final ECombatPhase.ECombatSubPhase combatSubPhase;

    @Nonnull
    private final MissileAmmunitionState ammunitionState = new MissileAmmunitionState();

    public MissileAmmunitionProfile(@Nonnull final CombatRound combatRound,
                                    @Nonnull final ECombatPhase.ECombatSubPhase combatSubPhase) {
        this.combatRound = Preconditions.checkNotNull(combatRound, "combatRound must not be empty");
        this.combatSubPhase = Preconditions.checkNotNull(combatSubPhase, "combatSubPhase must not be empty");
    }

    @Nonnull
    public CombatRound getCombatRound() {
        return combatRound;
    }

    @Nonnull
    public ECombatPhase.ECombatSubPhase getCombatSubPhase() {
        return combatSubPhase;
    }

    @Nonnull
    public MissileAmmunitionState getAmmunitionState() {
        return ammunitionState;
    }

    @Override
    public int compareTo(@Nonnull final MissileAmmunitionProfile o) {
        Preconditions.checkNotNull(o, "o must not be empty");

        return combatRound.compareTo(o.combatRound);
    }
}
