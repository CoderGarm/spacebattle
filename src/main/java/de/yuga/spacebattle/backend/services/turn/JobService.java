package de.yuga.spacebattle.backend.services.turn;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.Constructable;
import de.yuga.spacebattle.backend.entities.ResourceDeposit;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.turn.Job;
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
import java.math.BigDecimal;
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

    /**
     * Returns the job cost if they were payed - no costs for researches!
     *
     * @param entity the job to delete
     */
    //@Transactional
    public void delete(@Nullable final Job entity) {
        if (entity == null || entity.getId() < 1) {
            return;
        }
        Job doDelete = jobRepository.findById(entity.getId()).orElse(null);
        if (doDelete == null) {
            throw new NotifySBUserException("no job to delete");
        }

        if (doDelete.getJobDoneAtZero().compareTo(BigDecimal.ZERO) > 0 && entity.getFacility() != null) {
            // if reached job is not done -> payback the paycheck
            // not reached if the job is a research
            Map<EResourceType, BigDecimal> entityCosts = entity.getConstructable().getJobCosts();
            Planet planet = entity.getFacility().getPlanet();

            ResourceDeposit resourceDeposit = planet.getResourceDeposit();
            for (EResourceType resourceType : entityCosts.keySet()) {
                resourceDeposit.updateResource(resourceType, entityCosts.get(resourceType));
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
                                  @Nonnull final Map<EResourceType, BigDecimal> costs) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");
        Preconditions.checkNotNull(costs, "costs shouldn't be null!");

        ResourceDeposit resourceDeposit = planet.getResourceDeposit();
        boolean isFine = true;
        for (EResourceType resourceType : costs.keySet()) {
            BigDecimal credit = resourceDeposit.getResourceAmountByType(resourceType);
            BigDecimal debit = costs.get(resourceType);
            BigDecimal subtract = credit.subtract(debit, ResourceDeposit.mathContext);
            if (subtract.compareTo(BigDecimal.ZERO) < 0) {
                isFine = false;
            }
        }
        if (isFine) {
            for (EResourceType resourceType : costs.keySet()) {
                BigDecimal debit = costs.get(resourceType);
                resourceDeposit.updateResource(resourceType, debit.negate());
            }
        } else {
            throw new NotifySBUserException("This job is to expensive");
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

        Planet planet = planetService.find(idPlanet);
        Building building = buildingService.find(idBuilding);
        if (planet == null || planet.getOwner() == null || building == null) {
            throw new NotifySBUserException("not that way!");
        }

        canUseResearch(planet.getOwner(), building.getUnlockedThrough());

        Set<Construction> constructions = planet.getConstructions();
        Construction existingC = constructions.stream()
                .filter(construction -> construction.getBuilding().equals(building))
                .findFirst().orElse(null);

        Constructable constructable = new Constructable(building, existingC != null ? existingC.getLevel() + 1 : 1);
        Construction facility = planet.getConstructions().stream()
                .filter(construction -> construction.getBuilding().getResourceType() == EResourceType.CONSTRUCTION)
                .findFirst().orElse(null);

        checkIfFree(facility);
        checkAndBalances(planet, constructable.getJobCosts());
        Job entity = new Job(planet.getOwner(), facility, constructable);
        jobRepository.save(entity);
        return entity;
    }


    /**
     * Creates a entity by {@link Research#getId()} and {@link Planet#getId()}.
     * The research's level will be incremented by 1 in every {@link Job}.
     * <p>
     * The research is mapped to the home planet.
     *
     * @param idUser     the planet where the entity should be executed
     * @param idResearch the research which should be researches
     * @return the created entity
     */
    public Job createResearchJob(@Nonnull final Integer idUser, @Nonnull final Integer idResearch) {
        Preconditions.checkNotNull(idUser, "idUser shouldn't be null!");
        Preconditions.checkNotNull(idResearch, "idResearch shouldn't be null!");

        User user = userService.find(idUser);
        Research research = researchService.find(idResearch);
        if (user == null || research == null) {
            throw new NotifySBUserException("not that way!");
        }
        canUseResearch(user, research.getUnlockedThrough());

        int levelCap = research.getLevelCap();

        Map<Research, Integer> researches = user.getResearches();
        int level = 1;
        if (researches.containsKey(research)) {
            level = researches.get(research) + 1;
        }
        if (level > levelCap) {
            throw new NotifySBUserException("no way!");
        }
        Constructable constructable = new Constructable(research, level + 1);
        Planet planet = user.getOwnedPlanets().stream().filter(inlinePlanet -> {
            Construction orElse = inlinePlanet.getConstructions().stream()
                    .filter(construction -> construction.getBuilding().getResourceType() == EResourceType.RESEARCH)
                    .findFirst().orElse(null);
            return orElse != null;
        }).findFirst().orElse(null);

        if (planet == null) {
            throw new NotifySBUserException("You need a research facility on at leas one planet.");
        }

        if (jobRepository.researchPossible(planet.getOwner())) {
            throw new NotifySBUserException("Job in progress");
        }

        Job entity = new Job(user, null, constructable);
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
                .filter(construction -> construction.getBuilding().getResourceType() == EResourceType.ORBITALCONSTRUCTION)
                .findFirst().orElse(null);

        checkIfFree(facility);
        checkAndBalances(planet, constructable.getJobCosts());
        Job entity = new Job(planet.getOwner(), facility, constructable);
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
                .filter(construction -> construction.getBuilding().getResourceType() == EResourceType.ORBITALCONSTRUCTION)
                .findFirst().orElse(null);

        checkIfFree(facility);

        final Set<Constructable> constructableSet = shipJobPayload.entrySet().stream()
                .map(e -> new Constructable(e.getKey(), e.getValue())).collect(Collectors.toSet());

        constructableSet.forEach(constructable -> checkAndBalances(planet, constructable.getJobCosts()));
        Set<Job> newJobs = constructableSet.stream().map(constructable -> new Job(owner, facility, constructable)).collect(Collectors.toSet());

        Iterable<Job> jobIterable = jobRepository.saveAll(newJobs);
        return Sets.newHashSet(jobIterable);
    }
}
