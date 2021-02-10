package de.yuga.spacebattle.backend.services.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.Constructable;
import de.yuga.spacebattle.backend.entities.ResourceDeposit;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.Starsystem;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.backend.entities.turn.Move;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.repositories.turn.TickRepository;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.constructables.buildings.ConstructionService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class TickService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TickService.class);

    @Nonnull
    private final TickRepository tickC;

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

    @Autowired
    public TickService(@Nonnull final TickRepository tickC,
                       @Nonnull final JobService jobService,
                       @Nonnull final PlanetService planetService,
                       @Nonnull final MoveService moveService,
                       @Nonnull final FleetService fleetService,
                       @Nonnull final ConstructionService constructionService,
                       @Nonnull final UserService userService) {
        Preconditions.checkNotNull(tickC, "tickC shouldn't be null!");
        Preconditions.checkNotNull(jobService, "jobService shouldn't be null!");
        Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");
        Preconditions.checkNotNull(moveService, "moveService shouldn't be null!");
        Preconditions.checkNotNull(fleetService, "fleetService shouldn't be null!");
        Preconditions.checkNotNull(constructionService, "constructionService shouldn't be null!");
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");

        this.tickC = tickC;
        this.jobService = jobService;
        this.planetService = planetService;
        this.moveService = moveService;
        this.fleetService = fleetService;
        this.constructionService = constructionService;
        this.userService = userService;
    }

    //@Scheduled(cron = "* */1 * * * *")
    private void doIt() {
        LOGGER.info("Tick scheduled");
        this.doTick();
        LOGGER.info("Tick has processed!");
    }

    @Nonnull
    public List<Tick> findAll() {
        return tickC.findAllTicks();
    }

    @Nullable
    public Tick find(@Nonnull final Integer idHull) {
        Preconditions.checkNotNull(idHull, "idHull shouldn't be null!");

        return tickC.findById(idHull).orElse(null);
    }

    @Transactional(rollbackFor = Exception.class)
    public Tick doTick() {
        Tick entity = new Tick();
        tickC.save(entity);
        List<Planet> planets = planetService.findAllColonized();
        for (Planet p : planets) {
            tick(p);
            planetService.save(p);
        }
        List<Move> movings = moveService.findAll();
        for (Move m : movings) {
            boolean isDone = move(m);
            if (isDone) {
                fleetService.save(m.getFleet());
                moveService.delete(m);
            } else {
                moveService.save(m);
            }
        }

        entity.setTickEnds(LocalDateTime.now());
        return entity;
    }

    private boolean move(@Nonnull final Move move) {
        Preconditions.checkNotNull(move, "move shouldn't be null!");

        int moveDoneAtZero = move.getMoveDoneAtZero();
        moveDoneAtZero--;
        if (moveDoneAtZero > 0) {
            move.setMoveDoneAtZero(moveDoneAtZero);
            return false;
        }

        FleetOrbit targetOrbit = move.getTargetOrbit();
        Starsystem targetSystem = targetOrbit.getSystem();
        Planet targetPlanet = targetOrbit.getPlanet();

        Fleet fleet = move.getFleet();
        fleet.setOrbit(new FleetOrbit(targetSystem, targetPlanet));
        fleetService.save(fleet);
        moveService.save(move);
        return true;
    }

    /**
     * Calulates the tickly output of this planet.
     */
    @Transactional
    void tick(@Nonnull final Planet planet) {
        Preconditions.checkState(planet.getOwner() != null,
                "The owner must be set, otherwise there is nothing to do.");

        Set<Construction> constructions = planet.getConstructions();
        ResourceDeposit resourceDeposit = planet.getResourceDeposit();
        for (Construction construction : constructions) {
            EResourceType resourceType = calculateTickOutput(resourceDeposit, planet, construction);
            Job job = construction.getJob();
            if (job == null) {
                continue;
            }
            boolean remainingPoints = calculateConstructablePointsRemaining(job, resourceDeposit, resourceType);
            if (remainingPoints) {
                jobService.save(job);
                continue;
            }
            Constructable constructable = job.getConstructable();
            Integer targetLevel;
            User owner = planet.getOwner();
            switch (resourceType) {
                case RESEARCH:

                    Research research = constructable.getResearch();
                    targetLevel = constructable.getTargetLevel();
                    if (research == null || targetLevel == null) {
                        throw new NotifySBUserException("Oh fuck, this should not happen while research whatever!");
                    }
                    owner.getResearches().put(research, targetLevel);
                    userService.save(owner);
                    break;
                case CONSTRUCTION:

                    Building building = constructable.getBuilding();
                    targetLevel = constructable.getTargetLevel();
                    if (building == null || targetLevel == null) {
                        throw new NotifySBUserException("Oh fuck, this should not happen while constructing buildings!");
                    }
                    Construction workInProgress = constructions.stream()
                            .filter(c -> c.getBuilding().equals(building)).findFirst().orElse(null);
                    if (workInProgress != null) {
                        workInProgress.setLevel(targetLevel);
                    } else {
                        workInProgress = new Construction(planet, building, 1);
                        constructions.add(workInProgress);
                    }
                    constructionService.save(workInProgress);
                    break;
                case ORBITALCONSTRUCTION:

                    ShipClass shipClass = constructable.getShipClass();
                    Integer amountShips = constructable.getAmountShips();
                    if (shipClass == null || amountShips == null || amountShips == 0) {
                        throw new NotifySBUserException("This should never happen while build a fleet!");
                    }
                    Fleet fleet = new Fleet("Fresh Build @ " + planet.getName(), owner, new FleetOrbit(planet.getSystem(), planet));
                    fleet.updateShips(shipClass, amountShips);
                    fleetService.save(fleet);
                    break;
            }
            jobService.delete(job);
        }
        planetService.save(planet);
    }

    /**
     * Counts down the remaining {@link Job#getJobDoneAtZero()}.
     *
     * @param job             the {@link Job} to do
     * @param resourceDeposit the {@link ResourceDeposit} to take from
     * @param resourceType    the {@link EResourceType} wo calculate for
     * @return <code>true</code> is the job is done
     */
    private boolean calculateConstructablePointsRemaining(@Nonnull final Job job,
                                                          @Nonnull final ResourceDeposit resourceDeposit,
                                                          @Nonnull final EResourceType resourceType) {
        Preconditions.checkNotNull(job, "job shouldn't be null!");
        Preconditions.checkNotNull(resourceDeposit, "resourceDeposit shouldn't be null!");
        Preconditions.checkNotNull(resourceType, "resourceType shouldn't be null!");

        BigDecimal jobDoneAtZero = job.getJobDoneAtZero();
        BigDecimal pointsLeftOverForToday = resourceDeposit.getResourceAmountByType(resourceType);
        BigDecimal remainingToZero = jobDoneAtZero.subtract(pointsLeftOverForToday);
        resourceDeposit.updateResource(resourceType, pointsLeftOverForToday.negate().add(remainingToZero));

        if (remainingToZero.compareTo(BigDecimal.ZERO) < 1) {
            job.setJobDoneAtZero(BigDecimal.ZERO);
        } else {
            job.setJobDoneAtZero(remainingToZero);
            return true;
        }
        return false;
    }

    @Nonnull
    private EResourceType calculateTickOutput(@Nonnull final ResourceDeposit resourceDeposit,
                                              @Nonnull final Planet planet,
                                              @Nonnull final Construction construction) {
        Preconditions.checkNotNull(resourceDeposit, "resourceDeposit shouldn't be null!");
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");
        Preconditions.checkNotNull(construction, "construction shouldn't be null!");

        ResourceDeposit resourcefactors = planet.getResourcefactors();
        EResourceType resourceType = construction.getBuilding().getResourceType();
        BigDecimal factorByPlanet = resourcefactors.getResourceAmountByType(resourceType);
        BigDecimal tickOutput = construction.getTickOutput(factorByPlanet);
        resourceDeposit.updateResource(resourceType, tickOutput);
        return resourceType;
    }
}
