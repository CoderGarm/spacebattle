package de.yuga.spacebattle.backend.services.turn.tick;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.distance.NavigationCalculator;
import de.yuga.spacebattle.backend.dto.account.UserPoints;
import de.yuga.spacebattle.backend.entities.account.NonPlayerCharacter;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.turn.Move;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.services.account.NonPlayerCharacterService;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.caches.FleetMovementCache;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.ShipClassService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.WarShipService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.backend.services.MasterOfTheUniverseService.PIRATE;

@Service
public class PirateTickRunner implements TickRunner {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(PirateTickRunner.class);

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final NonPlayerCharacterService nonPlayerCharacterService;
    private final FleetService fleetService;
    private final ShipClassService shipClassService;
    private final WarShipService warShipService;
    private final FleetMovementCache fleetMovementCache;

    @Autowired
    public PirateTickRunner(@Nonnull final UserService userService,
                            @Nonnull final PlanetService planetService,
                            @Nonnull final NonPlayerCharacterService nonPlayerCharacterService, final FleetService fleetService, final ShipClassService shipClassService, final WarShipService warShipService, final FleetMovementCache fleetMovementCache) {
        this.userService = Preconditions.checkNotNull(userService, "userService must not be empty");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
        this.nonPlayerCharacterService = Preconditions.checkNotNull(nonPlayerCharacterService, "nonPlayerCharacterService must not be empty");
        this.fleetService = fleetService;
        this.shipClassService = shipClassService;
        this.warShipService = warShipService;
        this.fleetMovementCache = fleetMovementCache;
    }

    @Override
    public void tick(@Nonnull final Tick today) {
        LOGGER.info("Unleash the beast");

        final NonPlayerCharacter pirate = nonPlayerCharacterService.findByUsername(PIRATE);
        Preconditions.checkNotNull(pirate, "pirate must not be empty");

        final List<Move> resultingMoves = new ArrayList<>();
        final Map<User, Planet> targets = detectVictims();
        targets.forEach((user, planet) -> {
            final int planetaryPoints = new UserPoints(user).withPlanets(List.of(planet)).getPlanetaryPoints();
            // todo how the planet strength impacts the opposite forces?
            final int idFleet = createPirateFleet(user, planet);
            final Fleet pirateFleet = fleetService.find(idFleet);
            Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");
            final Move move = new Move(pirateFleet, new FleetOrbit(planet.getOrbit(), planet.getSystem()));
            resultingMoves.add(move);
        });

        final List<Fleet> fleets = fleetService.moveFleets(resultingMoves);
        //noinspection DataFlowIssue
        fleets.forEach(fleet -> fleetMovementCache.add(today, fleet, fleet.getMove(), fleet.getMove().getDestinationOrbit().getSystem()));
    }

    private Map<User, Planet> detectVictims() {
        final List<Planet> result = new ArrayList<>();
        final List<User> victims = userService.findAll();
        for (final User victim : victims) {
            final Planet planet = planetService.findMainPlanet(victim);
            result.add(planet);
        }
        return result.stream().collect(Collectors.toMap(Planet::getHumanOwner, Function.identity()));
    }

    private int createPirateFleet(@Nonnull final User victim, @Nonnull final Planet target) {
        Preconditions.checkNotNull(victim, "victim must not be empty");
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
