package de.yuga.spacebattle.rest.dto.spacecrafts.fittings;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.spacecrafts.ammunition.Missile;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

/**
 * The ammunition fitting represents an ammunition module and their amount.
 */
@Schema(description = ".")
public class AmmunitionFitting {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The ammunition module.")
    private Missile missile;

    @JsonProperty
    @Schema(required = true, description = "The amount of ammunition modules.")
    private int amount;

    public AmmunitionFitting() {
    }

    public AmmunitionFitting(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.fittings.AmmunitionFitting ammunitionFitting,
                             @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(ammunitionFitting, "ammunitionFitting shouldn't be null!");

        this.missile = new Missile(ammunitionFitting.getMissile(), languageCode);
        this.amount = ammunitionFitting.getAmount();
    }

    @Nonnull
    public Missile getMissile() {
        return missile;
    }

    public int getAmount() {
        return amount;
    }
}
