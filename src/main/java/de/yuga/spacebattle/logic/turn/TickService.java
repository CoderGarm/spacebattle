package de.yuga.spacebattle.logic.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.entities.Constructable;
import de.yuga.spacebattle.entities.ResourceDeposit;
import de.yuga.spacebattle.entities.buildings.Building;
import de.yuga.spacebattle.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.entities.orbitals.Planet;
import de.yuga.spacebattle.entities.orbitals.Starsystem;
import de.yuga.spacebattle.entities.turn.Job;
import de.yuga.spacebattle.entities.turn.Move;
import de.yuga.spacebattle.entities.turn.Tick;
import de.yuga.spacebattle.enums.EResourceType;
import de.yuga.spacebattle.repositories.combined.spacecraft.FleetRepository;
import de.yuga.spacebattle.repositories.orbitals.PlanetRepository;
import de.yuga.spacebattle.repositories.turn.JobRepository;
import de.yuga.spacebattle.repositories.turn.MoveRepository;
import de.yuga.spacebattle.repositories.turn.TickRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
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
    private final JobRepository jobC;

    @Nonnull
    private final PlanetRepository planetC;

    @Nonnull
    private final MoveRepository moveC;

    @Nonnull
    private final FleetRepository fleetC;

    @Autowired
    public TickService(@Nonnull final TickRepository tickC,
                       @Nonnull final JobRepository jobC,
                       @Nonnull final PlanetRepository planetC,
                       @Nonnull final MoveRepository moveC,
                       @Nonnull final FleetRepository fleetC) {
        Preconditions.checkNotNull(tickC, "tickC shouldn't be null!");
        Preconditions.checkNotNull(jobC, "jobC shouldn't be null!");
        Preconditions.checkNotNull(planetC, "planetC shouldn't be null!");
        Preconditions.checkNotNull(moveC, "moveC shouldn't be null!");
        Preconditions.checkNotNull(fleetC, "fleetC shouldn't be null!");

        this.tickC = tickC;
        this.jobC = jobC;
        this.planetC = planetC;
        this.moveC = moveC;
        this.fleetC = fleetC;
    }

    @Transactional(rollbackFor = Exception.class)
    public Tick doTick() {
        Tick entity = new Tick();
        tickC.save(entity);
        List<Planet> planets = planetC.findAllOwnedPlanets();
        for (Planet p : planets) {
            tick(p);
            planetC.save(p);
        }
        List<Move> movings = moveC.findAllMoves();
        for (Move m : movings) {
            boolean isDone = move(m);
            if (isDone) {
                fleetC.save(m.getFleet());
                moveC.delete(m);
            } else {
                moveC.save(m);
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
        fleetC.save(fleet);
        moveC.save(move);
        return true;
    }

    /**
     * Calulates the tickly output of this planet.
     */
    private void tick(@Nonnull final Planet planet) {
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
            boolean remainingPoints = calculateConstructablePointsRemaining(resourceDeposit, resourceType, job);
            if (remainingPoints) {
                continue;
            }
            Constructable constructable = job.getConstructable();
            switch (resourceType) {
                case RESEARCH:
                    LOGGER.info("Nothing to do here, sorry.");
                    break;
                case CONSTRUCTION:

                    Building building = constructable.getBuilding();
                    Integer targetLevel = constructable.getTargetLevel();
                    if (building == null || targetLevel == null) {
                        throw new NotifySBUserException("Oh fuck, this should not happen while constructing buildings!");
                    }
                    Construction toUpgrade = constructions.stream()
                            .filter(c -> c.getBuilding().equals(building)).findFirst().orElse(null);
                    if (toUpgrade != null) {
                        toUpgrade.setLevel(targetLevel);
                    } else {
                        constructions.add(new Construction(planet, building, 1));
                    }
                    break;
                case ORBITALCONSTRUCTION:

                    ShipClass shipClass = constructable.getShipClass();
                    Integer amountShips = constructable.getAmountShips();
                    if (shipClass == null || amountShips == null || amountShips == 0) {
                        throw new NotifySBUserException("This should never happen while build a fleet!");
                    }
                    Fleet fleet = new Fleet("Fresh Build @ " + planet.getName(), planet.getOwner(), new FleetOrbit(planet.getSystem(), planet));
                    fleet.updateShips(shipClass, amountShips);
                    fleetC.save(fleet);
                    break;
            }
            planetC.save(planet);
            jobC.delete(job);
        }
    }

    private boolean calculateConstructablePointsRemaining(ResourceDeposit resourceDeposit, EResourceType resourceType, Job job) {
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
