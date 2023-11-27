package de.yuga.spacebattle.rest.dto.constructables.spacecrafts;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = ".")
public class ShipyardOrbitalModuleConstructionSelection {

    @Schema(required = true, description = "The module which should be produced.")
    private int idOrbitalModule;

    @Schema(required = true, description = "The amount of the ship class which should be produced.")
    private int amount;

    public ShipyardOrbitalModuleConstructionSelection() {
    }

    public int getIdOrbitalModule() {
        return idOrbitalModule;
    }

    public int getAmount() {
        return amount;
    }
}
