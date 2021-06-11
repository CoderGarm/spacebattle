package de.yuga.spacebattle.backend.services.turn;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.Constructable;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.crew.CrewRequirementDTO;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.ECollectableType;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.repositories.turn.JobRepository;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.buildings.BuildingService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.ShipClassService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.researches.ResearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JobService {

    @Nonnull
    private final JobRepository jobRepository;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final BuildingService buildingService;

    @Nonnull
    private final ResearchService researchService;

    @Nonnull
    private final ShipClassService shipClassService;

    @Nonnull
    private final UserService userService;

    @Autowired
    public JobService(@Nonnull final JobRepository jobRepository,
                      @Nonnull final PlanetService planetService,
                      @Nonnull final BuildingService buildingService,
                      @Nonnull final ResearchService researchService,
                      @Nonnull final ShipClassService shipClassService,
                      @Nonnull final UserService userService) {
        Preconditions.checkNotNull(jobRepository, "jobC shouldn't be null!");
        Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");
        Preconditions.checkNotNull(buildingService, "buildingService shouldn't be null!");
        Preconditions.checkNotNull(researchService, "researchService shouldn't be null!");
        Preconditions.checkNotNull(shipClassService, "shipClassService shouldn't be null!");
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");

        this.jobRepository = jobRepository;
        this.planetService = planetService;
        this.buildingService = buildingService;
        this.researchService = researchService;
        this.shipClassService = shipClassService;
        this.userService = userService;
    }

    @Nonnull
    public final Job save(@Nonnull final Job entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        return jobRepository.save(entity);
    }

    @Nonnull
    public List<Job> findAllJobsForConstruction(@Nonnull Construction facility) {
        Preconditions.checkNotNull(facility, "facility shouldn't be null!");

        return jobRepository.findAllJobsForConstruction(facility);
    }

    /**
     * Returns the job cost if they were payed - no costs and no costs back for researches!
     *
     * @param entity the job to delete
     */
    //@Transactional todo test and remove or comment in
    public void delete(@Nullable final Job entity) {
        if (entity == null || entity.getId() < 1) {
            return;
        }
        final Job doDelete = jobRepository.findById(entity.getId()).orElse(null);
        if (doDelete == null) {
            throw new NotifySBUserException("no job to delete"); // fail first for development
        }

        if (doDelete.getJobDoneAtZero() > 0 && EResourceType.RESEARCH != doDelete.getConstructable().getResourceType()) {
            // if reached job is not done -> payback the paycheck
            // not reached if the job is a research
            final ResourceDeposit jobCosts = entity.getConstructable().getJobCosts();
            final Planet planet = entity.getFacility().getPlanet();

            final ResourceDeposit resourceDeposit = planet.getResourceDeposit();
            for (final EResourceType resourceType : EResourceType.values()) {
                // must be added again because payback and not thanks for the tip
                if (EResourceType.POPULATION == resourceType) {
                    final CrewRequirementDTO crewRequirementDTO = jobCosts.getCrewRequirement().toggleToDepositMode();
                    resourceDeposit.updatePopulation(crewRequirementDTO);
                } else {
                    resourceDeposit.updateResource(resourceType, jobCosts.getResourceAmountByType(resourceType));
                }
            }
            planetService.save(planet);
        }
        jobRepository.delete(doDelete);
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

        final ResourceDeposit debitorDeposit = planet.getResourceDeposit();
        if (!debitorDeposit.isPayingPossible(costs)) {
            throw new NotifySBUserException("This job is to expensive!");
        }

        for (final EResourceType resourceType : EResourceType.values()) {
            if (EResourceType.POPULATION == resourceType) {
                debitorDeposit.getCrewRequirement().updateCrew(costs.getCrewRequirement());
            } else if (ECollectableType.COLLECTABLE == resourceType.getCollectableType()) {
                final long debit = costs.getResourceAmountByType(resourceType) * -1;
                debitorDeposit.updateResource(resourceType, debit);
            }
        }
    }

    /**
     * Checks if the user can build that.
     *
     * @param user     the use who want to build the building
     * @param research the building to build
     */
    private void canUseResearch(@Nonnull final User user, @Nullable final Research research) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        if (research != null) {
            Set<Research> researches = user.getResearches().keySet();
            if (!researches.contains(research)) {
                throw new NotifySBUserException("You can't do that");
            }
        }
    }


    /**
     * Creates a entity by {@link Building#getId()} and {@link Planet#getId()}.
     * The building's level will be incremented by 1 in every {@link Job}.
     *
     * @param idPlanet   the planet where the entity should be executed
     * @param idBuilding the building which should be build
     * @return the created entity
     */
    public Job createConstructionYardJob(@Nonnull final Integer idPlanet, @Nonnull final Integer idBuilding) {
        Preconditions.checkNotNull(idPlanet, "idPlanet shouldn't be null!");
        Preconditions.checkNotNull(idBuilding, "idBuilding shouldn't be null!");

        final Planet planet = planetService.find(idPlanet);
        final Building building = buildingService.find(idBuilding);
        if (planet == null || planet.getOwner() == null || building == null) {
            throw new NotifySBUserException("not that way!");
        }

        final User userWithResearches = userService.findWithResearches(planet.getOwner());
        canUseResearch(userWithResearches, building.getUnlockedThrough());

        final Set<Construction> constructions = planet.getConstructions();
        final Construction existingC = constructions.stream()
                .filter(construction -> construction.getBuilding().equals(building))
                .findFirst().orElse(null);

        final Constructable constructable = new Constructable(building, existingC != null ? existingC.getLevel() + 1 : 1);
        final Construction facility = planet.getConstructions().stream()
                .filter(construction -> construction.getBuilding().getProductionTarget() == EResourceType.CONSTRUCTION)
                .findFirst().orElse(null);

        checkIfFree(facility);
        checkAndBalances(planet, constructable.getJobCosts());
        final Job entity = new Job(planet, facility, constructable);
        save(entity);
        return entity;
    }


    /**
     * Creates a entity by {@link Research#getId()} and {@link Planet#getId()}.
     * The research's level will be incremented by 1 in every {@link Job}.
     * <p>
     * The research is mapped to the planet with the lowest ID and a research facility.
     *
     * @param idUser     the planet where the entity should be executed
     * @param idResearch the research which should be researches
     * @return the created entity
     */
    public Job createResearchJob(@Nonnull final Integer idUser, @Nonnull final Integer idResearch) {
        Preconditions.checkNotNull(idUser, "idUser shouldn't be null!");
        Preconditions.checkNotNull(idResearch, "idResearch shouldn't be null!");

        final User user = userService.find(idUser);
        final Research research = researchService.find(idResearch);
        if (user == null || research == null) {
            throw new NotifySBUserException("not that way!");
        }
        canUseResearch(user, research.getUnlockedThrough());

        int levelCap = research.getLevelCap();

        final Map<Research, Integer> researches = user.getResearches();
        int level = 1;
        if (researches.containsKey(research)) {
            level = researches.get(research) + 1;
        }
        if (level > levelCap) {
            throw new NotifySBUserException("no way!");
        }
        final Constructable constructable = new Constructable(research, level);
        final Planet researchPlanet = planetService.findResearchPlanet(user);
        if (researchPlanet == null) {
            throw new NotifySBUserException("You need a research facility on at leas one planet.");
        }

        final Construction facility = researchPlanet.getConstructionByResource(EResourceType.RESEARCH)
                .stream().findFirst().orElse(null);
        checkIfFree(facility);

        final Job entity = new Job(researchPlanet, facility, constructable);
        jobRepository.save(entity);
        return entity;
    }

    /**
     * Creates a entity by {@link ShipClass#getId()} and {@link Planet#getId()}.
     * The buildings level will be incremented by 1 in every {@link Job}.
     *
     * @param idPlanet    the planet where the entity should be executed
     * @param idShipClass the ShipClass which should be build
     * @param amount      the amount of ship which should be build
     * @return the created entity
     */
    public Job createShipyardJob(@Nonnull final Integer idPlanet,
                                 @Nonnull final Integer idShipClass,
                                 @Nonnull final Integer amount) {
        Preconditions.checkNotNull(idPlanet, "idPlanet shouldn't be null!");
        Preconditions.checkNotNull(idShipClass, "idShipClass shouldn't be null!");
        Preconditions.checkNotNull(amount, "amount shouldn't be null!");
        Preconditions.checkArgument(amount > 0, "amount shouldn't be lower than one!");

        Planet planet = planetService.find(idPlanet);
        ShipClass shipClass = shipClassService.find(idShipClass);
        if (planet == null || planet.getOwner() == null || shipClass == null) {
            throw new NotifySBUserException("not that way!");
        }

        Constructable constructable = new Constructable(shipClass, amount);
        Construction facility = planet.getConstructions().stream()
                .filter(construction -> construction.getBuilding().getProductionTarget() == EResourceType.ORBITAL_CONSTRUCTION)
                .findFirst().orElse(null);

        checkIfFree(facility);
        checkAndBalances(planet, constructable.getJobCosts());
        Job entity = new Job(planet, facility, constructable);
        jobRepository.save(entity);
        return entity;
    }

    /**
     * Checks if the pointed facility is in use.
     *
     * @param facility the facility which could be in use
     */
    private void checkIfFree(@Nullable final Construction facility) {
        if (facility == null) {
            throw new NotifySBUserException("not here, buddy!");
        }
        if (!facility.getJobs().isEmpty()) {
            throw new NotifySBUserException("Job in progress");
        }
    }

    public Set<Job> createShipyardJob(@Nonnull final Planet planet, @Nonnull final Map<ShipClass, Integer> shipJobPayload) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");
        Preconditions.checkNotNull(shipJobPayload, "shipJobPayload shouldn't be null!");

        final User owner = planet.getOwner();
        if (owner == null) {
            throw new NotifySBUserException("You should own this planet, buddy.");
        }

        final Construction facility = planet.getConstructions().stream()
                .filter(construction -> construction.getBuilding().getProductionTarget() == EResourceType.ORBITAL_CONSTRUCTION)
                .findFirst().orElse(null);

        checkIfFree(facility);

        final Set<Constructable> constructableSet = shipJobPayload.entrySet().stream()
                .map(e -> new Constructable(e.getKey(), e.getValue())).collect(Collectors.toSet());

        constructableSet.forEach(constructable -> checkAndBalances(planet, constructable.getJobCosts()));
        Set<Job> newJobs = constructableSet.stream().map(constructable -> new Job(planet, facility, constructable)).collect(Collectors.toSet());

        Iterable<Job> jobIterable = jobRepository.saveAll(newJobs);
        return Sets.newHashSet(jobIterable);
    }
}
