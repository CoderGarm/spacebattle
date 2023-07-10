package de.yuga.spacebattle.backend.services.turn.mission;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.mission.Mission;
import de.yuga.spacebattle.backend.enums.EMissionType;
import de.yuga.spacebattle.backend.repositories.turn.mission.ConvoyProtectionMissionRepository;
import de.yuga.spacebattle.backend.repositories.turn.mission.MissionRepository;
import de.yuga.spacebattle.backend.repositories.turn.mission.PirateHuntMissionRepository;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.WarShipService;
import de.yuga.spacebattle.backend.services.turn.TickTimeService;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
    public Mission createMission(@Nonnull final User actor, @Nonnull final EMissionType missionType, @Nonnull final Set<Integer> warshipIDs, @Nonnull final Planet planet) {
        Preconditions.checkNotNull(actor, "actor must not be empty");
        Preconditions.checkNotNull(missionType, "missionType must not be empty");
        Preconditions.checkNotNull(warshipIDs, "warshipIDs must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");

        final Tick today = tickTimeService.getToday();

        final Mission mission = missionRepository.save(new Mission(actor, today, planet));
        return enrichWithShips(mission, warshipIDs);
    }

    @Nonnull
    private Mission enrichWithShips(@Nonnull final Mission mission, @Nonnull final Set<Integer> warshipIDs) {
        Preconditions.checkNotNull(mission, "mission must not be empty");
        Preconditions.checkNotNull(warshipIDs, "warshipIDs must not be empty");

        final List<WarShip> warShips = warShipService.findByIds(warshipIDs);
        warShips.forEach(w -> w.setMission(mission));
        warShipService.saveAll(warShips);

        mission.enrichWithShips(warShips);
        return mission;
    }
}
