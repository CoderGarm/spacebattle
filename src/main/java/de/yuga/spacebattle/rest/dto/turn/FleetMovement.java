package de.yuga.spacebattle.rest.dto.turn;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Schema(description = ".")
public class FleetMovement {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The fleets name.")
    private String fleetName;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The fleet owners name.")
    private String fleetOwnerName;

    @Nullable
    @JsonProperty
    @Schema(description = "The origin planet's name.")
    private String originPlanet = null;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The origin systems's name.")
    private String originSystem;

    @Nullable
    @JsonProperty
    @Schema(description = "The designation planet's name.")
    private String destinationPlanet = null;

    @Nullable
    @JsonProperty
    @Schema(description = "The designation planet owners name.")
    private String destinationPlanetOwner = null;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The designation system's name.")
    private String destinationSystem;

    @JsonProperty
    @Schema(required = true, description = "The duration of the journey.")
    private int duration;

    @JsonProperty
    @Schema(required = true, description = "If the incoming fleet is from a foreign empire.")
    private boolean isForeignFleet;

    public FleetMovement(@Nonnull final de.yuga.spacebattle.backend.dto.turn.FleetMovement movement) {
        Preconditions.checkNotNull(movement, "movement must not be empty");

        this.fleetName = movement.getFleet().getName();
        this.fleetOwnerName = movement.getFleet().getOwner().getUsername();

        if (movement.getOriginPlanet() != null) {
            this.originPlanet = movement.getOriginPlanet().getName();
        }

        this.originSystem = movement.getOriginSystem().getName();

        if (movement.getDestinationPlanet() != null) {
            this.destinationPlanet = movement.getDestinationPlanet().getName();
            final User owner = movement.getDestinationPlanet().getOwner();
            if (owner != null) {
                this.destinationPlanetOwner = owner.getUsername();
            }
        }

        this.destinationSystem = movement.getDestinationSystem().getName();
        this.duration = movement.getOriginalDuration();
        this.isForeignFleet = movement.isForeignFleet();
    }
}
