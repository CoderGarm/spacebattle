package de.yuga.spacebattle.backend.services.turn.tick;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.caches.DisabledWhileTicking;
import de.yuga.spacebattle.backend.services.caclulator.PopulationControlCalculator;
import de.yuga.spacebattle.backend.services.caclulator.TickOutputCalculator;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class HousekeepingRunner implements TickRunner {

    @Nonnull
    @SuppressWarnings("NotNullFieldNotInitialized")
    private Tick today;

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(HousekeepingRunner.class);

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final Set<DisabledWhileTicking> disabledWhileTickings;

    @Nonnull
    private final TickOutputCalculator tickOutputCalculator;

    @Nonnull
    private final PopulationControlCalculator populationControlCalculator;

    @Nonnull
    private final UserService userService;

    public HousekeepingRunner(@Nonnull final PlanetService planetService,
                              @Nonnull final Set<DisabledWhileTicking> disabledWhileTickings,
                              @Nonnull final TickOutputCalculator tickOutputCalculator,
                              @Nonnull final PopulationControlCalculator populationControlCalculator,
                              @Nonnull final UserService userService) {
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
        this.disabledWhileTickings = Preconditions.checkNotNull(disabledWhileTickings, "disabledWhileTickings must not be empty");
        this.tickOutputCalculator = Preconditions.checkNotNull(tickOutputCalculator, "tickOutputCalculator must not be empty");
        this.populationControlCalculator = Preconditions.checkNotNull(populationControlCalculator, "populationControlCalculator must not be empty");
        this.userService = Preconditions.checkNotNull(userService, "userService must not be empty");
    }


    @Override
    public void tick(@Nonnull final Tick today) {
        this.today = Preconditions.checkNotNull(today, "today must not be empty");

        LOGGER.info("Housekeeping is important every day");
        zeroManualTransports();

        LOGGER.info("Enabling caches");
        disabledWhileTickings.forEach(DisabledWhileTicking::enable);

        LOGGER.info("Load caches");
        final Set<Integer> userIDs = userService.findAllUserIDs();
        int userAmount = userIDs.size();
        for (final Integer idUser : userIDs) {
            LOGGER.info("Load Pops for '{}' - {} users left", idUser, userAmount--);
            populationControlCalculator.getPopOverview(idUser);
            final List<Integer> planetIDs = planetService.findAllColonizedByForIdPlanet(idUser);
            int planetAmount = planetIDs.size();
            for (final Integer idPlanet : planetIDs) {
                LOGGER.info("Load Incomes for '{}' - {} planets left", idPlanet, planetAmount--);
                tickOutputCalculator.getTicklyIncome(idPlanet);
            }
        }
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
