package de.yuga.spacebattle.rest.dto.constructables.spacecrafts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.OrbitalModuleJobElement;
import de.yuga.spacebattle.rest.dto.orbitals.FleetOrbit;
import de.yuga.spacebattle.rest.dto.spacecrafts.OrbitalModule;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.validation.constraints.Size;

@Schema(description = ".")
public class OrbitalStructures {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The name of this class.")
    @Size(min = 3, max = 30, message = "name should be between 3 and 30 characters long")
    private OrbitalModule module;

    @JsonProperty
    @Schema(required = true, description = "The amount.")
    private int amount;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The station.")
    private FleetOrbit fleetOrbit;

    public OrbitalStructures() {
    }

    public OrbitalStructures(@Nonnull final de.yuga.spacebattle.backend.entities.combined.spacecrafts.OrbitalModule module,
                             final int amount,
                             @Nonnull final FleetOrbit fleetOrbit,
                             @Nonnull final String langCode) {
        Preconditions.checkNotNull(module, "module must not be empty");
        Preconditions.checkNotNull(fleetOrbit, "fleetOrbit must not be empty");

        this.module = new OrbitalModule(module, langCode);
        this.amount = amount;
        this.fleetOrbit = fleetOrbit;
    }

    public OrbitalStructures(@Nonnull final OrbitalModuleJobElement jobElement,
                             @Nonnull final Planet station,
                             @Nonnull final String languageCode) {
        this(jobElement.getOrbitalModule(), jobElement.getAmount(), new FleetOrbit(station), languageCode);
    }
}
