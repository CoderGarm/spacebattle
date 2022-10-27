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
import de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthState;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.repositories.turn.TickRepository;
import de.yuga.spacebattle.backend.services.ResourceService;
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
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TickService {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(TickService.class);

    @Nonnull
    private Tick today;

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
    private final ResourceService resourceService;

    private boolean isTicking = false;

    @Autowired
    public TickService(@Nonnull final TickRepository tickRepository,
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
                       @Nonnull final ResourceService resourceService) {
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
        this.resourceService = Preconditions.checkNotNull(resourceService, "resourceService must not be empty");
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Berlin")
    protected void doIt() {
        // block all rest endpoints while ticking
        isTicking = true;
        final long start = Calendar.getInstance().getTimeInMillis();
        LOGGER.info("Tick scheduled");
        doTick();
        LOGGER.info("Tick has processed!");
        final long end = Calendar.getInstance().getTimeInMillis();
        final long duration = (end - start) / 1000;
        LOGGER.info("{} takes {} seconds", today, duration);
        isTicking = false;
    }

    @Nonnull
    public Tick doTick() {
        today = tickRepository.save(new Tick());
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
            colonization.setDoneAtZero(doneAtZero);

            if (doneAtZero < 1) {
                colonizationService.colonizePlanet(colonization);
                colonizationService.delete(colonization);
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
                final Fleet fleet = m.getFleet();
                fleet.setMove(null);
                fleetService.save(fleet);
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
     * Runs the tick for all planets.
     */
    private void tickPlanets() {
        final List<Planet> planets = planetService.findAllColonized();
        for (final Planet p : planets) {
            log(p, "Start ticking planet");
            tickPlanet(p);
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
        for (final EResourceType resourceType : EResourceType.values()) {
            updateResourceDeposit(planet, resourceType);
        }
        log(planet, "Done updating resources");
        planet = planetService.save(planet);

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
            job.setDeleted(today);
        }

        planetService.save(planet);
        log(planet, "Done tick planet.");
    }

    private void tickShipyard(@Nonnull final Planet planet, @Nonnull final Job job) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(job, "job must not be empty");

        log(planet, job, "Start processing shipyard job .");
        final User owner = planet.getOwner();
        if (owner == null) {
            throw new NotifyWebUserException("There must be a planet's owner.");
        }
        final Constructable constructable = job.getConstructable();

        realizeShipProduction(planet, owner, constructable, job);
        realizeFleetRepair(planet, owner, constructable, job);

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

        final Fleet fleet = constructable.getFleet();
        if (fleet == null) {
            return;
        }
        final Set<WarshipHealthState> toDelete = fleet.getAliveShips().stream()
                .map(WarShip::getWarshipHealthState)
                .collect(Collectors.toSet());
        warshipHealthStateService.deleteAll(toDelete);
        fleet.setNeedsRepair(false);
        fleetService.save(fleet);
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

        final ShipClass shipClass = constructable.getShipClass();
        if (shipClass == null) {
            return;
        }
        final Integer amountShips = constructable.getAmountShips();
        if (amountShips == null || amountShips == 0) {
            throw new NotifyWebUserException("This should never happen while build a fleet!");
        }
        Fleet fleet = new Fleet("Fresh Build @ " + planet.getName(), owner, new FleetOrbit(planet.getOrbit(), planet.getSystem()));
        fleet = fleetService.save(fleet);
        final Set<WarShip> newFleetComposition = new HashSet<>();
        for (int i = 0; i <= amountShips; i++) {
            final String randomName = resourceService.getRandomWarshipName();
            final WarShip warShip = new WarShip(randomName, planet, fleet, shipClass);
            newFleetComposition.add(warShip);
        }
        warShipService.saveAll(newFleetComposition);
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
            if (workInProgress.getLevel() == targetLevel || job.getJobDoneAtZero() < 0) {
                // just delete the job - the last tick wasn't processed correctly
                LOGGER.warn("Job already processed: " + job.getForWarnMessage());
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

        Long tickOutput = null;
        if (EResourceType.POPULATION != resourceType) {
            tickOutput = ResourceControlCalculator.getTickOutput(planet, resourceType);
        }
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

    @Nullable
    public Tick getLatest() {
        return tickRepository.getLatest();
    }

    public boolean isTicking() {
        return isTicking;
    }
}
