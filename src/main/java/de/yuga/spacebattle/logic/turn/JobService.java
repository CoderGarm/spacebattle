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
import de.yuga.spacebattle.repositories.account.UserRepository;
import de.yuga.spacebattle.repositories.buildings.BuildingRepository;
import de.yuga.spacebattle.repositories.constructables.spacecraft.ShipClassRepository;
import de.yuga.spacebattle.repositories.orbitals.PlanetRepository;
import de.yuga.spacebattle.repositories.researches.ResearchRepository;
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
    private final JobRepository jobC;

    @Nonnull
    private final PlanetRepository planetC;

    @Nonnull
    private final BuildingRepository buildingC;

    @Nonnull
    private final ResearchRepository researchC;

    @Nonnull
    private final ShipClassRepository shipClassC;

    @Nonnull
    private final UserRepository userC;

    @Autowired
    public JobService(@Nonnull final JobRepository jobC,
                      @Nonnull final PlanetRepository planetC,
                      @Nonnull final BuildingRepository buildingC,
                      @Nonnull final ResearchRepository researchC,
                      @Nonnull final ShipClassRepository shipClassC,
                      @Nonnull final UserRepository userC) {
        Preconditions.checkNotNull(jobC, "jobC shouldn't be null!");
        Preconditions.checkNotNull(planetC, "planetC shouldn't be null!");
        Preconditions.checkNotNull(buildingC, "buildingC shouldn't be null!");
        Preconditions.checkNotNull(researchC, "researchC shouldn't be null!");
        Preconditions.checkNotNull(shipClassC, "shipClassC shouldn't be null!");
        Preconditions.checkNotNull(userC, "userC shouldn't be null!");

        this.jobC = jobC;
        this.planetC = planetC;
        this.buildingC = buildingC;
        this.researchC = researchC;
        this.shipClassC = shipClassC;
        this.userC = userC;
    }

    public void delete(@Nullable final Job entity) {
        if (entity == null || entity.getId() < 1) {
            return;
        }
        Job doDelete = jobC.findById(entity.getId()).orElse(null);
        if (doDelete == null) {
            throw new NotifySBUserException("no job to delete");
        }
        Map<EResourceType, BigDecimal> entityCosts = entity.getConstructable().getJobCosts();
        Planet planet = entity.getFacility().getPlanet();
        ResourceDeposit resourceDeposit = planet.getResourceDeposit();
        for (EResourceType resourceType : entityCosts.keySet()) {
            resourceDeposit.updateResource(resourceType, entityCosts.get(resourceType));
        }
        planetC.save(planet);
        jobC.delete(doDelete);
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

        Planet planet = planetC.findById(idPlanet).get();
        Building building = buildingC.findById(idBuilding).get();
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
        Job entity = new Job(planet.getOwner(), facility, constructable);
        jobC.save(entity);

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

        Planet planet = planetC.findById(idPlanet).orElse(null);
        Research research = researchC.findById(idResearch).orElse(null);
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
        Job entity = new Job(owner, facility, constructable);
        jobC.save(entity);
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

        Planet planet = planetC.findById(idPlanet).orElse(null);
        ShipClass shipClass = shipClassC.findById(idShipClass).orElse(null);
        if (planet == null || planet.getOwner() == null || shipClass == null) {
            throw new NotifySBUserException("not that way!");
        }

        Constructable constructable = new Constructable(shipClass, amount);
        Construction facility = planet.getConstructions().stream()
                .filter(construction -> construction.getBuilding().getResourceType() == EResourceType.ORBITALCONSTRUCTION)
                .findFirst().orElse(null);

        checkIfFree(facility);
        Job entity = new Job(planet.getOwner(), facility, constructable);
        jobC.save(entity);
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
