package de.yuga.spacebattle.backend.services.turn.tick;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.resource.PopulationControlCalculator;
import de.yuga.spacebattle.backend.calculator.resource.ResourceControlCalculator;
import de.yuga.spacebattle.backend.dto.research.EmpireResearchCapability;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.turn.Constructable;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthState;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.constructables.OperationalService;
import de.yuga.spacebattle.backend.services.constructables.buildings.ConstructionService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.researches.ResearchService;
import de.yuga.spacebattle.backend.services.turn.JobService;
import de.yuga.spacebattle.backend.services.turn.battle.combat.WarshipHealthStateService;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PlanetTickRunner implements TickRunner {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(PlanetTickRunner.class);

    @Nullable
    private Tick today;

    @Nonnull
    private final JobService jobService;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final FleetService fleetService;

    @Nonnull
    private final ConstructionService constructionService;

    @Nonnull
    private final ResearchService researchService;

    @Nonnull
    private final WarshipHealthStateService warshipHealthStateService;

    @Nonnull
    private final OperationalService operationalService;

    @Autowired
    public PlanetTickRunner(@Nonnull final JobService jobService,
                            @Nonnull final PlanetService planetService,
                            @Nonnull final FleetService fleetService,
                            @Nonnull final ConstructionService constructionService,
                            @Nonnull final ResearchService researchService,
                            @Nonnull final WarshipHealthStateService warshipHealthStateService,
                            @Nonnull final OperationalService operationalService) {
        this.jobService = Preconditions.checkNotNull(jobService, "jobService shouldn't be null!");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService shouldn't be null!");
        this.constructionService = Preconditions.checkNotNull(constructionService, "constructionService shouldn't be null!");
        this.researchService = Preconditions.checkNotNull(researchService, "researchService shouldn't be null!");
        this.warshipHealthStateService = Preconditions.checkNotNull(warshipHealthStateService, "warshipHealthStateService must not be empty");
        this.operationalService = Preconditions.checkNotNull(operationalService, "operationalService must not be empty");
    }

    @Override
    public void tick(@Nonnull final Tick today) {
        this.today = Preconditions.checkNotNull(today, "today must not be empty");

        LOGGER.info("Tick planets");
        tickPlanets();
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
     * Calculates the tickly output of this planet.
     * This includes the amount of generated resources and the calculations of jobs which could be successfully ended.
     */
    private void tickPlanet(@Nonnull Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkState(planet.getOwner() != null, "The owner must be set, otherwise there is nothing to do.");

        log(planet, "Start updating resources.");
        planet = updateResources(planet);
        log(planet, "Done updating resources");
        planet = runJobs(planet);
        log(planet, "Done tick planet.");
    }

    private Planet runJobs(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(today, "today must not be empty");

        final Set<Construction> constructions = constructionService.findAllConstructionsOnPlanetWithJobs(planet.getId());

        final Set<Construction> withJobs = constructions.stream().filter(c -> !c.getJobs().isEmpty()).collect(Collectors.toSet());
        for (final Construction facility : withJobs) {
            final EResourceType resourceType = facility.getBuilding().getProductionTarget();
            final Set<Job> jobs = facility.getJobs();

            final Job job = jobs.stream()
                    .min(Job::compareTo)
                    .orElseThrow(() -> new NotifyWebUserException("Yeah, shit happens. This can not happen."));
            log(planet, job, "Start processing " + resourceType + " job.");

            if (resourceType == EResourceType.RESEARCH) {
                final int idUser = Objects.requireNonNull(planet.getOwner()).getId();
                final long empireWideResearchPointsLeftOver = planetService.getEmpireWideResearchPoints(idUser).getEmpireWideResearchPointsLeftOver();
                final long usedPoints = tickJob(job, empireWideResearchPointsLeftOver);
                if (!job.isFinished()) {
                    jobService.save(job);
                    log(planet, job, "Shifting job for tick after " + today + ".");
                    continue;
                }
                completeResearch(planet, job, usedPoints, today);
            } else {
                final long points = planet.getResourceDeposit().getResourceAmountByType(resourceType);
                final long usedPoints = tickJob(job, points);
                planet.getResourceDeposit().updateResource(resourceType, -usedPoints);
                if (!job.isFinished()) {
                    jobService.save(job);
                    log(planet, job, "Shifting job for tick after " + today + ".");
                    continue;
                }
                switch (resourceType) {
                    case CONSTRUCTION:
                        completeConstruction(planet, constructions, job, today);
                        break;
                    case ORBITAL_CONSTRUCTION:
                        completeShipyard(planet, job, today);
                        break;
                }
            }
        }
        return planetService.save(planet);
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

    private void completeShipyard(@Nonnull final Planet planet, @Nonnull final Job job, @Nonnull final Tick today) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(job, "job must not be empty");
        Preconditions.checkNotNull(today, "today must not be empty");

        log(planet, job, "Start processing shipyard job .");
        final Constructable constructable = job.getConstructable();
        final Fleet fleet = constructable.getFleet();
        if (fleet == null) {
            return;
        }

        final User owner = planet.getHumanOwner();
        assert owner != null : "There must be a planet's owner.";
        job.setFinished(today);
        if (constructable.isRepairJob()) {
            realizeFleetRepair(planet, owner, constructable, job);
        } else if (constructable.isUpgradeJob()) {
            realizeFleetUpgrade(planet, owner, constructable, job);
        } else {
            realizeShipProduction(planet, owner, constructable, job);
        }

        log(planet, job, "Done processing shipyard job.");
    }

    private void realizeFleetUpgrade(@Nonnull final Planet planet,
                                     @Nonnull final User owner,
                                     @Nonnull final Constructable constructable,
                                     @Nonnull final Job job) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(owner, "owner must not be empty");
        Preconditions.checkNotNull(constructable, "constructable must not be empty");
        Preconditions.checkNotNull(job, "job must not be empty");
        Preconditions.checkNotNull(constructable.getFleet(), "fleet must not be empty");

        final Fleet fleet = constructable.getFleet();
        log(planet, job, "Start upgrade fleet '" + fleet.getId() + "'.");
        final Set<WarShip> withSuccessors = fleet.getAliveShips().stream()
                .filter(w -> w.getShipClass().hasSuccessor())
                .collect(Collectors.toSet());

        withSuccessors.forEach(warShip -> {
            // this is introducing a nice exploit by creating a new flight after paying the old one
            final ShipClass shipClass = warShip.getShipClass();
            ShipClass successor = shipClass.getSuccessor();
            while (Objects.requireNonNull(successor).hasSuccessor()) {
                successor = successor.getSuccessor();
            }
            warShip.upgrade(planet, successor);
        });

        fleetService.save(fleet);
        log(planet, job, "Done upgrade fleet '" + fleet.getId() + "'.");
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
            fleetService.save(biggestInOrbit);
        } else {
            fleet.animate();
            fleetService.save(fleet);
        }
        log(planet, job, "Done creating warships.");
    }

    private void completeConstruction(@Nonnull final Planet planet,
                                      @Nonnull final Set<Construction> constructions,
                                      @Nonnull final Job job,
                                      @Nonnull final Tick today) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(constructions, "constructions must not be empty");
        Preconditions.checkNotNull(job, "job must not be empty");
        Preconditions.checkNotNull(today, "today must not be empty");

        log(planet, job, "Start processing construction job.");
        job.setFinished(today);
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

    private void completeResearch(@Nonnull final Planet planet, @Nonnull final Job job, final long usedPoints, @Nonnull final Tick today) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(job, "job must not be empty");
        Preconditions.checkNotNull(today, "today must not be empty");

        log(planet, job, "Start processing research job.");
        job.setFinished(today);
        final Constructable constructable = job.getConstructable();
        final User owner = planet.getHumanOwner();
        if (owner == null) {
            throw new NotifyWebUserException("There must be a planet's owner.");
        }
        final Research research = constructable.getResearch();
        if (research == null) {
            throw new NotifyWebUserException("Oh fuck, this should not happen while research whatever!");
        }
        researchService.addResearch(owner, List.of(research));
        planetService.reduceResearchPoints(owner.getId(), usedPoints);
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
                final ResourceDeposit demand = operationalService.getPopulationDemandForPlanet(planet.getId());
                PopulationControlCalculator.educatePopulation(planet, demand);
                // do birth
                PopulationControlCalculator.populatePlanet(planet, operationalService.getUtilizedPopulationForPlanet(planet.getId()));
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
     * Counts down the remaining {@link Job#getPointsLeft()}.
     *
     * @param job the {@link Job} to do
     * @return the used points will be returned
     */
    private long tickJob(@Nonnull final Job job, final long points) {
        Preconditions.checkNotNull(job, "job shouldn't be null!");

        return job.tick(points);
    }

    @Nonnull
    public Job tickInstaResearch(@Nonnull final Job job, @Nonnull final EmpireResearchCapability capability, @Nonnull final Tick today) {
        Preconditions.checkNotNull(job, "job must not be empty");
        Preconditions.checkNotNull(capability, "capability must not be empty");
        Preconditions.checkNotNull(today, "today must not be empty");

        final Planet planet = planetService.find(job.getFacility().getPlanet());
        Preconditions.checkNotNull(planet, "planet must not be empty");

        if (job.getPointsLeft() <= capability.getEmpireWideResearchPointsLeftOver()) {
            final long usedPoints = tickJob(job, capability.getEmpireWideResearchPointsLeftOver());
            completeResearch(job.getFacility().getPlanet(), job, usedPoints, today);
            return jobService.save(job);
        }
        return job;
    }

    public void tickInstaShipyard(@Nonnull Job job, @Nonnull final Tick today) {
        Preconditions.checkNotNull(job, "job must not be empty");
        Preconditions.checkNotNull(today, "today must not be empty");

        job = jobService.findById(job.getId());
        Preconditions.checkNotNull(job, "job must not be empty");

        final Planet planet = job.getFacility().getPlanet();
        if (JobService.isLocalInstaJobPossible(planet, job)) {
            final long points = planet.getResourceDeposit().getResourceAmountByType(EResourceType.ORBITAL_CONSTRUCTION);
            final long usedPoints = tickJob(job, points);
            planet.getResourceDeposit().updateResource(EResourceType.ORBITAL_CONSTRUCTION, -usedPoints);
            completeShipyard(planet, job, today);
            planetService.save(planet);
            jobService.save(job);
        }
    }

    public void tickInstaConstruction(@Nonnull Job job, @Nonnull final Tick today) {
        Preconditions.checkNotNull(job, "job must not be empty");
        Preconditions.checkNotNull(today, "today must not be empty");

        job = jobService.findById(job.getId());
        Preconditions.checkNotNull(job, "job must not be empty");

        final Planet planet = job.getFacility().getPlanet();
        final Set<Construction> constructions = constructionService.findAllConstructionsOnPlanet(planet.getId());
        if (JobService.isLocalInstaJobPossible(planet, job)) {
            final long points = planet.getResourceDeposit().getResourceAmountByType(EResourceType.CONSTRUCTION);
            final long usedPoints = tickJob(job, points);
            planet.getResourceDeposit().updateResource(EResourceType.CONSTRUCTION, -usedPoints);
            completeConstruction(planet, constructions, job, today);
            planetService.save(planet);
            jobService.save(job);
        }
    }
}
