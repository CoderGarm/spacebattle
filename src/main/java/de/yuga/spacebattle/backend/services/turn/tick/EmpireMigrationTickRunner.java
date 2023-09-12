package de.yuga.spacebattle.backend.services.turn.tick;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.services.caches.TransportationCache;
import de.yuga.spacebattle.backend.services.constructables.OperationalService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EmpireMigrationTickRunner implements TickRunner {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(EmpireMigrationTickRunner.class);

    @Nullable
    private Tick today;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final TransportationCache transportationCache;

    @Nonnull
    private final OperationalService operationalService;

    @Autowired
    public EmpireMigrationTickRunner(@Nonnull final PlanetService planetService,
                                     @Nonnull final TransportationCache transportationCache,
                                     @Nonnull final OperationalService operationalService) {
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
        this.transportationCache = Preconditions.checkNotNull(transportationCache, "transportationCache must not be empty");
        this.operationalService = Preconditions.checkNotNull(operationalService, "operationalService must not be empty");
    }

    @Override
    public void tick(@Nonnull final Tick today) {
        this.today = Preconditions.checkNotNull(today, "today must not be empty");

        LOGGER.info("Migrate by education in the empires");
        tickMigrations();
    }


    /**
     * Ticks the migration between plants of the empire.
     * todo implement some level of market strength and time to fly
     */
    private void tickMigrations() {
        final Map<User, Set<Planet>> planetsByUser = getPlanetsByUser();

        final Set<Planet> toStore = new HashSet<>();
        planetsByUser.forEach((user, owned) -> {
            // fill the main planet's deposit at first
            final List<Planet> planetSet = owned.stream().sorted((o1, o2) -> o1.isMain() ? -1 : o2.isMain() ? 1 : 0).collect(Collectors.toList());

            final Map<Planet, ResourceDeposit> deposits = planetSet.stream()
                    .filter(p -> p.getResourceDeposit().hasData())
                    .collect(Collectors.toMap(Function.identity(), Planet::getResourceDeposit));

            final Map<Planet, ResourceDeposit> demands = operationalService.getPopulationDemandForUserByPlanet(user.getId());

            for (final Planet to : demands.keySet()) {
                final ResourceDeposit demand = demands.get(to);
                final Set<EEducationType> educationTypes = demand.getHumanResources().entrySet().stream()
                        .filter(e -> e.getValue() > 0).map(Map.Entry::getKey)
                        .collect(Collectors.toSet());

                final Set<Planet> sources = deposits.keySet().stream().filter(p -> !to.equals(p)).collect(Collectors.toSet());
                for (final Planet from : sources) {
                    final ResourceDeposit deposit = from.getResourceDeposit();
                    for (final EEducationType educationType : educationTypes) {
                        final long demandedAmount = demand.getCrewAmountByType(educationType);

                        final long present = deposit.getCrewAmountByType(educationType);
                        long amount = Long.min(present, demandedAmount);
                        long capacity = to.getResourceCapacity().getResourceAmountByType(EResourceType.POPULATION);
                        final long currentPopulation = to.getResourceDeposit().getResourceAmountByType(EResourceType.POPULATION);
                        capacity = capacity - currentPopulation;
                        if (amount <= 0 || capacity <= 0) {
                            continue;
                        }
                        amount = Long.min(amount, capacity);
                        executeTransportation(toStore, to, demand, educationType, from, deposit, amount);
                    }
                }
            }
        });
        if (!toStore.isEmpty()) {
            planetService.saveAll(toStore);
        }
    }

    private void executeTransportation(@Nonnull final Set<Planet> toStore,
                                       @Nonnull final Planet planet,
                                       @Nonnull final ResourceDeposit demand,
                                       @Nonnull final EEducationType demandedType,
                                       @Nonnull final Planet from,
                                       @Nonnull final ResourceDeposit unused,
                                       final long amount) {
        Preconditions.checkNotNull(toStore, "toStore must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(demand, "demand must not be empty");
        Preconditions.checkNotNull(demandedType, "demandedType must not be empty");
        Preconditions.checkNotNull(from, "from must not be empty");
        Preconditions.checkNotNull(unused, "unused must not be empty");

        if (amount > 0) {
            // reduce the free amount of resources
            unused.updateCrewRequirement(demandedType, -amount);
            // reduce the transient storage
            demand.updateCrewRequirement(demandedType, -amount);
            // reduce the real demand by updating the deposit
            planet.getResourceDeposit().updateCrewRequirement(demandedType, amount);
            // reduce the deposit of the sending planet
            from.getResourceDeposit().updateCrewRequirement(demandedType, -amount);
            toStore.add(from);
            toStore.add(planet);
            transportationCache.add(today, from, planet, demandedType, amount);
        }
    }

    @Nonnull
    private Map<User, Set<Planet>> getPlanetsByUser() {
        final List<Planet> planets = planetService.findAllColonized();
        return planets.stream()
                .filter(p -> Objects.nonNull(p.getHumanOwner()))
                .collect(Collectors.groupingBy(Planet::getHumanOwner,
                        Collectors.mapping(Function.identity(), Collectors.toSet())));
    }
}
