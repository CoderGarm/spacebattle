package de.yuga.spacebattle.rest.dto.enums;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

/**
 * Defines if a value in a calculation will be in- or decreased.
 */
@Schema(description = ".")
public class ECalculationType extends HasTypeName {

    @Schema(required = true, description = "The calculation type multiplier - to add or subtract with 1 or -1.")
    private final int multiplier;

    public ECalculationType() {
        super();
        multiplier = Integer.MIN_VALUE;
    }

    ECalculationType(@Nonnull final de.yuga.spacebattle.backend.enums.ECalculationType calculationType) {
        super(calculationType);

        this.multiplier = calculationType.getMultiplier();
    }

    public int getMultiplier() {
        return multiplier;
    }
}
