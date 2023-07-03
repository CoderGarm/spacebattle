package de.yuga.spacebattle.backend.services.turn.tick.mission.phases;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.CargoCalculator;
import de.yuga.spacebattle.backend.entities.account.NonPlayerCharacter;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.ETransportType;
import de.yuga.spacebattle.backend.services.account.NonPlayerCharacterService;
import de.yuga.spacebattle.backend.services.caches.TransportationCache;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.turn.tick.mission.MissionPhaseRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static de.yuga.spacebattle.backend.services.MasterOfTheUniverseService.PIRATE;

/**
 * Checks if pirates are in a planetary orbit and raids the full cargo space.
 */
@Service
public class PirateRaiderPhase implements MissionPhaseRunner {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(PirateRaiderPhase.class);

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final NonPlayerCharacterService nonPlayerCharacterService;

    @Nonnull
    private final FleetService fleetService;

    @Nonnull
    private final TransportationCache transportationCache;

    @Autowired
    public PirateRaiderPhase(@Nonnull final PlanetService planetService,
                             @Nonnull final NonPlayerCharacterService nonPlayerCharacterService,
                             @Nonnull final FleetService fleetService,
                             @Nonnull final TransportationCache transportationCache) {
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
        this.nonPlayerCharacterService = Preconditions.checkNotNull(nonPlayerCharacterService, "nonPlayerCharacterService must not be empty");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService must not be empty");
        this.transportationCache = Preconditions.checkNotNull(transportationCache, "transportationCache must not be empty");
    }

    @Override
    public void executePhase(@Nonnull final Tick today) {
        LOGGER.info("Raid some victims");

        final NonPlayerCharacter pirate = nonPlayerCharacterService.findByUsername(PIRATE);
        Preconditions.checkNotNull(pirate, "pirate must not be empty");

        final List<Planet> planetToStore = new ArrayList<>();
        final List<Fleet> fleetToStore = new ArrayList<>();
        final List<Fleet> pirateFleets = fleetService.findAllFleetsInOrbitByUser(pirate);
        for (final Fleet pirateFleet : pirateFleets) {
            final long freeCargoUnits = CargoCalculator.getFreeCargoUnits(pirateFleet);
            if (freeCargoUnits > 0) {
                final Planet target = planetService.findByCoordinates(Objects.requireNonNull(pirateFleet.getOrbit()));
                if (target == null) {
                    // not in a planetary orbit
                    continue;
                }
                final ResourceDeposit raid = target.getResourceDeposit().raid(pirateFleet, freeCargoUnits);
                planetToStore.add(target);
                fleetToStore.add(pirateFleet);
                transportationCache.add(today, pirateFleet, target, raid, ETransportType.FLEET_TO_PLANET);
            }
        }
        planetService.saveAll(planetToStore);
        fleetService.saveAll(fleetToStore);
    }
}
