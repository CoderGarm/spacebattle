package de.yuga.spacebattle.rest.dto.combined.spacecrafts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthState;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthStateAccessor;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Schema(description = "Contains the several states which can be taken from a spacecraft.")
public class StateBlock {

    @JsonProperty
    @Schema(required = true, description = "If the spacecraft is marked as wrecked.")
    private boolean isDeleted;

    @JsonProperty
    @Schema(required = true, description = "If the spacecraft is marked as active.")
    private boolean isOperational;

    @JsonProperty
    @Schema(required = true, description = "If the spacecraft can do actions.")
    private boolean isActive;

    @JsonProperty
    @Schema(required = true, description = "If the fleet can run interstellar movements.")
    private boolean isFTLCapable;

    @JsonProperty
    @Schema(required = true, description = "If the spacecraft is able to fight.")
    private boolean isFightingCapable;

    @JsonProperty
    @Schema(required = true, description = "If the spacecraft needs a repair.")
    private boolean needsRepair;

    @JsonProperty
    @Schema(required = true, description = "If the spacecraft is in the shipyard.")
    private boolean isInYard = false;

    @Nullable
    @JsonProperty
    @Schema(description = "The amount of ships in the fleet.")
    private Integer fleetSize;

    @JsonProperty
    @Schema(required = true, description = "If the spacecraft needs ammunition.")
    private boolean needsAmmunition;

    public StateBlock() {
    }

    public StateBlock(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet must not be empty");

        this.isDeleted = fleet.isDeleted();
        this.isOperational = fleet.isOperational();
        this.isActive = fleet.isActive();
        this.isFTLCapable = fleet.isFTLCapable();
        this.needsRepair = fleet.isNeedsRepair();
        this.needsAmmunition = fleet.getAliveShips().stream().anyMatch(w -> w.getWarshipHealthState().needsAmmunition());
        this.isFightingCapable = fleet.isOperational();
        this.isInYard = fleet.isInYard();
        this.fleetSize = fleet.getAliveShips().size();
    }

    public StateBlock(@Nonnull final WarShip warShip) {
        Preconditions.checkNotNull(warShip, "warShip must not be empty");

        this.isDeleted = !warShip.isAlive();
        this.isOperational = warShip.isOperational();
        this.isActive = warShip.isAlive();
        this.isFTLCapable = warShip.getShipClass().isFTLCapable();
        final WarshipHealthState warshipHealthState = warShip.getWarshipHealthState();
        this.needsRepair = warshipHealthState.needsRepair();
        this.needsAmmunition = warshipHealthState.needsAmmunition();
        this.isFightingCapable = warshipHealthState.isFightingCapable();
        this.isInYard = warShip.getFleet() != null && warShip.getFleet().isInYard();
    }

    public StateBlock(@Nonnull final WarshipHealthStateAccessor warshipHealthState) {
        Preconditions.checkNotNull(warshipHealthState, "warshipHealthState must not be empty");

        this.isDeleted = !warshipHealthState.isAlive();
        this.isOperational = warshipHealthState.isOperational();
        this.isActive = warshipHealthState.isAlive();
        this.isFTLCapable = warshipHealthState.getWarShip().getShipClass().isFTLCapable();
        this.needsRepair = warshipHealthState.needsRepair();
        this.needsAmmunition = warshipHealthState.needsAmmunition();
        this.isFightingCapable = warshipHealthState.isFightingCapable();
        this.isInYard = warshipHealthState.getWarShip().getFleet() != null && warshipHealthState.getWarShip().getFleet().isInYard();
    }
}
