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
import javax.annotation.PostConstruct;
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
        planMigrations();
    }

    @PostConstruct // fixme remove post construct
    private void planMigrations() {
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
        final Map<Integer, ResourceDeposit> capacitiesByPlanetID = tickOutputCalculator.getResourceCapacities(planetIDs);
        final Map<Integer, Long> popCapacitiesByPlanetID = new HashMap<>();
        capacitiesByPlanetID.forEach((idPlanet, resourceDeposit) -> popCapacitiesByPlanetID.put(idPlanet, resourceDeposit.getResourceAmountByType(EResourceType.POPULATION)));
        limitDemandToCapacity(demands, popCapacitiesByPlanetID);

        /*
            wie viel brauche ich? (demand > 0)
            wer hat? (demand == 0)
                wie viel kannst du abgeben? (deposit - originaler demand)

            verrechnen
            weiter
         */

        final Map<User, Map<Planet, ResourceDeposit>> giveAway = calculateDeliveryAbility(planetsByUser, demands, originalDemands);

        planetsByUser.forEach((user, planets) -> {
            final Map<Planet, ResourceDeposit> delivery = giveAway.getOrDefault(user, new HashMap<>());
            final Map<Planet, ResourceDeposit> demand = demands.getOrDefault(user, new HashMap<>());

            if (user.getId() == 1) {
                int br = 0; // fixme hier weiter
            }
        });
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

    /**
     * Ticks the migration between plants of the empire.
     * todo implement some level of market strength and time to fly
     */
    private void tickMigrations() {
        final Map<User, Set<Planet>> planetsByUser = getPlanetsByUser();

        final Set<Planet> toStore = new HashSet<>();
        planetsByUser.forEach((user, owned) -> {
            // fill the main planet's deposit at first
            final List<Planet> planets = owned.stream().sorted((o1, o2) -> o1.isMain() ? -1 : o2.isMain() ? 1 : 0).collect(Collectors.toList());

            final Map<Planet, ResourceDeposit> deposits = planets.stream()
                    .filter(p -> p.getResourceDeposit().hasData())
                    .collect(Collectors.toMap(Function.identity(), Planet::getResourceDeposit));

            final Map<Planet, ResourceDeposit> demands = operationalService.getPopulationDemandForUserByPlanet(user.getId());

            final Map<Planet, ResourceDeposit> demandOfSourcePlanets = deposits.keySet().stream()
                    .collect(Collectors.toMap(k -> k, k -> operationalService.getPopulationDemandForPlanet(k.getId())));

            final Map<Integer, ResourceDeposit> resourceCapacities = tickOutputCalculator.getResourceCapacities(planets.stream().map(AbstractEntityKey::getId).collect(Collectors.toSet()));
            for (final Planet to : demands.keySet()) {
                final ResourceDeposit demand = demands.get(to);
                final Set<EEducationType> educationTypes = demand.getHumanResources().entrySet().stream()
                        .filter(e -> e.getValue() > 0).map(Map.Entry::getKey)
                        .collect(Collectors.toSet());

                final Set<Planet> sources = deposits.keySet().stream().filter(p -> !to.equals(p)).collect(Collectors.toSet());
                for (final Planet source : sources) {
                    final ResourceDeposit demandOfSource = demandOfSourcePlanets.getOrDefault(source, new ResourceDeposit(EDepositType.DEMAND));
                    final ResourceDeposit deposit = source.getResourceDeposit();
                    for (final EEducationType educationType : educationTypes) {
                        final long demandedAmount = demand.getCrewAmountByType(educationType);

                        final long present = deposit.getCrewAmountByType(educationType) - demandOfSource.getCrewAmountByType(educationType);
                        if (present <= 0) {
                            continue;
                        }
                        long amount = Long.min(present, demandedAmount);
                        final long currentPopulation = to.getResourceDeposit().getResourceAmountByType(EResourceType.POPULATION);
                        long capacity = resourceCapacities.get(to.getId()).getResourceAmountByType(EResourceType.POPULATION);
                        capacity = capacity - currentPopulation;
                        if (amount <= 0 || capacity <= 0) {
                            continue;
                        }
                        amount = Long.min(amount, capacity);
                        executeTransportation(toStore, to, demand, educationType, source, deposit, amount);
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
