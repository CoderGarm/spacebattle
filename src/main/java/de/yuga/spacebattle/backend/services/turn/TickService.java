package de.yuga.spacebattle.backend.services.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.resource.JobCostsCalculator;
import de.yuga.spacebattle.backend.calculator.resource.PopulationControlCalculator;
import de.yuga.spacebattle.backend.calculator.resource.ResourceControlCalculator;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.misc.Operationable;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.turn.*;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthState;
import de.yuga.spacebattle.backend.entities.turn.resources.PayingPossibleResult;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.*;
import de.yuga.spacebattle.backend.repositories.turn.TickRepository;
import de.yuga.spacebattle.backend.services.MailService;
import de.yuga.spacebattle.backend.services.caches.ColonizationCache;
import de.yuga.spacebattle.backend.services.caches.FleetMovementCache;
import de.yuga.spacebattle.backend.services.caches.OperationalCache;
import de.yuga.spacebattle.backend.services.caches.TransportationCache;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.constructables.buildings.ConstructionService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.WarShipService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.researches.ResearchService;
import de.yuga.spacebattle.backend.services.spacecraft.BattleService;
import de.yuga.spacebattle.backend.services.turn.battle.combat.WarshipHealthStateService;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TickService {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(TickService.class);

    @Nonnull
    @SuppressWarnings("NotNullFieldNotInitialized")
    private Tick today;

    @Nonnull
    private final TransportationCache transportationCache;

    @Nonnull
    private final FleetMovementCache fleetMovementCache;

    @Nonnull
    private final ColonizationCache colonizationCache;

    @Nonnull
    private final OperationalCache operationalCache;

    @Nonnull
    private final TickRepository tickRepository;

    @Nonnull
    private final JobService jobService;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final MoveService moveService;

    @Nonnull
    private final FleetService fleetService;

    @Nonnull
    private final ConstructionService constructionService;

    @Nonnull
    private final ResearchService researchService;

    @Nonnull
    private final ColonizationService colonizationService;

    @Nonnull
    private final WarShipService warShipService;

    @Nonnull
    private final WarshipHealthStateService warshipHealthStateService;

    @Nonnull
    private final BattleService battleService;

    @Nonnull
    private final MailService mailService;

    private boolean isTicking = false;

    @Autowired
    public TickService(@Nonnull final TransportationCache transportationCache,
                       @Nonnull final FleetMovementCache fleetMovementCache,
                       @Nonnull final ColonizationCache colonizationCache,
                       @Nonnull final OperationalCache operationalCache,
                       @Nonnull final TickRepository tickRepository,
                       @Nonnull final JobService jobService,
                       @Nonnull final PlanetService planetService,
                       @Nonnull final MoveService moveService,
                       @Nonnull final FleetService fleetService,
                       @Nonnull final ConstructionService constructionService,
                       @Nonnull final ResearchService researchService,
                       @Nonnull final ColonizationService colonizationService,
                       @Nonnull final WarShipService warShipService,
                       @Nonnull final WarshipHealthStateService warshipHealthStateService,
                       @Nonnull final BattleService battleService,
                       @Nonnull final MailService mailService) {
        this.transportationCache = Preconditions.checkNotNull(transportationCache, "transportationCache must not be empty");
        this.fleetMovementCache = Preconditions.checkNotNull(fleetMovementCache, "fleetMovementCache must not be empty");
        this.colonizationCache = Preconditions.checkNotNull(colonizationCache, "colonizationCache must not be empty");
        this.operationalCache = Preconditions.checkNotNull(operationalCache, "operationalCache must not be empty");
        this.tickRepository = Preconditions.checkNotNull(tickRepository, "tickRepository shouldn't be null!");
        this.jobService = Preconditions.checkNotNull(jobService, "jobService shouldn't be null!");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");
        this.moveService = Preconditions.checkNotNull(moveService, "moveService shouldn't be null!");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService shouldn't be null!");
        this.constructionService = Preconditions.checkNotNull(constructionService, "constructionService shouldn't be null!");
        this.researchService = Preconditions.checkNotNull(researchService, "researchService shouldn't be null!");
        this.colonizationService = Preconditions.checkNotNull(colonizationService, "colonizationService shouldn't be null!");
        this.warShipService = Preconditions.checkNotNull(warShipService, "warShipService must not be empty");
        this.warshipHealthStateService = Preconditions.checkNotNull(warshipHealthStateService, "warshipHealthStateService must not be empty");
        this.battleService = Preconditions.checkNotNull(battleService, "battleService must not be empty");
        this.mailService = Preconditions.checkNotNull(mailService, "mailService must not be empty");
    }

    @PostConstruct
    private void load() {
        this.today = getToday();
    }

    @Scheduled(cron = "${sb.tick.cron}", zone = "Europe/Berlin")
    protected void doIt() {
        doTick();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void doTick() {
        final long startB = Calendar.getInstance().getTimeInMillis();

        try {
            LOGGER.info("Tick scheduled");
            // block all rest endpoints while ticking
            isTicking = true;
            today = tickRepository.save(new Tick());
            LOGGER.info("Today is " + today);
            final String start = "Start ticking";
            LOGGER.info(start + " transportation.");
            tickTransportations();
            LOGGER.info(start + " migration.");
            tickMigrations();
            LOGGER.info(start + " planets.");
            tickPlanets();
            LOGGER.info(start + " movements.");
            tickMovements();
            LOGGER.info(start + " colonization.");
            tickColonizations();
            LOGGER.info(start + " battles.");
            battleService.runBattles(today);
            LOGGER.info("Tick done.");
        } catch (final Exception ex) {
            mailService.sendExceptionMail(ex);
            throw ex;
        } finally {
            today.setTickEnds(LocalDateTime.now());
            tickRepository.save(today);
            LOGGER.info("Tick has processed!");
            final long end = Calendar.getInstance().getTimeInMillis();
            final long duration = (end - startB) / 1000;
            LOGGER.info("{} takes {} seconds", today, duration);
            isTicking = false;
        }
    }

    /**
     * Runs the tick for all colonizations.
     */
    private void tickColonizations() {
        final List<Colonization> colonizations = colonizationService.findAll();
        for (final Colonization colonization : colonizations) {
            int doneAtZero = colonization.getDoneAtZero();
            doneAtZero--;
            colonization.setDoneAtZero(doneAtZero);

            if (doneAtZero < 1) {
                final Planet planet = colonizationService.colonizePlanet(colonization);
                operateInoperationals(planet);
                colonizationService.delete(colonization);
                colonizationCache.add(today, planet);
            } else {
                colonizationService.save(colonization);
            }
        }
    }

    /**
     * Runs the tick for all movements.
     */
    private void tickMovements() {
        final List<Move> movements = moveService.findAll();
        for (final Move m : movements) {
            boolean isDone = move(m);
            if (isDone) {
                Fleet fleet = m.getFleet();
                fleet.setMove(null);
                fleet = fleetService.save(fleet);
                final Planet originPlanet = planetService.findByCoordinates(m.getOriginOrbit());
                final Planet destinationPlanet = planetService.findByCoordinates(m.getDestinationOrbit());
                if (originPlanet != null && destinationPlanet != null) {
                    // planet to planet travel
                    fleetMovementCache.add(today, fleet, m, originPlanet, destinationPlanet);
                }
                if (destinationPlanet == null && m.getOriginOrbit().getSystem() != null && m.getDestinationOrbit().getSystem() != null) {
                    // somewhere to hyperlimit travel
                    fleetMovementCache.add(today, fleet, m, originPlanet, m.getOriginOrbit().getSystem(), m.getDestinationOrbit().getSystem());
                }
            } else {
                moveService.save(m);
            }
        }
    }

    private void log(@Nonnull final Planet planet, @Nonnull final String msg) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(msg, "msg must not be empty");

        LOGGER.info("[Planet #{}] {}", planet.getId(), msg);
    }

    private void log(@Nonnull final Planet planet, @Nonnull final Job job, @Nonnull final String msg) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(job, "job must not be empty");
        Preconditions.checkNotNull(msg, "msg must not be empty");

        LOGGER.info("[Planet #{}] [Job #{}] {}", planet.getId(), job.getId(), msg);
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

            final Map<Planet, ResourceDeposit> demands = planetSet.stream()
                    .filter(p -> p.getResourceDemand().hasData())
                    .collect(Collectors.toMap(Function.identity(), p -> new ResourceDeposit(p.getResourceDemand())));

            demands.forEach((planet, demand) -> {
                demand.humanResources().forEach((demandedType, demandedAmount) -> {
                    if (demandedAmount > 0) {
                        for (final Map.Entry<Planet, ResourceDeposit> e : deposits.entrySet()) {
                            final Planet from = e.getKey();
                            final ResourceDeposit deposit = e.getValue();
                            if (from.equals(planet)) {
                                continue;
                            }
                            final long present = deposit.getCrewAmountByType(demandedType);
                            final long demandFrom = from.getResourceDemand().getCrewAmountByType(demandedType);
                            final long amount = Long.min(present - demandFrom, demandedAmount);
                            executeTransportation(toStore, planet, demand, demandedType, from, deposit, amount);
                        }
                    }
                });
            });
        });
        if (!toStore.isEmpty()) {
            planetService.saveAll(toStore);
        }
    }

    /**
     * Ticks the transportations.
     * todo implement some level of market strength and time to fly
     */
    private void tickTransportations() {
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
                demand.resources().forEach((demandedType, demandedAmount) -> {
                    if (demandedAmount > 0) {
                        for (final Map.Entry<Planet, ResourceDeposit> e : unusedMap.entrySet()) {
                            final Planet from = e.getKey();
                            final ResourceDeposit unused = e.getValue();
                            if (from.equals(planet)) {
                                continue;
                            }
                            final long present = unused.getResourceAmountByType(demandedType);
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

                demand.humanResources().forEach((demandedType, demandedAmount) -> {
                    if (demandedAmount > 0) {
                        for (final Map.Entry<Planet, ResourceDeposit> e : unusedMap.entrySet()) {
                            final Planet from = e.getKey();
                            final ResourceDeposit unused = e.getValue();
                            if (from.equals(planet)) {
                                continue;
                            }
                            final long present = unused.getCrewAmountByType(demandedType);
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
            // reduce the persistent storage
            planet.getResourceDemand().updateCrewRequirement(demandedType, -amount);
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
                .filter(p -> Objects.nonNull(p.getOwner()))
                .collect(Collectors.groupingBy(Planet::getOwner,
                        Collectors.mapping(Function.identity(), Collectors.toSet())));
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

            delivery.resources().forEach((eResourceType, deliveryAmount) -> {
                final long presentAmount = deposit.getResourceAmountByType(eResourceType);
                orDefault.setAbsoluteResourceValue(eResourceType, Long.min(deliveryAmount, presentAmount));
            });

            delivery.humanResources().forEach((eEducationType, deliveryAmount) -> {
                final long presentAmount = deposit.getCrewAmountByType(eEducationType);
                orDefault.setAbsoluteCrewRequirement(eEducationType, Long.min(deliveryAmount, presentAmount));
            });
        });
    }

    /**
     * Runs the tick for all planets.
     */
    private void tickPlanets() {
        final List<Planet> planets = planetService.findAllColonized();
        for (final Planet p : planets) {
            log(p, "Start ticking planet");
            tickPlanet(p);
            tickFleetsAtStarbase(p);
        }
    }

    /**
     * Processes a movement.
     *
     * @param move the movement to process
     * @return <code>true</code> if the movement is done, <code>false</code> otherwise
     */
    private boolean move(@Nonnull final Move move) {
        Preconditions.checkNotNull(move, "move shouldn't be null!");

        int moveDoneAtZero = move.getMoveDoneAtZero();
        moveDoneAtZero--;
        if (moveDoneAtZero > 0) {
            move.setMoveDoneAtZero(moveDoneAtZero);
            // todo detect if fleet is in hyperspace and remove fleet orbit completely
            return false;
        }

        final FleetOrbit targetOrbit = move.getDestinationOrbit();
        final StarSystem targetSystem = targetOrbit.getSystem();
        final Orbit orbit = targetOrbit.getOrbit();

        final Fleet fleet = move.getFleet();
        fleet.setOrbit(new FleetOrbit(orbit, targetSystem));
        return true;
    }

    /**
     * Calculates the tickly output of this planet.
     * This includes the amount of generated resources and the calculations of jobs which could be successfully ended.
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    void tickPlanet(@Nonnull Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");
        Preconditions.checkState(planet.getOwner() != null, "The owner must be set, otherwise there is nothing to do.");

        log(planet, "Start updating resources.");
        planet = updateResources(planet);
        log(planet, "Done updating resources");
        planet = runJobs(planet);
        planet = operateInoperationals(planet);
        log(planet, "Done tick planet.");
    }

    private Planet runJobs(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet must not be empty");

        final Set<Construction> constructions = planet.getConstructions().stream()
                .filter(c -> !c.getJobs().isEmpty())
                .collect(Collectors.toSet());

        for (final Construction facility : constructions) {
            final EResourceType resourceType = facility.getBuilding().getProductionTarget();
            final Set<Job> jobs = facility.getJobs();

            final Job job = jobs.stream()
                    .min(Job::compareTo)
                    .orElseThrow(() -> new NotifyWebUserException("Yeah, shit happens. This can not happen."));

            log(planet, job, "Start processing job.");
            planet.getResourceDeposit().setAbsoluteResourceValue(resourceType, 0);
            if (!tickJob(job)) {
                jobService.save(job);
                log(planet, job, "Shifting job for tick after " + today + ".");
                continue;
            }
            log(planet, job, "Processing " + resourceType + " job.");
            switch (resourceType) {
                case RESEARCH:
                    tickResearch(planet, job);
                    break;
                case CONSTRUCTION:
                    tickConstruction(planet, planet.getConstructions(), job);
                    break;
                case ORBITAL_CONSTRUCTION:
                    tickShipyard(planet, job);
                    break;
            }
            job.setFinished(today);
        }
        return planetService.save(planet);
    }

    @Nonnull
    @Transactional(propagation = Propagation.REQUIRED)
    public Planet operateInoperationals(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet must not be empty");

        final ResourceDeposit deposit = planet.getResourceDeposit();
        final ResourceDeposit demand = planet.getResourceDemand();
        final ResourceDeposit utilization = planet.getResourceUtilization();

        activateWarships(planet, deposit, demand, utilization);
        activateConstructions(planet, deposit, demand, utilization);

        return planetService.save(planet);
    }

    public void activateConstructions(@Nonnull final Planet planet,
                                      @Nonnull final ResourceDeposit deposit,
                                      @Nonnull final ResourceDeposit demand,
                                      @Nonnull final ResourceDeposit utilization) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(deposit, "deposit must not be empty");
        Preconditions.checkNotNull(demand, "demand must not be empty");
        Preconditions.checkNotNull(utilization, "utilization must not be empty");

        final List<Construction> ops = new ArrayList<>();
        // prio 1: military stuff, prio 2: higher tech level
        final List<Construction> supplyNeeded = planet.getConstructions().stream()
                .filter(c -> c.getOperationalLevel() < c.getLevel())
                .sorted((o1, o2) -> {
                    final ERefinementSequence o1RS = o1.getBuilding().getProductionType().getRefinementSequence();
                    final ERefinementSequence o2RS = o2.getBuilding().getProductionType().getRefinementSequence();
                    if (o1RS != null && o2RS != null) {
                        return Integer.compare(o1RS.getEducationPriority(), o2RS.getEducationPriority());
                    }
                    final ERefinementSequence valid = o1RS != null ? o1RS : o2RS;
                    if (valid != null) {
                        return valid.getEducationPriority() == 2 ? 1 : -1;
                    }
                    return Integer.compare(o1.getBuilding().getTechLevel().ordinal(), o2.getBuilding().getTechLevel().ordinal());
                })
                .collect(Collectors.toList());
        Collections.reverse(supplyNeeded);
        for (final Construction inoperational : supplyNeeded) {
            final ResourceDeposit costs = inoperational.getBuilding().getCosts();
            final int activeLevel = inoperational.getOperationalLevel();
            final int level = inoperational.getLevel();
            for (int i = activeLevel + 1; i <= level; i++) {
                final CrewRequirement costsForLevel = JobCostsCalculator.getCostsForLevel(costs, i).getCrewRequirement();
                final PayingPossibleResult result = deposit.isPayingPossible(costsForLevel);
                if (result.isValidForPops()) {
                    deposit.updateCrew(costsForLevel, ECalculationType.SUBTRACT);
                    demand.updateCrew(costsForLevel, ECalculationType.SUBTRACT);
                    utilization.updateCrew(costsForLevel, ECalculationType.ADD);

                    inoperational.setOperationalLevel(i);
                    ops.remove(inoperational);
                    ops.add(inoperational);
                }
            }
        }
        if (!ops.isEmpty()) {
            operationalCache.activateConstructions(today, planet, constructionService.saveAll(ops));
        }
    }

    public void activateWarships(@Nonnull final Planet planet,
                                 @Nonnull final ResourceDeposit deposit,
                                 @Nonnull final ResourceDeposit demand,
                                 @Nonnull final ResourceDeposit utilization) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(deposit, "deposit must not be empty");
        Preconditions.checkNotNull(demand, "demand must not be empty");
        Preconditions.checkNotNull(utilization, "utilization must not be empty");

        final List<WarShip> operationals = new ArrayList<>();
        final List<WarShip> inoperationals = warShipService.findAliveInoperationalForPlanet(planet.getId());
        for (final WarShip inoperational : inoperationals) {
            final CrewRequirement costs = inoperational.getShipClass().getCosts().getCrewRequirement();
            final PayingPossibleResult result = deposit.isPayingPossible(costs);
            if (result.isValidForPops()) {
                deposit.updateCrew(costs, ECalculationType.SUBTRACT);
                demand.updateCrew(costs, ECalculationType.SUBTRACT);
                utilization.updateCrew(costs, ECalculationType.ADD);

                inoperational.setOperational();
                operationals.add(inoperational);
            }
        }
        if (!operationals.isEmpty()) {
            warShipService.saveAll(operationals);
            Set<Fleet> fleets = operationals.stream().map(WarShip::getFleet).collect(Collectors.toSet());
            fleets = fleets.stream().filter(f -> f.getAliveShips().stream().allMatch(Operationable::isOperational)).collect(Collectors.toSet());
            fleets.forEach(Fleet::setOperational);
            fleetService.saveAll(fleets);
            operationalCache.activateWarships(today, planet, operationals);
        }
    }

    @Nonnull
    private Planet updateResources(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet must not be empty");

        for (final EResourceType resourceType : EResourceType.values()) {
            updateResourceDeposit(planet, resourceType);
        }
        LOGGER.info("Saving planet");
        return planetService.save(planet);
    }

    /**
     * Refresh all ammunition for a fleet in a starbase orbit.
     */
    private void tickFleetsAtStarbase(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet must not be empty");

        assert planet.getOwner() != null : "Please be colonized!";
        final Set<Fleet> anchoredFleets = fleetService.findAllAnchoredForPlanet(planet);
        final Set<WarshipHealthState> healthStates = anchoredFleets.stream()
                .filter(f -> f.getOwner().getId() == planet.getOwner().getId())
                .map(Fleet::getAliveShips)
                .flatMap(Collection::stream)
                .map(WarShip::getWarshipHealthState)
                .collect(Collectors.toSet());
        healthStates.forEach(WarshipHealthState::ammoUp);
        warshipHealthStateService.saveAll(healthStates);
    }

    private void tickShipyard(@Nonnull final Planet planet, @Nonnull final Job job) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(job, "job must not be empty");

        log(planet, job, "Start processing shipyard job .");
        final Constructable constructable = job.getConstructable();
        final Fleet fleet = constructable.getFleet();
        if (fleet == null) {
            return;
        }

        final User owner = planet.getOwner();
        assert owner != null : "There must be a planet's owner.";
        if (constructable.isRepairJob()) {
            realizeFleetRepair(planet, owner, constructable, job);
        } else {
            realizeShipProduction(planet, owner, constructable, job);
        }

        log(planet, job, "Done processing shipyard job.");
    }

    private void realizeFleetRepair(@Nonnull final Planet planet,
                                    @Nonnull final User owner,
                                    @Nonnull final Constructable constructable,
                                    @Nonnull final Job job) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(owner, "owner must not be empty");
        Preconditions.checkNotNull(constructable, "constructable must not be empty");
        Preconditions.checkNotNull(job, "job must not be empty");
        Preconditions.checkNotNull(constructable.getFleet(), "fleet must not be empty");

        log(planet, job, "Start repair fleet.");
        final Fleet fleet = constructable.getFleet();
        final Set<WarshipHealthState> toRepair = fleet.getAliveShips().stream()
                .map(WarShip::getWarshipHealthState)
                .collect(Collectors.toSet());
        toRepair.forEach(WarshipHealthState::repair);
        warshipHealthStateService.saveAll(toRepair);
        log(planet, job, "Done repairing fleet.");
    }

    private void realizeShipProduction(@Nonnull final Planet planet,
                                       @Nonnull final User owner,
                                       @Nonnull final Constructable constructable,
                                       @Nonnull final Job job) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(owner, "owner must not be empty");
        Preconditions.checkNotNull(constructable, "constructable must not be empty");
        Preconditions.checkNotNull(job, "job must not be empty");
        Preconditions.checkNotNull(constructable.getFleet(), "fleet must not be empty");

        log(planet, job, "Start realizing warships.");
        final Set<Fleet> anchoredFleets = fleetService.findAllAnchoredForPlanet(planet);
        final Fleet biggestInOrbit = anchoredFleets.stream().sorted(Comparator.comparingInt(o -> o.getAliveShips().size())).reduce((o1, o2) -> o2).orElse(null);

        final Fleet fleet = constructable.getFleet();
        fleet.getAllShips().forEach(WarShip::animate);
        if (biggestInOrbit != null) {
            biggestInOrbit.addShips(fleet.getAllShips());
        } else {
            fleet.animate();
        }
        fleetService.save(fleet);
        log(planet, job, "Done creating warships.");
    }

    private void tickConstruction(@Nonnull final Planet planet,
                                  @Nonnull final Set<Construction> constructions,
                                  @Nonnull final Job job) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(constructions, "constructions must not be empty");
        Preconditions.checkNotNull(job, "job must not be empty");

        log(planet, job, "Start processing construction job.");
        final Constructable constructable = job.getConstructable();
        final Integer targetLevel = constructable.getTargetLevel();
        final Building building = constructable.getBuilding();
        if (building == null || targetLevel == null) {
            throw new NotifyWebUserException("Oh fuck, this should not happen while constructing buildings!");
        }
        Construction workInProgress = constructions.stream()
                .filter(c -> c.getBuilding().equals(building)).findFirst().orElse(null);
        if (workInProgress != null) {
            if (workInProgress.getLevel() >= targetLevel) {
                // just delete the job - the last tick wasn't processed correctly
                LOGGER.warn("Job already processed: " + job.getId());
            } else {
                workInProgress.setLevel(targetLevel);
            }
        } else {
            workInProgress = new Construction(planet, building, 1);
        }
        constructionService.save(workInProgress);
        log(planet, job, "Done processing construction job.");
    }

    private void tickResearch(@Nonnull final Planet planet, @Nonnull final Job job) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(job, "job must not be empty");

        log(planet, job, "Start processing research job.");
        final Constructable constructable = job.getConstructable();
        final User owner = planet.getOwner();
        if (owner == null) {
            throw new NotifyWebUserException("There must be a planet's owner.");
        }
        final Research research = constructable.getResearch();
        if (research == null) {
            throw new NotifyWebUserException("Oh fuck, this should not happen while research whatever!");
        }
        researchService.addResearch(owner, List.of(research));
        log(planet, job, "Done processing research job.");
    }

    /**
     * Will update the resource deposit of a planet with the newly created stuff.
     *
     * @param planet       the planet to update
     * @param resourceType the resource type
     */
    private void updateResourceDeposit(@Nonnull final Planet planet, @Nonnull final EResourceType resourceType) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");
        Preconditions.checkNotNull(resourceType, "resourceType shouldn't be null!");

        final ResourceDeposit resourceDeposit = planet.getResourceDeposit();
        switch (resourceType.getCollectableType()) {
            case VIABLE:
                // do school
                PopulationControlCalculator.educatePopulation(planet);
                // do birth
                PopulationControlCalculator.populatePlanet(planet);
                break;
            case FORFEITABLE:
                // only set new available points
                resourceDeposit.setAbsoluteResourceValue(resourceType, ResourceControlCalculator.getTickOutput(planet, resourceType));
                break;
            default:
            case COLLECTABLE:
                // add points to the old deposit
                resourceDeposit.updateResource(resourceType, ResourceControlCalculator.getTickOutput(planet, resourceType));
                break;
        }
    }

    /**
     * Counts down the remaining {@link Job#getJobDoneAtZero()}.
     *
     * @param job the {@link Job} to do
     * @return <code>true</code> if the job is done
     */
    private boolean tickJob(@Nonnull final Job job) {
        Preconditions.checkNotNull(job, "job shouldn't be null!");

        job.tick();
        return job.getJobDoneAtZero() <= 0;
    }

    @Nonnull
    public List<Tick> findAll() {
        return tickRepository.findAllTicks();
    }

    @Nullable
    public Tick find(@Nonnull final Integer idHull) {
        Preconditions.checkNotNull(idHull, "idHull shouldn't be null!");

        return tickRepository.findById(idHull).orElse(null);
    }

    @Nonnull
    public Tick getToday() {
        final Tick latest = tickRepository.getLatest();
        Preconditions.checkNotNull(latest, "latest must not be empty");
        return latest;
    }

    public boolean isTicking() {
        return isTicking;
    }
}
