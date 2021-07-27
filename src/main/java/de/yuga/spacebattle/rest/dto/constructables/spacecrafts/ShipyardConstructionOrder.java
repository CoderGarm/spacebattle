package de.yuga.spacebattle.rest.dto.constructables.spacecrafts;

import com.google.common.base.Preconditions;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ShipyardConstructionOrder {

    @ApiModelProperty(required = true, value = "The planet which should run the job.")
    private Integer idPlanet;

    @Nonnull
    @ApiModelProperty(required = true, value = "The job to run - key must be the idShipClass, value must be the amount.")
    private final List<ShipyardConstructionSelection> shipJobPayload = new ArrayList<>();

    public ShipyardConstructionOrder() {
    }

    public int getIdPlanet() {
        Preconditions.checkArgument(idPlanet != null, "idPlanet shouldn't be null!");

        return idPlanet;
    }

    @Nonnull
    public List<ShipyardConstructionSelection> getShipJobPayload() {
        return shipJobPayload;
    }
}
