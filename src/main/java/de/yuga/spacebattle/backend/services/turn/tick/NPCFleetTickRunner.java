package de.yuga.spacebattle.backend.services.turn.tick;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.NonPlayerCharacter;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.services.MasterOfTheUniverseService;
import de.yuga.spacebattle.backend.services.ResourceService;
import de.yuga.spacebattle.backend.services.account.NonPlayerCharacterService;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.WarShipService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.List;

@Service
public class NPCFleetTickRunner implements TickRunner {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(NPCFleetTickRunner.class);

    @Nonnull
    private final NonPlayerCharacterService nonPlayerCharacterService;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final FleetService fleetService;

    @Nonnull
    private final ResourceService resourceService;

    @Nonnull
    private final WarShipService warShipService;

    @Autowired
    public NPCFleetTickRunner(@Nonnull final NonPlayerCharacterService nonPlayerCharacterService,
                              @Nonnull final PlanetService planetService,
                              @Nonnull final FleetService fleetService,
                              @Nonnull final ResourceService resourceService,
                              @Nonnull final WarShipService warShipService) {
        this.nonPlayerCharacterService = Preconditions.checkNotNull(nonPlayerCharacterService, "nonPlayerCharacterService must not be empty");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService must not be empty");
        this.resourceService = Preconditions.checkNotNull(resourceService, "resourceService must not be empty");
        this.warShipService = Preconditions.checkNotNull(warShipService, "warShipService must not be empty");
    }

    @Override
    public void tick(@Nonnull final Tick today) {
        LOGGER.info("Upgrade NPC fleets");
        upgradeNPCFleets();
    }

    private void upgradeNPCFleets() {
        final List<NonPlayerCharacter> all = nonPlayerCharacterService.findAll();
        all.removeIf(o -> o.getUsername().equals(MasterOfTheUniverseService.DEFEATED_OPPONENT));

        for (final NonPlayerCharacter nonPlayerCharacter : all) {

            final Planet mainPlanet = planetService.findMainPlanet(nonPlayerCharacter);
            final List<Fleet> allFleetsByUser = fleetService.findAllFleetsByUser(nonPlayerCharacter);
            for (final Fleet fleet : allFleetsByUser) {
                final ShipClass shipClass = fleet.getShipsByClass().keySet().stream().findFirst().orElseThrow(NullPointerException::new);
                final String randomWarshipName = resourceService.getRandomWarshipName();
                final WarShip warShip = new WarShip(randomWarshipName, mainPlanet, fleet, shipClass);
                warShip.setOperational();
                warShipService.save(warShip);
            }
        }
    }
}
