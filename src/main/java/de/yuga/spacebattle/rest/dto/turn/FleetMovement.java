package de.yuga.spacebattle.rest.dto.turn;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.turn.Move;
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
    @Schema(required = true, description = "The size of the moving fleet.")
    private int fleetSize;

    @JsonProperty
    @Schema(required = true, description = "If the incoming fleet is from a foreign empire.")
    private boolean isForeignFleet;

    public FleetMovement(@Nonnull final Move movement, final int idUserRequestInformation) {
        Preconditions.checkNotNull(movement, "movement must not be empty");
        Preconditions.checkNotNull(movement.getFleetSnapshot(), "movement.getFleetSnapshot() must not be empty");
        Preconditions.checkNotNull(movement.getFleetSnapshot().getOwner(), "movement.getFleetSnapshot().getOwner() must not be empty");

        final Owner fleetOwner = movement.getFleetSnapshot().getOwner();
        this.isForeignFleet = fleetOwner.getId() != idUserRequestInformation;

        if (movement.isDeleted()) {
            this.fleetName = movement.getFleetSnapshot().getName();
            this.fleetOwnerName = movement.getFleetSnapshot().getOwner().getUsername();
            this.fleetSize = movement.getFleetSnapshot().getFleet().getAliveShips().size();
        } else {
            final Fleet fleet = movement.getFleet();
            this.fleetName = fleet.getName();
            this.fleetOwnerName = fleet.getOwner().getUsername();
            this.fleetSize = fleet.getAliveShips().size();
        }

        if (movement.getDestinationOrbit().getPlanet() != null) {
            this.destinationPlanet = movement.getDestinationOrbit().getPlanet().getName();
            if (movement.getDestinationOrbit().getPlanet().getOwner() != null) {
                this.destinationPlanetOwner = movement.getDestinationOrbit().getPlanet().getOwner().getUsername();
            }
        }
        if (movement.getDestinationOrbit().getSystem() != null) {
            this.destinationSystem = movement.getDestinationOrbit().getSystem().getName();
        }

        this.duration = movement.getOriginalDuration();
    }
}
