package de.yuga.spacebattle.backend.calculator;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.physics.Mass;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.enums.physics.EMassMetric;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.SpacecraftCapacityAreas;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Map;

import static de.yuga.spacebattle.backend.calculator.SpacecraftTonnageCalculator.MC;

public class CargoCalculator {

    /**
     * A single capacity unit of a spacecraft multiplied by this is the amount of resources the capacity represents.<br>
     * <br>
     * In short: 1 capacity are 1000 resources or a mix of them.
     */
    private static final BigDecimal CAPACITY_TO_RESOURCE_UNIT_CONVERSION_FACTOR = BigDecimal.valueOf(1000);

    private CargoCalculator() {
    }

    public static long getResourceUnitsPer(@Nonnull final Mass mass) {
        Preconditions.checkNotNull(mass, "mass must not be empty");

        return mass.getCoordinateInMetric(EMassMetric.T).multiply(CAPACITY_TO_RESOURCE_UNIT_CONVERSION_FACTOR, MC).longValue();
    }

    public static long getFreeCargoUnits(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet must not be empty");

        final Mass cargoHold = new SpacecraftCapacityAreas(fleet).getCargoHold();
        final long units = CargoCalculator.getResourceUnitsPer(cargoHold);
        final Map<EResourceType, Long> resources = fleet.getResourceDeposit().getResources();
        final Long loaded = resources.values().stream().reduce(0L, Long::sum);
        return units - loaded;
    }
}
