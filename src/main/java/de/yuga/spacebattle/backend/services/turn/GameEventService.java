package de.yuga.spacebattle.backend.services.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.dto.FleetClash;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.enums.physics.EMassMetric;
import de.yuga.spacebattle.backend.services.MasterOfTheUniverseService;
import org.apache.commons.lang3.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameEventService {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(GameEventService.class);

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
    public List<FleetClash> organize(@Nonnull final Map<FleetOrbit, List<Fleet>> fleetsByOrbit) {
        Preconditions.checkNotNull(fleetsByOrbit, "fleetsByOrbit must not be empty");

        final Set<FleetOrbit> candidates = fleetsByOrbit.entrySet().stream()
                .filter(e -> e.getValue().size() > 1 && e.getValue().stream().map(Fleet::getOwner).collect(Collectors.toSet()).size() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        final List<FleetClash> result = new ArrayList<>();

        if (!isWarHarvest23()) {
            LOGGER.info("Setting up regular fleet clashes");
            for (final FleetOrbit fleetOrbit : fleetsByOrbit.keySet().stream().filter(candidates::contains).collect(Collectors.toSet())) {
                final List<Fleet> fleets = fleetsByOrbit.get(fleetOrbit);
                setUpFleetClashes(fleetOrbit, fleets, result);
            }
            return result;
        }

        LOGGER.info("Setting up war harvest fleet clashes");
        for (final FleetOrbit fleetOrbit : fleetsByOrbit.keySet().stream().filter(candidates::contains).collect(Collectors.toSet())) {
            final List<Fleet> participatingFleets = fleetsByOrbit.get(fleetOrbit);

            final Set<Fleet> interceptFleets = participatingFleets.stream().filter(f -> f.getName().startsWith(INTERCEPT_PREFIX)).collect(Collectors.toSet());
            if (interceptFleets.isEmpty()) {
                // process regularly on single or no intercept fleet
                setUpFleetClashes(fleetOrbit, participatingFleets, result);
            } else {
                // assign combatants based on event status
                matchInterceptFleetsToCombatants(participatingFleets, result, fleetOrbit);
                final Set<Fleet> otherFleets = participatingFleets.stream().filter(f -> !f.getName().startsWith(INTERCEPT_PREFIX)).collect(Collectors.toSet());
                if (otherFleets.stream().map(Fleet::getOwner).collect(Collectors.toSet()).size() >= 2) {
                    // process regularly on single or no intercept fleet
                    setUpFleetClashes(fleetOrbit, otherFleets, result);
                }
            }
        }

        return result;
    }

    private static void matchInterceptFleetsToCombatants(@Nonnull final Collection<Fleet> fleets,
                                                         @Nonnull final List<FleetClash> result,
                                                         @Nonnull final FleetOrbit fleetOrbit) {
        Preconditions.checkNotNull(fleets, "fleets must not be empty");
        Preconditions.checkNotNull(result, "result must not be empty");
        Preconditions.checkNotNull(fleetOrbit, "fleetOrbit must not be empty");

        final Set<Owner> participants = fleets.stream().filter(f -> f.getName().startsWith(INTERCEPT_PREFIX)).map(Fleet::getOwner).collect(Collectors.toSet());
        for (final Owner attacker : participants) {
            final Set<Fleet> attackersInterceptFleets = fleets.stream()
                    .filter(f -> f.getName().startsWith(INTERCEPT_PREFIX))
                    .filter(f -> f.getOwner().equals(attacker))
                    .collect(Collectors.toSet());

            for (int i = 0; i < attackersInterceptFleets.size(); i++) {
                final List<Fleet> otherFleets = fleets.stream()
                        .filter(fleet -> !isCoalition(attacker, fleet) && !fleet.getName().startsWith(INTERCEPT_PREFIX))
                        .collect(Collectors.toList());

                final Set<Fleet> pirates = otherFleets.stream()
                        .filter(fleet -> fleet.getOwner().getUsername().equals(MasterOfTheUniverseService.PIRATE))
                        .collect(Collectors.toSet());

                final Fleet biggestAttacker = attackersInterceptFleets.stream().max(Comparator.comparing(GameEventService::getTonnage)).orElse(null);
                final Fleet biggestOther = (!pirates.isEmpty() ? pirates : otherFleets).stream().max(Comparator.comparing(GameEventService::getTonnage)).orElse(null);

                if (biggestOther != null) {
                    result.add(new FleetClash(fleetOrbit, List.of(biggestAttacker, biggestOther)));
                    fleets.remove(biggestAttacker);
                    fleets.remove(biggestOther);
                }
            }
        }
    }

    private static void setUpFleetClashes(@Nonnull final FleetOrbit fleetOrbit,
                                          @Nonnull final Collection<Fleet> fleets,
                                          @Nonnull final Collection<FleetClash> result) {
        Preconditions.checkNotNull(fleetOrbit, "fleetOrbit must not be empty");
        Preconditions.checkNotNull(fleets, "fleets must not be empty");
        Preconditions.checkNotNull(result, "result must not be empty");

        final Set<Owner> participants = fleets.stream().map(Fleet::getOwner).collect(Collectors.toSet());
        for (final Owner attacker : participants) {
            final Set<Fleet> attackersFleets = fleets.stream().filter(f -> f.getOwner().equals(attacker)).collect(Collectors.toSet());

            for (int i = 0; i < attackersFleets.size(); i++) {
                final Set<Fleet> otherFleets = fleets.stream().filter(others -> !isCoalition(attacker, others)).collect(Collectors.toSet());

                final Fleet biggestAttacker = attackersFleets.stream().max(Comparator.comparing(GameEventService::getTonnage)).orElse(null);
                final Fleet biggestOther = otherFleets.stream().max(Comparator.comparing(GameEventService::getTonnage)).orElse(null);

                if (biggestOther != null) {
                    result.add(new FleetClash(fleetOrbit, List.of(biggestAttacker, biggestOther)));
                    fleets.remove(biggestAttacker);
                    fleets.remove(biggestOther);
                }
            }
        }
    }

    private static boolean isCoalition(@Nonnull final Owner owner, @Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(owner, "owner must not be empty");
        Preconditions.checkNotNull(fleet, "fleet must not be empty");

        final Owner fleetOwner = fleet.getOwner();
        final Alliance fleetAlliance = fleetOwner.getHumanOwner() != null ? fleetOwner.getHumanOwner().getAlliance() : null;
        final Alliance ownerAlliance = owner.getHumanOwner() != null ? owner.getHumanOwner().getAlliance() : null;

        return fleetOwner.equals(owner) || (fleetAlliance != null && fleetAlliance.equals(ownerAlliance));
    }

    @Nonnull
    private static Integer getTonnage(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet must not be empty");

        return fleet.getTonnage(EMassMetric.KT).getCoordinate().intValue();
    }
}
