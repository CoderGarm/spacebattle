package de.yuga.spacebattle.backend.services.turn.tick.mission.phases;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.distance.NavigationCalculator;
import de.yuga.spacebattle.backend.dto.account.UserPoints;
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
import de.yuga.spacebattle.backend.enums.EMissionType;
import de.yuga.spacebattle.backend.services.account.NonPlayerCharacterService;
import de.yuga.spacebattle.backend.services.caches.FleetMovementCache;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.ShipClassService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.WarShipService;
import de.yuga.spacebattle.backend.services.turn.tick.mission.HeatMapService;
import de.yuga.spacebattle.backend.services.turn.tick.mission.MissionPhaseRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static de.yuga.spacebattle.backend.services.MasterOfTheUniverseService.PIRATE;

/**
 * Spawns a pirate fleet at the hyper limit of the main planet's system and approaches the main planet.
 */
@Service
public class PirateSpawnPhase implements MissionPhaseRunner {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(PirateSpawnPhase.class);

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

    @Autowired
    public PirateSpawnPhase(@Nonnull final NonPlayerCharacterService nonPlayerCharacterService,
                            @Nonnull final FleetService fleetService,
                            @Nonnull final ShipClassService shipClassService,
                            @Nonnull final WarShipService warShipService,
                            @Nonnull final FleetMovementCache fleetMovementCache,
                            @Nonnull final HeatMapService heatMapService) {
        this.nonPlayerCharacterService = Preconditions.checkNotNull(nonPlayerCharacterService, "nonPlayerCharacterService must not be empty");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService must not be empty");
        this.shipClassService = Preconditions.checkNotNull(shipClassService, "shipClassService must not be empty");
        this.warShipService = Preconditions.checkNotNull(warShipService, "warShipService must not be empty");
        this.fleetMovementCache = Preconditions.checkNotNull(fleetMovementCache, "fleetMovementCache must not be empty");
        this.heatMapService = Preconditions.checkNotNull(heatMapService, "heatMapService must not be empty");
    }

    @Override
    public void executePhase(@Nonnull final Tick today) {

        if (today.getNo() % 3 != 0) {
            LOGGER.info("Nothing will be unleashed today");
            return;
        }
        LOGGER.info("Unleash the beast");

        final NonPlayerCharacter pirate = nonPlayerCharacterService.findByUsername(PIRATE);
        Preconditions.checkNotNull(pirate, "pirate must not be empty");

        final List<Planet> targets = detectVictims();
        final List<Move> resultingMoves = new ArrayList<>();
        final List<Planet> heating = new ArrayList<>();
        for (final Planet planet : targets) {
            final Move approach = approach(planet);
            if (approach != null) {
                heating.add(planet);
                resultingMoves.add(approach);
            }
        }

        heatMapService.reduceHeat(heating, EMissionType.PIRATE_RAID);
        executeMovement(today, resultingMoves);
    }

    private void executeMovement(@Nonnull final Tick today, @Nonnull final List<Move> resultingMoves) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(resultingMoves, "resultingMoves must not be empty");

        final List<Fleet> fleets = fleetService.moveFleets(resultingMoves);
        //noinspection DataFlowIssue
        fleets.forEach(fleet -> fleetMovementCache.add(today, fleet, fleet.getMove(), fleet.getMove().getDestinationOrbit().getSystem()));
    }

    @Nullable
    private Move approach(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet must not be empty");

        final Owner owner = planet.getOwner() != null ? planet.getOwner() : Owner.UNCOLONIZED;

        final int idFleet = createPirateFleet(planet);
        final Fleet pirateFleet = fleetService.find(idFleet);
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");

        final boolean withdrawEarly = withdrawEarly(planet, pirateFleet);
        if (!withdrawEarly) {
            LOGGER.info("\tVisiting '" + owner.getUsername() + "' at '" + planet.getName() + "'");
            return new Move(pirateFleet, new FleetOrbit(planet.getOrbit(), planet.getSystem()));
        }

        LOGGER.info("\tWithdraw early against the strong opposite '" + owner.getUsername() + "' at '" + planet.getName() + "'");
        return null;
    }

    private boolean withdrawEarly(@Nonnull final Planet planet, @Nonnull final Fleet pirateFleet) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");

        final Set<Fleet> allAnchoredForPlanet = fleetService.findAllAnchoredForPlanet(planet);
        final int planetaryPoints = new UserPoints().withFleets(allAnchoredForPlanet).getFleetPoints();
        final int piratePoints = new UserPoints().withFleets(List.of(pirateFleet)).getFleetPoints();
        return planetaryPoints >= piratePoints * 3;
    }

    private List<Planet> detectVictims() {
        return heatMapService.findHottestPlanets(EMissionType.PIRATE_RAID);
    }

    private int createPirateFleet(@Nonnull final Planet target) {
        Preconditions.checkNotNull(target, "target must not be empty");

        final NonPlayerCharacter opponent = nonPlayerCharacterService.findByUsername(PIRATE);
        final List<ShipClass> classList = shipClassService.findAllLatestByOwner(Objects.requireNonNull(opponent));
        final ShipClass ship = classList.get(0);

        final Fleet piratesFleet = createFleet(opponent, target);
        final WarShip warShip = new WarShip("Corsair", target, piratesFleet, ship);
        warShip.setOperational();
        warShipService.save(warShip);
        return piratesFleet.getId();
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
