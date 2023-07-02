package de.yuga.spacebattle.backend.services.turn.tick;

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
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.turn.Constructable;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthState;
import de.yuga.spacebattle.backend.entities.turn.resources.PayingPossibleResult;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.ECalculationType;
import de.yuga.spacebattle.backend.enums.ERefinementSequence;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.services.caches.OperationalCache;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.constructables.buildings.ConstructionService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.WarShipService;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlanetTickRunner implements TickRunner {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(PlanetTickRunner.class);

    @Nullable
    private Tick today;

    @Nonnull
    private final OperationalCache operationalCache;

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
    private final WarShipService warShipService;

    @Nonnull
    private final WarshipHealthStateService warshipHealthStateService;

    @Autowired
    public PlanetTickRunner(@Nonnull final OperationalCache operationalCache,
                            @Nonnull final JobService jobService,
                            @Nonnull final PlanetService planetService,
                            @Nonnull final FleetService fleetService,
                            @Nonnull final ConstructionService constructionService,
                            @Nonnull final ResearchService researchService,
                            @Nonnull final WarShipService warShipService,
                            @Nonnull final WarshipHealthStateService warshipHealthStateService) {
        this.operationalCache = Preconditions.checkNotNull(operationalCache, "operationalCache must not be empty");
        this.jobService = Preconditions.checkNotNull(jobService, "jobService shouldn't be null!");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService shouldn't be null!");
        this.constructionService = Preconditions.checkNotNull(constructionService, "constructionService shouldn't be null!");
        this.researchService = Preconditions.checkNotNull(researchService, "researchService shouldn't be null!");
        this.warShipService = Preconditions.checkNotNull(warShipService, "warShipService must not be empty");
        this.warshipHealthStateService = Preconditions.checkNotNull(warshipHealthStateService, "warshipHealthStateService must not be empty");
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
            tickFleetsAtStarbase(p);
        }
    }

    /**
     * Calculates the tickly output of this planet.
     * This includes the amount of generated resources and the calculations of jobs which could be successfully ended.
     */
    public void tickPlanet(@Nonnull Planet planet) {
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

        final List<Construction> ops = new ArrayList<>();
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

        final User owner = planet.getHumanOwner();
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
            fleetService.save(biggestInOrbit);
        } else {
            fleet.animate();
            fleetService.save(fleet);
        }
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
        final User owner = planet.getHumanOwner();
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
     * Counts down the remaining {@link Job#getTicksLeft()}.
     *
     * @param job the {@link Job} to do
     * @return <code>true</code> if the job is done
     */
    private boolean tickJob(@Nonnull final Job job) {
        Preconditions.checkNotNull(job, "job shouldn't be null!");

        job.tick();
        return job.getTicksLeft() <= 0;
    }
}
