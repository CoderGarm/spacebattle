package de.yuga.spacebattle.backend.services.turn.tick;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.services.caclulator.PopulationControlCalculator;
import de.yuga.spacebattle.backend.services.caclulator.TickOutputCalculator;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.constructables.buildings.ConstructionService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.OrbitalStructureService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.WarShipService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.turn.JobService;
import de.yuga.spacebattle.backend.services.turn.battle.combat.WarshipHealthStateService;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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
    private final WarshipHealthStateService warshipHealthStateService;

    @Nonnull
    private final PopulationControlCalculator populationControlCalculator;

    @Nonnull
    private final TickOutputCalculator tickOutputCalculator;

    @Nonnull
    private final WarShipService warShipService;

    @Nonnull
    private final OrbitalStructureService orbitalStructureService;

    @Autowired
    public PlanetTickRunner(@Nonnull final JobService jobService,
                            @Nonnull final PlanetService planetService,
                            @Nonnull final FleetService fleetService,
                            @Nonnull final ConstructionService constructionService,
                            @Nonnull final WarshipHealthStateService warshipHealthStateService,
                            @Nonnull final PopulationControlCalculator populationControlCalculator,
                            @Nonnull final TickOutputCalculator tickOutputCalculator,
                            @Nonnull final WarShipService warShipService,
                            @Nonnull final OrbitalStructureService orbitalStructureService) {
        this.jobService = Preconditions.checkNotNull(jobService, "jobService shouldn't be null!");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService shouldn't be null!");
        this.constructionService = Preconditions.checkNotNull(constructionService, "constructionService shouldn't be null!");
        this.warshipHealthStateService = Preconditions.checkNotNull(warshipHealthStateService, "warshipHealthStateService must not be empty");
        this.populationControlCalculator = Preconditions.checkNotNull(populationControlCalculator, "populationControlCalculator must not be empty");
        this.tickOutputCalculator = Preconditions.checkNotNull(tickOutputCalculator, "tickOutputCalculator must not be empty");
        this.warShipService = Preconditions.checkNotNull(warShipService, "warShipService must not be empty");
        this.orbitalStructureService = Preconditions.checkNotNull(orbitalStructureService, "orbitalStructureService must not be empty");
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

        LOGGER.info("[Planet #{} - #{}] [Job #{}] {}", planet.getId(), planet.getName(), job.getId(), msg);
    }

    /**
     * Runs the tick for all planets.
     */
    private void tickPlanets() {

        final List<CompletableFuture<Boolean>> futures = new ArrayList<>();

        final List<Planet> planets = planetService.findAllForTick();
        for (final Planet p : planets) {
            final CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                log(p, "Start ticking planet");
                tickPlanet(p);
                return true;
            });
            futures.add(future);
        }

        final CompletableFuture<Void> allCompleted = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            allCompleted.get();
            LOGGER.info("Tick planets done");
        } catch (final InterruptedException | ExecutionException e) {
            LOGGER.warn("Exception ticking planets in parallel.", e);
            throw new NotifyWebUserException(e.getMessage());
        }
    }

    /**
     * Calculates the tickly output of this planet.
     * This includes the amount of generated resources and the calculations of jobs which could be successfully ended.
     */
    private void tickPlanet(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkState(planet.getOwner() != null, "The owner must be set, otherwise there is nothing to do.");

        log(planet, "Start updating resources.");
        updateResources(planet);
        log(planet, "Done updating resources");
        runJobs(planet);
        log(planet, "Done tick planet.");
    }

    private void runJobs(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(today, "today must not be empty");

        final Set<Construction> constructions = planet.getConstructions().stream()
                .filter(c -> !c.getJobs().isEmpty())
                .filter(c -> c.getBuilding().getProductionTarget() != EResourceType.RESEARCH)
                .collect(Collectors.toSet());
        for (final Construction facility : constructions) {
            final EResourceType resourceType = facility.getBuilding().getProductionTarget();
            final Set<Job> jobs = facility.getJobs();

            final Job job = jobs.stream()
                    .min(Job::compareTo)
                    .orElseThrow(() -> new NotifyWebUserException("Yeah, shit happens. This can not happen."));
            log(planet, job, "Start processing " + resourceType + " job.");

            final long points = planet.getResourceDeposit().getResourceAmountByType(resourceType);
            final long usedPoints = jobService.tickJob(job, points);
            planet.getResourceDeposit().updateResource(resourceType, -usedPoints);
            if (!job.isFinished()) {
                jobService.save(job);
                planetService.save(planet);
                log(planet, job, "Shifting job for tick after " + today + ".");
                continue;
            }
            switch (resourceType) {
                case CONSTRUCTION:
                    log(planet, job, "Start processing construction job.");
                    jobService.completeConstruction(planet, planet.getConstructions(), job, today);
                    log(planet, job, "Done processing construction job.");
                    break;
                case ORBITAL_CONSTRUCTION:
                    log(planet, job, "Start processing shipyard job .");
                    jobService.completeShipyard(planet, job, today);
                    log(planet, job, "Done processing shipyard job.");
                    break;
            }
        }
        planetService.save(planet);
    }

    private void updateResources(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet must not be empty");

        for (final EResourceType resourceType : EResourceType.values()) {
            updateResourceDeposit(planet, resourceType);
        }
        LOGGER.info("Saving planet");
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

        final ResourceDeposit resourceDeposit = planet.getResourceDeposit();
        switch (resourceType.getCollectableType()) {
            case VIABLE:
                // do school
                populationControlCalculator.educatePopulation(planet);
                // do birth
                populationControlCalculator.populatePlanet(planet);
                break;
            case FORFEITABLE:
                // only set new available points
                resourceDeposit.setAbsoluteResourceValue(resourceType, tickOutputCalculator.getTickOutput(planet, resourceType));
                break;
            default:
            case COLLECTABLE:
                // add points to the old deposit
                resourceDeposit.updateResource(resourceType, tickOutputCalculator.getTickOutput(planet, resourceType));
                break;
        }
    }

}
