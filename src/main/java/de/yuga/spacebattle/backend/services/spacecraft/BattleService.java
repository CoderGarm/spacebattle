package de.yuga.spacebattle.backend.services.spacecraft;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.BattleLogger;
import de.yuga.spacebattle.backend.combat.dto.BattleResult;
import de.yuga.spacebattle.backend.combat.dto.FleetClash;
import de.yuga.spacebattle.backend.combat.main.Cage;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.battle.BattleReport;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthState;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.WarShipService;
import de.yuga.spacebattle.backend.services.turn.GameEventService;
import de.yuga.spacebattle.backend.services.turn.battle.BattleReportService;
import de.yuga.spacebattle.backend.services.turn.battle.combat.WarshipHealthStateService;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BattleService {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(BattleService.class);

    @Nonnull
    private final BattleLogger battleLogger;

    @Nonnull
    private final FleetService fleetService;

    @Nonnull
    private final BattleReportService battleReportService;

    @Nonnull
    private final WarShipService warShipService;

    @Nonnull
    private final WarshipHealthStateService warshipHealthStateService;

    @Nonnull
    private final GameEventService gameEventService;

    @Autowired
    public BattleService(@Nonnull final FleetService fleetService,
                         @Nonnull final BattleReportService battleReportService,
                         @Nonnull final WarShipService warShipService,
                         @Nonnull final BattleLogger battleLogger,
                         @Nonnull final WarshipHealthStateService warshipHealthStateService,
                         @Nonnull final GameEventService gameEventService) {
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService shouldn't be null!");
        this.battleReportService = Preconditions.checkNotNull(battleReportService, "battleReportService shouldn't be null!");
        this.warShipService = Preconditions.checkNotNull(warShipService, "warShipService shouldn't be null!");
        this.battleLogger = Preconditions.checkNotNull(battleLogger, "battleLogger shouldn't be null!");
        this.warshipHealthStateService = Preconditions.checkNotNull(warshipHealthStateService, "warshipHealthStateService must not be empty");
        this.gameEventService = Preconditions.checkNotNull(gameEventService, "gameEventService must not be empty");
    }


    @Nonnull
    private List<FleetClash> findAllFleetClashes() {
        final List<Fleet> nonMovingFleets = fleetService.findAllFleetsWithoutMovement();
        final Map<FleetOrbit, List<Fleet>> fleetsByOrbit = detectActiveFleetsByOrbit(nonMovingFleets);
        return gameEventService.organize(fleetsByOrbit);
    }


    @Nonnull
    private static Map<FleetOrbit, List<Fleet>> detectActiveFleetsByOrbit(@Nonnull final Collection<Fleet> fleets) {
        Preconditions.checkNotNull(fleets, "fleets must not be empty");

        return fleets.stream()
                .filter(Fleet::isActive)
                .filter(f -> f.getOrbit() != null)
                .collect(Collectors.groupingBy(Fleet::getOrbit, Collectors.mapping(Function.identity(), Collectors.toList())));
    }

    @Nonnull
    private List<FleetClash> findFleetClashesAtPlanet(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet must not be empty");

        final Set<Fleet> allFleetsByPlanet = fleetService.findAllFleetsByPlanet(planet);
        final Map<FleetOrbit, List<Fleet>> fleetsByOrbit = detectActiveFleetsByOrbit(allFleetsByPlanet);
        return gameEventService.organize(fleetsByOrbit);
    }

    public void runBattles(@Nonnull final Tick today) {
        Preconditions.checkNotNull(today, "today shouldn't be null!");

        final List<FleetClash> fleetClashes = findAllFleetClashes();
        LOGGER.info("# {} battles for {}", fleetClashes.size(), today);

        final List<BattleReport> reports = new ArrayList<>();
        final List<CompletableFuture<Cage>> futures = new ArrayList<>();
        for (FleetClash fleetClash : fleetClashes) {
            final CompletableFuture<Cage> future = CompletableFuture.supplyAsync(() -> {
                final Cage cage = new Cage(fleetClash, battleLogger);
                cage.handleCombatPhases();
                return cage;
            });
            futures.add(future);
        }
        try {
            // runs the fight
            for (CompletableFuture<Cage> f : futures) {
                final Cage cage = f.get();
                reports.add(processFightingResult(today, cage.getBattleResult()));
            }
        } catch (final ExecutionException | InterruptedException e) {
            e.printStackTrace();
            throw new NotifyWebUserException(e.getMessage());
        }

        LOGGER.info("# {} battles executed for {}", reports.size(), today);
        battleReportService.saveAll(reports);
    }

    @Nonnull
    public List<BattleReport> runBattleAtPlanet(@Nonnull final Tick today, @Nonnull final Planet planet) {
        Preconditions.checkNotNull(today, "today shouldn't be null!");
        Preconditions.checkNotNull(planet, "planet must not be empty");

        final List<FleetClash> fleetClashes = findFleetClashesAtPlanet(planet);
        LOGGER.info("# {} battles for {} at planet {} ('{}')", fleetClashes.size(), today, planet.getName(), planet.getId());

        if (fleetClashes.isEmpty()) {
            return new ArrayList<>();
        }

        final List<BattleReport> reports = new ArrayList<>();
        final List<CompletableFuture<Cage>> futures = new ArrayList<>();
        for (FleetClash fleetClash : fleetClashes) {
            final CompletableFuture<Cage> future = CompletableFuture.supplyAsync(() -> {
                final Cage cage = new Cage(fleetClash, battleLogger);
                cage.handleCombatPhases();
                return cage;
            });
            futures.add(future);
        }
        try {
            // runs the fight
            for (CompletableFuture<Cage> f : futures) {
                final Cage cage = f.get();
                reports.add(processFightingResult(today, cage.getBattleResult()));
            }
        } catch (final ExecutionException | InterruptedException e) {
            e.printStackTrace();
            throw new NotifyWebUserException(e.getMessage());
        }

        LOGGER.info("# {} battles executed for {} at planet {} ('{}')", fleetClashes.size(), today, planet.getName(), planet.getId());
        return battleReportService.saveAll(reports);
    }

    private BattleReport processFightingResult(@Nonnull final Tick latest, @Nonnull final BattleResult battleResult) {
        Preconditions.checkNotNull(latest, "latest shouldn't be null!");
        Preconditions.checkNotNull(battleResult, "fightingResult shouldn't be null!");

        final BattleReport battleReport = battleReportService.save(new BattleReport(latest, battleResult));
        final List<WarShip> warShips = battleResult.getFleetClash()
                .getParticipatingFleets().stream()
                .map(Fleet::getAliveShips)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        final Map<WarShip, de.yuga.spacebattle.backend.combat.round.WarshipHealthState> byResult = battleResult.getWarshipHealthStates();

        final Set<WarshipHealthState> statesToPersist = new HashSet<>();
        warShips.stream()
                .collect(Collectors.toMap(Function.identity(), WarShip::getWarshipHealthState))
                .forEach((warShip, knownState) -> {
                    final de.yuga.spacebattle.backend.combat.round.WarshipHealthState newState = byResult.get(warShip);
                    final boolean needsRepair = knownState.needsRepair(newState);
                    final boolean needsAmmunition = knownState.needsAmmunition(newState);
                    if (needsRepair || needsAmmunition) {
                        knownState.update(newState);
                        statesToPersist.add(knownState);
                    }
                });

        warshipHealthStateService.saveAll(statesToPersist);

        final Set<WarShip> losses = battleResult.getLosses();
        // todo how to handle fighting incapable ships?
        warShipService.markAllAsDestroyed(losses);
        fleetService.markFleetsWithoutShipsAsDeleted(battleResult.getFleetClash().getParticipatingFleets());

        battleLogger.logBattleResult(battleReport, battleResult);
        gameEventService.logResult(battleReport);
        return battleReport;
    }
}
