package de.yuga.spacebattle.backend.services.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.i18n.Translation;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.turn.Constructable;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.backend.entities.turn.resources.PayingPossibleResult;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.EJobPriority;
import de.yuga.spacebattle.backend.enums.EJobType;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.repositories.turn.JobRepository;
import de.yuga.spacebattle.backend.services.ResourceService;
import de.yuga.spacebattle.backend.services.buildings.BuildingService;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.constructables.OperationalService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.WarShipService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.researches.ResearchService;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.*;

@Service
public class JobService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobService.class);

    @Nonnull
    private final JobRepository jobRepository;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final BuildingService buildingService;

    @Nonnull
    private final ResearchService researchService;

    @Nonnull
    private final WarShipService warShipService;

    @Nonnull
    private final ResourceService resourceService;

    @Nonnull
    private final FleetService fleetService;

    @Nonnull
    private final OperationalService operationalService;

    @Autowired
    public JobService(@Nonnull final JobRepository jobRepository,
                      @Nonnull final PlanetService planetService,
                      @Nonnull final BuildingService buildingService,
                      @Nonnull final ResearchService researchService,
                      @Nonnull final WarShipService warShipService,
                      @Nonnull final ResourceService resourceService,
                      @Nonnull final FleetService fleetService,
                      @Nonnull final OperationalService operationalService) {
        this.jobRepository = Preconditions.checkNotNull(jobRepository, "jobC shouldn't be null!");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");
        this.buildingService = Preconditions.checkNotNull(buildingService, "buildingService shouldn't be null!");
        this.researchService = Preconditions.checkNotNull(researchService, "researchService shouldn't be null!");
        this.warShipService = Preconditions.checkNotNull(warShipService, "warShipService must not be empty");
        this.resourceService = Preconditions.checkNotNull(resourceService, "resourceService must not be empty");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService must not be empty");
        this.operationalService = Preconditions.checkNotNull(operationalService, "operationalService must not be empty");
    }

    @Nonnull
    public final Job save(@Nonnull final Job entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        return jobRepository.save(entity);
    }

    public List<Research> getResearchesFromActiveJobs(final int idUser) {
        return jobRepository.getResearchesFromActiveJobs(idUser);
    }

    /**
     * Returns the job cost if they were paid - no costs and no costs back for researches!
     *
     * @param job the job to delete
     */
    public void refundJobAndDelete(@Nonnull final Job job) {
        Preconditions.checkNotNull(job, "job must not be empty");

        final Job doDelete = jobRepository.findById(job.getId()).orElse(null);
        if (doDelete == null) {
            return;
        }

        if (doDelete.getTicksLeft() > 0 && EResourceType.RESEARCH != doDelete.getConstructable().getResourceType()) {
            // if reached job is not done -> payback the paycheck
            // not reached if the job is a research
            final ResourceDeposit jobCosts = job.getConstructable().getJobCosts();
            final Planet planet = job.getFacility().getPlanet();

            final Fleet fleet = doDelete.getConstructable().getFleet();
            if (fleet != null) {
                final boolean repairJob = doDelete.getConstructable().isRepairJob();
                if (!repairJob) {
                    fleet.delete();
                    fleet.getAliveShips().forEach(WarShip::delete);
                    fleetService.save(fleet);
                }
            }

            final ResourceDeposit resourceDeposit = planet.getResourceDeposit();
            for (final EResourceType resourceType : EResourceType.valuesWhichAreCollectable()) {
                resourceDeposit.updateResource(resourceType, jobCosts.getResourceAmountByType(resourceType));
            }
            planetService.save(planet);
        }
        doDelete.complete();
        jobRepository.save(doDelete);
    }

    /**
     * Checks if the debit is in the credit and calculates if in the good case.
     * No exception is thrown if the bill is payed.
     *
     * @param planet the planet which should pay the bill
     * @param costs  the costs
     */
    private void checkAndBalances(@Nonnull final Planet planet,
                                  @Nonnull final ResourceDeposit costs) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");
        Preconditions.checkNotNull(costs, "costs shouldn't be null!");
        Preconditions.checkArgument(EDepositType.COSTS == costs.getSubType(), "costs must be flagged as costs!");

        final ResourceDeposit debtorDeposit = planet.getResourceDeposit();
        final PayingPossibleResult result = debtorDeposit.isPayingPossible(costs);
        if (!result.isValid()) {
            // new propagation idea for "no, this not"
            throw new NotifyWebUserException("This job is to expensive!", result);
        }
        debtorDeposit.pay(costs);
    }

    /**
     * Creates a entity by {@link Building#getId()} and {@link Planet#getId()}.
     * The building's level will be incremented by 1 in every {@link Job}.
     *
     * @param idPlanet   the planet where the entity should be executed
     * @param idBuilding the building which should be build
     * @return the created entity
     */
    @Nonnull
    public Job createConstructionYardJob(@Nonnull final Integer idPlanet, @Nonnull final Integer idBuilding) {
        Preconditions.checkNotNull(idPlanet, "idPlanet shouldn't be null!");
        Preconditions.checkNotNull(idBuilding, "idBuilding shouldn't be null!");

        final Planet planet = planetService.find(idPlanet);
        final Building building = buildingService.find(idBuilding);
        if (planet == null || planet.getHumanOwner() == null || building == null) {
            throw new NotifyWebUserException("not that way!");
        }

        final Set<Construction> constructions = planet.getConstructions();
        final Construction existingC = constructions.stream()
                .filter(construction -> construction.getBuilding().equals(building))
                .findFirst().orElse(null);

        final int level = researchService.getLevelForResearch(planet.getHumanOwner(), building.getUnlockedThrough());
        if (existingC != null && existingC.getLevel() >= level) {
            throw new NotifyWebUserException("You can't do that - first you have to research the '" + building.getUnlockedThrough().getName(Translation.DEFAULT_LANGUAGE) + "' research.");
        }

        final int targetLevel = existingC != null ? existingC.getLevel() + 1 : 1;
        final Constructable constructable = new Constructable(building, targetLevel);
        final Construction facility = planet.getConstructions().stream()
                .filter(construction -> construction.getBuilding().getProductionTarget() == EResourceType.CONSTRUCTION)
                .findFirst().orElse(null);

        checkIfFree(facility);
        checkAndBalances(planet, constructable.getJobCosts());
        Job entity = new Job(planet, facility, constructable, operationalService.getUtilizedPopulationForPlanet(idPlanet));
        entity = save(entity);

        LOGGER.info("Creating construction yard idJob '" + entity.getId() + "' " +
                "for idBuilding: '" + building.getId() + "' " +
                "with target level '" + constructable.getTargetLevel() + "' " +
                "from level '" + (targetLevel - 1) + "'");

        planetService.save(planet);
        return entity;
    }

    @Nonnull
    public Job createResearchJob(@Nonnull final User user, @Nonnull final Research research) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");
        Preconditions.checkNotNull(research, "research shouldn't be null!");

        if (researchService.isResearchAtLevelCap(user, research)) {
            throw new NotifyWebUserException("You can't do that - you already have the '" + research.getName(Translation.DEFAULT_LANGUAGE) + "' research.");
        }

        int levelCap = research.getLevelCap();
        int level = researchService.getLevelForResearch(user, research) + 1;
        if (level > levelCap) {
            throw new NotifyWebUserException("no way!");
        }
        final Planet researchPlanet = planetService.findResearchPlanet(user);
        if (researchPlanet == null) {
            throw new NotifyWebUserException("You need a research facility on at leas one planet.");
        }

        final Construction facility = researchPlanet.getConstructionByResource(EResourceType.RESEARCH)
                .stream().findFirst().orElse(null);
        checkIfFree(facility);

        final BigDecimal empireWideResearchPoints = planetService.getEmpireWideResearchPoints(user.getId());

        final Constructable constructable = new Constructable(research, level, empireWideResearchPoints);
        final Job entity = new Job(researchPlanet, facility, constructable, operationalService.getUtilizedPopulationForPlanet(researchPlanet.getId()));
        jobRepository.save(entity);
        return entity;
    }

    private void checkIfFree(@Nullable final Construction facility) {
        if (facility == null) {
            throw new NotifyWebUserException("not here, buddy!");
        }
        if (!facility.getJobs().isEmpty()) {
            throw new NotifyWebUserException("Job in progress");
        }
    }

    public void createShipyardRepairJob(@Nonnull final Planet planet, @Nonnull final Map<ShipClass, Integer> shipJobPayload) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");
        Preconditions.checkNotNull(shipJobPayload, "shipJobPayload shouldn't be null!");

        final User owner = planet.getHumanOwner();
        if (owner == null) {
            throw new NotifyWebUserException("You should own this planet, buddy.");
        }

        final Construction facility = planet.getConstructions().stream()
                .filter(construction -> construction.getBuilding().getProductionTarget() == EResourceType.ORBITAL_CONSTRUCTION)
                .findFirst().orElse(null);

        checkIfFree(facility);
        Fleet fleet = new Fleet("Fresh Build @ " + planet.getName(), owner, new FleetOrbit(planet.getOrbit(), planet.getSystem()));
        // small hack to don't display the new build fleet on map and in fleet list before "under construction handling"
        fleet.delete();
        fleet = fleetService.save(fleet);
        final List<WarShip> newFleetComposition = new ArrayList<>();
        for (final Map.Entry<ShipClass, Integer> entry : shipJobPayload.entrySet()) {
            final ShipClass shipClass = entry.getKey();
            final Integer amount = entry.getValue();

            final Set<String> randomNames = resourceService.getRandomShipNamesForOwner(owner, amount);
            for (final String randomName : randomNames) {
                final WarShip warShip = new WarShip(randomName, planet, fleet, shipClass);
                // analogous behavior like at the 'delete' fleet
                warShip.delete();
                newFleetComposition.add(warShip);
            }
        }
        warShipService.saveAll(newFleetComposition);

        fleet = fleetService.find(fleet);
        final Constructable constructable = new Constructable(fleet, EJobType.CONSTRUCTION);
        checkAndBalances(planet, constructable.getJobCosts());
        final Job job = new Job(planet, facility, constructable, operationalService.getUtilizedPopulationForPlanet(planet.getId()));
        jobRepository.save(job);
        planetService.save(planet);
    }

    public void createShipyardUpgradeJob(@Nonnull final Planet planet, @Nonnull final Fleet toUpgrade) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(toUpgrade, "toUpgrade must not be empty");

        final Construction facility = planet.getConstructions().stream()
                .filter(construction -> construction.getBuilding().getProductionTarget() == EResourceType.ORBITAL_CONSTRUCTION)
                .findFirst().orElseThrow(() -> new NotifyWebUserException("If you want to repair a fleet, you need a shipyard."));

        // do not check if free, the new job will squeeze in
        final Constructable constructable = new Constructable(toUpgrade, EJobType.UPGRADE);
        checkAndBalances(planet, constructable.getJobCosts());
        final Job job = new Job(planet, facility, constructable, operationalService.getUtilizedPopulationForPlanet(planet.getId()));
        job.setPriority(EJobPriority.PRIORITY);
        jobRepository.save(job);
        fleetService.disableFleet(toUpgrade);
        fleetService.transferCrewToPlanet(toUpgrade, planet);
        planetService.save(planet);
    }

    public void createShipyardRepairJob(@Nonnull final Planet planet, @Nonnull final Fleet toRepair) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(toRepair, "toRepair must not be empty");

        final Construction facility = planet.getConstructions().stream()
                .filter(construction -> construction.getBuilding().getProductionTarget() == EResourceType.ORBITAL_CONSTRUCTION)
                .findFirst().orElseThrow(() -> new NotifyWebUserException("If you want to repair a fleet, you need a shipyard."));

        // do not check if free, the new job will squeeze in
        final Constructable constructable = new Constructable(toRepair, EJobType.REPAIR);
        checkAndBalances(planet, constructable.getJobCosts());
        final Job job = new Job(planet, facility, constructable, operationalService.getUtilizedPopulationForPlanet(planet.getId()));
        job.setPriority(EJobPriority.PRIORITY);
        jobRepository.save(job);
        fleetService.disableFleet(toRepair);
        fleetService.transferCrewToPlanet(toRepair, planet);
        planetService.save(planet);
    }

    @Nonnull
    public List<Job> findAllJobsByPlanet(final int idPlanet) {
        return jobRepository.findAllJobsByPlanet(idPlanet);
    }

    @Nonnull
    public List<Job> findAllJobsForUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user must not be empty");

        return jobRepository.findAllJobsForUser(user.getId());
    }

    @Nullable
    public Job findResearchJobForUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user must not be empty");

        return jobRepository.findResearchJobForUser(user.getId());
    }

    @Nonnull
    public List<Job> findResearchJobs() {
        return Objects.requireNonNullElse(jobRepository.findResearchJobs(), new ArrayList<>());
    }

    @Nonnull
    public List<Job> findAllJobsForUser(final int idUser) {
        return jobRepository.findAllJobsForUser(idUser);
    }

    public boolean isJobRunningFor(final int idUser, final int idFleet) {
        return jobRepository.isActiveJobRunningFor(idUser, idFleet);
    }

    public List<Job> findTodayFinishedJobsForUser(final int idUser) {
        return Objects.requireNonNullElse(jobRepository.findTodayFinishedJobsForUser(idUser), new ArrayList<>());
    }

    public boolean cancelJob(final int idUser, final int idJob) {
        final Job job = jobRepository.findById(idJob).orElse(null);
        if (job == null || job.getOwner().getId() != idUser) {
            return false;
        }
        refundJobAndDelete(job);
        return true;
    }

    public void saveAll(@Nonnull final List<Job> jobs) {
        Preconditions.checkNotNull(jobs, "jobs must not be empty");

        jobRepository.saveAll(jobs);
    }
}
