package de.yuga.spacebattle.rest.dto.combined.spacecrafts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.SpacecraftCalculator;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.FleetSnapshot;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.enums.ECapacityAreaType;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Schema(description = ".")
public class SpacecraftCapacityAreas {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The construction capacity values per area.")
    private final List<CapacityValue> capacityValues = new ArrayList<>();

    public SpacecraftCapacityAreas() {
    }

    /**
     * Creates an image of all warship states combined.
     */
    public SpacecraftCapacityAreas(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        setValues(new SpacecraftCalculator().getSpacecraftCapacityAreas(fleet));
    }

    @JsonIgnore
    private void setValues(@Nonnull final SpacecraftCapacityAreas spaceCraftCapabilities) {
        Preconditions.checkNotNull(spaceCraftCapabilities, "spaceCraftCapabilities must not be empty");

        capacityValues.addAll(spaceCraftCapabilities.getCapacityValues());
    }

    public SpacecraftCapacityAreas(@Nonnull final ShipClass shipClass) {
        Preconditions.checkNotNull(shipClass, "shipClass must not be empty");

        setValues(new SpacecraftCalculator().getSpacecraftCapacityAreas(shipClass));
    }

    public SpacecraftCapacityAreas(@Nonnull final Map<ShipClass, Integer> shipClasses) {
        Preconditions.checkNotNull(shipClasses, "shipClasses must not be empty");

        setValues(new SpacecraftCalculator().getSpacecraftCapacityAreas(shipClasses));
    }

    public SpacecraftCapacityAreas(@Nonnull final FleetSnapshot fleetSnapshot) {
        Preconditions.checkNotNull(fleetSnapshot, "fleetSnapshot must not be empty");

        setValues(new SpacecraftCalculator().getSpacecraftCapacityAreas(fleetSnapshot));
    }

    @Nonnull
    @JsonIgnore
    public List<CapacityValue> getCapacityValues() {
        return capacityValues;
    }

    public SpacecraftCapacityAreas withValues(@Nonnull final Map<ECapacityAreaType, CapacityValue> capacities) {
        Preconditions.checkNotNull(capacities, "capacities must not be empty");

        this.capacityValues.addAll(capacities.values().stream().sorted(CapacityValue::compareTo).collect(Collectors.toList()));
        return this;
    }
}
