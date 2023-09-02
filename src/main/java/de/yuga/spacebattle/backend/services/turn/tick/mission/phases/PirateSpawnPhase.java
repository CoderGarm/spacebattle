package de.yuga.spacebattle.backend.services.turn.tick.mission.phases;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.MissionRandomizer;
import de.yuga.spacebattle.backend.calculator.distance.NavigationCalculator;
import de.yuga.spacebattle.backend.entities.account.NonPlayerCharacter;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.turn.Move;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.enums.EMissionAction;
import de.yuga.spacebattle.backend.enums.EMissionType;
import de.yuga.spacebattle.backend.services.account.NonPlayerCharacterService;
import de.yuga.spacebattle.backend.services.caches.FleetMovementCache;
import de.yuga.spacebattle.backend.services.caches.MissionCache;
import de.yuga.spacebattle.backend.services.caches.RaidingPirateCache;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.ShipClassService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.WarShipService;
import de.yuga.spacebattle.backend.services.turn.tick.mission.HeatMapService;
import de.yuga.spacebattle.backend.services.turn.tick.mission.MissionPhaseRunner;
import de.yuga.spacebattle.backend.services.turn.tick.mission.RaidingPiratesMission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

import static de.yuga.spacebattle.backend.services.MasterOfTheUniverseService.PIRATE;

/**
 * Spawns a pirate fleet at the hyper limit of the main planet's system and approaches the main planet.
 */
@Service
public class PirateSpawnPhase implements MissionPhaseRunner {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(PirateSpawnPhase.class);

    @Nonnull
    @SuppressWarnings("NotNullFieldNotInitialized")
    private Tick today;

    @Nonnull
    private final NonPlayerCharacterService nonPlayerCharacterService;

    @Nonnull
    private final FleetService fleetService;

    @Nonnull
    private final ShipClassService shipClassService;

    @Nonnull
    private final WarShipService warShipService;

    @Nonnull
    private final FleetMovementCache fleetMovementCache;

    @Nonnull
    private final HeatMapService heatMapService;

    @Nonnull
    private final MissionCache missionCache;

    @Nonnull
    private final RaidingPirateCache raidingPirateCache;

    @Autowired
    public PirateSpawnPhase(@Nonnull final NonPlayerCharacterService nonPlayerCharacterService,
                            @Nonnull final FleetService fleetService,
                            @Nonnull final ShipClassService shipClassService,
                            @Nonnull final WarShipService warShipService,
                            @Nonnull final FleetMovementCache fleetMovementCache,
                            @Nonnull final HeatMapService heatMapService,
                            @Nonnull final MissionCache missionCache,
                            @Nonnull final RaidingPirateCache raidingPirateCache) {
        this.nonPlayerCharacterService = Preconditions.checkNotNull(nonPlayerCharacterService, "nonPlayerCharacterService must not be empty");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService must not be empty");
        this.shipClassService = Preconditions.checkNotNull(shipClassService, "shipClassService must not be empty");
        this.warShipService = Preconditions.checkNotNull(warShipService, "warShipService must not be empty");
        this.fleetMovementCache = Preconditions.checkNotNull(fleetMovementCache, "fleetMovementCache must not be empty");
        this.heatMapService = Preconditions.checkNotNull(heatMapService, "heatMapService must not be empty");
        this.missionCache = Preconditions.checkNotNull(missionCache, "missionCache must not be empty");
        this.raidingPirateCache = Preconditions.checkNotNull(raidingPirateCache, "raidingPirateCache must not be empty");
    }

    @Override
    public void executePhase(@Nonnull final Tick today) {
        this.today = Preconditions.checkNotNull(today, "today must not be empty");

        if (!RaidingPiratesMission.canRun(today)) {
            LOGGER.info("Nothing will be unleashed today");
            return;
        }
        LOGGER.info("Unleash the beast");

        final NonPlayerCharacter pirate = nonPlayerCharacterService.findByUsername(PIRATE);
        Preconditions.checkNotNull(pirate, "pirate must not be empty");

        final List<Planet> targets = detectVictims();
        for (final Planet target : targets) {
            final Fleet pirateFleet = createPirateFleet(target);

            final Owner owner = target.getOwner() != null ? target.getOwner() : Owner.UNCOLONIZED;

            if (MissionRandomizer.shouldWait(EMissionType.PIRATE_RAID, pirateFleet, today)) {
                LOGGER.info("\tWaiting for tomorrow with approaching '" + owner.getUsername() + "' at '" + target.getName() + "'");
                raidingPirateCache.executeNext(today, pirateFleet, EMissionAction.WAIT, EMissionAction.APPROACH);
                missionCache.pirateRaidWait(today, pirateFleet, target);
            }
            // notify spawn
            assert pirateFleet.getOrbit() != null;
            final Move move = new Move(pirateFleet, pirateFleet.getOrbit(), 10, 10);
            fleetMovementCache.add(today, pirateFleet, move, target.getSystem());
        }
    }

    @Nonnull
    private List<Planet> detectVictims() {
        return heatMapService.findHottestPlanets(EMissionType.PIRATE_RAID);
    }

    @Nonnull
    private Fleet createPirateFleet(@Nonnull final Planet target) {
        Preconditions.checkNotNull(target, "target must not be empty");

        final Owner owner = target.getOwner() != null ? target.getOwner() : Owner.UNCOLONIZED;

        final NonPlayerCharacter opponent = nonPlayerCharacterService.findByUsername(PIRATE);
        final List<ShipClass> classList = shipClassService.findAllLatestByOwner(Objects.requireNonNull(opponent));
        final ShipClass ship = classList.get(0);

        final Fleet pirateFleet = createFleet(opponent, target);
        raidingPirateCache.setTarget(today, pirateFleet, target);
        final WarShip warShip = new WarShip("Corsair", target, pirateFleet, ship);
        warShip.setOperational();
        warShipService.save(warShip);
        missionCache.pirateRaidSpawn(today, pirateFleet, target);
        LOGGER.info("\tPirate spawn for '" + owner.getUsername() + "' at '" + target.getName() + "'");
        return pirateFleet;
    }

    @Nonnull
    private Fleet createFleet(@Nonnull final Owner user, @Nonnull final Planet planet) {
        Preconditions.checkNotNull(user, "user must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");

        final FleetOrbit destination = new FleetOrbit(planet.getOrbit(), planet.getSystem());
        final Fleet fleet = new Fleet("Kersey Association", user, destination);
        fleet.setOperational();

        final Orbit positionOnHyperlimit = NavigationCalculator.getPositionOnHyperlimit(fleet, destination);
        fleet.setOrbit(new FleetOrbit(positionOnHyperlimit, planet.getSystem()));

        return fleetService.save(fleet);
    }
}
