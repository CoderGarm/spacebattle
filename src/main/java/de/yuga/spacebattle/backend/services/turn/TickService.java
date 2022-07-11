package de.yuga.spacebattle.backend.services.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.resource.PopulationControlCalculator;
import de.yuga.spacebattle.backend.calculator.resource.ResourceControlCalculator;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.turn.*;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.repositories.turn.TickRepository;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.constructables.buildings.ConstructionService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.WarShipService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.researches.ResearchService;
import de.yuga.spacebattle.backend.services.spacecraft.BattleService;
import de.yuga.spacebattle.backend.services.turn.battle.BattleReportService;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Service
public class TickService {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(TickService.class);

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
    private final UserService userService;

    @Nonnull
    private final ResearchService researchService;

    @Nonnull
    private final ColonizationService colonizationService;

    @Nonnull
    private final WarShipService warShipService;

    @Nonnull
    private final BattleReportService battleReportService;

    @Nonnull
    private final BattleService battleService;

    @Autowired
    public TickService(@Nonnull final TickRepository tickRepository,
                       @Nonnull final JobService jobService,
                       @Nonnull final PlanetService planetService,
                       @Nonnull final MoveService moveService,
                       @Nonnull final FleetService fleetService,
                       @Nonnull final ConstructionService constructionService,
                       @Nonnull final UserService userService,
                       @Nonnull final ResearchService researchService,
                       @Nonnull final ColonizationService colonizationService,
                       @Nonnull final WarShipService warShipService,
                       @Nonnull final BattleReportService battleReportService,
                       @Nonnull final BattleService battleService) {
        Preconditions.checkNotNull(tickRepository, "tickRepository shouldn't be null!");
        Preconditions.checkNotNull(jobService, "jobService shouldn't be null!");
        Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");
        Preconditions.checkNotNull(moveService, "moveService shouldn't be null!");
        Preconditions.checkNotNull(fleetService, "fleetService shouldn't be null!");
        Preconditions.checkNotNull(constructionService, "constructionService shouldn't be null!");
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        Preconditions.checkNotNull(researchService, "researchService shouldn't be null!");
        Preconditions.checkNotNull(colonizationService, "colonizationService shouldn't be null!");
        Preconditions.checkNotNull(battleReportService, "fightingReportService shouldn't be null!");
        Preconditions.checkNotNull(battleService, "battleService shouldn't be null!");

        this.tickRepository = tickRepository;
        this.jobService = jobService;
        this.planetService = planetService;
        this.moveService = moveService;
        this.fleetService = fleetService;
        this.constructionService = constructionService;
        this.userService = userService;
        this.researchService = researchService;
        this.colonizationService = colonizationService;
        this.warShipService = warShipService;
        this.battleReportService = battleReportService;
        this.battleService = battleService;
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Berlin")
    protected void doIt() {
        LOGGER.info("Tick scheduled");
        this.doTick();
        LOGGER.info("Tick has processed!");
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

    @Nullable
    public Tick getLatest() {
        return tickRepository.getLatest();
    }

    @Nonnull
    @Transactional(rollbackFor = Exception.class)
    public Tick doTick() {
        Tick today = new Tick();
        today = tickRepository.save(today);
        LOGGER.info("Today is " + today);
        final String start = "Start ticking";
        LOGGER.info(start + " planets.");
        tickPlanets();
        LOGGER.info(start + " movements.");
        tickMovements();
        LOGGER.info(start + " colonization.");
        tickColonizations();
        LOGGER.info(start + " battles.");
        battleService.runBattles(today);
        LOGGER.info("Tick done.");

        today.setTickEnds(LocalDateTime.now());
        return tickRepository.save(today);
    }

    /**
     * Runs the tick for all colonizations.
     */
    private void tickColonizations() {
        final List<Colonization> colonizations = colonizationService.findAll();
        for (final Colonization colonization : colonizations) {
            int doneAtZero = colonization.getDoneAtZero();
            doneAtZero--;

            if (doneAtZero < 1) {
                colonizationService.colonizePlanet(colonization);
                colonizationService.delete(colonization);
            } else {
                colonization.setDoneAtZero(doneAtZero);
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
                final Fleet fleet = m.getFleet();
                fleet.setMove(null);
                fleetService.save(fleet);
            } else {
                moveService.save(m);
            }
        }
    }

    /**
     * Runs the tick for all planets.
     */
    private void tickPlanets() {
        final List<Planet> planets = planetService.findAllColonized();
        for (final Planet p : planets) {
            tick(p);
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
    void tick(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");
        Preconditions.checkState(planet.getOwner() != null, "The owner must be set, otherwise there is nothing to do.");

        final Set<Construction> constructions = planet.getConstructions();
        for (final EResourceType resourceType : EResourceType.values()) {
            updateResourceDeposit(planet, resourceType);
        }
        for (Construction facility : constructions) {
            final EResourceType resourceType = facility.getBuilding().getProductionTarget();
            final Set<Job> toDelete = new HashSet<>();
            final Set<Job> jobs = facility.getJobs();
            for (final Job job : jobs) {
                if (!tickJob(job)) {
                    jobService.save(job);
                    continue;
                }
                // realize job result if job is done
                final Constructable constructable = job.getConstructable();
                final Integer targetLevel;
                final User owner = planet.getOwner();
                switch (resourceType) {
                    case RESEARCH:

                        final Research research = constructable.getResearch();
                        targetLevel = constructable.getTargetLevel();
                        if (research == null || targetLevel == null) {
                            throw new NotifyWebUserException("Oh fuck, this should not happen while research whatever!");
                        }
                        researchService.addResearch(owner, List.of(research));
                        break;
                    case CONSTRUCTION:

                        final Building building = constructable.getBuilding();
                        targetLevel = constructable.getTargetLevel();
                        if (building == null || targetLevel == null) {
                            throw new NotifyWebUserException("Oh fuck, this should not happen while constructing buildings!");
                        }
                        Construction workInProgress = constructions.stream()
                                .filter(c -> c.getBuilding().equals(building)).findFirst().orElse(null);
                        if (workInProgress != null) {
                            workInProgress.setLevel(targetLevel);
                        } else {
                            workInProgress = new Construction(planet, building, 1);
                            constructionService.save(workInProgress);
                        }
                        break;
                    case ORBITAL_CONSTRUCTION:

                        final ShipClass shipClass = constructable.getShipClass();
                        final Integer amountShips = constructable.getAmountShips();
                        if (shipClass == null || amountShips == null || amountShips == 0) {
                            throw new NotifyWebUserException("This should never happen while build a fleet!");
                        }
                        final Fleet fleet = fleetService.save(new Fleet("Fresh Build @ " + planet.getName(), owner, new FleetOrbit(planet.getOrbit(), planet.getSystem())));
                        final Set<WarShip> newFleetComposition = new HashSet<>();
                        for (int i = 0; i <= amountShips; i++) {
                            final String randomName = generateRandomName();
                            final WarShip warShip = new WarShip(randomName, planet, fleet, shipClass);
                            newFleetComposition.add(warShip);
                        }
                        warShipService.saveAll(newFleetComposition);
                        break;
                }
                toDelete.add(job);
            }
            jobs.removeIf(toDelete::contains);
        }
        planetService.save(planet);
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

        Long tickOutput = null;
        if (EResourceType.POPULATION != resourceType) {
            tickOutput = ResourceControlCalculator.getTickOutput(planet, resourceType);
        }
        final ResourceDeposit resourceDeposit = planet.getResourceDeposit();
        switch (resourceType.getCollectableType()) {
            case VIABLE:
                // do school
                PopulationControlCalculator.educatePopulation(planet);
                planetService.save(planet);
                // do birth
                PopulationControlCalculator.populatePlanet(planet);
                break;
            case FORFEITABLE:
                // only set new available points
                if (tickOutput != null) {
                    resourceDeposit.setAbsoluteResourceValue(resourceType, tickOutput);
                }
                break;
            default:
            case COLLECTABLE:
                // add points to the old deposit
                if (tickOutput != null) {
                    resourceDeposit.updateResource(resourceType, tickOutput);
                }
                break;
        }
        planetService.save(planet);
    }

    private String generateRandomName() {
        // todo create name list
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
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
}
