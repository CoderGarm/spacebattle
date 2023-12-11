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
    public Map<StarSystem, Integer> findHyperPrintSensorStrengthBySystemForUser(final int idOwner) {
        final Map<StarSystem, List<OrbitalStructure>> structures = orbitalStructureRepository.findAllBySystemForUser(idOwner);

        final List<WarShip> ships = warShipService.findAliveOperationalForUser(idOwner);
        final Set<WarShip> inSystem = ships.stream()
                .filter(s -> s.getDetachment() != null)
                .filter(s -> s.getDetachment().getFleet() != null)
                .filter(s -> s.getDetachment().getFleet().getOrbit() != null)
                .filter(s -> s.getDetachment().getFleet().getOrbit().getSystem() != null)
                .collect(Collectors.toSet());

        final Set<WarShip> interplanetaryMovement = ships.stream()
                .filter(s -> s.getDetachment() != null)
                .filter(s -> s.getDetachment().getFleet() != null)
                .filter(s -> s.getDetachment().getFleet().getMove() != null)
                .filter(s -> !s.getDetachment().getFleet().getMove().isInterstellarTravel())
                .collect(Collectors.toSet());


        //noinspection DataFlowIssue
        final Map<StarSystem, List<WarShip>> shipsBySystem = inSystem.stream()
                .collect(Collectors.groupingBy(a -> a.getDetachment().getFleet().getOrbit().getSystem(),
                        Collectors.mapping(Function.identity(), Collectors.toList())));

        interplanetaryMovement.forEach(w -> {
            //noinspection DataFlowIssue
            final StarSystem key = w.getDetachment().getFleet().getMove().getDestinationOrbit().getSystem();
            final List<WarShip> orDefault = shipsBySystem.getOrDefault(key, new ArrayList<>());
            orDefault.add(w);
            shipsBySystem.put(key, orDefault);
        });

        missionService.findPirateHuntForUser(idOwner).forEach(e -> {
            final StarSystem key = e.getVenue().getSystem();
            final List<WarShip> orDefault = shipsBySystem.getOrDefault(key, new ArrayList<>());
            orDefault.addAll(e.getShips());
            shipsBySystem.put(key, orDefault);
        });

        // 10 is base sensor value for every ship by design
        final Map<StarSystem, Integer> maxSensorStrengthByShip = shipsBySystem.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> e.getValue().stream()
                                .map(m -> m.getCapabilityValue(EModuleType.ELECTRONIC_WARFARE) > 0 ? m.getCapabilityValue(EModuleType.ELECTRONIC_WARFARE) : 10)
                                .sorted(Integer::compare)
                                .reduce((o1, o2) -> o2)
                                .orElse(10)
                ));

        final Map<StarSystem, Integer> combinedSensorStrengthByArrayStructures = structures.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> e.getValue().stream()
                                .filter(m -> m.getModule().getEffect().getModifiedProperty() == EModuleType.ELECTRONIC_WARFARE)
                                .map(m -> m.getModule().getBaseValue())
                                .reduce(10, Integer::sum)
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

        return result.entrySet().stream().filter(e -> e.getValue() > 0).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
