package de.yuga.spacebattle.rest.dto.constructables.buildings;

import io.swagger.annotations.ApiModelProperty;

public class PlannedConstruction {

    @ApiModelProperty(required = true, value = "The building which should be build.")
    private int idBuilding;

    @ApiModelProperty(required = true, value = "The target level.")
    private int targetLevel;

    public int getIdBuilding() {
        return idBuilding;
    }

    public int getTargetLevel() {
        return targetLevel;
    }
}
