package de.yuga.spacebattle.rest.dto.combined.spacecrafts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthState;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthStateAccessor;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = "Contains the several states which can be taken from a spacecraft.")
public class StateBlock {

    @JsonProperty
    @Schema(required = true, description = "If the spacecraft is marked as wrecked.")
    private final boolean isDeleted;

    @JsonProperty
    @Schema(required = true, description = "If the spacecraft is marked as active.")
    private final boolean isOperational;

    @JsonProperty
    @Schema(required = true, description = "If the spacecraft can do actions.")
    private final boolean isActive;

    @JsonProperty
    @Schema(required = true, description = "If the spacecraft is able to fight.")
    private final boolean isFightingCapable;

    @JsonProperty
    @Schema(required = true, description = "If the spacecraft needs a repair.")
    private final boolean needsRepair;

    @JsonProperty
    @Schema(required = true, description = "If the spacecraft needs ammunition.")
    private final boolean needsAmmunition;

    public StateBlock(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet must not be empty");

        this.isDeleted = fleet.isDeleted();
        this.isOperational = fleet.isOperational();
        this.isActive = fleet.isActive();
        this.needsRepair = fleet.isNeedsRepair();
        this.needsAmmunition = fleet.getAliveShips().stream().anyMatch(w -> w.getWarshipHealthState().needsAmmunition());
        this.isFightingCapable = fleet.isOperational();
    }

    public StateBlock(@Nonnull final WarShip warShip) {
        Preconditions.checkNotNull(warShip, "warShip must not be empty");

        this.isDeleted = !warShip.isAlive();
        this.isOperational = warShip.isOperational();
        this.isActive = warShip.isAlive();
        final WarshipHealthState warshipHealthState = warShip.getWarshipHealthState();
        this.needsRepair = warshipHealthState.needsRepair();
        this.needsAmmunition = warshipHealthState.needsAmmunition();
        this.isFightingCapable = warshipHealthState.isFightingCapable();
    }

    public StateBlock(@Nonnull final WarshipHealthStateAccessor warshipHealthState) {
        Preconditions.checkNotNull(warshipHealthState, "warshipHealthState must not be empty");

        this.isDeleted = !warshipHealthState.isAlive();
        this.isOperational = warshipHealthState.isOperational();
        this.isActive = warshipHealthState.isAlive();
        this.needsRepair = warshipHealthState.needsRepair();
        this.needsAmmunition = warshipHealthState.needsAmmunition();
        this.isFightingCapable = warshipHealthState.isFightingCapable();
    }
}
