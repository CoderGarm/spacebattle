package de.yuga.spacebattle.logic.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.entities.Constructable;
import de.yuga.spacebattle.entities.ResourceDeposit;
import de.yuga.spacebattle.entities.account.User;
import de.yuga.spacebattle.entities.buildings.Building;
import de.yuga.spacebattle.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.entities.orbitals.Planet;
import de.yuga.spacebattle.entities.researches.Research;
import de.yuga.spacebattle.entities.turn.Job;
import de.yuga.spacebattle.enums.EResourceType;
import de.yuga.spacebattle.logic.account.UserService;
import de.yuga.spacebattle.logic.buildings.BuildingService;
import de.yuga.spacebattle.logic.constructables.spacecraft.ShipClassService;
import de.yuga.spacebattle.logic.orbitals.PlanetService;
import de.yuga.spacebattle.logic.researches.ResearchService;
import de.yuga.spacebattle.repositories.turn.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

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

    public void delete(@Nullable final Job entity) {
        if (entity == null || entity.getId() < 1) {
            return;
        }
        Job doDelete = jobRepository.findById(entity.getId()).orElse(null);
        if (doDelete == null) {
            throw new NotifySBUserException("no job to delete");
        }
        Map<EResourceType, BigDecimal> entityCosts = entity.getConstructable().getJobCosts();
        Planet planet = entity.getFacility().getPlanet();
        ResourceDeposit resourceDeposit = planet.getResourceDeposit();
        for (EResourceType resourceType : entityCosts.keySet()) {
            resourceDeposit.updateResource(resourceType, entityCosts.get(resourceType));
        }
        planetService.save(planet);
        jobRepository.delete(doDelete);
    }

    /**
     * Checks if the debit is in the credit and calculates if in the good case.
     *
     * @param planet the planet which should pay the bill
     * @param costs  the costs
     * @return <code>true</code>, if the bill is payed, <code>false</code> if not
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
     *
     * @param idPlanet   the planet where the entity should be executed
     * @param idResearch the research which should be researches
     * @return the created entity
     */
    public Job createResearchJob(@Nonnull final Integer idPlanet, @Nonnull final Integer idResearch) {
        Preconditions.checkNotNull(idPlanet, "idPlanet shouldn't be null!");
        Preconditions.checkNotNull(idResearch, "idResearch shouldn't be null!");

        Planet planet = planetService.find(idPlanet);
        Research research = researchService.find(idResearch);
        if (planet == null || planet.getOwner() == null || research == null) {
            throw new NotifySBUserException("not that way!");
        }
        int levelCap = research.getLevelCap();

        User owner = planet.getOwner();
        Map<Research, Integer> researches = owner.getResearches();
        Integer level = 0;
        if (researches.containsKey(research)) {
            level = researches.get(research);
        }

        if (level + 1 > levelCap) {
            throw new NotifySBUserException("no way!");
        }

        Constructable constructable = new Constructable(research, level + 1);
        Construction facility = planet.getConstructions().stream()
                .filter(construction -> construction.getBuilding().getResourceType() == EResourceType.RESEARCH)
                .findFirst().orElse(null);

        checkIfFree(facility);
        checkAndBalances(planet, constructable.getJobCosts());
        Job entity = new Job(owner, facility, constructable);
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

    private void checkIfFree(Construction facility) {
        if (facility == null) {
            throw new NotifySBUserException("not here, buddy!");
        }
        if (facility.getJob() != null) {
            throw new NotifySBUserException("Job in progress");
        }
    }
}
