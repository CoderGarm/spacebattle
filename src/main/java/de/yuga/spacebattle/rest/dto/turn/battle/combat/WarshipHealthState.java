package de.yuga.spacebattle.rest.dto.turn.battle.combat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.SpacecraftCapabilities;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class WarshipHealthState {

    @JsonProperty
    @Schema(required = true, description = "The current capabilities per module type.")
    private SpacecraftCapabilities spacecraftCapabilities;

    @JsonProperty
    @Schema(required = true, description = "If the ship is alive after damage.")
    private boolean isAlive;

    @JsonProperty
    @Schema(required = true, description = "If the ship is capable of staying in the battle after damage.")
    private boolean isFightingCapable;

    public WarshipHealthState(final boolean isAlive, final boolean isFightingCapable) {
        this.isAlive = isAlive;
        this.isFightingCapable = isFightingCapable;
    }

    public WarshipHealthState(@Nonnull final de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthStateAccessor warshipHealthState) {
        Preconditions.checkNotNull(warshipHealthState, "warshipHealthState shouldn't be null!");

        this.spacecraftCapabilities = new SpacecraftCapabilities(warshipHealthState);
        this.isAlive = warshipHealthState.isAlive();
        this.isFightingCapable = warshipHealthState.isFightingCapable();
    }
}
