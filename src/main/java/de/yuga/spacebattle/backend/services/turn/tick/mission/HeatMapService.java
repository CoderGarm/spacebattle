package de.yuga.spacebattle.backend.services.turn.tick.mission;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.mission.HeatMap;
import de.yuga.spacebattle.backend.enums.EMissionType;
import de.yuga.spacebattle.backend.repositories.turn.mission.HeatMapRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HeatMapService {

    @Nonnull
    private final HeatMapRepository heatMapRepository;

    public HeatMapService(@Nonnull final HeatMapRepository heatMapRepository) {
        this.heatMapRepository = Preconditions.checkNotNull(heatMapRepository, "heatMapRepository must not be empty");
    }

    @Nonnull
    public List<Planet> findHottestPlanets(@Nonnull final EMissionType eMissionType) {
        Preconditions.checkNotNull(eMissionType, "eMissionType must not be empty");

        return Objects.requireNonNullElse(heatMapRepository.findHottestUsers(eMissionType), new ArrayList<>());
    }

    public void reduceHeat(@Nonnull final Collection<Planet> planets, @Nonnull final EMissionType eMissionType) {
        Preconditions.checkNotNull(planets, "planets must not be empty");
        Preconditions.checkNotNull(eMissionType, "eMissionType must not be empty");

        final Set<HeatMap> heats = Objects.requireNonNullElse(heatMapRepository.findHeatForPlanet(planets.stream().map(AbstractEntityKey::getId).collect(Collectors.toList()), eMissionType), new HashSet<>());
        heats.forEach(HeatMap::decrease);
        heats.forEach(HeatMap::decrease);
        heatMapRepository.saveAll(heats);
    }
}
