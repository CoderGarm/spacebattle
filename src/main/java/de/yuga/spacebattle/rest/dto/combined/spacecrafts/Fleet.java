package de.yuga.spacebattle.rest.dto.combined.spacecrafts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.account.UserJson;
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
    private UserJson owner;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The fleet's individual war ships.")
    private Set<WarShip> ships = new HashSet<>();

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

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The current effect value per module type.")
    private SpacecraftCapabilities spacecraftCapabilities;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The without-any-damage effect value per module type.")
    private SpacecraftCapabilities baseSpacecraftCapabilities;

    @JsonProperty
    @Schema(required = true, description = "If the fleet can run interstellar movements.")
    private boolean isFTLCapable;

    @JsonProperty
    @Schema(required = true, description = "The states of the fleet.")
    private StateBlock state;

    public Fleet() {
    }

    public Fleet(@Nonnull final de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet fleet,
                 @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        this.idFleet = fleet.getId();
        this.owner = new UserJson(fleet.getOwner());
        this.name = fleet.getName();
        this.orbit = fleet.getOrbit() != null ? new FleetOrbit(fleet.getOrbit()) : null;
        this.move = fleet.getMove() != null ? new Move(fleet.getMove()) : null;
        this.ships.addAll(fleet.getAllShips().stream().map(w -> new WarShip(w, w.getWarshipHealthState(), languageCode)).collect(Collectors.toList()));
        this.spacecraftCapabilities = new SpacecraftCapabilities(fleet);
        this.baseSpacecraftCapabilities = new SpacecraftCapabilities(fleet.getShipsByClass());
        this.isFTLCapable = fleet.isFTLCapable();
        this.state = new StateBlock(fleet);
    }

    public Fleet(@Nonnull final de.yuga.spacebattle.backend.entities.combined.spacecrafts.FleetSnapshot fleetSnapshot,
                 @Nonnull final String languageCode) {
        Preconditions.checkNotNull(fleetSnapshot, "fleetSnapshot shouldn't be null!");
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");

        final de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet fleet = fleetSnapshot.getFleet();
        this.idFleet = fleet.getId();
        this.owner = new UserJson(fleet.getOwner());
        this.name = fleet.getName();
        this.orbit = fleet.getOrbit() != null ? new FleetOrbit(fleet.getOrbit()) : null;
        this.move = fleet.getMove() != null ? new Move(fleet.getMove()) : null;

        this.ships.addAll(fleetSnapshot.getShips().stream().map(w -> new WarShip(w, languageCode)).collect(Collectors.toList()));
        this.spacecraftCapabilities = new SpacecraftCapabilities(fleetSnapshot);
        this.baseSpacecraftCapabilities = new SpacecraftCapabilities(fleet.getShipsByClass());
        this.isFTLCapable = fleet.isFTLCapable();
        this.state = new StateBlock(fleet);
    }
}
