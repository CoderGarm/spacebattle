package de.yuga.spacebattle.rest.dto.constructables.spacecrafts;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@Schema(description = ".")
public class ShipyardConstructionOrder {

    @Schema(required = true, description = "The planet which should run the job.")
    private int idPlanet;

    @Nonnull
    @Schema(required = true, description = "The job to run - key must be the idShipClass, value must be the amount.")
    private final List<ShipyardConstructionSelection> shipJobPayload = new ArrayList<>();

    @Nonnull
    @Schema(required = true, description = "The job to run - key must be the idOrbitalModule, value must be the amount.")
    private final List<ShipyardOrbitalModuleConstructionSelection> orbitalsJobPayload = new ArrayList<>();

    public ShipyardConstructionOrder() {
    }

    public int getIdPlanet() {
        return idPlanet;
    }

    @Nonnull
    public List<ShipyardConstructionSelection> getShipJobPayload() {
        return shipJobPayload;
    }

    @Nonnull
    public List<ShipyardOrbitalModuleConstructionSelection> getOrbitalsJobPayload() {
        return orbitalsJobPayload;
    }
}
