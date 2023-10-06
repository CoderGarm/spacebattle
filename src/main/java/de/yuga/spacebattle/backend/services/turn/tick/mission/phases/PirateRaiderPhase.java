package de.yuga.spacebattle.backend.services.turn.tick.mission.phases;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.CargoCalculator;
import de.yuga.spacebattle.backend.calculator.CombatAllowanceCalculator;
import de.yuga.spacebattle.backend.entities.account.NonPlayerCharacter;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.FleetSnapshot;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.battle.BattleReport;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EMissionAction;
import de.yuga.spacebattle.backend.enums.ETransportType;
import de.yuga.spacebattle.backend.services.account.NonPlayerCharacterService;
import de.yuga.spacebattle.backend.services.caches.MissionCache;
import de.yuga.spacebattle.backend.services.caches.RaidingPirateCache;
import de.yuga.spacebattle.backend.services.caches.TransportationCache;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.spacecraft.BattleService;
import de.yuga.spacebattle.backend.services.turn.tick.mission.MissionPhaseRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.backend.services.MasterOfTheUniverseService.PIRATE;

/**
 * Checks if pirates are in a planetary orbit and raids the full cargo space.
 */
@Service
public class PirateRaiderPhase implements MissionPhaseRunner {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(PirateRaiderPhase.class);

    @Nullable
    private Tick today;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final NonPlayerCharacterService nonPlayerCharacterService;

    @Nonnull
    private final FleetService fleetService;

    @Nonnull
    private final TransportationCache transportationCache;

    @Nonnull
    private final BattleService battleService;

    @Nonnull
    private final MissionCache missionCache;

    @Nonnull
    private final RaidingPirateCache raidingPirateCache;

    @Autowired
    public PirateRaiderPhase(@Nonnull final PlanetService planetService,
                             @Nonnull final NonPlayerCharacterService nonPlayerCharacterService,
                             @Nonnull final FleetService fleetService,
                             @Nonnull final TransportationCache transportationCache,
                             @Nonnull final BattleService battleService,
                             @Nonnull final MissionCache missionCache,
                             @Nonnull final RaidingPirateCache raidingPirateCache) {
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
        this.nonPlayerCharacterService = Preconditions.checkNotNull(nonPlayerCharacterService, "nonPlayerCharacterService must not be empty");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService must not be empty");
        this.transportationCache = Preconditions.checkNotNull(transportationCache, "transportationCache must not be empty");
        this.battleService = Preconditions.checkNotNull(battleService, "battleService must not be empty");
        this.missionCache = Preconditions.checkNotNull(missionCache, "missionCache must not be empty");
        this.raidingPirateCache = Preconditions.checkNotNull(raidingPirateCache, "raidingPirateCache must not be empty");
    }

    @Override
    public void executePhase(@Nonnull final Tick today) {
        this.today = Preconditions.checkNotNull(today, "today must not be empty");

        LOGGER.info("Raid some victims");
        final NonPlayerCharacter pirate = nonPlayerCharacterService.findByUsername(PIRATE);
        Preconditions.checkNotNull(pirate, "pirate must not be empty");

        final List<Planet> planetToStore = new ArrayList<>();
        final List<Fleet> fleetToStore = new ArrayList<>();
        final List<Fleet> pirateFleets = fleetService.findAllFleetsWithoutMovementByUser(pirate.getId());
        for (final Fleet pirateFleet : pirateFleets) {
            final long freeCargoUnits = CargoCalculator.getFreeCargoUnits(pirateFleet);
            if (freeCargoUnits > 0) {
                final Planet target = planetService.findByCoordinates(Objects.requireNonNull(pirateFleet.getOrbit()));
                if (target == null) {
                    // not in a planetary orbit
                    continue;
                }

                final BattleReport battleReport = fight(today, target);
                final Owner owner = target.getOwner() != null ? target.getOwner() : Owner.UNCOLONIZED;
                boolean userDefeated = true;
                if (battleReport == null) {
                    LOGGER.info("\tNo combat happen at '" + owner.getUsername() + "' at '" + target.getName() + "'");
                } else {
                    userDefeated = hasPirateWon(battleReport);
                }
                missionCache.pirateRaidBattleResult(today, pirateFleet, target, battleReport, userDefeated);

                if (userDefeated) {
                    raidPlanet(today, planetToStore, fleetToStore, pirateFleet, freeCargoUnits, target);
                } else {
                    pirateFleet.delete();
                    fleetService.save(pirateFleet);
                    LOGGER.info("\tPirate fleet with idFleet '" + pirateFleet.getId() + "' defeated from '" + owner.getUsername() + "' at '" + target.getName() + "'");
                }
            }
        }
        planetService.saveAll(planetToStore);
        fleetService.saveAll(fleetToStore);
    }

    @Nullable
    private BattleReport fight(@Nonnull final Tick today, @Nonnull final Planet target) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(target, "target must not be empty");

        return battleService.runBattleAtPlanet(today, target);
    }

    private boolean hasPirateWon(@Nonnull final BattleReport battleReport) {
        Preconditions.checkNotNull(battleReport, "battleReport must not be empty");

        final Set<Integer> fleetIDs = battleReport.getParticipatingFleets().stream().map(FleetSnapshot::getFleet).map(AbstractEntityKey::getId).collect(Collectors.toSet());
        final Map<Owner, List<Fleet>> byOwner = fleetService.findByIds(fleetIDs).stream().collect(Collectors.groupingBy(Fleet::getOwner,
                Collectors.mapping(Function.identity(), Collectors.toList())));


        int pirateFleetsAlive = 0;
        int userFleetsAlive = 0;
        final Set<Owner> participatingUsers = battleReport.getParticipatingUsers();
        // makes no sense here, just to link all the places which must be "3 way fight" amended
        final boolean combatAllowed = CombatAllowanceCalculator.isCombatAllowed(participatingUsers);
        for (final Owner owner : participatingUsers) {

            final int fleets = (int) byOwner.getOrDefault(owner, new ArrayList<>()).stream().filter(Fleet::isAlive).count();
            if (owner.getNpcOwner() != null) {
                pirateFleetsAlive += fleets;
            } else {
                userFleetsAlive += fleets;
            }
        }
        return pirateFleetsAlive > userFleetsAlive;
    }

    private void raidPlanet(@Nonnull final Tick today,
                            @Nonnull final List<Planet> planetToStore,
                            @Nonnull final List<Fleet> fleetToStore,
                            @Nonnull final Fleet pirateFleet,
                            final long freeCargoUnits,
                            @Nonnull final Planet target) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(planetToStore, "planetToStore must not be empty");
        Preconditions.checkNotNull(fleetToStore, "fleetToStore must not be empty");
        Preconditions.checkNotNull(pirateFleet, "pirateFleet must not be empty");
        Preconditions.checkNotNull(target, "target must not be empty");

        if (target.getOwner() == null) {
            LOGGER.info("\tNot raiding uncolonized planet at '" + target.getName() + "'");
            return;
        }

        LOGGER.info("\tRaiding '" + target.getOwner().getUsername() + "' at '" + target.getName() + "'");
        final ResourceDeposit raid = target.getResourceDeposit().raid(pirateFleet, freeCargoUnits);
        planetToStore.add(target);
        fleetToStore.add(pirateFleet);
        transportationCache.add(today, pirateFleet, target, raid, ETransportType.PLANET_TO_FLEET);
        missionCache.pirateRaidTargetRaided(today, pirateFleet, target);
        raidingPirateCache.executeNext(today, pirateFleet, EMissionAction.LEAVE_ORBIT, EMissionAction.WITHDRAW);
    }
}
