package de.yuga.spacebattle.rest.dto.combined.spacecrafts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.spacecrafts.ammunition.Missile;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class AmmunitionValue {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The value's type.")
    private Missile missile;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The value.")
    private int value;

    public AmmunitionValue() {
    }

    public AmmunitionValue(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile missile,
                           final int value,
                           @Nonnull final String langCode) {
        Preconditions.checkNotNull(missile, "missile must not be empty");
        Preconditions.checkNotNull(langCode, "langCode must not be empty");

        this.missile = new Missile(missile, langCode);
        this.value = value;
    }


    @Override
    @JsonIgnore
    public String toString() {
        return missile.getTypeName() + ": " + value;
    }
}
