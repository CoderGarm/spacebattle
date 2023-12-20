package de.yuga.spacebattle.rest.dto.combined.spacecrafts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.FleetSnapshot;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.rest.dto.AbstractId;
import de.yuga.spacebattle.rest.dto.orbitals.FleetOrbit;
import de.yuga.spacebattle.rest.dto.turn.Move;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The fleet's individual war ships.")
    private final Set<AbstractId> ships = new HashSet<>();

    @Nullable
    @JsonProperty
    @Schema(description = "The current location of this fleet.\n" +
            "     \n" +
            "     If null, then this is in hyper space.\n" +
            "     The planet could be null if the fleet is on a local movement.")
    private FleetOrbit orbit;

    @Nullable
    @JsonProperty
    @Schema(description = "The location of this fleet when at move.")
    private FleetOrbit currentOrbit;

    /**
     * The move includes the origin and the destination if the start is different from the current {@link #orbit}.
     */
    @Nullable
    @JsonProperty
    @Schema(description = "The fleet's current moving.")
    private Move move;

    @JsonProperty
    @Schema(required = true, description = "The states of the fleet.")
    private StateBlock state;

    @JsonProperty
    @Schema(required = true, description = "The value of the user's sensors in that system. Defines how good other ships can be seen.")
    private int hyperPrintSensorValue = 0;

    public FleetMarker() {
    }

    public FleetMarker(@Nonnull final de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        this.fleet = new AbstractId(fleet, fleet.getName());
        this.owner = new AbstractId(fleet.getOwner(), fleet.getOwner().getUsername());
        this.ships.addAll(fleet.getAliveShips().stream().map(w -> new AbstractId(w, w.getName())).collect(Collectors.toSet()));
        this.name = fleet.getName();
        this.currentOrbit = fleet.getCurrentOrbit() != null ? new FleetOrbit(fleet.getCurrentOrbit()) : null;
        this.orbit = fleet.getCurrentOrbit() != null ? new FleetOrbit(fleet.getCurrentOrbit()) : null;
        this.move = fleet.getMove() != null ? new Move(fleet.getMove()) : null;
        this.state = new StateBlock(fleet);
    }

    public FleetMarker(@Nonnull final FleetSnapshot fleetSnapshot) {
        Preconditions.checkNotNull(fleetSnapshot, "fleetSnapshot must not be empty");

        final Fleet fleet = fleetSnapshot.getFleet();
        this.fleet = new AbstractId(fleet, fleet.getName());
        this.owner = new AbstractId(fleet.getOwner(), fleet.getOwner().getUsername());
        this.ships.addAll(fleetSnapshot.getShips().stream().map(w -> new AbstractId(w.getWarShip(), w.getWarShip().getName())).collect(Collectors.toSet()));
        this.name = fleet.getName();
        this.currentOrbit = fleet.getCurrentOrbit() != null ? new FleetOrbit(fleet.getCurrentOrbit()) : null;
        this.orbit = fleet.getCurrentOrbit() != null ? new FleetOrbit(fleet.getCurrentOrbit()) : null;
        this.move = fleet.getMove() != null ? new Move(fleet.getMove()) : null;
        this.state = new StateBlock(fleet);
    }

    public FleetMarker(@Nonnull final Fleet fleet, @Nonnull final Map<StarSystem, Integer> sensorStrength) {
        this(fleet);
        Preconditions.checkNotNull(sensorStrength, "sensorStrength must not be empty");

        final de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit orbit = fleet.getCurrentOrbit();
        if (orbit != null) {
            this.hyperPrintSensorValue = sensorStrength.getOrDefault(orbit.getSystem(), 0);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final FleetMarker that = (FleetMarker) o;

        return new EqualsBuilder().append(fleet, that.fleet).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(fleet).toHashCode();
    }
}
