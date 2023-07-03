package de.yuga.spacebattle.backend.services.turn.tick.mission.phases;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.CargoCalculator;
import de.yuga.spacebattle.backend.calculator.distance.NavigationCalculator;
import de.yuga.spacebattle.backend.entities.account.NonPlayerCharacter;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Move;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.services.account.NonPlayerCharacterService;
import de.yuga.spacebattle.backend.services.caches.FleetMovementCache;
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
 * Checks if pirates are in a planetary orbit and have a full cargo space, then retreats.
 */
@Service
public class PirateWithdrawPhase implements MissionPhaseRunner {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(PirateWithdrawPhase.class);

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final NonPlayerCharacterService nonPlayerCharacterService;

    @Nonnull
    private final FleetService fleetService;

    @Nonnull
    private final FleetMovementCache fleetMovementCache;

    @Autowired
    public PirateWithdrawPhase(@Nonnull final PlanetService planetService,
                               @Nonnull final NonPlayerCharacterService nonPlayerCharacterService,
                               @Nonnull final FleetService fleetService,
                               @Nonnull final FleetMovementCache fleetMovementCache) {
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
        this.nonPlayerCharacterService = Preconditions.checkNotNull(nonPlayerCharacterService, "nonPlayerCharacterService must not be empty");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService must not be empty");
        this.fleetMovementCache = Preconditions.checkNotNull(fleetMovementCache, "fleetMovementCache must not be empty");
    }

    @Override
    public void executePhase(@Nonnull final Tick today) {
        LOGGER.info("Pirates withdraw");

        final NonPlayerCharacter pirate = nonPlayerCharacterService.findByUsername(PIRATE);
        Preconditions.checkNotNull(pirate, "pirate must not be empty");

        List<Fleet> pirateFleets = fleetService.findAllFleetsInOrbitByUser(pirate);
        retreatToHyperlimit(today, pirateFleets);

        pirateFleets = fleetService.findAllFleetsWithoutMovementAtHyperlimitByUser(pirate);
        retreatToHyperspace(today, pirateFleets);
    }

    private void retreatToHyperspace(@Nonnull final Tick today, @Nonnull final List<Fleet> fleets) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(fleets, "fleets must not be empty");

        fleetService.markAsDestroyed(fleets);
    }

    private void retreatToHyperlimit(@Nonnull final Tick today, @Nonnull final List<Fleet> fleets) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(fleets, "fleets must not be empty");

        final List<Move> resultingMoves = new ArrayList<>();
        for (final Fleet pirateFleet : fleets) {
            final long freeCargoUnits = CargoCalculator.getFreeCargoUnits(pirateFleet);
            if (freeCargoUnits == 0) {
                final Planet target = planetService.findByCoordinates(Objects.requireNonNull(pirateFleet.getOrbit()));
                if (target == null) {
                    // not in a planetary orbit
                    LOGGER.warn("Pirate fleet with idFleet '" + pirateFleet.getId() + "' is not in a planetary orbit");
                    continue;
                }
                final FleetOrbit destination = new FleetOrbit(target.getOrbit(), target.getSystem());
                final Orbit positionOnHyperlimit = NavigationCalculator.getPositionOnHyperlimit(pirateFleet, destination); // fixme position is pretty funny on the map
                final Move move = new Move(pirateFleet, new FleetOrbit(positionOnHyperlimit, target.getSystem()));
                resultingMoves.add(move);
            }
        }

        //noinspection DataFlowIssue
        fleetService.moveFleets(resultingMoves).forEach(fleet -> fleetMovementCache.add(today, fleet, fleet.getMove(), fleet.getMove().getDestinationOrbit().getSystem()));
    }
}
