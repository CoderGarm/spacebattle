package de.yuga.spacebattle.rest.dto.combined.spacecrafts;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Nonnull
    @Schema(required = true, description = "The id.")
    private Integer idFleet;

    @Nonnull
    @Schema(required = true, description = "The name of the fleet")
    private String name;

    @Nonnull
    @Schema(required = true, description = "The owner of the fleet")
    private UserJson owner;

    @Nonnull
    @Schema(required = true, description = "The fleet's individual war ships.")
    private Set<WarShip> ships = new HashSet<>();

    /**
     * The current location of this fleet. <br>
     * <br>
     * If null, then this is in hyper space.<br>
     * The planet could be null if the fleet is on a local movement.
     */
    @Nullable
    @Schema(description = "The current location of this fleet.\n" +
            "     \n" +
            "     If null, then this is in hyper space.\n" +
            "     The planet could be null if the fleet is on a local movement.")
    private FleetOrbit orbit;

    /**
     * The move includes the origin and the destination if the start is different from the current {@link #orbit}.
     */
    @Nullable
    @Schema(description = "The fleet's current moving.")
    private Move move;

    @Nonnull
    @Schema(required = true, description = "The effect value per module type.")
    private FleetCapabilities fleetCapabilities;

    @JsonProperty
    @Schema(required = true, description = "If the fleet can run interstellar movements.")
    private boolean isFTLCapable;

    public Fleet() {
    }

    public Fleet(@Nonnull final de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        this.idFleet = fleet.getId();
        this.owner = new UserJson(fleet.getOwner());
        this.name = fleet.getName();
        this.orbit = fleet.getOrbit() != null ? new FleetOrbit(fleet.getOrbit()) : null;
        this.move = fleet.getMove() != null ? new Move(fleet.getMove()) : null;
        this.ships.addAll(fleet.getShips().stream().map(WarShip::new).collect(Collectors.toList()));
        this.fleetCapabilities = new FleetCapabilities(fleet);
        this.isFTLCapable = fleet.isFTLCapable();
    }

    @Nonnull
    public Integer getIdFleet() {
        return idFleet;
    }

    public void setIdFleet(@Nonnull Integer idFleet) {
        this.idFleet = idFleet;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    public void setName(@Nonnull String name) {
        this.name = name;
    }

    @Nonnull
    public UserJson getOwner() {
        return owner;
    }

    public void setOwner(@Nonnull UserJson owner) {
        this.owner = owner;
    }

    @Nonnull
    public Set<WarShip> getShips() {
        return ships;
    }

    public void setShips(@Nonnull Set<WarShip> ships) {
        this.ships = ships;
    }

    @Nullable
    public FleetOrbit getOrbit() {
        return orbit;
    }

    public void setOrbit(@Nullable FleetOrbit orbit) {
        this.orbit = orbit;
    }

    @Nullable
    public Move getMove() {
        return move;
    }

    public void setMove(@Nullable Move move) {
        this.move = move;
    }

    @Nonnull
    public FleetCapabilities getFleetCapabilities() {
        return fleetCapabilities;
    }

    public void setFleetCapabilities(@Nonnull FleetCapabilities fleetCapabilities) {
        this.fleetCapabilities = fleetCapabilities;
    }

    @JsonIgnore
    public boolean isFTLCapable() {
        return isFTLCapable;
    }
}
