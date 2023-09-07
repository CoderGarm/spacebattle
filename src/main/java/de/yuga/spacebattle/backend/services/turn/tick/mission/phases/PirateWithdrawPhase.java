package de.yuga.spacebattle.backend.services.turn.tick.mission.phases;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.distance.NavigationCalculator;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.account.NonPlayerCharacter;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Move;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.enums.EMissionAction;
import de.yuga.spacebattle.backend.services.account.NonPlayerCharacterService;
import de.yuga.spacebattle.backend.services.caches.FleetMovementCache;
import de.yuga.spacebattle.backend.services.caches.MissionCache;
import de.yuga.spacebattle.backend.services.caches.RaidingPirateCache;
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
import java.util.stream.Collectors;

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

    @Nonnull
    private final MissionCache missionCache;
    private final RaidingPirateCache raidingPirateCache;

    @Autowired
    public PirateWithdrawPhase(@Nonnull final PlanetService planetService,
                               @Nonnull final NonPlayerCharacterService nonPlayerCharacterService,
                               @Nonnull final FleetService fleetService,
                               @Nonnull final FleetMovementCache fleetMovementCache,
                               @Nonnull final MissionCache missionCache, final RaidingPirateCache raidingPirateCache) {
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
        this.nonPlayerCharacterService = Preconditions.checkNotNull(nonPlayerCharacterService, "nonPlayerCharacterService must not be empty");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService must not be empty");
        this.fleetMovementCache = Preconditions.checkNotNull(fleetMovementCache, "fleetMovementCache must not be empty");
        this.missionCache = Preconditions.checkNotNull(missionCache, "missionCache must not be empty");
        this.raidingPirateCache = raidingPirateCache;
    }

    @Override
    public void executePhase(@Nonnull final Tick today) {
        LOGGER.info("Pirates withdraw");

        final NonPlayerCharacter pirate = nonPlayerCharacterService.findByUsername(PIRATE);
        Preconditions.checkNotNull(pirate, "pirate must not be empty");

        final List<Fleet> pirateFleets = fleetService.findAllFleetsWithoutMovementByUser(pirate);
        final List<Fleet> leaveOrbit = pirateFleets.stream().filter(fleet -> {
            final Planet planet = raidingPirateCache.getTarget(fleet);
            final boolean isInOrbitOfTarget = planet != null && fleet.getOrbit() != null && fleet.getOrbit().getOrbit() != null &&
                    planet.getOrbit().getDistance(fleet.getOrbit().getOrbit()).equals(Distance.ZERO);
            if (!isInOrbitOfTarget) {
                return false;
            }
            if (raidingPirateCache.getNextActions(fleet).isEmpty()) {
                // no instruction, just proceed and leave if in orbit of target
                return true;
            }
            if (raidingPirateCache.isPhaseSequenceValid(fleet, EMissionAction.WAIT, EMissionAction.LEAVE_ORBIT)) {
                raidingPirateCache.dropFirstActionItem(today, fleet, EMissionAction.WAIT);
                return false;
            }
            return raidingPirateCache.isPhaseSequenceValid(fleet, EMissionAction.LEAVE_ORBIT);

        }).collect(Collectors.toList());
        retreatToHyperlimit(today, leaveOrbit);

        final List<Fleet> withdrawHyperspace = pirateFleets.stream().filter(fleet -> {
            final Planet planet = raidingPirateCache.getTarget(fleet);
            final boolean isInOrbitOfTarget = planet != null && fleet.getOrbit() != null && fleet.getOrbit().getOrbit() != null &&
                    planet.getOrbit().getDistance(fleet.getOrbit().getOrbit()).equals(Distance.ZERO);
            if (isInOrbitOfTarget) {
                return false;
            }
            if (raidingPirateCache.getNextActions(fleet).isEmpty()) {
                // no instruction, just proceed and leave if not in planetary orbit
                return true;
            }
            if (raidingPirateCache.isPhaseSequenceValid(fleet, EMissionAction.WAIT, EMissionAction.WITHDRAW)) {
                raidingPirateCache.dropFirstActionItem(today, fleet, EMissionAction.WAIT);
                return false;
            }
            return raidingPirateCache.isPhaseSequenceValid(fleet, EMissionAction.WITHDRAW);
        }).collect(Collectors.toList());
        retreatToHyperspace(today, withdrawHyperspace);
    }

    private void retreatToHyperspace(@Nonnull final Tick today, @Nonnull final List<Fleet> fleets) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(fleets, "fleets must not be empty");

        fleetService.markAsDestroyed(fleets);
        fleets.forEach(fleet -> {
            assert fleet.getOrbit() != null;
            assert fleet.getOrbit().getSystem() != null;
            // no jump-into-hyperspace-sensors possible - no notification
            LOGGER.info("\tPirate fleet with idFleet '" + fleet.getId() + "' withdraws to hyper space from '" + fleet.getOrbit().getSystem().getName() + "'");
        });
    }

    private void retreatToHyperlimit(@Nonnull final Tick today, @Nonnull final List<Fleet> fleets) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(fleets, "fleets must not be empty");

        final List<Move> resultingMoves = new ArrayList<>();
        for (final Fleet pirateFleet : fleets) {
            final Planet planet = raidingPirateCache.getTarget(pirateFleet);
            final boolean isInOrbitOfTarget = planet != null && pirateFleet.getOrbit() != null && pirateFleet.getOrbit().getOrbit() != null &&
                    planet.getOrbit().getDistance(pirateFleet.getOrbit().getOrbit()).equals(Distance.ZERO);
            if (!isInOrbitOfTarget) {
                // not in a planetary orbit
                LOGGER.warn("\tPirate fleet with idFleet '" + pirateFleet.getId() + "' is not in a planetary orbit");
                continue;
            }
            LOGGER.info("\tPirate fleet with idFleet '" + pirateFleet.getId() + "' withdraws to hyper limit from '" + planet.getName() + "'");
            missionCache.pirateRaidWithdrawFromOrbit(today, pirateFleet, planet);
            raidingPirateCache.dropFirstActionItem(today, pirateFleet, EMissionAction.LEAVE_ORBIT);
            final FleetOrbit destination = new FleetOrbit(planet.getOrbit(), planet.getSystem());
            final Orbit positionOnHyperlimit = NavigationCalculator.getPositionOnHyperlimit(destination);
            final Move move = new Move(pirateFleet, new FleetOrbit(positionOnHyperlimit, planet.getSystem()));
            resultingMoves.add(move);
        }
        //noinspection DataFlowIssue
        fleetService.moveFleets(resultingMoves).forEach(fleet -> fleetMovementCache.add(today, fleet, fleet.getMove(), fleet.getMove().getDestinationOrbit().getSystem()));
    }
}
