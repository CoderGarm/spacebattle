package de.yuga.spacebattle.rest.dto.combined.spacecrafts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.SpacecraftCalculator;
import de.yuga.spacebattle.backend.dto.physics.Mass;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.FleetSnapshot;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthStateSnapshot;
import de.yuga.spacebattle.backend.enums.ECapacityAreaType;
import de.yuga.spacebattle.backend.enums.physics.EMassMetric;
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

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The tonnage of freight which can be taken over.")
    private Mass cargoHold = new Mass(0, EMassMetric.T);

    @JsonProperty
    @Schema(required = true, description = "The amount of passengers can be taken over.")
    private int passengerSpace;

    public SpacecraftCapacityAreas() {
    }

    /**
     * Creates an image of all warship states combined.
     */
    public SpacecraftCapacityAreas(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        setValues(new SpacecraftCalculator().getSpacecraftCapacityAreas(fleet));
        fleet.getAliveShips().stream().map(WarShip::getShipClass).forEach(this::addCargoHolds);
    }

    public SpacecraftCapacityAreas(@Nonnull final ShipClass shipClass) {
        Preconditions.checkNotNull(shipClass, "shipClass must not be empty");

        setValues(new SpacecraftCalculator().getSpacecraftCapacityAreas(shipClass));
        addCargoHolds(shipClass);
    }

    public SpacecraftCapacityAreas(@Nonnull final Map<ShipClass, Integer> shipClasses) {
        Preconditions.checkNotNull(shipClasses, "shipClasses must not be empty");

        setValues(new SpacecraftCalculator().getSpacecraftCapacityAreas(shipClasses));
        shipClasses.forEach((shipClass, amount) -> {
            for (int i = 0; i < amount; i++) {
                addCargoHolds(shipClass);
            }
        });
    }

    public SpacecraftCapacityAreas(@Nonnull final FleetSnapshot fleetSnapshot) {
        Preconditions.checkNotNull(fleetSnapshot, "fleetSnapshot must not be empty");

        setValues(new SpacecraftCalculator().getSpacecraftCapacityAreas(fleetSnapshot));
        fleetSnapshot.getShips().stream().map(WarshipHealthStateSnapshot::getWarShip).map(WarShip::getShipClass).forEach(this::addCargoHolds);
    }

    @JsonIgnore
    private void setValues(@Nonnull final SpacecraftCapacityAreas spaceCraftCapabilities) {
        Preconditions.checkNotNull(spaceCraftCapabilities, "spaceCraftCapabilities must not be empty");

        capacityValues.addAll(spaceCraftCapabilities.getCapacityValues());
    }

    @JsonIgnore
    private void addCargoHolds(@Nonnull final ShipClass shipClass) {
        this.cargoHold = shipClass.getSupportFittings().stream().filter(f -> f.getPassiveModule().isCargo()).map(f -> f.getPassiveModule().getCargoCapacity().multiply(f.getAmount())).reduce(this.cargoHold, Mass::add);
        this.passengerSpace += shipClass.getSupportFittings().stream().filter(f -> f.getPassiveModule().isPassenger()).map(f -> f.getPassiveModule().getPassengers() * f.getAmount()).reduce(0, Integer::sum);
    }

    @Nonnull
    @JsonIgnore
    public List<CapacityValue> getCapacityValues() {
        return capacityValues;
    }

    @Nonnull
    @JsonIgnore
    public Mass getCargoHold() {
        return cargoHold;
    }

    @JsonIgnore
    public int getPassengerSpace() {
        return passengerSpace;
    }

    public SpacecraftCapacityAreas withValues(@Nonnull final Map<ECapacityAreaType, CapacityValue> capacities) {
        Preconditions.checkNotNull(capacities, "capacities must not be empty");

        this.capacityValues.addAll(capacities.values().stream().sorted(CapacityValue::compareTo).collect(Collectors.toList()));
        return this;
    }
}
