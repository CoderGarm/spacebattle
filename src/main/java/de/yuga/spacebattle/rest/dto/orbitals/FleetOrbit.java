package de.yuga.spacebattle.rest.dto.orbitals;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Schema(description = ".")
public class FleetOrbit {

    @Nullable
    @JsonProperty
    @Schema(description = "The planet which defines the inertial system of the orbit. If undefined the orbit lives in the universe.")
    private Planet planet;

    @Nullable
    @JsonProperty
    @Schema(description = "The star system which defines the inertial system of the orbit. If undefined the orbit lives in the universe.")
    private StarSystem system;

    @Nullable
    @JsonProperty
    @Schema(description = "The orbit inside the inertial system defined by the star system.")
    private Orbit orbit;

    public FleetOrbit() {
    }

    public FleetOrbit(@Nonnull final de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit fleetOrbit) {
        Preconditions.checkNotNull(fleetOrbit, "fleetOrbit shouldn't be null!");

        this.planet = fleetOrbit.getPlanet() != null ? new Planet(fleetOrbit.getPlanet()) : null;
        this.system = fleetOrbit.getSystem() != null ? new StarSystem(fleetOrbit.getSystem()) : null;
        this.orbit = fleetOrbit.getResultingOrbit() != null ? new Orbit(fleetOrbit.getResultingOrbit()) : null;
    }
}
