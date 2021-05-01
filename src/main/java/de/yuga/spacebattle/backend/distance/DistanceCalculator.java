package de.yuga.spacebattle.backend.distance;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class DistanceCalculator {

    /**
     * Calculates the time-to-travel from the actual sojourn to the given target.
     *
     * @param fleet  the fleet with it's actual sojourn
     * @param planet the target
     * @return the time-to-travel in ticks, not below one
     */
    public static int calculateTimeToTravel(@Nonnull final Fleet fleet, @Nonnull final Planet planet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");
        Preconditions.checkArgument(fleet.getOrbit() != null, "fleet's orbit shouldn't be null here");

        FleetOrbit fleetOrbit = fleet.getOrbit();
        Orbit systemFleetOrbit = fleetOrbit.getSystem().getOrbit();
        Orbit targetSystemOrbit = planet.getSystem().getOrbit();

        BigDecimal distance = systemFleetOrbit.getDistance(targetSystemOrbit);
        BigDecimal ftlRangePerTick = fleet.getFTLRangePerTick();
        int ticksToTravel = distance.divide(ftlRangePerTick, 1, RoundingMode.UP).intValue();
        if (ticksToTravel == 0) {
            return 1;
        } else if (ticksToTravel < 0) {
            throw new NotifySBUserException("mathe genius, check that please");
        }
        return ticksToTravel;
    }


}
