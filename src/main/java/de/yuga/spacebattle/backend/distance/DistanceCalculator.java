package de.yuga.spacebattle.backend.distance;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.enums.EModuleType;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class DistanceCalculator {

    /**
     * Calculates the time-to-travel from the actual sojourn to the given target.
     *
     * @param fleet       the fleet
     * @param origin      the origin of this calculation
     * @param destination the target
     * @return the time-to-travel in ticks, not below one
     */
    public static int calculateTimeToTravel(@Nonnull final Fleet fleet, @Nonnull final Planet origin, @Nonnull final Planet destination) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkNotNull(origin, "origin shouldn't be null!");
        Preconditions.checkNotNull(destination, "destination shouldn't be null!");

        int ftlDuration = 0;
        if (!origin.getSystem().equals(destination.getSystem())) {
            // todo calc time from planet to hyper limit
            // https://honorverse.fandom.com/wiki/Hyper_limit
            Orbit targetSystemOrbit = destination.getSystem().getOrbit();

            ftlDuration = getFTLDuration(fleet, origin.getSystem().getOrbit(), targetSystemOrbit);

            // todo calc time from hyper limit to planet
        }

        final Orbit destinationOrbit = destination.getOrbit();
        final int subLightDuration = getSubLightDuration(fleet, origin.getOrbit(), destinationOrbit);

        final int ticksToTravel = ftlDuration + subLightDuration;

        if (ticksToTravel == 0) {
            return 1;
        } else if (ticksToTravel < 0) {
            throw new NotifySBUserException("mathe genius, check that please");
        }
        return ticksToTravel;
    }

    /**
     * Calculates the time-to-travel from the actual sojourn to the given target.
     *
     * @param fleet       the fleet with it's actual sojourn
     * @param destination the target
     * @return the time-to-travel in ticks, not below one
     */
    public static int calculateTimeToTravel(@Nonnull final Fleet fleet, @Nonnull final Planet destination) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkNotNull(destination, "destination shouldn't be null!");
        Preconditions.checkArgument(fleet.getOrbit() != null, "fleet's orbit shouldn't be null here");

        Planet planet = fleet.getOrbit().getPlanet();
        if (planet == null && fleet.getMove() != null) {
            planet = fleet.getMove().getStartOrbit().getPlanet();
        }
        return calculateTimeToTravel(fleet, planet, destination);
    }

    private static int getSubLightDuration(@Nonnull final Fleet fleet,
                                           @Nonnull final Orbit origin,
                                           @Nonnull final Orbit destination) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkNotNull(origin, "origin shouldn't be null!");
        Preconditions.checkNotNull(destination, "destination shouldn't be null!");

        final BigDecimal distance = origin.getDistance(destination);
        final BigDecimal subLightRangePerTick = fleet.getRangePerTick(EModuleType.PROPULSION);
        return distance.divide(subLightRangePerTick, 1, RoundingMode.UP).intValue();
    }

    private static int getFTLDuration(@Nonnull final Fleet fleet,
                                      @Nonnull final Orbit origin,
                                      @Nonnull final Orbit destination) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkNotNull(origin, "origin shouldn't be null!");
        Preconditions.checkNotNull(destination, "destination shouldn't be null!");

        BigDecimal distance = origin.getDistance(destination);
        BigDecimal ftlRangePerTick = fleet.getRangePerTick(EModuleType.FTLPROPULSION);
        return distance.divide(ftlRangePerTick, 1, RoundingMode.UP).intValue();
    }


}
