package de.yuga.spacebattle.rest.dto.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;

@ApiModel(parent = HasIcon.class)
public class EFleetSizeType extends HasIcon {

    @ApiModelProperty(required = true, value = "The maximum fleet size which is defined by this entry.")
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
