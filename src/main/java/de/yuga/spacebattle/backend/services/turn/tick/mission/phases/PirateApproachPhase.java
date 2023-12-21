package de.yuga.spacebattle.backend.services.turn.tick.mission.phases;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.MissionRandomizer;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.dto.turn.FlightPlanDto;
import de.yuga.spacebattle.backend.entities.account.NonPlayerCharacter;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Move;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.mission.PirateHuntMission;
import de.yuga.spacebattle.backend.enums.EMissionAction;
import de.yuga.spacebattle.backend.enums.EMissionType;
import de.yuga.spacebattle.backend.services.account.NonPlayerCharacterService;
import de.yuga.spacebattle.backend.services.caches.MissionCache;
import de.yuga.spacebattle.backend.services.caches.RaidingPirateCache;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.spacecraft.FleetMovementExecutorService;
import de.yuga.spacebattle.backend.services.turn.mission.MissionService;
import de.yuga.spacebattle.backend.services.turn.tick.HeatMapRunner;
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
public class PirateApproachPhase implements MissionPhaseRunner {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(PirateApproachPhase.class);

    @Nonnull
    private final NonPlayerCharacterService nonPlayerCharacterService;

    @Nonnull
    private final FleetService fleetService;

    @Nonnull
    private final HeatMapService heatMapService;

    @Nonnull
    private final MissionService missionService;

    @Nonnull
    private final MissionCache missionCache;

    @Nonnull
    private final RaidingPirateCache raidingPirateCache;

    @Nonnull
    private final FleetMovementExecutorService movementExecutorService;

    @Autowired
    public PirateApproachPhase(@Nonnull final NonPlayerCharacterService nonPlayerCharacterService,
                               @Nonnull final FleetService fleetService,
                               @Nonnull final HeatMapService heatMapService,
                               @Nonnull final MissionService missionService,
                               @Nonnull final MissionCache missionCache,
                               @Nonnull final RaidingPirateCache raidingPirateCache,
                               @Nonnull final FleetMovementExecutorService movementExecutorService) {
        this.nonPlayerCharacterService = Preconditions.checkNotNull(nonPlayerCharacterService, "nonPlayerCharacterService must not be empty");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService must not be empty");
        this.heatMapService = Preconditions.checkNotNull(heatMapService, "heatMapService must not be empty");
        this.missionService = Preconditions.checkNotNull(missionService, "missionService must not be empty");
        this.missionCache = Preconditions.checkNotNull(missionCache, "missionCache must not be empty");
        this.raidingPirateCache = Preconditions.checkNotNull(raidingPirateCache, "raidingPirateCache must not be empty");
        this.movementExecutorService = Preconditions.checkNotNull(movementExecutorService, "movementExecutorService must not be empty");
    }

    @Override
    public void executePhase(@Nonnull final Tick today) {
        Preconditions.checkNotNull(today, "today must not be empty");

        LOGGER.info("Approaching victims");

        final NonPlayerCharacter pirate = nonPlayerCharacterService.findByUsername(PIRATE);
        Preconditions.checkNotNull(pirate, "pirate must not be empty");

        final List<Fleet> pirateFleets = fleetService.findAllFleetsWithoutMovementByUser(pirate.getId());
        pirateFleets.removeIf(fleet -> {
            if (raidingPirateCache.getNextActions(fleet).isEmpty()) {
                // no instruction, just proceed
                return false;
            }
            if (raidingPirateCache.isPhaseSequenceValid(fleet, EMissionAction.WAIT, EMissionAction.APPROACH)) {
                raidingPirateCache.dropFirstActionItem(today, fleet, EMissionAction.WAIT);
                return true;
            }
            return !raidingPirateCache.isPhaseSequenceValid(fleet, EMissionAction.APPROACH);
        });

        final List<Move> resultingMoves = new ArrayList<>();
        final List<Planet> heating = new ArrayList<>();
        for (final Fleet pirateFleet : pirateFleets) {
            final Planet planet = raidingPirateCache.getTarget(pirateFleet);

            if (planet == null) {
                raidingPirateCache.executeNext(today, pirateFleet, EMissionAction.WITHDRAW);
                LOGGER.info("\tNo target found for idFleet '" + pirateFleet.getId() + "' - withdraw automatically.");
                continue;
            } else if (pirateFleet.getOrbit() != null && pirateFleet.getOrbit().getOrbit() != null &&
                    planet.getOrbit().getDistance(pirateFleet.getOrbit().getOrbit()).equals(Distance.ZERO)) {
                // is already in orbit
                continue;
            }

            final Move approach = approach(today, pirateFleet, planet);
            if (approach != null) {
                raidingPirateCache.dropFirstActionItem(today, pirateFleet, EMissionAction.APPROACH);
                heating.add(planet);
                resultingMoves.add(approach);
            }
        }

        // withdraw without a direct check in the orbit means kind of fear
        final int heatAddition = -HeatMapRunner.BASIC_HEAT_INCREMENT * 3;
        heatMapService.reduceHeat(heating, EMissionType.PIRATE_RAID, heatAddition);
        executeMovement(today, resultingMoves);
    }

    private void executeMovement(@Nonnull final Tick today, @Nonnull final List<Move> resultingMoves) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(resultingMoves, "resultingMoves must not be empty");

        final List<Fleet> fleets = fleetService.moveFleets(resultingMoves);
        fleets.stream()
                .map(Fleet::getMove)
                .filter(Objects::nonNull)
                .forEach(move -> movementExecutorService.executeMove(move, today));
    }

    @Nullable
    private Move approach(@Nonnull final Tick today, @Nonnull final Fleet pirateFleet, @Nonnull final Planet planet) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");

        final Owner owner = planet.getOwner() != null ? planet.getOwner() : Owner.UNCOLONIZED;
        final boolean withdrawEarly = withdrawEarly(planet, pirateFleet);
        if (!withdrawEarly) {

            if (MissionRandomizer.shouldWait(EMissionType.PIRATE_RAID, pirateFleet, today)) {
                raidingPirateCache.executeNext(today, pirateFleet, EMissionAction.APPROACH);
                LOGGER.info("\tPirate fleet with idFleet '" + pirateFleet.getId() + "' is waiting for tomorrow with approaching '" + owner.getUsername() + "' at '" + planet.getName() + "'");
                missionCache.pirateRaidWait(today, pirateFleet, planet);
                return null;
            }

            LOGGER.info("\tPirate fleet with idFleet '" + pirateFleet.getId() + "' visiting '" + owner.getUsername() + "' at '" + planet.getName() + "'");
            missionCache.pirateRaidApproach(today, pirateFleet, planet);
            return new Move(today, pirateFleet, new FleetOrbit(planet), FlightPlanDto.empty());
        }

        LOGGER.info("\tPirate fleet with idFleet '" + pirateFleet.getId() + "' withdraw early against the strong opposite '" + owner.getUsername() + "' at '" + planet.getName() + "'");
        missionCache.pirateRaidWithdraw(today, pirateFleet, planet);
        return null;
    }

    private boolean withdrawEarly(@Nonnull final Planet planet, @Nonnull final Fleet pirateFleet) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");

        final List<PirateHuntMission> counterMissions = missionService.findPirateHuntByPlanet(planet);
        final int impactOfRaidCounter = HeatMapRunner.getImpactOfRaidCounter(counterMissions);

        final Set<Fleet> allAnchoredForPlanet = fleetService.findAllAnchoredForPlanet(planet);
        final int orbitalImpact = HeatMapRunner.getOrbitalImpact(allAnchoredForPlanet);

        final int pirateImpact = HeatMapRunner.getPirateImpact(pirateFleet);

        return (impactOfRaidCounter + orbitalImpact) >= pirateImpact * 3;
    }
}
