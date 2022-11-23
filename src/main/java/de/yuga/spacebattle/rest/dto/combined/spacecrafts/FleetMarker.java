package de.yuga.spacebattle.rest.dto.combined.spacecrafts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.AbstractId;
import de.yuga.spacebattle.rest.dto.orbitals.FleetOrbit;
import de.yuga.spacebattle.rest.dto.turn.Move;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Schema(description = ".")
public class FleetMarker {

    @JsonProperty
    @Schema(required = true, description = "The id.")
    private AbstractId fleet;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The name of the fleet")
    private String name;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The owner of the fleet")
    private AbstractId owner;

    /**
     * The current location of this fleet. <br>
     * <br>
     * If null, then this is in hyper space.<br>
     * The planet could be null if the fleet is on a local movement.
     */
    @Nullable
    @JsonProperty
    @Schema(description = "The current location of this fleet.\n" +
            "     \n" +
            "     If null, then this is in hyper space.\n" +
            "     The planet could be null if the fleet is on a local movement.")
    private FleetOrbit orbit;

    /**
     * The move includes the origin and the destination if the start is different from the current {@link #orbit}.
     */
    @Nullable
    @JsonProperty
    @Schema(description = "The fleet's current moving.")
    private Move move;

    @JsonProperty
    @Schema(required = true, description = "If the fleet can run interstellar movements.")
    private boolean isFTLCapable;

    @JsonProperty
    @Schema(required = true, description = "The states of the fleet.")
    private StateBlock state;

    public FleetMarker() {
    }

    public FleetMarker(@Nonnull final de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        this.fleet = new AbstractId(fleet.getId());
        this.owner = new AbstractId(fleet.getOwner());
        this.name = fleet.getName();
        this.orbit = fleet.getOrbit() != null ? new FleetOrbit(fleet.getOrbit()) : null;
        this.move = fleet.getMove() != null ? new Move(fleet.getMove()) : null;
        this.isFTLCapable = fleet.isFTLCapable();
        this.state = new StateBlock(fleet);
    }
}
