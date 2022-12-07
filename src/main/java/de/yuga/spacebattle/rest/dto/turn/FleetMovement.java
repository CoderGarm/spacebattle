package de.yuga.spacebattle.rest.dto.turn;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class FleetMovement {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The transferred resources.")
    private String fleetName;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The origin planet's name.")
    private String fromPlanet;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The origin systems's name.")
    private String fromSystem;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The designation planet's name.")
    private String toPlanet;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The designation system's name.")
    private String toSystem;

    @JsonProperty
    @Schema(required = true, description = "The duration of the journey.")
    private int duration;

    public FleetMovement(@Nonnull final de.yuga.spacebattle.backend.dto.turn.FleetMovement movement) {
        Preconditions.checkNotNull(movement, "movement must not be empty");

        this.fleetName = movement.getFleet().getName();
        this.fromPlanet = movement.getOrigin().getName();
        this.fromSystem = movement.getOrigin().getSystem().getName();
        this.toPlanet = movement.getDestination().getName();
        this.toSystem = movement.getDestination().getSystem().getName();
        this.duration = movement.getOriginalDuration();
    }
}
