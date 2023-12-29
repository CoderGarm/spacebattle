package de.yuga.spacebattle.backend.services.turn.mission;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.mission.ConvoyProtectionMission;
import de.yuga.spacebattle.backend.entities.turn.mission.Mission;
import de.yuga.spacebattle.backend.entities.turn.mission.PirateHuntMission;
import de.yuga.spacebattle.backend.entities.turn.resources.trade.TradedResource;
import de.yuga.spacebattle.backend.enums.EMissionType;
import de.yuga.spacebattle.backend.repositories.turn.mission.ConvoyProtectionMissionRepository;
import de.yuga.spacebattle.backend.repositories.turn.mission.MissionRepository;
import de.yuga.spacebattle.backend.repositories.turn.mission.PirateHuntMissionRepository;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.WarShipService;
import de.yuga.spacebattle.backend.services.turn.TickTimeService;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@Service
public class MissionService {

    @Nonnull
    private final MissionRepository missionRepository;

    @Nonnull
    private final PirateHuntMissionRepository pirateHuntMissionRepository;

    @Nonnull
    private final ConvoyProtectionMissionRepository convoyProtectionMissionRepository;

    @Nonnull
    private final TickTimeService tickTimeService;

    @Nonnull
    private final WarShipService warShipService;

    public MissionService(@Nonnull final MissionRepository missionRepository,
                          @Nonnull final PirateHuntMissionRepository pirateHuntMissionRepository,
                          @Nonnull final ConvoyProtectionMissionRepository convoyProtectionMissionRepository,
                          @Nonnull final TickTimeService tickTimeService,
                          @Nonnull final WarShipService warShipService) {
        this.missionRepository = Preconditions.checkNotNull(missionRepository, "missionRepository must not be empty");
        this.pirateHuntMissionRepository = Preconditions.checkNotNull(pirateHuntMissionRepository, "pirateHuntMissionRepository must not be empty");
        this.convoyProtectionMissionRepository = Preconditions.checkNotNull(convoyProtectionMissionRepository, "convoyProtectionMissionRepository must not be empty");
        this.tickTimeService = Preconditions.checkNotNull(tickTimeService, "tickTimeService must not be empty");
        this.warShipService = Preconditions.checkNotNull(warShipService, "warShipService must not be empty");
    }

    @Nonnull
    public List<Mission> findAllMissions(final int idUser) {
        return Objects.requireNonNullElse(missionRepository.findAllForUser(idUser), new ArrayList<>());
    }

    @Nullable
    public Mission updateMission(final int idMission, @Nonnull final Set<Integer> warshipIDs) {
        Preconditions.checkNotNull(warshipIDs, "warshipIDs must not be empty");

        final Mission mission = missionRepository.findById(idMission).orElse(null);
        if (mission != null) {
            return enrichWithShips(mission, warshipIDs);
        }
        return null;
    }

    @Nonnull
    public Mission createPirateHuntMission(@Nonnull final User actor, @Nonnull final Set<Integer> warshipIDs, @Nonnull final Planet planet) {
        Preconditions.checkNotNull(actor, "actor must not be empty");
        Preconditions.checkNotNull(warshipIDs, "warshipIDs must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");

        final Tick today = tickTimeService.getToday();
        final Mission mission = pirateHuntMissionRepository.save(new PirateHuntMission(actor, today, planet));
        mission.setMissionType(EMissionType.PIRATE_HUNT);
        return enrichWithShips(mission, warshipIDs);
    }

    @Nonnull
    public Mission createConvoyProtectionMission(@Nonnull final User actor, @Nonnull final Set<Integer> warshipIDs, @Nonnull final TradedResource protectedTrade) {
        Preconditions.checkNotNull(actor, "actor must not be empty");
        Preconditions.checkNotNull(warshipIDs, "warshipIDs must not be empty");
        Preconditions.checkNotNull(protectedTrade, "protectedTrade must not be empty");

        final Tick today = tickTimeService.getToday();
        final Mission mission = convoyProtectionMissionRepository.save(new ConvoyProtectionMission(actor, today, protectedTrade));
        mission.setMissionType(EMissionType.CONVOY_PROTECTION);
        return enrichWithShips(mission, warshipIDs);
    }

    @Nonnull
    public Mission enrichWithShips(@Nonnull final Mission mission, @Nonnull final Set<Integer> warshipIDs) {
        Preconditions.checkNotNull(mission, "mission must not be empty");
        Preconditions.checkNotNull(warshipIDs, "warshipIDs must not be empty");

        final List<WarShip> warShips = warShipService.findByIds(warshipIDs);
        warShips.forEach(w -> w.setMission(mission));
        warShipService.saveAll(warShips);

        mission.enrichWithShips(warShips);
        return mission;
    }

    @Nonnull
    public List<PirateHuntMission> findPirateHuntByPlanet(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet must not be empty");

        return Objects.requireNonNullElse(pirateHuntMissionRepository.findAllForPlanet(planet), new ArrayList<>());
    }

    @Nonnull
    public List<PirateHuntMission> findPirateHuntByPlanets(@Nonnull final Collection<Planet> planets) {
        Preconditions.checkNotNull(planets, "planets must not be empty");

        return Objects.requireNonNullElse(pirateHuntMissionRepository.findAllForPlanets(planets), new ArrayList<>());
    }

    public void stopMission(@Nonnull final Mission mission, final int idUser) {
        Preconditions.checkNotNull(mission, "mission must not be empty");

        if (missionRepository.missionExistsForActor(mission.getId(), idUser)) {
            if (mission instanceof ConvoyProtectionMission) {
                resetShipsFromConvoyMission((ConvoyProtectionMission) mission, idUser);
            } else {
                resetShipsFromPirateHuntMission((PirateHuntMission) mission);
            }
            missionRepository.deleteById(mission.getId());
        }
    }

    private void resetShipsFromPirateHuntMission(@Nonnull final PirateHuntMission mission) {
        Preconditions.checkNotNull(mission, "mission must not be empty");

        final Planet venue = mission.getVenue();
        final Set<WarShip> ships = mission.getShips();
        setMothballAndSave(ships, venue);
    }

    private void resetShipsFromConvoyMission(@Nonnull final ConvoyProtectionMission mission, final int idUser) {
        Preconditions.checkNotNull(mission, "mission must not be empty");

        final Set<WarShip> ships = mission.getShips();
        final Planet mothball;
        if (mission.getProtectedTrade().getBuyer().getId() == idUser) {
            mothball = mission.getProtectedTrade().getDestination();
        } else {
            mothball = mission.getProtectedTrade().getTradeOffer().getOrigin();
        }
        setMothballAndSave(ships, mothball);
    }

    private void setMothballAndSave(@Nonnull final Set<WarShip> ships, @Nonnull final Planet mothball) {
        Preconditions.checkNotNull(ships, "ships must not be empty");
        Preconditions.checkNotNull(mothball, "mothball must not be empty");

        ships.forEach(s -> s.setMothball(mothball));
        warShipService.saveAll(ships);
    }

    @Nonnull
    public List<ConvoyProtectionMission> findConvoyProtectionForTrades(@Nonnull final Collection<Integer> tradedResourcesIDs) {
        Preconditions.checkNotNull(tradedResourcesIDs, "tradedResourcesIDs must not be empty");

        return Objects.requireNonNullElse(convoyProtectionMissionRepository.findConvoyProtectionForTrades(tradedResourcesIDs), new ArrayList<>());
    }

    @Nonnull
    public Set<Planet> findAllPlanetsWithoutPirateHunt(final int idUser) {
        return Objects.requireNonNullElse(pirateHuntMissionRepository.findAllPlanetsWithoutPirateHunt(idUser), new HashSet<>());
    }

    @Nonnull
    public Set<TradedResource> findAllConvoysWithoutEscort(final int idUser) {
        final Tick today = tickTimeService.getToday();
        return Objects.requireNonNullElse(convoyProtectionMissionRepository.findAllConvoysWithoutEscort(today.getNo(), idUser), new HashSet<>());
    }

    @Nullable
    public Mission findById(final int idMission) {
        return missionRepository.findById(idMission).orElse(null);
    }


    @Nonnull
    public List<PirateHuntMission> findPirateHuntForUser(final int idOwner) {
        return Objects.requireNonNullElse(pirateHuntMissionRepository.findPirateHuntForUser(idOwner), new ArrayList<>());
    }

    public List<Mission> forDeletionFindAllByUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user must not be empty");

        return Objects.requireNonNullElse(missionRepository.forDeletionFindAllByUser(user.getId()), new ArrayList<>());
    }

    public void saveAll(@Nonnull final Collection<Mission> missions) {
        Preconditions.checkNotNull(missions, "missions must not be empty");

        missionRepository.saveAll(missions);
    }
}
