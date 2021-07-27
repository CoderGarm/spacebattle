package de.yuga.spacebattle.rest.dto.spacecrafts.details;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.PassiveModule;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;

/**
 * The support fitting represents a passive module and their amount.
 */
public class SupportFitting {

    /**
     * The support module.
     */
    @Nonnull
    @ApiModelProperty(required = true, value = "The passive module of this fitting")
    private PassiveModule passiveModule;

    /**
     * The amount of this weapon with the given {@link PassiveModule}.
     */
    @ApiModelProperty(required = true, value = "The amount of passive modules in this fitting")
    private int amount;

    public SupportFitting() {
    }

    public SupportFitting(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.details.SupportFitting supportFitting) {
        Preconditions.checkNotNull(supportFitting, "supportFitting shouldn't be null!");

        this.passiveModule = new PassiveModule(supportFitting.getPassiveModule());
        this.amount = supportFitting.getAmount();
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
}
