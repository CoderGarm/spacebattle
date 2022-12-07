package de.yuga.spacebattle.rest.dto.turn;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class FinishedColonization {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The fresh colonized planet's name.")
    private String colonizedPlanetName;

    public FinishedColonization(@Nonnull final de.yuga.spacebattle.backend.dto.turn.FinishedColonization colonization) {
        Preconditions.checkNotNull(colonization, "colonization must not be empty");

        this.colonizedPlanetName = colonization.getPlanet().getName();
    }
}
