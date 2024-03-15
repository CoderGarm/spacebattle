package de.yuga.spacebattle.backend.services.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.research.EmpireResearchCapability;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.OrbitalModule;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.OrbitalStructure;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.i18n.Translation;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.turn.Constructable;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.backend.entities.turn.OrbitalModuleJobElement;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthState;
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
import de.yuga.spacebattle.backend.services.constructables.buildings.ConstructionService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.OrbitalStructureService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.WarShipService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.researches.ResearchService;
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
    private final ConstructionService constructionService;

    @Nonnull
    private final OrbitalStructureService orbitalStructureService;

    @Nonnull
    private final WarshipHealthStateService warshipHealthStateService;

    @Autowired
    public JobService(@Nonnull final JobRepository jobRepository,
                      @Nonnull final PlanetService planetService,
                      @Nonnull final BuildingService buildingService,
                      @Nonnull final ResearchService researchService,
                      @Nonnull final WarShipService warShipService,
                      @Nonnull final ResourceService resourceService,
                      @Nonnull final FleetService fleetService,
                      @Nonnull final ConstructionService constructionService,
                      @Nonnull final OrbitalStructureService orbitalStructureService,
                      @Nonnull final WarshipHealthStateService warshipHealthStateService) {
        this.jobRepository = Preconditions.checkNotNull(jobRepository, "jobC shouldn't be null!");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");
        this.buildingService = Preconditions.checkNotNull(buildingService, "buildingService shouldn't be null!");
        this.researchService = Preconditions.checkNotNull(researchService, "researchService shouldn't be null!");
        this.warShipService = Preconditions.checkNotNull(warShipService, "warShipService must not be empty");
        this.resourceService = Preconditions.checkNotNull(resourceService, "resourceService must not be empty");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService must not be empty");
        this.constructionService = Preconditions.checkNotNull(constructionService, "constructionService must not be empty");
        this.orbitalStructureService = orbitalStructureService;
        this.warshipHealthStateService = warshipHealthStateService;
    }

    @Nonnull
    public Job save(@Nonnull final Job entity) {
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

        if (doDelete.getPointsLeft() > 0 && EResourceType.RESEARCH != doDelete.getConstructable().getResourceType()) {
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

    public boolean isLocalInstaJobPossible(final int idPlanet, @Nonnull final Job job) {
        Preconditions.checkNotNull(job, "job must not be empty");

        final int inDeposit = planetService.howMuchInDeposit(idPlanet, job.getConstructable().getResourceType());
        return job.getPointsLeft() <= inDeposit;
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
    public Job createConstructionYardJob(final int idPlanet, final int idBuilding) {
        final Planet planet = planetService.find(idPlanet);
        final Building building = buildingService.find(idBuilding);
        if (planet == null || planet.getHumanOwner() == null || building == null) {
            throw new NotifyWebUserException("not that way!");
        }

        final Construction existingC = constructionService.findByPlanetAndBuilding(idPlanet, idBuilding);

        final int level = researchService.getLevelForResearch(planet.getHumanOwner(), building.getUnlockedThrough());
        if (existingC != null && existingC.getLevel() >= level) {
            throw new NotifyWebUserException("You can't do that - first you have to research the '" + building.getUnlockedThrough().getName(Translation.DEFAULT_LANGUAGE) + "' research.");
        }

        final int targetLevel = existingC != null ? existingC.getLevel() + 1 : 1;
        final Constructable constructable = new Constructable(building, targetLevel);
        final Construction facility = constructionService.findByPlanetAndProductionType(idPlanet, EResourceType.CONSTRUCTION);

        checkIfFree(facility);
        checkAndBalances(planet, constructable.getJobCosts());
        Job job = new Job(planet, facility, constructable, true);
        job = jobRepository.save(job);

        LOGGER.info("Creating construction yard idJob '" + job.getId() + "' " +
                "for idBuilding: '" + building.getId() + "' " +
                "with target level '" + constructable.getTargetLevel() + "' " +
                "from level '" + (targetLevel - 1) + "'");

        planetService.save(planet);
        return job;
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

        final Construction facility = constructionService.findByPlanetAndProductionType(researchPlanet.getId(), EResourceType.RESEARCH);
        checkIfFree(facility);

        final Constructable constructable = new Constructable(research, level);
        final Job job = new Job(researchPlanet, facility, constructable, false);

        final EmpireResearchCapability capability = planetService.getEmpireWideResearchPoints(user.getId());
        final long usedPoints = job.tick(capability.getEmpireWideResearchPointsLeftOver());
        planetService.reduceResearchPoints(user.getId(), usedPoints);

        return jobRepository.save(job);
    }

    private void checkIfFree(@Nullable final Construction facility) {
        if (facility == null) {
            throw new NotifyWebUserException("not here, buddy!");
        }
        if (!facility.getJobs().isEmpty()) {
            throw new NotifyWebUserException("Job in progress");
        }
    }

    @Nonnull
    public Job createShipyardJob(final int idPlanet, @Nonnull final Map<ShipClass, Integer> shipJobPayload) {
        Preconditions.checkNotNull(shipJobPayload, "shipJobPayload shouldn't be null!");

        final Planet planet = planetService.find(idPlanet);
        Preconditions.checkNotNull(planet, "planet must not be empty");
        final User owner = planet.getHumanOwner();
        if (owner == null) {
            throw new NotifyWebUserException("You should own this planet, buddy.");
        }

        final Construction facility = constructionService.findByPlanetAndProductionType(planet.getId(), EResourceType.ORBITAL_CONSTRUCTION);
        checkIfFree(facility);

        Fleet fleet = new Fleet("Fresh Build @ " + planet.getName(), owner, new FleetOrbit(planet));
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
        Job job = new Job(planet, facility, constructable, true);
        job = jobRepository.save(job);
        planetService.save(planet);
        return job;
    }


    public Job createShipyardOrbitalModuleJob(final int idPlanet, @Nonnull final Map<OrbitalModule, Integer> jobLoad) {
        Preconditions.checkNotNull(jobLoad, "jobLoad shouldn't be null!");

        final Planet planet = planetService.find(idPlanet);
        Preconditions.checkNotNull(planet, "planet must not be empty");
        final User owner = planet.getHumanOwner();
        if (owner == null) {
            throw new NotifyWebUserException("You should own this planet, buddy.");
        }

        final Construction facility = constructionService.findByPlanetAndProductionType(planet.getId(), EResourceType.ORBITAL_CONSTRUCTION);
        checkIfFree(facility);

        final Constructable constructable = new Constructable(jobLoad, EJobType.CONSTRUCTION);
        checkAndBalances(planet, constructable.getJobCosts());
        Job job = new Job(planet, facility, constructable, true);
        job = jobRepository.save(job);
        planetService.save(planet);
        return job;
    }

    @Nonnull
    public Job createShipyardUpgradeJob(@Nonnull final Planet planet, @Nonnull final Fleet toUpgrade) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(toUpgrade, "toUpgrade must not be empty");

        final Construction facility = constructionService.findByPlanetAndProductionType(planet.getId(), EResourceType.ORBITAL_CONSTRUCTION);

        // do not check if free, the new job will squeeze in
        final Constructable constructable = new Constructable(toUpgrade, EJobType.UPGRADE);
        checkAndBalances(planet, constructable.getJobCosts());
        Job job = new Job(planet, Objects.requireNonNull(facility), constructable, true);
        job.setPriority(EJobPriority.PRIORITY);
        job = jobRepository.save(job);
        fleetService.disableFleet(toUpgrade);
        fleetService.transferCrewToPlanet(toUpgrade, planet);
        planetService.save(planet);
        return job;
    }

    @Nonnull
    public Job startShipyardRepairJob(@Nonnull final Planet planet, @Nonnull final Fleet toRepair) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(toRepair, "toRepair must not be empty");

        final Construction facility = constructionService.findByPlanetAndProductionType(planet.getId(), EResourceType.ORBITAL_CONSTRUCTION);

        // do not check if free, the new job will squeeze in
        final Constructable constructable = new Constructable(toRepair, EJobType.REPAIR);
        Job job = new Job(planet, Objects.requireNonNull(facility), constructable, true);
        job.setPriority(EJobPriority.PRIORITY);
        job = jobRepository.save(job);
        checkAndBalances(planet, constructable.getJobCosts());
        fleetService.disableFleet(toRepair);
        fleetService.transferCrewToPlanet(toRepair, planet);
        planetService.save(planet);
        return job;
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

    @Nonnull
    public List<Job> forDeletionFindAllJobsForUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user must not be empty");

        return Objects.requireNonNullElse(jobRepository.forDeletionFindAllJobsForUser(user.getId()), new ArrayList<>());
    }

    @Nonnull
    public List<Job> findAllJobsForUser(final int idUser) {
        return jobRepository.findAllJobsForUser(idUser);
    }


    @Nonnull
    public List<Job> findAllResearchJobsForUser(final int idUser) {
        return Objects.requireNonNullElse(jobRepository.findAllResearchJobsForUser(idUser), new ArrayList<>());
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

    @Nullable
    public Job findById(final int idJob) {
        return jobRepository.findById(idJob).orElse(null);
    }

    public void deleteAll(@Nonnull final Collection<Job> jobs) {
        Preconditions.checkNotNull(jobs, "jobs must not be empty");

        jobRepository.deleteAll(jobs);
    }

    public void completeShipyard(@Nonnull final Planet planet, @Nonnull final Job job, @Nonnull final Tick today) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(job, "job must not be empty");
        Preconditions.checkNotNull(today, "today must not be empty");

        final Constructable constructable = job.getConstructable();
        if (constructable.getFleet() == null && constructable.getOrbitalModuleJobElements().isEmpty()) {
            return;
        }

        final User owner = planet.getHumanOwner();
        assert owner != null : "There must be a planet's owner.";
        job.setFinished(today);
        if (constructable.isRepairJob()) {
            realizeFleetRepair(planet, owner, job);
        } else if (constructable.isUpgradeJob()) {
            realizeFleetUpgrade(today, planet, owner, job);
        } else {
            realizeShipyardProduction(planet, owner, job);
        }
    }

    private void log(@Nonnull final Planet planet, @Nonnull final Job job, @Nonnull final String msg) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(job, "job must not be empty");
        Preconditions.checkNotNull(msg, "msg must not be empty");

        LOGGER.info("[Planet #{} - #{}] [Job #{}] {}", planet.getId(), planet.getName(), job.getId(), msg);
    }

    private void realizeFleetUpgrade(@Nonnull final Tick today,
                                     @Nonnull final Planet planet,
                                     @Nonnull final User owner,
                                     @Nonnull final Job job) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(owner, "owner must not be empty");
        Preconditions.checkNotNull(job, "job must not be empty");
        Preconditions.checkNotNull(job.getConstructable().getFleet(), "job.getConstructable().getFleet() must not be empty");

        final Constructable constructable = job.getConstructable();
        final Fleet fleet = constructable.getFleet();

        log(planet, job, "Start upgrade fleet '" + fleet.getId() + "'.");
        final Set<WarShip> withSuccessors = fleet.getAliveShips().stream()
                .filter(w -> w.getShipClass().hasSuccessor())
                .collect(Collectors.toSet());

        final List<WarShip> toStore = new ArrayList<>();
        withSuccessors.forEach(warShip -> {
            // this is introducing a nice exploit by creating a new flight after paying the old one
            final ShipClass shipClass = warShip.getShipClass();
            ShipClass successor = shipClass.getSuccessor();
            while (Objects.requireNonNull(successor).hasSuccessor()) {
                successor = successor.getSuccessor();
            }
            warShip.upgrade(today, planet, successor);
            toStore.add(warShip);
        });

        warShipService.saveAll(toStore);
        fleetService.save(fleet);
        log(planet, job, "Done upgrade fleet '" + fleet.getId() + "'.");
    }

    private void realizeFleetRepair(@Nonnull final Planet planet,
                                    @Nonnull final User owner,
                                    @Nonnull final Job job) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(owner, "owner must not be empty");
        Preconditions.checkNotNull(job, "job must not be empty");
        Preconditions.checkNotNull(job.getFinished(), "job.getFinished() must not be empty");
        Preconditions.checkNotNull(job.getConstructable().getFleet(), "job.getConstructable().getFleet() must not be empty");

        log(planet, job, "Start repair fleet.");
        final Fleet fleet = job.getConstructable().getFleet();
        final Set<WarshipHealthState> toRepair = fleet.getAliveShips().stream()
                .map(WarShip::getWarshipHealthState)
                .collect(Collectors.toSet());
        toRepair.forEach(w -> w.repair(job.getFinished()));
        warshipHealthStateService.saveAll(toRepair);
        fleet.setOperational(job.getFinished());
        fleetService.save(fleet);
        log(planet, job, "Done repairing fleet.");
    }

    private void realizeShipyardProduction(@Nonnull final Planet planet,
                                           @Nonnull final User owner,
                                           @Nonnull final Job job) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(owner, "owner must not be empty");
        Preconditions.checkNotNull(job, "job must not be empty");

        log(planet, job, "Start realizing shipyard production.");

        realizeWarships(planet, job);
        realizeOrbitalModules(planet, job);
    }

    private void realizeOrbitalModules(@Nonnull final Planet planet, @Nonnull final Job job) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(job, "job must not be empty");

        final Set<OrbitalModuleJobElement> orbitalModuleJobElements = job.getConstructable().getOrbitalModuleJobElements();
        if (orbitalModuleJobElements.isEmpty()) {
            return;
        }

        final List<OrbitalStructure> toStore = orbitalModuleJobElements.stream()
                .map(e -> new OrbitalStructure(planet, e.getOrbitalModule(), e.getAmount()))
                .collect(Collectors.toList());
        orbitalStructureService.saveAll(toStore);

        save(job);
        log(planet, job, "Done creating orbital modules.");
    }

    private void realizeWarships(@Nonnull final Planet planet, @Nonnull final Job job) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(job, "job must not be empty");

        // fleet is "destroyed" here
        final Fleet fleet = job.getConstructable().getFleet();
        if (fleet == null) {
            return;
        }
        final Set<WarShip> newShips = fleet.getAllShips();
        newShips.forEach(WarShip::animate);
        newShips.forEach(w -> w.setMothball(planet));
        warShipService.saveAll(newShips);

        job.getConstructable().snapshotFleet();
        save(job);

        log(planet, job, "Done creating warships.");
    }

    public void completeConstruction(@Nonnull final Planet planet,
                                     @Nonnull final Set<Construction> constructions,
                                     @Nonnull final Job job,
                                     @Nonnull final Tick today) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(constructions, "constructions must not be empty");
        Preconditions.checkNotNull(job, "job must not be empty");
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(job.getConstructable().getBuilding(), "job.getConstructable().getBuilding() must not be empty");
        Preconditions.checkNotNull(job.getConstructable().getTargetLevel(), "job.getConstructable().getTargetLevel() must not be empty");

        job.setFinished(today);
        final Constructable constructable = job.getConstructable();
        final Integer targetLevel = constructable.getTargetLevel();
        final Building building = constructable.getBuilding();

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
        save(job);
    }

    /**
     * Counts down the remaining {@link Job#getPointsLeft()}.
     *
     * @param job the {@link Job} to do
     * @return the used points will be returned
     */
    public long tickJob(@Nonnull final Job job, final long points) {
        Preconditions.checkNotNull(job, "job shouldn't be null!");

        return job.tick(points);
    }

    public void tickInstaShipyard(@Nonnull Job job, @Nonnull final Tick today) {
        Preconditions.checkNotNull(job, "job must not be empty");
        Preconditions.checkNotNull(today, "today must not be empty");

        job = findById(job.getId());
        Preconditions.checkNotNull(job, "job must not be empty");

        final Planet planet = job.getFacility().getPlanet();
        if (isLocalInstaJobPossible(planet.getId(), job)) {
            final long points = planet.getResourceDeposit().getResourceAmountByType(EResourceType.ORBITAL_CONSTRUCTION);
            final long usedPoints = tickJob(job, points);
            planet.getResourceDeposit().updateResource(EResourceType.ORBITAL_CONSTRUCTION, -usedPoints);
            completeShipyard(planet, job, today);
            planetService.save(planet);
            save(job);
        }
    }

    public void tickInstaConstruction(@Nonnull Job job, @Nonnull final Tick today) {
        Preconditions.checkNotNull(job, "job must not be empty");
        Preconditions.checkNotNull(today, "today must not be empty");

        job = findById(job.getId());
        Preconditions.checkNotNull(job, "job must not be empty");

        final Planet planet = job.getFacility().getPlanet();
        final Set<Construction> constructions = constructionService.findAllConstructionsOnPlanet(planet.getId());
        if (isLocalInstaJobPossible(planet.getId(), job)) {
            final long points = planet.getResourceDeposit().getResourceAmountByType(EResourceType.CONSTRUCTION);
            final long usedPoints = tickJob(job, points);
            planet.getResourceDeposit().updateResource(EResourceType.CONSTRUCTION, -usedPoints);
            completeConstruction(planet, constructions, job, today);
            planetService.save(planet);
        }
    }
}
