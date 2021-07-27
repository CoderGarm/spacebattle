package de.yuga.spacebattle.backend.combat.dto;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.round.WarshipHealthState;
import de.yuga.spacebattle.backend.enums.EHitArea;

import javax.annotation.Nonnull;

public class HitLog extends Historizable<HitLog> implements Cloneable {

    @Nonnull
    private Historizable<? extends Cloneable> damageDealer;

    @Nonnull
    private WarshipHealthState warshipHealthState;

    private final long damageValue;

    private final int state;

    @Nonnull
    private final EHitArea attackedPart;

    private final boolean isAlive;

    private final boolean isFightingCapable;

    public HitLog(@Nonnull final Historizable<? extends Cloneable> damageDealer,
                  @Nonnull final WarshipHealthState warshipHealthState,
                  final long damageValue,
                  final int state,
                  @Nonnull final EHitArea attackedPart,
                  final boolean isAlive,
                  final boolean isFightingCapable) {
        Preconditions.checkNotNull(warshipHealthState, "warshipHealthState shouldn't be null!");
        Preconditions.checkNotNull(attackedPart, "attackedPart shouldn't be null!");

        this.damageDealer = damageDealer instanceof BeamVolley ? ((BeamVolley) damageDealer).clone() : ((MissileSalvo) damageDealer).clone();
        this.warshipHealthState = warshipHealthState.clone();
        this.damageValue = damageValue;
        this.state = state;
        this.attackedPart = attackedPart;
        this.isAlive = isAlive;
        this.isFightingCapable = isFightingCapable;
    }

    @Nonnull
    public Historizable<? extends Cloneable> getDamageDealer() {
        return damageDealer;
    }

    @Nonnull
    public WarshipHealthState getWarshipHealthState() {
        return warshipHealthState;
    }

    public long getDamageValue() {
        return damageValue;
    }

    public int getState() {
        return state;
    }

    @Nonnull
    public EHitArea getAttackedPart() {
        return attackedPart;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public boolean isFightingCapable() {
        return isFightingCapable;
    }

    @Override
    public HitLog clone() {
        final HitLog clone = (HitLog) super.clone();
        clone.damageDealer = damageDealer instanceof BeamVolley ? ((BeamVolley) damageDealer).clone() : ((MissileSalvo) damageDealer).clone();
        clone.warshipHealthState = warshipHealthState.clone();
        return clone;
    }
}
