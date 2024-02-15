package de.yuga.spacebattle.backend.combat.dto;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.combat.round.WarshipHealthState;
import de.yuga.spacebattle.backend.enums.EHitArea;

import javax.annotation.Nonnull;
import java.util.Objects;

public class HitLog {

    /**
     * The damage dealer.
     */
    @Nonnull
    private final DamageDealer damageDealer;

    /**
     * The attacked ship and its state.
     */
    @Nonnull
    private final WarshipHealthState targetHealthState; // fixme replace by smaller and more specialized dto

    private final long damageValue;

    private final int state;

    @Nonnull
    private final EHitArea attackedPart;

    private final boolean isAlive;

    private final boolean isFightingCapable;

    public HitLog(@Nonnull final DamageDealer damageDealer,
                  @Nonnull final WarshipHealthState targetHealthState,
                  final long damageValue,
                  final int state,
                  @Nonnull final EHitArea attackedPart,
                  final boolean isAlive,
                  final boolean isFightingCapable) {
        Preconditions.checkNotNull(damageDealer, "damageDealer must not be empty");
        Preconditions.checkNotNull(targetHealthState, "targetHealthState shouldn't be null!");
        Preconditions.checkNotNull(attackedPart, "attackedPart shouldn't be null!");

        this.damageDealer = damageDealer;
        this.targetHealthState = targetHealthState.clone();
        this.damageValue = damageValue;
        this.state = state;
        this.attackedPart = attackedPart;
        this.isAlive = isAlive;
        this.isFightingCapable = isFightingCapable;
    }

    @Nonnull
    public DamageDealer getDamageDealer() {
        return damageDealer;
    }

    @Nonnull
    public WarshipHealthState getTargetHealthState() {
        return targetHealthState;
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
    public String toString() {

        final boolean idBeamVolley = damageDealer instanceof BeamVolley;

        return damageDealer.getUuid() + " applied " + damageValue + " " + (idBeamVolley ? "beam" : "missile") + " damage to " + attackedPart + " (" + state + " left)"
                + " to " + targetHealthState.getWarShip().getName() + " of " + targetHealthState.getWarShip().getShipClass().getOwner().getUsername() + "."
                + " Ship is " + (isAlive ? "alive" : "dead") + " and " + (isFightingCapable ? "active" : "finished");
    }

    @Nonnull
    public CombatRound getCombatRound() {
        CombatRound round = null;
        if (damageDealer instanceof BeamVolley) {
            round = ((BeamVolley) damageDealer).getCombatRound();
        }
        if (damageDealer instanceof MissileSalvo) {
            round = ((MissileSalvo) damageDealer).getCombatRound();
        }
        return Objects.requireNonNull(round, "Hell no, this will not happen!");
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;
        // Object.equals() only! Even the identical combination of data is possible
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        // Object.hashCode() only! Even the identical combination of data is possible
        return super.hashCode();
    }
}
