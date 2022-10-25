package de.yuga.spacebattle.rest.dto.combined.spacecrafts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.SpacecraftCalculator;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Schema(description = ".")
public class SpacecraftCapabilities {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The effect values per module type.")
    private final List<CapabilityValue> capabilities = new ArrayList<>();

    public SpacecraftCapabilities() {
    }

    /**
     * Creates an image of all warship states combined.
     */
    public SpacecraftCapabilities(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        capabilities.addAll(new SpacecraftCalculator().getSpaceCraftCapabilities(fleet).getCapabilities());
    }

    public SpacecraftCapabilities(@Nonnull final ShipClass shipClass) {
        Preconditions.checkNotNull(shipClass, "shipClass must not be empty");

        capabilities.addAll(new SpacecraftCalculator().getSpaceCraftCapabilities(shipClass).getCapabilities());
    }

    public SpacecraftCapabilities(@Nonnull final Map<ShipClass, Integer> shipClasses) {
        Preconditions.checkNotNull(shipClasses, "shipClasses must not be empty");

        capabilities.addAll(new SpacecraftCalculator().getSpaceCraftCapabilities(shipClasses).getCapabilities());
    }

    public SpacecraftCapabilities(@Nonnull final de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthState warshipHealthState) {
        Preconditions.checkNotNull(warshipHealthState, "warshipHealthState must not be empty");

        capabilities.addAll(new SpacecraftCalculator().getSpaceCraftCapabilities(warshipHealthState).getCapabilities());
    }

    @Nonnull
    @JsonIgnore
    public List<CapabilityValue> getCapabilities() {
        return capabilities;
    }

    @JsonIgnore
    public SpacecraftCapabilities withValues(@Nonnull final List<CapabilityValue> capabilities) {
        this.capabilities.addAll(capabilities);
        return this;
    }
}
