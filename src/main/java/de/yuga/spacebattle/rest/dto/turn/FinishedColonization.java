package de.yuga.spacebattle.rest.dto.turn;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class FinishedColonization {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The fresh colonized planet's name.")
    private String colonizedPlanetName;

    public FinishedColonization(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet must not be empty");

        this.colonizedPlanetName = planet.getName();
    }
}
