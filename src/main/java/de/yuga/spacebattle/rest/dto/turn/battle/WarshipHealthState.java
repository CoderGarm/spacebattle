package de.yuga.spacebattle.rest.dto.turn.battle;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.SpacecraftCapabilities;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class WarshipHealthState {

    @JsonProperty
    @Schema(required = true, description = "")
    private SpacecraftCapabilities spacecraftCapabilities;

    public WarshipHealthState(@Nonnull final de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthState warshipHealthState) {
        Preconditions.checkNotNull(warshipHealthState, "warshipHealthState shouldn't be null!");

        this.spacecraftCapabilities = new SpacecraftCapabilities(warshipHealthState);
    }
}
