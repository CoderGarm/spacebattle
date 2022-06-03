package de.yuga.spacebattle.backend.services.spacecraft;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.BattleLogger;
import de.yuga.spacebattle.backend.combat.dto.BattleResult;
import de.yuga.spacebattle.backend.combat.dto.FleetClash;
import de.yuga.spacebattle.backend.combat.main.Cage;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.battle.BattleReport;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.WarShipService;
import de.yuga.spacebattle.backend.services.turn.battle.BattleReportService;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class BattleService {

    @Nonnull
    private final static Logger LOGGER = LoggerFactory.getLogger(BattleService.class);

    @Nonnull
    private final static BattleLogger battleLogger = new BattleLogger(LOGGER);

    @Nonnull
    private final FleetService fleetService;

    @Nonnull
    private final BattleReportService battleReportService;

    @Nonnull
    private final WarShipService warShipService;

    public BattleService(@Nonnull final FleetService fleetService,
                         @Nonnull final BattleReportService battleReportService,
                         @Nonnull final WarShipService warShipService) {
        Preconditions.checkNotNull(fleetService, "fleetService shouldn't be null!");
        Preconditions.checkNotNull(battleReportService, "battleReportService shouldn't be null!");
        Preconditions.checkNotNull(warShipService, "warShipService shouldn't be null!");

        this.fleetService = fleetService;
        this.battleReportService = battleReportService;
        this.warShipService = warShipService;
    }

    public void runBattles(@Nonnull final Tick today) {
        Preconditions.checkNotNull(today, "today shouldn't be null!");

        final List<BattleReport> reports = new ArrayList<>();
        final List<FleetClash> fleetClashes = fleetService.findAllFleetClashes();
        // todo first step is just to fight all users against each other

        final List<CompletableFuture<Cage>> futures = new ArrayList<>();
        for (FleetClash fleetClash : fleetClashes) {
            final CompletableFuture<Cage> future = CompletableFuture.supplyAsync(() -> {
                final Cage cage = new Cage(fleetClash);
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

        battleReportService.saveAll(reports);
    }

    private BattleReport processFightingResult(@Nonnull final Tick latest, @Nonnull final BattleResult battleResult) {
        Preconditions.checkNotNull(latest, "latest shouldn't be null!");
        Preconditions.checkNotNull(battleResult, "fightingResult shouldn't be null!");
        
        final Set<WarShip> losses = battleResult.getLosses();
        warShipService.deleteAll(losses);
        fleetService.deleteFleetsWithoutShips(battleResult.getFleetClash().getParticipatingFleets());

        battleLogger.logBattleResult(battleResult);

        return new BattleReport(latest, battleResult);
    }
}
