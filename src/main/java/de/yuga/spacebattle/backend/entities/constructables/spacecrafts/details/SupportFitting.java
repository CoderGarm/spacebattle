package de.yuga.spacebattle.backend.entities.constructables.spacecrafts.details;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.PassiveModule;

import javax.annotation.Nonnull;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * The support fitting represents a passive module and their amount.
 */
@Embeddable
public class SupportFitting {

    /**
     * The support module.
     */
    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idPassiveModule")
    private PassiveModule passiveModule;

    /**
     * The amount of this weapon with the given {@link PassiveModule}.
     */
    @Min(1)
    private int amount;

    public SupportFitting() {
    }

    public SupportFitting(@Nonnull final PassiveModule passiveModule,
                          final int amount) {
        Preconditions.checkNotNull(passiveModule, "passiveModule shouldn't be null!");

        this.passiveModule = passiveModule;
        this.amount = amount;
    }

    @Nonnull
    public PassiveModule getPassiveModule() {
        return passiveModule;
    }

    public void setPassiveModule(@Nonnull PassiveModule passiveModule) {
        Preconditions.checkNotNull(passiveModule, "passiveModule shouldn't be null!");

        this.passiveModule = passiveModule;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    /**
     * Calculates and returns the bonus which is provided by these support fitting.
     *
     * @return the bonus as factor
     */
    public double getAbsoluteValueAsFactor() {
        return 100 + passiveModule.getCalculationType().getMultiplier() * passiveModule.getEffectValue() * amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SupportFitting)) return false;

        SupportFitting that = (SupportFitting) o;

        return passiveModule.equals(that.passiveModule);
    }

    @Override
    public int hashCode() {
        return passiveModule.hashCode();
    }
}
