package de.yuga.spacebattle.backend.entities.spacecrafts.fittings;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.AmmunitionModule;

import javax.annotation.Nonnull;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
    @JoinColumn(name = "idAmmunitionModule")
    private AmmunitionModule ammunitionModule;

    /**
     * The amount of this weapon with the given {@link AmmunitionModule}.
     */
    @Min(0)
    private int amount;

    public AmmunitionFitting() {
    }

    public AmmunitionFitting(@Nonnull final AmmunitionModule ammunitionModule,
                             final int amount) {
        Preconditions.checkNotNull(ammunitionModule, "ammunitionModule shouldn't be null!");

        this.ammunitionModule = ammunitionModule;
        this.amount = amount;
    }

    @Nonnull
    public AmmunitionModule getAmmunitionModule() {
        return ammunitionModule;
    }

    public void setAmmunitionModule(@Nonnull AmmunitionModule ammunitionModule) {
        Preconditions.checkNotNull(ammunitionModule, "ammunitionModule shouldn't be null!");

        this.ammunitionModule = ammunitionModule;
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

        return ammunitionModule.equals(that.ammunitionModule);
    }

    @Override
    public int hashCode() {
        return ammunitionModule.hashCode();
    }

    public int calculateUsedCapacity() {
        return amount * ammunitionModule.getUseCapacity();
    }
}
