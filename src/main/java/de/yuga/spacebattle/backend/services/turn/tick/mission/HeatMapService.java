package de.yuga.spacebattle.backend.services.turn.tick.mission;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.mission.HeatMap;
import de.yuga.spacebattle.backend.enums.EMissionType;
import de.yuga.spacebattle.backend.repositories.turn.mission.HeatMapRepository;
import de.yuga.spacebattle.backend.services.orbitals.StarSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class HeatMapService {

    @Nonnull
    private final HeatMapRepository heatMapRepository;

    @Nonnull
    private final StarSystemService starSystemService;

    @Autowired
    public HeatMapService(@Nonnull final HeatMapRepository heatMapRepository,
                          @Nonnull final StarSystemService starSystemService) {
        this.heatMapRepository = Preconditions.checkNotNull(heatMapRepository, "heatMapRepository must not be empty");
        this.starSystemService = Preconditions.checkNotNull(starSystemService, "starSystemService must not be empty");
    }

    @Nonnull
    public List<Planet> findHottestPlanets(@Nonnull final EMissionType eMissionType) {
        Preconditions.checkNotNull(eMissionType, "eMissionType must not be empty");

        return Objects.requireNonNullElse(heatMapRepository.findHottestUsers(eMissionType), new ArrayList<>());
    }

    public void reduceHeat(@Nonnull final Collection<Planet> planets, @Nonnull final EMissionType eMissionType) {
        Preconditions.checkNotNull(planets, "planets must not be empty");
        Preconditions.checkNotNull(eMissionType, "eMissionType must not be empty");

        final Set<HeatMap> heats = findHeatForPlanets(planets, eMissionType);
        heats.forEach(HeatMap::decrease);
        heats.forEach(HeatMap::decrease);
        heatMapRepository.saveAll(heats);
    }

    @Nonnull
    public Set<HeatMap> findHeatForPlanets(final @Nonnull Collection<Planet> planets, final @Nonnull EMissionType eMissionType) {
        Preconditions.checkNotNull(planets, "planets must not be empty");
        Preconditions.checkNotNull(eMissionType, "eMissionType must not be empty");

        return Objects.requireNonNullElse(heatMapRepository.findHeatForPlanets(planets.stream().map(AbstractEntityKey::getId).collect(Collectors.toList()), eMissionType), new HashSet<>());
    }

    @Nonnull
    public Set<HeatMap> findHeatForMissionType(final @Nonnull EMissionType eMissionType) {
        Preconditions.checkNotNull(eMissionType, "eMissionType must not be empty");

        return Objects.requireNonNullElse(heatMapRepository.findHeatForMissionType(eMissionType), new HashSet<>());
    }

    public void createHeatForNeighbourhood(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet must not be empty");

        final Set<Planet> neighbours = starSystemService.findNeighbourPlanets(planet.getSystem());
        neighbours.addAll(planet.getSystem().getPlanets());
        final Set<HeatMap> heatForPlanets = findHeatForPlanets(neighbours);

        final List<HeatMap> sortedHeat = heatForPlanets.stream().sorted((Comparator.comparingInt(HeatMap::getHeat))).collect(Collectors.toList());
        final int medianIndex = (sortedHeat.size() - 1) / 2;
        final HeatMap medianHeatMap = sortedHeat.get(medianIndex);

        final int medianHeat = medianHeatMap.getHeat();
        final int minHeat = sortedHeat.get(0).getHeat();
        final int maxHeat = sortedHeat.get(sortedHeat.size() - 1).getHeat();

        final Set<Planet> planetsInHeat = heatForPlanets.stream().map(HeatMap::getPlanet).collect(Collectors.toSet());
        neighbours.removeAll(planetsInHeat);

        final List<Planet> planets = new ArrayList<>(neighbours);
        final List<HeatMap> newHeat = pseudoRandomizeHeatMap(medianHeat, minHeat, maxHeat, planets);
        heatMapRepository.saveAll(newHeat);
    }

    @Nonnull
    private List<HeatMap> pseudoRandomizeHeatMap(final int medianHeat, final int minHeat, final int maxHeat, @Nonnull final List<Planet> planets) {
        Preconditions.checkNotNull(planets, "planets must not be empty");

        final List<HeatMap> newHeat = new ArrayList<>();
        for (int i = 0; i < planets.size(); i++) {
            final Planet p = planets.get(i);
            int heat = medianHeat;
            final int maxDeviance = Integer.max(Math.abs(maxHeat) - Math.abs(medianHeat), Math.abs(medianHeat) - Math.abs(minHeat));
            if (i % 3 == 0) {
                heat = maxDeviance > 1 ? ThreadLocalRandom.current().nextInt(medianHeat, maxHeat) : medianHeat - 1;
            }
            if (i % 2 == 0) {
                heat = maxDeviance > 1 ? ThreadLocalRandom.current().nextInt(minHeat, maxHeat) : medianHeat + 1;
            }

            newHeat.add(new HeatMap(p, EMissionType.PIRATE_RAID, heat));
        }
        return newHeat;
    }

    @Nonnull
    public Set<HeatMap> findHeatForPlanets(final Set<Planet> neighbours) {
        return Objects.requireNonNullElse(heatMapRepository.findHeatForPlanets(neighbours.stream().map(AbstractEntityKey::getId).collect(Collectors.toList())), new HashSet<>());
    }

    public void saveAll(@Nonnull final Collection<HeatMap> heatMaps) {
        Preconditions.checkNotNull(heatMaps, "heatMaps must not be empty");

        heatMapRepository.saveAll(heatMaps);
    }
}
