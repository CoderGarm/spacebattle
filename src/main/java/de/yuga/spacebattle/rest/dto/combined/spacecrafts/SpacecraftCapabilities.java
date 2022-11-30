package de.yuga.spacebattle.rest.dto.combined.spacecrafts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.SpacecraftCalculator;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.FleetSnapshot;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthStateAccessor;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
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

        setValues(new SpacecraftCalculator().getSpaceCraftCapabilities(fleet));
    }

    @JsonIgnore
    private void setValues(@Nonnull final SpacecraftCapabilities spaceCraftCapabilities) {
        Preconditions.checkNotNull(spaceCraftCapabilities, "spaceCraftCapabilities must not be empty");

        capabilities.addAll(spaceCraftCapabilities.getCapabilities());
    }

    public SpacecraftCapabilities(@Nonnull final Collection<WarshipHealthStateAccessor> warshipHealthStateSnapshots) {
        Preconditions.checkNotNull(warshipHealthStateSnapshots, "warshipHealthStateSnapshots shouldn't be null!");

        setValues(new SpacecraftCalculator().getSpaceCraftCapabilities(warshipHealthStateSnapshots));
    }

    public SpacecraftCapabilities(@Nonnull final ShipClass shipClass) {
        Preconditions.checkNotNull(shipClass, "shipClass must not be empty");

        setValues(new SpacecraftCalculator().getSpaceCraftCapabilities(shipClass));
    }

    public SpacecraftCapabilities(@Nonnull final Map<ShipClass, Integer> shipClasses) {
        Preconditions.checkNotNull(shipClasses, "shipClasses must not be empty");

        setValues(new SpacecraftCalculator().getSpaceCraftCapabilities(shipClasses));
    }

    public SpacecraftCapabilities(@Nonnull final de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthStateAccessor warshipHealthState) {
        Preconditions.checkNotNull(warshipHealthState, "warshipHealthState must not be empty");

        setValues(new SpacecraftCalculator().getSpaceCraftCapabilities(warshipHealthState));
    }

    public SpacecraftCapabilities(@Nonnull final FleetSnapshot fleetSnapshot) {
        Preconditions.checkNotNull(fleetSnapshot, "fleetSnapshot must not be empty");

        setValues(new SpacecraftCalculator().getSpaceCraftCapabilities(fleetSnapshot));
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
