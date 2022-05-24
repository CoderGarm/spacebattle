package de.yuga.spacebattle.rest.dto.orbitals;

import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Schema(description = ".")
public class FleetOrbit {

    @Nullable
    @Schema(description = "The star system which defines the inertial system of the orbit. If undefined the orbit lives in the universe.")
    private StarSystem system;

    @Nullable
    @Schema(description = "The orbit inside the inertial system defined by the star system.")
    private Orbit orbit;

    public FleetOrbit() {
    }

    public FleetOrbit(@Nonnull final de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit fleetOrbit) {
        Preconditions.checkNotNull(fleetOrbit, "fleetOrbit shouldn't be null!");

        this.system = fleetOrbit.getSystem() != null ? new StarSystem(fleetOrbit.getSystem()) : null;
        this.orbit = fleetOrbit.getOrbit() != null ? new Orbit(fleetOrbit.getOrbit()) : null;
    }

    @Nullable
    public StarSystem getSystem() {
        return system;
    }

    @Nullable
    public Orbit getOrbit() {
        return orbit;
    }
}
