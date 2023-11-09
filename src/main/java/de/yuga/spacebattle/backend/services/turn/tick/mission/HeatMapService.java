package de.yuga.spacebattle.backend.services.turn.tick.mission;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.mission.HeatMap;
import de.yuga.spacebattle.backend.enums.EMissionType;
import de.yuga.spacebattle.backend.repositories.turn.mission.HeatMapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.backend.entities.turn.mission.HeatMap.STARTING_HEAT_FOR_MAIN_PLANET;

@Service
public class HeatMapService {

    @Nonnull
    private final HeatMapRepository heatMapRepository;

    @Autowired
    public HeatMapService(@Nonnull final HeatMapRepository heatMapRepository) {
        this.heatMapRepository = Preconditions.checkNotNull(heatMapRepository, "heatMapRepository must not be empty");
    }

    @Nonnull
    public List<HeatMap> findHottestPlanets(@Nonnull final EMissionType eMissionType) {
        Preconditions.checkNotNull(eMissionType, "eMissionType must not be empty");

        return Objects.requireNonNullElse(heatMapRepository.findHottestUsers(eMissionType), new ArrayList<>());
    }

    public void reduceHeat(@Nonnull final Collection<Planet> planets, @Nonnull final EMissionType eMissionType, final int heatAddition) {
        Preconditions.checkNotNull(planets, "planets must not be empty");
        Preconditions.checkNotNull(eMissionType, "eMissionType must not be empty");

        final Set<HeatMap> heats = findHeatForPlanets(planets, eMissionType);
        heats.forEach(h -> h.add(heatAddition));
        heatMapRepository.saveAll(heats);
    }

    @Nonnull
    public Set<HeatMap> findHeatForPlanets(final @Nonnull Collection<Planet> planets, final @Nonnull EMissionType eMissionType) {
        Preconditions.checkNotNull(planets, "planets must not be empty");
        Preconditions.checkNotNull(eMissionType, "eMissionType must not be empty");

        final List<Integer> iDs = planets.stream().map(AbstractEntityKey::getId).collect(Collectors.toList());
        return Objects.requireNonNullElse(heatMapRepository.findHeatForPlanets(iDs, eMissionType), new HashSet<>());
    }

    @Nonnull
    public Set<HeatMap> findHeatForMissionType(final @Nonnull EMissionType eMissionType) {
        Preconditions.checkNotNull(eMissionType, "eMissionType must not be empty");

        return Objects.requireNonNullElse(heatMapRepository.findHeatForMissionType(eMissionType), new HashSet<>());
    }

    public void createHeatForMainPlanet(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet must not be empty");

        heatMapRepository.save(new HeatMap(planet, EMissionType.PIRATE_RAID, STARTING_HEAT_FOR_MAIN_PLANET));
    }

    public void saveAll(@Nonnull final Collection<HeatMap> heatMaps) {
        Preconditions.checkNotNull(heatMaps, "heatMaps must not be empty");

        heatMapRepository.saveAll(heatMaps);
    }
}
