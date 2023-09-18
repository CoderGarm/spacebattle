package de.yuga.spacebattle.backend.services.turn.tick;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

@Service
public class HousekeepingRunner implements TickRunner {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(HousekeepingRunner.class);

    @Nonnull
    @SuppressWarnings("NotNullFieldNotInitialized")
    private Tick today;

    @Nonnull
    private final PlanetService planetService;

    public HousekeepingRunner(@Nonnull final PlanetService planetService) {
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
    }


    @Override
    public void tick(@Nonnull final Tick today) {
        this.today = Preconditions.checkNotNull(today, "today must not be empty");

        LOGGER.info("Housekeeping is important every day");
        zeroManualTransports();
    }

    private void zeroManualTransports() {
        final List<Planet> planets = planetService.findAllColonized();
        planets.forEach(p -> {
            Map<EResourceType, Long> resources = p.getResourceTransportationDemand().getResources();
            resources.keySet().forEach(type -> p.getResourceTransportationDemand().setAbsoluteResourceValue(type, 0));

            Map<EEducationType, Long> humanResources = p.getResourceTransportationDemand().getHumanResources();
            humanResources.keySet().forEach(type -> p.getResourceTransportationDemand().setAbsoluteCrewRequirement(type, 0));

            resources = p.getResourceTransportationDelivery().getResources();
            resources.keySet().forEach(type -> p.getResourceTransportationDelivery().setAbsoluteResourceValue(type, 0));

            humanResources = p.getResourceTransportationDelivery().getHumanResources();
            humanResources.keySet().forEach(type -> p.getResourceTransportationDelivery().setAbsoluteCrewRequirement(type, 0));
        });
        planetService.saveAll(planets);
    }
}
