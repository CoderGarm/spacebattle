package de.yuga.spacebattle.backend.services.turn.tick;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.mission.HeatMap;
import de.yuga.spacebattle.backend.entities.turn.mission.PirateHuntMission;
import de.yuga.spacebattle.backend.enums.EMissionType;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.turn.mission.MissionService;
import de.yuga.spacebattle.backend.services.turn.tick.mission.HeatMapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class HeatMapRunner implements TickRunner {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(HeatMapRunner.class);

    /**
     * The basic amount of increased heat per tick for all mission targets.
     */
    public static final int BASIC_HEAT_INCREMENT = 1;

    @Nullable
    private Tick today;

    @Nonnull
    private final HeatMapService heatMapService;

    @Nonnull
    private final MissionService missionService;

    @Nonnull
    private final FleetService fleetService;

    @Autowired
    public HeatMapRunner(@Nonnull final HeatMapService heatMapService,
                         @Nonnull final MissionService missionService,
                         @Nonnull final FleetService fleetService) {
        this.heatMapService = Preconditions.checkNotNull(heatMapService, "heatMapService must not be empty");
        this.missionService = Preconditions.checkNotNull(missionService, "missionService must not be empty");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService must not be empty");
    }

    @Override
    public void tick(@Nonnull final Tick today) {
        this.today = Preconditions.checkNotNull(today, "today must not be empty");

        LOGGER.info("Inflame the universe");
        adjustPirateRaidHeatMap();
    }

    private void adjustPirateRaidHeatMap() {
        final EMissionType pirateRaid = EMissionType.PIRATE_RAID;
        final Set<HeatMap> pirateRaidHeatMap = heatMapService.findHeatForMissionType(pirateRaid);

        final Set<Planet> planets = pirateRaidHeatMap.stream().map(HeatMap::getPlanet).collect(Collectors.toSet());
        final List<PirateHuntMission> missions = missionService.findPirateHuntByPlanets(planets);

        pirateRaidHeatMap.forEach(heatMap -> {
            final List<PirateHuntMission> counterMissions = missions.stream().filter(m -> m.getVenue().equals(heatMap.getPlanet())).collect(Collectors.toList());

            // add heat from missions to the regular heat increasement
            final int impact = BASIC_HEAT_INCREMENT + getImpactOfRaidCounter(counterMissions);
            heatMap.add(impact);

            final Set<Fleet> anchored = fleetService.findAllAnchoredForPlanet(heatMap.getPlanet());
            int orbitalImpact = getOrbitalImpact(anchored);

            if (orbitalImpact > 0) {
                // manual trade routes found? This is the biggest prey!
                orbitalImpact += BASIC_HEAT_INCREMENT;
            }
            // do not relatively reduce the mission effect more than necessary
            heatMap.add(orbitalImpact / 2);
        });

        heatMapService.saveAll(pirateRaidHeatMap);
    }

    public static int getOrbitalImpact(@Nonnull final Set<Fleet> anchoredFleets) {
        Preconditions.checkNotNull(anchoredFleets, "anchoredFleets must not be empty");

        return anchoredFleets.stream()
                .map(Fleet::getAliveShips)
                .flatMap(Collection::stream)
                .map(WarShip::getShipClass)
                .map(ShipClass::getShipClassType)
                .map(type -> type.getHeatImpact(EMissionType.PIRATE_RAID))
                .reduce(0, Integer::sum);
    }

    public static int getImpactOfRaidCounter(@Nonnull final List<PirateHuntMission> counterMissions) {
        Preconditions.checkNotNull(counterMissions, "counterMissions must not be empty");

        return counterMissions.stream()
                .map(PirateHuntMission::getShips)
                .flatMap(Collection::stream)
                .map(WarShip::getShipClass)
                .map(ShipClass::getShipClassType)
                .map(type -> type.getHeatImpact(EMissionType.PIRATE_RAID))
                .reduce(0, Integer::sum);
    }

}
