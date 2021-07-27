package de.yuga.spacebattle.rest.dto.constructables.spacecrafts;

import io.swagger.annotations.ApiModelProperty;

public class ShipyardConstructionSelection {

    @ApiModelProperty(required = true, value = "The ship class which should be produced.")
    private Integer idShipClass;

    @ApiModelProperty(required = true, value = "The amount of the ship class which should be produced.")
    private Integer amount;

    public ShipyardConstructionSelection() {
    }

    public Integer getIdShipClass() {
        return idShipClass;
    }

    public Integer getAmount() {
        return amount;
    }
}
