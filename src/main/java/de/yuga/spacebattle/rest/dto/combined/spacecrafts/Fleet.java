package de.yuga.spacebattle.rest.dto.combined.spacecrafts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.account.Player;
import de.yuga.spacebattle.rest.dto.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.rest.dto.orbitals.FleetOrbit;
import de.yuga.spacebattle.rest.dto.turn.Move;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Schema(description = ".")
public class Fleet {

    @JsonProperty
    @Schema(required = true, description = "The id.")
    private int idFleet;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The name of the fleet")
    private String name;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The owner of the fleet")
    private Player owner;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The fleet's individual war ships.")
    private Set<WarShip> ships = new HashSet<>();

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

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The current effect value per module type.")
    private SpacecraftCapabilities spacecraftCapabilities;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The without-any-damage effect value per module type.")
    private SpacecraftCapabilities baseSpacecraftCapabilities;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The capacities used per area.")
    private SpacecraftCapacityAreas spacecraftCapacityAreas;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The states of the fleet.")
    private StateBlock state;

    public Fleet() {
    }

    public Fleet(@Nonnull final de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet fleet,
                 @Nonnull final String languageCode) {
        this(fleet, fleet.getAliveShips(), languageCode);
    }

    public Fleet(@Nonnull final de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet fleet,
                 @Nonnull final Set<de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip> containingShips,
                 @Nonnull final String languageCode) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkNotNull(containingShips, "containingShips must not be empty");
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");

        this.idFleet = fleet.getId();
        this.owner = new Player(fleet.getOwner());
        this.name = fleet.getName();
        this.orbit = fleet.getOrbit() != null ? new FleetOrbit(fleet.getOrbit()) : null;
        this.currentOrbit = fleet.getCurrentOrbit() != null ? new FleetOrbit(fleet.getCurrentOrbit()) : null;
        this.move = fleet.getMove() != null ? new Move(fleet.getMove()) : null;
        this.ships.addAll(containingShips.stream().map(w -> new WarShip(w, w.getWarshipHealthState(), languageCode)).collect(Collectors.toList()));
        this.spacecraftCapabilities = new SpacecraftCapabilities(fleet);
        this.baseSpacecraftCapabilities = new SpacecraftCapabilities(fleet.getShipsByClass());
        this.spacecraftCapacityAreas = new SpacecraftCapacityAreas(fleet);
        this.state = new StateBlock(fleet);
    }

    public Fleet(@Nonnull final de.yuga.spacebattle.backend.entities.combined.spacecrafts.FleetSnapshot fleetSnapshot,
                 @Nonnull final String languageCode) {
        Preconditions.checkNotNull(fleetSnapshot, "fleetSnapshot shouldn't be null!");
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");

        final de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet fleet = fleetSnapshot.getFleet();
        this.idFleet = fleet.getId();
        this.owner = new Player(fleet.getOwner());
        this.name = fleet.getName();
        this.orbit = fleet.getOrbit() != null ? new FleetOrbit(fleet.getOrbit()) : null;
        this.currentOrbit = fleet.getCurrentOrbit() != null ? new FleetOrbit(fleet.getCurrentOrbit()) : null;
        this.move = fleet.getMove() != null ? new Move(fleet.getMove()) : null;
        this.ships.addAll(fleetSnapshot.getShips().stream().map(w -> new WarShip(w, languageCode)).collect(Collectors.toList()));
        this.spacecraftCapabilities = new SpacecraftCapabilities(fleetSnapshot);
        this.baseSpacecraftCapabilities = new SpacecraftCapabilities(fleet.getShipsByClass());
        this.spacecraftCapacityAreas = new SpacecraftCapacityAreas(fleet);
        this.state = new StateBlock(fleet);
    }
}
