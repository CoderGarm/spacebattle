package de.yuga.spacebattle.backend.combat.round;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

public class BeamState implements Cloneable {

    @Nonnull
    private final WarShip actor;

    @Nonnull
    private final WarShip target;

    private final long damageValue;

    private final BigDecimal chanceToHit;

    public BeamState(@Nonnull final WarShip actor, @Nonnull final WarShip target, final long damageValue, @Nonnull final BigDecimal chanceToHit) {
        Preconditions.checkNotNull(actor, "actor shouldn't be null!");
        Preconditions.checkNotNull(target, "target shouldn't be null!");
        Preconditions.checkNotNull(chanceToHit, "chanceToHit shouldn't be null!");

        this.actor = actor;
        this.target = target;
        this.damageValue = damageValue;
        this.chanceToHit = chanceToHit;
    }

    @Nonnull
    public WarShip getActor() {
        return actor;
    }

    @Nonnull
    public WarShip getTarget() {
        return target;
    }

    public long getDamageValue() {
        return damageValue;
    }

    public BigDecimal getChanceToHit() {
        return chanceToHit;
    }

    @Override
    public BeamState clone() {
        try {
            return (BeamState) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
