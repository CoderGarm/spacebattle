package de.yuga.spacebattle.rest.dto.constructables.buildings;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = ".")
public class PlannedConstruction {

    @Schema(required = true, description = "The building which should be build.")
    private int idBuilding;

    @Schema(required = true, description = "The target level.")
    private int targetLevel;

    public int getIdBuilding() {
        return idBuilding;
    }

    public int getTargetLevel() {
        return targetLevel;
    }
}
