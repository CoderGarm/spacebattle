package de.yuga.spacebattle.rest.dto.constructables.spacecrafts;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = ".")
public class ShipyardConstructionSelection {

    @Schema(required = true, description = "The ship class which should be produced.")
    private Integer idShipClass;

    @Schema(required = true, description = "The amount of the ship class which should be produced.")
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
