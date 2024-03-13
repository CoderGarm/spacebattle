package de.yuga.spacebattle.backend.services.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.dto.FleetClash;
import de.yuga.spacebattle.backend.dto.physics.Mass;
import de.yuga.spacebattle.backend.entities.account.NonPlayerCharacter;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.account.forum.ForumMessage;
import de.yuga.spacebattle.backend.entities.account.forum.ForumThread;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.FleetSnapshot;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.battle.BattleReport;
import de.yuga.spacebattle.backend.entities.turn.battle.SharedBattleReport;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthStateSnapshot;
import de.yuga.spacebattle.backend.entities.turn.mission.HeatMap;
import de.yuga.spacebattle.backend.enums.ECapacityAreaType;
import de.yuga.spacebattle.backend.enums.EMissionType;
import de.yuga.spacebattle.backend.enums.physics.EMassMetric;
import de.yuga.spacebattle.backend.services.MasterOfTheUniverseService;
import de.yuga.spacebattle.backend.services.account.ForumService;
import de.yuga.spacebattle.backend.services.account.OwnerService;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.WarShipService;
import de.yuga.spacebattle.backend.services.events.RankingService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.turn.battle.combat.WarshipHealthStateService;
import de.yuga.spacebattle.backend.services.turn.tick.mission.HeatMapService;
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
    private static final Range<Tick> WAR_HARVEST_2023 = Range.between(new Tick(244), new Tick(257), Tick::compareTo);

    @Nonnull
    private static final Range<Tick> TOURNAMENT_24 = Range.between(new Tick(311), new Tick(326), Tick::compareTo);

    @Nonnull
    public static final String INTERCEPT_PREFIX = "INTERCEPT";

    @Nonnull
    public static final String TOURNAMENT_PREFIX = "TD";

    @Nonnull
    public static final String TOURNAMENT_v1_PREFIX = "TD1-";

    @Nonnull
    public static final String TOURNAMENT_v3_PREFIX = "TD3-";

    @Nonnull
    public static final String TOURNAMENT_v5_PREFIX = "TD5-";

    @Nonnull
    public static final String WAR_HARVEST_2023_PREFIX = "WAR_HARVEST_2023: ";

    @Nonnull
    public static final String TOURNAMENT_2024_PREFIX = "TOURNAMENT_2024: ";

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

    @Nonnull
    private final RankingService rankingService;

    @Nonnull
    private final HeatMapService heatMapService;

    @Nonnull
    private final WarShipService warShipService;

    @Nonnull
    private final WarshipHealthStateService warshipHealthStateService;

    @Autowired
    public GameEventService(@Nonnull final TickTimeService timeService,
                            @Nonnull final ForumService forumService,
                            @Nonnull final OwnerService ownerService,
                            @Nonnull final FleetService fleetService,
                            @Nonnull final PlanetService planetService,
                            @Nonnull final RankingService rankingService,
                            @Nonnull final HeatMapService heatMapService,
                            @Nonnull final WarShipService warShipService,
                            @Nonnull final WarshipHealthStateService warshipHealthStateService) {
        this.timeService = Preconditions.checkNotNull(timeService, "timeService must not be empty");
        this.forumService = Preconditions.checkNotNull(forumService, "forumService must not be empty");
        this.ownerService = Preconditions.checkNotNull(ownerService, "ownerService must not be empty");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService must not be empty");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
        this.rankingService = Preconditions.checkNotNull(rankingService, "rankingService must not be empty");
        this.heatMapService = Preconditions.checkNotNull(heatMapService, "heatMapService must not be empty");
        this.warShipService = Preconditions.checkNotNull(warShipService, "warShipService must not be empty");
        this.warshipHealthStateService = Preconditions.checkNotNull(warshipHealthStateService, "warshipHealthStateService must not be empty");
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isWarHarvest23() {
        final Tick today = timeService.getToday();
        return WAR_HARVEST_2023.contains(today);
    }

    public boolean isTournament24() {
        final Tick today = timeService.getToday();
        return TOURNAMENT_24.contains(today);
    }

    @Nonnull
    public List<FleetClash> organize(@Nonnull final Map<FleetOrbit, List<Fleet>> fleetsByOrbit) {
        Preconditions.checkNotNull(fleetsByOrbit, "fleetsByOrbit must not be empty");

        final Set<FleetOrbit> candidates = fleetsByOrbit.entrySet().stream()
                .filter(e -> e.getValue().size() > 1 && e.getValue().stream().map(Fleet::getOwner).collect(Collectors.toSet()).size() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        final List<FleetClash> result = new ArrayList<>();

        if (isTournament24()) {
            LOGGER.info("Setting up tournament fleet clashes");
            for (final FleetOrbit fleetOrbit : fleetsByOrbit.keySet().stream().filter(candidates::contains).collect(Collectors.toSet())) {
                final List<Fleet> participatingFleets = fleetsByOrbit.get(fleetOrbit);

                final Set<Fleet> eventFleets = participatingFleets.stream().filter(f -> f.getName().startsWith(TOURNAMENT_PREFIX)).collect(Collectors.toSet());
                if (eventFleets.isEmpty()) {
                    // process regularly on single or no intercept fleet
                    setUpFleetClashes(fleetOrbit, participatingFleets, result);
                } else {
                    // assign combatants based on event status
                    matchTournamentFleetsToCombatants(participatingFleets, result, fleetOrbit);
                }
            }
            return result;
        }

        if (isWarHarvest23()) {
            LOGGER.info("Setting up war harvest fleet clashes");
            for (final FleetOrbit fleetOrbit : fleetsByOrbit.keySet().stream().filter(candidates::contains).collect(Collectors.toSet())) {
                final List<Fleet> participatingFleets = fleetsByOrbit.get(fleetOrbit);

                final Set<Fleet> eventFleets = participatingFleets.stream().filter(f -> f.getName().startsWith(INTERCEPT_PREFIX)).collect(Collectors.toSet());
                if (eventFleets.isEmpty()) {
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

        LOGGER.info("Setting up regular fleet clashes");
        for (final FleetOrbit fleetOrbit : fleetsByOrbit.keySet().stream().filter(candidates::contains).collect(Collectors.toSet())) {
            final List<Fleet> fleets = fleetsByOrbit.get(fleetOrbit);
            setUpFleetClashes(fleetOrbit, fleets, result);
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

    private static void matchTournamentFleetsToCombatants(@Nonnull final Collection<Fleet> fleets,
                                                          @Nonnull final List<FleetClash> result,
                                                          @Nonnull final FleetOrbit fleetOrbit) {
        Preconditions.checkNotNull(fleets, "fleets must not be empty");
        Preconditions.checkNotNull(result, "result must not be empty");
        Preconditions.checkNotNull(fleetOrbit, "fleetOrbit must not be empty");

        final Set<Owner> participants = fleets.stream().filter(f -> f.getName().startsWith(TOURNAMENT_PREFIX)).map(Fleet::getOwner).collect(Collectors.toSet());
        if (participants.size() != 2 || fleets.size() != 2) {
            LOGGER.info(TOURNAMENT_2024_PREFIX + "Ranked Battle sabotaged by {}", participants.stream().map(Owner::getUsername).collect(Collectors.joining(", ")));
            return;
        }
        final List<Fleet> list = new ArrayList<>(fleets);
        result.add(new FleetClash(fleetOrbit, List.of(list.get(0), list.get(1))));
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

        final Set<HeatMap> heatMap = heatMapService.findHeatForPlanets(conquered, EMissionType.PIRATE_RAID);
        final Set<Planet> known = heatMap.stream().map(HeatMap::getPlanet).collect(Collectors.toSet());
        final Set<Planet> withoutHeat = conquered.stream().filter(h -> !known.contains(h)).collect(Collectors.toSet());

        final List<HeatMap> newHeat = withoutHeat.stream()
                .filter(p -> p.getOwner() != null)
                .map(p -> new HeatMap(p, EMissionType.PIRATE_RAID, p.getOwner().getId() == 3 ? 30 : 15))
                .collect(Collectors.toList());
        heatMapService.saveAll(newHeat);


        final List<Planet> planets = planetService.findWithConstructions(conquered);

        final int gainedLevels = planets.stream().map(Planet::getConstructions).flatMap(Collection::stream).mapToInt(Construction::getLevel).sum();

        LOGGER.info(WAR_HARVEST_2023_PREFIX + author.getUsername() + " has claimed the planet(s) '"
                + conquered.stream().map(p -> p.getName() + "(" + p.getId() + ")").collect(Collectors.joining(", ")) + "'"
                + " with collected gained construction levels of '{}'.", gainedLevels);

        rankingService.addPoints(author, planets.size(), gainedLevels);

        final Owner pirate = ownerService.findByUsername(MasterOfTheUniverseService.PIRATE);
        Preconditions.checkNotNull(pirate, "pirate must not be empty");

        final String text = createConquerText(planets);
        return forumService.createForumMessage(forumThread, pirate, text);
    }

    @Nonnull
    private static String createConquerText(@Nonnull final List<Planet> planets) {
        Preconditions.checkNotNull(planets, "planets must not be empty");

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
        return text;
    }

    public void logResult(@Nonnull final SharedBattleReport sharedBattleReport, @Nonnull final BattleReport battleReport, @Nonnull final Set<WarShip> losses) {
        Preconditions.checkNotNull(sharedBattleReport, "sharedBattleReport must not be empty");
        Preconditions.checkNotNull(battleReport, "battleReport must not be empty");
        Preconditions.checkNotNull(losses, "losses must not be empty");

        logTournamentResult(sharedBattleReport, battleReport, losses);
        logInterceptResult(sharedBattleReport, battleReport, losses);
    }

    private void logTournamentResult(@Nonnull final SharedBattleReport sharedBattleReport, @Nonnull final BattleReport battleReport, @Nonnull final Set<WarShip> losses) {
        Preconditions.checkNotNull(sharedBattleReport, "sharedBattleReport must not be empty");
        Preconditions.checkNotNull(battleReport, "battleReport must not be empty");
        Preconditions.checkNotNull(losses, "losses must not be empty");

        if (!isTournament24()) {
            return;
        }
        // todo multicombat will affect this

        final User player = sharedBattleReport.getParticipatingUsers().stream().map(Owner::getHumanOwner).filter(Objects::nonNull).findFirst().orElseThrow(NullPointerException::new);
        final User other = sharedBattleReport.getParticipatingUsers().stream().map(Owner::getHumanOwner).filter(Objects::nonNull).filter(o1 -> !o1.equals(player)).findFirst().orElseThrow(NullPointerException::new);

        final List<String> names = battleReport.getParticipatingFleets().stream()
                .map(FleetSnapshot::getFleet)
                .map(Fleet::getName)
                .collect(Collectors.toList());

        final boolean matchingNames = names.stream().filter(name -> name.startsWith(TOURNAMENT_PREFIX)).count() == 2;
        if (!matchingNames) {
            return;
        }

        final boolean is1v1 = names.stream().filter(name -> name.startsWith(TOURNAMENT_v1_PREFIX)).count() == 2;
        final boolean is3v3 = names.stream().filter(name -> name.startsWith(TOURNAMENT_v3_PREFIX)).count() == 2;
        final boolean is5v5 = names.stream().filter(name -> name.startsWith(TOURNAMENT_v5_PREFIX)).count() == 2;

        if (!is1v1 && !is3v3 && !is5v5) {
            LOGGER.info(TOURNAMENT_2024_PREFIX + "Ranked Battle broken - {}. No Match mode detectable,", battleReport.getId());
            return;
        }

        final Set<WarShip> warships = battleReport.getParticipatingFleets().stream()
                .map(FleetSnapshot::getShips)
                .flatMap(Collection::stream)
                .map(WarshipHealthStateSnapshot::getWarShip)
                .collect(Collectors.toSet());

        warships.removeAll(losses);

        final Set<User> owner = warships.stream().map(WarShip::getShipClass).map(ShipClass::getHumanOwner).collect(Collectors.toSet());
        final User winner = new ArrayList<>(owner).get(0);
        rankingService.addPoints(winner, is1v1, is3v3, is5v5);

        LOGGER.info(TOURNAMENT_2024_PREFIX + "Ranked Battle - {} won {}",
                winner.getUsername(),
                (is1v1 ? "1v1" : is3v3 ? "3v3" : is5v5 ? "5v5" : ""));
    }

    private void logInterceptResult(@Nonnull final SharedBattleReport sharedBattleReport, @Nonnull final BattleReport battleReport, @Nonnull final Set<WarShip> losses) {
        Preconditions.checkNotNull(sharedBattleReport, "sharedBattleReport must not be empty");
        Preconditions.checkNotNull(battleReport, "battleReport must not be empty");
        Preconditions.checkNotNull(losses, "losses must not be empty");

        final boolean isNotAPiratePlanet = battleReport.getVenue().getPlanet() == null || battleReport.getVenue().getPlanet().getOwner() == null || battleReport.getVenue().getPlanet().getOwner().getId() != 15;
        if (!isWarHarvest23() || isNotAPiratePlanet) {
            return;
        }
        // todo multicombat will affect this

        final User player = sharedBattleReport.getParticipatingUsers().stream().map(Owner::getHumanOwner).filter(Objects::nonNull).findFirst().orElseThrow(NullPointerException::new);
        final NonPlayerCharacter pirate = sharedBattleReport.getParticipatingUsers().stream().map(Owner::getNpcOwner).filter(Objects::nonNull).findFirst().orElse(null);

        if (pirate == null) {
            return;
        }

        final String fleetName = battleReport.getParticipatingFleets().stream()
                .map(FleetSnapshot::getFleet)
                .filter(f -> f.getOwner().equals(player))
                .map(Fleet::getName).findFirst().orElse("NO NAME FOUND");

        if (!fleetName.startsWith(INTERCEPT_PREFIX)) {
            return;
        }

        final Mass tonnageDestroyed = losses.stream()
                .filter(f -> f.getFleet() != null)
                .filter(f -> f.getFleet().getOwner().equals(pirate))
                .map(w -> w.getShipClass().getTonnage(ECapacityAreaType.OVERALL)).reduce(Mass.ZERO, Mass::add);

        final Mass tonnageLoss = losses.stream()
                .filter(f -> f.getFleet() != null)
                .filter(f -> f.getFleet().getOwner().equals(player))
                .map(w -> w.getShipClass().getTonnage(ECapacityAreaType.OVERALL)).reduce(Mass.ZERO, Mass::add);

        rankingService.addPoints(player, tonnageDestroyed, tonnageLoss);

        LOGGER.info(WAR_HARVEST_2023_PREFIX + "Battle for Planet {} - {} starts with fleet {} and destroyed a ship mass of {} and ends with a mass loss of {}",
                battleReport.getVenue().getPlanet().getName(),
                player.getUsername(),
                fleetName,
                tonnageDestroyed,
                tonnageLoss);
    }

    public void repairAllWarships() {
        if (isTournament24()) {
            LOGGER.info("Repair Tournament Fleets");

            final Tick today = timeService.getToday();
            final Set<Fleet> toRepair = fleetService.findTournamentFleets()
                    .stream()
                    .filter(f -> f.getName().startsWith(GameEventService.TOURNAMENT_v1_PREFIX) || f.getName().startsWith(GameEventService.TOURNAMENT_v3_PREFIX))
                    .collect(Collectors.toSet());

            final Set<WarShip> warShips = toRepair.stream()
                    .map(Fleet::getAliveShips)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());

            warShips.forEach(warShip -> warShip.getWarshipHealthState().repair(today));
            warShipService.saveAll(warShips);

            toRepair.forEach(o -> o.setOperational(today));
            fleetService.saveAll(toRepair);
        }
    }
}
