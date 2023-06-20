package de.yuga.spacebattle.backend.entities.spacecrafts.fittings;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.physics.Mass;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import javax.annotation.Nonnull;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * The ammunition fitting represents an ammunition module and their amount.
 */
@Embeddable
public class AmmunitionFitting {

    /**
     * The ammunition module.
     */
    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idMissile")
    private Missile missile;

    /**
     * The amount of missiles in this fitting.
     */
    @Min(0)
    private int amount;

    public AmmunitionFitting() {
    }

    public AmmunitionFitting(@Nonnull final Missile missile,
                             final int amount) {
        this.missile = Preconditions.checkNotNull(missile, "missile must not be empty");
        this.amount = amount;
    }

    @Nonnull
    public Missile getMissile() {
        return missile;
    }


    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AmmunitionFitting)) return false;

        AmmunitionFitting that = (AmmunitionFitting) o;

        return missile.equals(that.missile);
    }

    @Override
    public int hashCode() {
        return missile.hashCode();
    }

    @Nonnull
    public Mass getTonnage() {
        return missile.getTonnage().multiply(amount);
    }
}
