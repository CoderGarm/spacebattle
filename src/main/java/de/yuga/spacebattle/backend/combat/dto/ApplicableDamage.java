package de.yuga.spacebattle.backend.combat.dto;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.round.BeamState;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

public class ApplicableDamage {

    private final long damageValue;

    @Nonnull
    private final BigDecimal chanceToHit;

    public ApplicableDamage(@Nonnull final BeamState shot) {
        Preconditions.checkNotNull(shot, "shot shouldn't be null!");

        this.damageValue = shot.getDamageValue();
        this.chanceToHit = shot.getChanceToHit();
    }

    public ApplicableDamage(@Nonnull final Missile missile) {
        Preconditions.checkNotNull(missile, "missile shouldn't be null!");

        this.damageValue = missile.getWarhead().getDamageValue();
        this.chanceToHit = BigDecimal.ONE;
    }

    /**
     * Returns the effective damage by the chance to hit.
     *
     * @return the effective damage
     */
    public long getEffectiveDamage() {
        return BigDecimal.valueOf(damageValue).multiply(chanceToHit).longValue();
    }

    public long getDamageValue() {
        return damageValue;
    }

    @Nonnull
    public BigDecimal getChanceToHit() {
        return chanceToHit;
    }
}
