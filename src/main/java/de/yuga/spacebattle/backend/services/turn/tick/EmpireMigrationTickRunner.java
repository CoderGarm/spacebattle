package de.yuga.spacebattle.backend.services.turn.tick;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.services.caches.TransportationCache;
import de.yuga.spacebattle.backend.services.caclulator.TickOutputCalculator;
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

    @Nonnull
    private final TickOutputCalculator tickOutputCalculator;

    @Autowired
    public EmpireMigrationTickRunner(@Nonnull final PlanetService planetService,
                                     @Nonnull final TransportationCache transportationCache,
                                     @Nonnull final OperationalService operationalService,
                                     @Nonnull final TickOutputCalculator tickOutputCalculator) {
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
        this.transportationCache = Preconditions.checkNotNull(transportationCache, "transportationCache must not be empty");
        this.operationalService = Preconditions.checkNotNull(operationalService, "operationalService must not be empty");
        this.tickOutputCalculator = Preconditions.checkNotNull(tickOutputCalculator, "tickOutputCalculator must not be empty");
    }

    @Override
    public void tick(@Nonnull final Tick today) {
        this.today = Preconditions.checkNotNull(today, "today must not be empty");

        LOGGER.info("Migrate by education in the empires");
        tickMigrations();
    }

    private void tickMigrations() {
        Preconditions.checkNotNull(today, "today must not be empty");

        final Map<User, Set<Planet>> planetsByUser = getPlanetsByUser();

        final Map<User, Map<Planet, ResourceDeposit>> originalDemands = new HashMap<>();
        final Map<User, Map<Planet, ResourceDeposit>> demands = new HashMap<>();
        planetsByUser.keySet().forEach(user -> demands.put(user, operationalService.getPopulationDemandForUserByPlanet(user.getId())));

        demands.forEach((user, map) -> {
            // clone them
            final Map<Planet, ResourceDeposit> innterMap = new HashMap<>();
            map.forEach((planet, resourceDeposit) -> innterMap.put(planet, new ResourceDeposit(resourceDeposit)));
            originalDemands.put(user, innterMap);
        });

        reduceDemandAboutDeposit(demands);

        final Set<Integer> planetIDs = planetsByUser.values().stream()
                .flatMap(Collection::stream)
                .map(AbstractEntityKey::getId).collect(Collectors.toSet());
        final Map<Integer, ResourceDeposit> capacitiesByPlanetID = planetService.getResourceCapacities(planetIDs);
        final Map<Integer, Long> popCapacitiesByPlanetID = new HashMap<>();
        capacitiesByPlanetID.forEach((idPlanet, resourceDeposit) -> popCapacitiesByPlanetID.put(idPlanet, resourceDeposit.getResourceAmountByType(EResourceType.POPULATION)));
        limitDemandToCapacity(demands, popCapacitiesByPlanetID);

        final Map<User, Map<Planet, ResourceDeposit>> giveAway = calculateDeliveryAbility(planetsByUser, demands, originalDemands);

        planetsByUser.keySet().forEach(user -> {
            final Map<Planet, ResourceDeposit> delivery = giveAway.getOrDefault(user, new HashMap<>());
            final Map<Planet, ResourceDeposit> demand = demands.getOrDefault(user, new HashMap<>());

            final Set<Planet> planetsWithDemand = demand.entrySet().stream().filter(e -> e.getValue().getCrewRequirement().getSumOfPopulation() > 0).map(Map.Entry::getKey).collect(Collectors.toSet());
            final Set<Planet> planetsWithDelivery = delivery.entrySet().stream().filter(e -> e.getValue().getCrewRequirement().getSumOfPopulation() > 0).map(Map.Entry::getKey).collect(Collectors.toSet());

            for (final Planet to : planetsWithDemand) {
                final ResourceDeposit demandResourceDeposit = demand.get(to);

                for (final Planet from : planetsWithDelivery) {
                    final ResourceDeposit presentResourceDeposit = delivery.get(from);

                    demandResourceDeposit.getHumanResources().forEach((educationType, neededAmount) -> {

                        final long presentAmount = presentResourceDeposit.getCrewAmountByType(educationType);

                        long transferredAmount = Math.min(presentAmount, neededAmount);
                        if (transferredAmount > 0) {
                            // log transfer
                            transportationCache.add(today, from, to, educationType, transferredAmount);
                            // update "organisation data"
                            presentResourceDeposit.updateCrewRequirement(educationType, -transferredAmount);
                            demandResourceDeposit.updateCrewRequirement(educationType, -transferredAmount);
                            // execute transfer
                            from.getResourceDeposit().updateCrewRequirement(educationType, -transferredAmount);
                            to.getResourceDeposit().updateCrewRequirement(educationType, transferredAmount);
                        }
                    });
                }
            }
        });

        planetService.saveAll(giveAway.values().stream().map(Map::keySet).flatMap(Collection::stream).collect(Collectors.toList()));
        planetService.saveAll(demands.values().stream().map(Map::keySet).flatMap(Collection::stream).collect(Collectors.toList()));
    }

    private static Map<User, Map<Planet, ResourceDeposit>> calculateDeliveryAbility(@Nonnull final Map<User, Set<Planet>> planetsByUser,
                                                                                    @Nonnull final Map<User, Map<Planet, ResourceDeposit>> demands,
                                                                                    @Nonnull final Map<User, Map<Planet, ResourceDeposit>> originalDemands) {
        Preconditions.checkNotNull(planetsByUser, "planetsByUser must not be empty");
        Preconditions.checkNotNull(demands, "demands must not be empty");
        Preconditions.checkNotNull(originalDemands, "originalDemands must not be empty");

        final Map<User, Map<Planet, ResourceDeposit>> giveAway = new HashMap<>();
        planetsByUser.forEach((user, planets) -> {

            final Map<Planet, ResourceDeposit> give = giveAway.getOrDefault(user, new HashMap<>());

            final Map<Planet, ResourceDeposit> actuallyDemands = demands.getOrDefault(user, new HashMap<>());
            final Map<Planet, ResourceDeposit> originals = originalDemands.getOrDefault(user, new HashMap<>());

            for (final Planet planet : planets) {
                final ResourceDeposit actuallyDemand = actuallyDemands.get(planet);
                final ResourceDeposit originalDemand = originals.get(planet);

                Arrays.stream(EEducationType.values()).forEach(educationType -> {
                    final long demand = actuallyDemand != null ? actuallyDemand.getCrewAmountByType(educationType) : 0;
                    final long presentPops = planet.getResourceDeposit().getCrewAmountByType(educationType);
                    if (demand == 0 && presentPops > 0) {
                        // can give
                        final long toGive = presentPops - (originalDemand != null ? originalDemand.getCrewAmountByType(educationType) : 0);
                        if (toGive > 0) {
                            final ResourceDeposit toGiveDeposit = give.getOrDefault(planet, new ResourceDeposit(EDepositType.DEPOSITS));
                            toGiveDeposit.updateCrewRequirement(educationType, toGive);
                            give.put(planet, toGiveDeposit);
                        }
                    }
                });
            }
            giveAway.put(user, give);
        });
        return giveAway;
    }

    private static void limitDemandToCapacity(@Nonnull final Map<User, Map<Planet, ResourceDeposit>> demands,
                                              @Nonnull final Map<Integer, Long> popCapacitiesByPlanet) {
        Preconditions.checkNotNull(demands, "demands must not be empty");
        Preconditions.checkNotNull(popCapacitiesByPlanet, "popCapacitiesByPlanet must not be empty");

        demands.values().forEach(map -> {
            map.forEach((planet, popDemand) -> {
                final long capacity = popCapacitiesByPlanet.getOrDefault(planet.getId(), 0L);
                final long summedPopulation = planet.getResourceDeposit().getCrewRequirement().getSumOfPopulation();
                final long summedDemand = popDemand.getCrewRequirement().getSumOfPopulation();
                if ((summedPopulation + summedDemand) > capacity) {
                    // if not enough capacity is present, reduce equally over all education types
                    final long populationOverflow = capacity - summedPopulation - summedDemand;
                    final List<EEducationType> demandedTypes = new ArrayList<>(popDemand.getHumanResources().keySet());
                    int index = 0;
                    for (long i = 0; i < populationOverflow; i++) {
                        if (index > demandedTypes.size() - 1) {
                            index = 0;
                        }

                        EEducationType type = demandedTypes.get(0);
                        final long crewAmountByType = popDemand.getCrewAmountByType(type);
                        if (crewAmountByType >= 1) {
                            popDemand.updateCrewRequirement(type, -1);
                        }

                        index++;
                    }
                }
            });
        });
    }

    private static void reduceDemandAboutDeposit(@Nonnull final Map<User, Map<Planet, ResourceDeposit>> demands) {
        Preconditions.checkNotNull(demands, "demands must not be empty");

        demands.values().forEach(map -> {
            map.forEach((planet, popDemand) -> {
                Arrays.stream(EEducationType.values()).forEach(educationType -> {
                    final long presentAmount = planet.getResourceDeposit().getCrewAmountByType(educationType);
                    if (presentAmount <= popDemand.getCrewAmountByType(educationType)) {
                        popDemand.updateCrewRequirement(educationType, -presentAmount);
                    } else {
                        popDemand.setAbsoluteCrewRequirement(educationType, 0);
                    }
                });
            });
        });
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
