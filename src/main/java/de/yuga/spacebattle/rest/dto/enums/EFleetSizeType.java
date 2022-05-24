package de.yuga.spacebattle.rest.dto.enums;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class EFleetSizeType extends HasIcon {

    @Schema(required = true, description = "The maximum fleet size which is defined by this entry.")
    private final int fleetSize;

    public EFleetSizeType() {
        super();
        this.fleetSize = Integer.MIN_VALUE;
    }

    EFleetSizeType(@Nonnull final de.yuga.spacebattle.backend.enums.EFleetSizeType fleetSizeType) {
        super(fleetSizeType);

        this.fleetSize = fleetSizeType.getFleetSize();
    }

    public int getFleetSize() {
        return fleetSize;
    }

}
