package de.yuga.spacebattle.backend.services.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.dto.FleetClash;
import de.yuga.spacebattle.backend.dto.physics.Mass;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.account.forum.ForumMessage;
import de.yuga.spacebattle.backend.entities.account.forum.ForumThread;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.FleetSnapshot;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.battle.BattleReport;
import de.yuga.spacebattle.backend.enums.physics.EMassMetric;
import de.yuga.spacebattle.backend.services.MasterOfTheUniverseService;
import de.yuga.spacebattle.backend.services.account.ForumService;
import de.yuga.spacebattle.backend.services.account.OwnerService;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
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
    private static final Range<Tick> WAR_HARVEST_2023 = Range.between(new Tick(244), new Tick(255), Tick::compareTo);

    @Nonnull
    public static final String INTERCEPT_PREFIX = "INTERCEPT";

    @Nonnull
    public static final String WAR_HARVEST_2023_PREFIX = "WAR_HARVEST_2023: ";

    @Nonnull
    private final TickTimeService timeService;

    @Nonnull
    private final ForumService forumService;

    @Nonnull
    private final OwnerService ownerService;

    @Nonnull
    private final FleetService fleetService;

    @Nonnull
    private final PlanetService planetService;

    @Autowired
    public GameEventService(@Nonnull final TickTimeService timeService,
                            @Nonnull final ForumService forumService,
                            @Nonnull final OwnerService ownerService,
                            @Nonnull final FleetService fleetService,
                            @Nonnull final PlanetService planetService) {
        this.timeService = Preconditions.checkNotNull(timeService, "timeService must not be empty");
        this.forumService = Preconditions.checkNotNull(forumService, "forumService must not be empty");
        this.ownerService = Preconditions.checkNotNull(ownerService, "ownerService must not be empty");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService must not be empty");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
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

    @Nonnull
    public ForumMessage warHarvestClaimResponse(@Nonnull final ForumMessage forumMessage) {
        Preconditions.checkNotNull(forumMessage, "forumMessage must not be empty");

        final ForumThread forumThread = forumMessage.getForumThread();
        if (!isWarHarvest23() || forumThread.getId() != 149 || forumMessage.getAuthor().getHumanOwner() == null) {
            return forumMessage;
        }

        final String message = forumMessage.getMessage();

        final User author = forumMessage.getAuthor().getHumanOwner();
        final List<Fleet> fleets = fleetService.findAllFleetsWithoutMovementByUser(author.getId());
        final Set<Fleet> fleetsInGoalOrbit = fleets.stream()
                .filter(f -> f.getName().startsWith(INTERCEPT_PREFIX))
                .filter(f -> f.getOrbit() != null)
                .filter(f -> f.getOrbit().getPlanet() != null)
                .filter(f -> f.getOrbit().getPlanet().getOwner() != null)
                .filter(f -> f.getOrbit().getPlanet().getOwner().getId() == 15)
                .filter(fleet -> {
                    final Planet planet = fleet.getOrbit().getPlanet();
                    final String name = planet.getName();
                    return message.toLowerCase().contains(name.toLowerCase());
                }).collect(Collectors.toSet());

        final Set<Fleet> onlyFleetInOrbit = fleetsInGoalOrbit.stream()
                .filter(f -> f.getOrbit() != null)
                .filter(f -> f.getOrbit().getPlanet() != null)
                .filter(fleet -> {
                    final Set<Fleet> allAnchoredForPlanet = fleetService.findAllAnchoredForPlanet(fleet.getOrbit().getPlanet());
                    allAnchoredForPlanet.removeIf(f -> f.getOwner().equals(fleet.getOwner()));
                    return allAnchoredForPlanet.isEmpty();
                }).collect(Collectors.toSet());

        final Set<Planet> conquered = onlyFleetInOrbit.stream()
                .filter(f -> f.getOrbit() != null)
                .filter(f -> f.getOrbit().getPlanet() != null)
                .map(f -> f.getOrbit().getPlanet()).collect(Collectors.toSet());

        if (conquered.isEmpty()) {
            return forumMessage;
        }

        conquered.forEach(p -> p.setOwner(author));
        planetService.saveAll(conquered);

        LOGGER.info(WAR_HARVEST_2023_PREFIX + author.getUsername() + " has claimed '" + conquered.stream().map(p -> p.getName() + "(" + p.getId() + ")").collect(Collectors.joining(", ")) + "'");

        final Owner pirate = ownerService.findByUsername(MasterOfTheUniverseService.PIRATE);
        Preconditions.checkNotNull(pirate, "pirate must not be empty");

        final ArrayList<Planet> planets = new ArrayList<>(conquered);

        String prey = "";

        for (final Planet p : planets) {
            //noinspection StringConcatenationInLoop
            prey += p.getName() + ", " + p.getSystem().getName() + "  \n";
        }

        String text = "You Sir,  \n";
        text += "are great!\n";
        text += "\n";
        text += "You liberated the following planets by claiming it with your INTERCEPT fleet in the orbit!\n";
        text += "\n";
        text += prey;
        text += "\n";
        text += "Sincerely,  \n";
        text += "Kersey Blackbeard\n";
        text += "Kersey Outpost " + planets.get(0).getName();

        return forumService.createForumMessage(forumThread, pirate, text);
    }

    public void logResult(@Nonnull final BattleReport battleReport) {
        Preconditions.checkNotNull(battleReport, "battleReport must not be empty");

        if (!isWarHarvest23()) {
            return;
        }

        final Set<FleetSnapshot> resultingFleets = battleReport.getParticipatingFleets();

        // fixme reload active ships and remove comment in log
        for (final FleetSnapshot snap : resultingFleets) {
            final Fleet fleet = snap.getFleet();
            final Mass tonnage = fleet.getTonnage(EMassMetric.KT);
            final Mass snapTonnage = snap.getTonnage(EMassMetric.KT);
            LOGGER.info(WAR_HARVEST_2023_PREFIX + "Battle for Planet {} - {} starts with fleet {} and a mass of {} and ends with a mass of {} - not reliable",
                    battleReport.getVenue().getPlanet().getName(),
                    fleet.getOwner().getUsername(),
                    fleet.getName(),
                    tonnage,
                    snapTonnage);
        }

    }
}
