package de.yuga.spacebattle.backend.services.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.dto.FleetClash;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.enums.physics.EMassMetric;
import org.apache.commons.lang3.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameEventService {

    @Nonnull
    private final TickTimeService timeService;

    @Nonnull
    private static final Range<Tick> WAR_HARVEST_2023 = Range.between(new Tick(244), new Tick(255), Tick::compareTo);

    @Nonnull
    private static final String INTERCEPT_PREFIX = "INTERCEPT";

    @Autowired
    public GameEventService(@Nonnull final TickTimeService timeService) {
        this.timeService = Preconditions.checkNotNull(timeService, "timeService must not be empty");
    }

    private boolean isWarHarvest23() {
        final Tick today = timeService.getToday();
        return WAR_HARVEST_2023.contains(today);
    }

    @Nonnull
    public List<FleetClash> organize(@Nonnull final List<FleetClash> clashes) {
        Preconditions.checkNotNull(clashes, "clashes must not be empty");

        if (!isWarHarvest23()) {
            return clashes;
        }

        final List<FleetClash> result = new ArrayList<>();

        clashes.forEach(clash -> {
            final FleetOrbit fleetOrbit = clash.getOrbit();
            final List<Fleet> participatingFleets = clash.getParticipatingFleets();

            final Set<Fleet> interceptFleets = participatingFleets.stream().filter(f -> f.getName().startsWith(INTERCEPT_PREFIX)).collect(Collectors.toSet());
            final Set<Fleet> otherFleets = participatingFleets.stream().filter(f -> !f.getName().startsWith(INTERCEPT_PREFIX)).collect(Collectors.toSet());
            if (interceptFleets.size() <= 1) {
                // process regularly on single or no intercept fleet
                result.add(clash);
            }

            if (interceptFleets.size() > 1) {
                // assign combatants based on event status

                final Set<Fleet> iFleets = new HashSet<>(interceptFleets);
                final Set<Fleet> oFleets = new HashSet<>(otherFleets);
                matchInterceptFleetsToCombatants(iFleets, oFleets, result, fleetOrbit);

                if (otherFleets.stream().map(Fleet::getOwner).collect(Collectors.toSet()).size() >= 2) {
                    result.add(new FleetClash(fleetOrbit, oFleets));
                }
            }
        });

        return result;
    }

    private static void matchInterceptFleetsToCombatants(@Nonnull final Set<Fleet> iFleets,
                                                         @Nonnull final Set<Fleet> oFleets,
                                                         @Nonnull final List<FleetClash> result,
                                                         @Nonnull final FleetOrbit fleetOrbit) {
        Preconditions.checkNotNull(iFleets, "iFleets must not be empty");
        Preconditions.checkNotNull(oFleets, "oFleets must not be empty");
        Preconditions.checkNotNull(result, "result must not be empty");
        Preconditions.checkNotNull(fleetOrbit, "fleetOrbit must not be empty");

        for (int i = 0; i < iFleets.size(); i++) {
            final Fleet biggestIntercept = iFleets.stream().max(Comparator.comparing(GameEventService::getTonnage)).orElse(null);
            final Owner owner = biggestIntercept.getOwner();

            final Fleet biggestOther = oFleets.stream()
                    .filter(f -> !f.getOwner().equals(owner))
                    .max(Comparator.comparing(GameEventService::getTonnage)).orElse(null);
            if (biggestOther != null) {
                result.add(new FleetClash(fleetOrbit, List.of(biggestIntercept, biggestOther)));
                iFleets.remove(biggestIntercept);
                oFleets.remove(biggestOther);
            }
        }
    }

    @Nonnull
    private static Integer getTonnage(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet must not be empty");

        return fleet.getTonnage(EMassMetric.KT).getCoordinate().intValue();
    }
}
