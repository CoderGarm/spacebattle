package de.yuga.spacebattle.backend.distance;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class DistanceCalculator {

    /**
     * Calcuates the time-to-travel from the actual sojourn to the given target.
     *
     * @param fleet  the fleet with it's actual sojourn
     * @param planet the target
     * @return the time-to-travel in ticks
     */
    public static int calculateDistance(@Nonnull final Fleet fleet, @Nonnull final Planet planet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        FleetOrbit fleetOrbit = fleet.getOrbit();
        Orbit systemFleetOrbit = fleetOrbit.getSystem().getOrbit();
        Orbit targetSystemOrbit = planet.getSystem().getOrbit();

        BigDecimal distance = systemFleetOrbit.getDistance(targetSystemOrbit);
        BigDecimal ftlRangePerTick = fleet.getFTLRangePerTick();
        int ticksToFly = distance.divide(ftlRangePerTick, 1, RoundingMode.UP).intValue();
        return ticksToFly;
    }


}
