package de.yuga.spacebattle.backend.services.constructables.spacecraft;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.OrbitalStructure;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.backend.services.turn.mission.MissionService;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class HyperprintSensorService {

    @Nonnull
    private final OrbitalStructureService orbitalStructureRepository;

    @Nonnull
    private final WarShipService warShipService;
    private final MissionService missionService;

    public HyperprintSensorService(@Nonnull final OrbitalStructureService orbitalStructureRepository,
                                   @Nonnull final WarShipService warShipService, final MissionService missionService) {
        this.orbitalStructureRepository = Preconditions.checkNotNull(orbitalStructureRepository, "orbitalStructureRepository shouldn't be null!");
        this.warShipService = Preconditions.checkNotNull(warShipService, "warShipService must not be empty");
        this.missionService = missionService;
    }

    @Nonnull
    public Map<StarSystem, Integer> findHyperPrintSensorStrengthBySystemForUser(@Nonnull final Set<Integer> starSystemIDs, final int idOwner) {
        Preconditions.checkNotNull(starSystemIDs, "starSystemIDs must not be empty");

        final Map<StarSystem, List<OrbitalStructure>> structures = orbitalStructureRepository.findAllBySystemForUser(starSystemIDs, idOwner);
        //noinspection DataFlowIssue
        final Map<StarSystem, List<WarShip>> shipsBySystem = warShipService.findActiveShipsBySystemForUser(starSystemIDs, idOwner).stream()
                .collect(Collectors.groupingBy(a -> a.getDetachment().getFleet().getOrbit().getSystem(),
                        Collectors.mapping(Function.identity(), Collectors.toList())));

        missionService.findMissionsBySystemForUser(starSystemIDs, idOwner).forEach(e -> {
            final StarSystem key = e.getVenue().getSystem();
            final List<WarShip> orDefault = shipsBySystem.getOrDefault(key, new ArrayList<>());
            orDefault.addAll(e.getShips());
            shipsBySystem.put(key, orDefault);
        });

        final Map<StarSystem, Integer> maxSensorStrengthByShip = shipsBySystem.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> e.getValue().stream()
                                .map(m -> m.getCapabilityValue(EModuleType.ELECTRONIC_WARFARE))
                                .sorted(Integer::compare)
                                .reduce((o1, o2) -> o2)
                                .orElse(0)
                ));

        final Map<StarSystem, Integer> combinedSensorStrengthByArrayStructures = structures.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> e.getValue().stream()
                                .filter(m -> m.getModule().getEffect().getModifiedProperty() == EModuleType.ELECTRONIC_WARFARE)
                                .map(m -> m.getModule().getBaseValue())
                                .reduce(0, Integer::sum)
                ));

        final Map<StarSystem, Integer> result = new HashMap<>();
        maxSensorStrengthByShip.forEach((starSystem, sensorValue) -> {
            final int knownSensorValue = result.getOrDefault(starSystem, 0);
            if (sensorValue > knownSensorValue) {
                result.put(starSystem, sensorValue);
            }
        });

        combinedSensorStrengthByArrayStructures.forEach((starSystem, sensorValue) -> {
            final int knownSensorValue = result.getOrDefault(starSystem, 0);
            if (sensorValue > knownSensorValue) {
                result.put(starSystem, sensorValue);
            }
        });

        return result;
    }
}
