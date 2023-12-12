package de.yuga.spacebattle.backend.services.turn.tick;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.TransportJob;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.services.caches.TransportationCache;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.turn.TransportJobService;
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
public class EmpireTransportationTickRunner implements TickRunner {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(EmpireTransportationTickRunner.class);

    @Nullable
    private Tick today;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final TransportationCache transportationCache;

    @Nonnull
    private final TransportJobService transportJobService;

    @Nonnull
    private final FleetService fleetService;

    @Autowired
    public EmpireTransportationTickRunner(@Nonnull final PlanetService planetService,
                                          @Nonnull final TransportationCache transportationCache,
                                          @Nonnull final TransportJobService transportJobService,
                                          @Nonnull final FleetService fleetService) {
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
        this.transportationCache = Preconditions.checkNotNull(transportationCache, "transportationCache must not be empty");
        this.transportJobService = Preconditions.checkNotNull(transportJobService, "transportJobService must not be empty");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService must not be empty");
    }

    @Override
    public void tick(@Nonnull final Tick today) {
        this.today = Preconditions.checkNotNull(today, "today must not be empty");

        LOGGER.info("Move stuff in the empires");
        tickTransportations(today);
        tickWarshipMoves();
    }

    private void tickWarshipMoves() {
        Preconditions.checkNotNull(today, "today must not be empty");

        final List<TransportJob> transportJobs = transportJobService.findAllPending();
        final List<TransportJob> toExecute = new ArrayList<>();
        final List<TransportJob> toStore = new ArrayList<>();
        for (final TransportJob transportJob : transportJobs) {
            transportJob.tick();
            if (transportJob.getTicksLeft() <= 0) {
                transportJob.setFinished(today);
                toExecute.add(transportJob);
            } else {
                toStore.add(transportJob);
            }
        }

        toExecute.forEach(t -> fleetService.executeTransferPooledWarship(t.getOwner(), t.getShips(), t.getDestination()));
        transportJobService.finishAll(today, toExecute);
        transportJobService.saveAll(toStore);
    }

    /**
     * Ticks the transportations.
     * todo implement some level of market strength and time to fly
     */
    private void tickTransportations(@Nonnull final Tick today) {
        final Map<User, Set<Planet>> planetsByUser = getPlanetsByUser();

        final Set<Planet> toStore = new HashSet<>();
        planetsByUser.forEach((user, owned) -> {
            // fill the main planet's deposit at first
            final List<Planet> planetSet = owned.stream().sorted((o1, o2) -> o1.isMain() ? -1 : o2.isMain() ? 1 : 0).collect(Collectors.toList());

            final Map<Planet, ResourceDeposit> deposits = planetSet.stream()
                    .filter(p -> p.getResourceDeposit().hasData())
                    .collect(Collectors.toMap(Function.identity(), Planet::getResourceDeposit));

            final Map<Planet, ResourceDeposit> deliveries = planetSet.stream()
                    .filter(p -> p.getResourceTransportationDelivery().hasData())
                    .collect(Collectors.toMap(Function.identity(), Planet::getResourceTransportationDelivery));

            final Map<Planet, ResourceDeposit> unusedMap = new HashMap<>();
            // state all possible deliveries and from where it comes
            stateUnusedResourcesForDelivery(deposits, deliveries, unusedMap);

            final Map<Planet, ResourceDeposit> demands = planetSet.stream()
                    .filter(p -> p.getResourceTransportationDemand().hasData())
                    .collect(Collectors.toMap(Function.identity(), p -> new ResourceDeposit(p.getResourceTransportationDemand())));
            demands.forEach((planet, demand) -> {
                demand.getResources().forEach((demandedType, demandedAmount) -> {
                    if (demandedAmount > 0) {
                        for (final Map.Entry<Planet, ResourceDeposit> e : unusedMap.entrySet()) {
                            final Planet from = e.getKey();
                            final ResourceDeposit unused = e.getValue();
                            if (from.equals(planet)) {
                                continue;
                            }
                            final long present = unused.getResourceAmountByType(demandedType);
                            demandedAmount = demand.getResourceAmountByType(demandedType);
                            final long amount = Long.min(present, demandedAmount);
                            if (amount > 0) {
                                // reduce the free amount of resources
                                unused.updateResource(demandedType, -amount);
                                // reduce the transient storage
                                demand.updateResource(demandedType, -amount);
                                // reduce the real demand by updating the deposit
                                planet.getResourceDeposit().updateResource(demandedType, amount);
                                // reduce the deposit of the sending planet
                                from.getResourceDeposit().updateResource(demandedType, -amount);
                                toStore.add(from);
                                toStore.add(planet);
                                transportationCache.add(today, from, planet, demandedType, amount);
                            }
                        }
                    }
                });

                demand.getHumanResources().forEach((demandedType, demandedAmount) -> {
                    if (demandedAmount > 0) {
                        for (final Map.Entry<Planet, ResourceDeposit> e : unusedMap.entrySet()) {
                            final Planet from = e.getKey();
                            final ResourceDeposit unused = e.getValue();
                            if (from.equals(planet)) {
                                continue;
                            }
                            final long present = unused.getCrewAmountByType(demandedType);
                            demandedAmount = demand.getCrewAmountByType(demandedType);
                            final long amount = Long.min(present, demandedAmount);
                            executeTransportation(toStore, planet, demand, demandedType, from, unused, amount);
                        }
                    }
                });
            });
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

    private void stateUnusedResourcesForDelivery(@Nonnull final Map<Planet, ResourceDeposit> deposits,
                                                 @Nonnull final Map<Planet, ResourceDeposit> deliveries,
                                                 @Nonnull final Map<Planet, ResourceDeposit> unusedMap) {
        Preconditions.checkNotNull(deposits, "deposits must not be empty");
        Preconditions.checkNotNull(deliveries, "deliveries must not be empty");
        Preconditions.checkNotNull(unusedMap, "unusedMap must not be empty");

        deliveries.forEach((planet, delivery) -> {
            final ResourceDeposit deposit = deposits.get(planet);
            final ResourceDeposit orDefault = unusedMap.getOrDefault(planet, new ResourceDeposit(EDepositType.TRANSPORTATION_DELIVERY));
            unusedMap.put(planet, orDefault);

            delivery.getResources().forEach((eResourceType, deliveryAmount) -> {
                final long presentAmount = deposit.getResourceAmountByType(eResourceType);
                orDefault.setAbsoluteResourceValue(eResourceType, Long.min(deliveryAmount, presentAmount));
            });

            delivery.getHumanResources().forEach((eEducationType, deliveryAmount) -> {
                final long presentAmount = deposit.getCrewAmountByType(eEducationType);
                orDefault.setAbsoluteCrewRequirement(eEducationType, Long.min(deliveryAmount, presentAmount));
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
