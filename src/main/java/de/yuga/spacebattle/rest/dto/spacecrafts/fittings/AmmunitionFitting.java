package de.yuga.spacebattle.rest.dto.spacecrafts.fittings;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.AmmunitionModule;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

/**
 * The ammunition fitting represents an ammunition module and their amount.
 */
@Schema(description = ".")
public class AmmunitionFitting {

    /**
     * The ammunition module.
     */
    @Nonnull
    @Schema(required = true, description = "The ammunition module.")
    private AmmunitionModule ammunitionModule;

    /**
     * The amount of this weapon with the given {@link AmmunitionModule}.
     */
    @Schema(required = true, description = "The amount of ammunition modules.")
    private int amount;

    public AmmunitionFitting() {
    }

    public AmmunitionFitting(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.fittings.AmmunitionFitting ammunitionFitting,
                             @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(ammunitionFitting, "ammunitionFitting shouldn't be null!");

        this.ammunitionModule = new de.yuga.spacebattle.rest.dto.spacecrafts.modules.AmmunitionModule(ammunitionFitting.getAmmunitionModule(), languageCode);
        this.amount = ammunitionFitting.getAmount();
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
}
