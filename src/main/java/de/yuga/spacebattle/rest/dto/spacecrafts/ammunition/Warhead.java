package de.yuga.spacebattle.rest.dto.spacecrafts.ammunition;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.enums.EWarheadType;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class Warhead {

    @JsonProperty
    @Schema(required = true, description = "The projected damage of this warhead.")
    private long damageValue;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The effective range of this warhead.")
    private Distance damageProjectionRange;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The warhead type.")
    private EWarheadType warheadType;

    @JsonProperty
    @Schema(required = true, description = "The used capacity of this warhead.")
    private int useCapacity;

    public Warhead() {
    }

    public Warhead(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Warhead warhead,
                   @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(warhead, "warhead shouldn't be null!");

        this.damageValue = warhead.getDamageValue();
        this.damageProjectionRange = warhead.getDamageProjectionRange();
        this.warheadType = warhead.getWarheadType();
    }
}
