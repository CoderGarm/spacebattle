package de.yuga.spacebattle.backend.services.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.NotifyUserException;
import de.yuga.spacebattle.backend.calculator.colonization.ColonizationCostCalculator;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.turn.Colonization;
import de.yuga.spacebattle.backend.entities.turn.resources.PayingPossibleResult;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.repositories.turn.ColonizationRepository;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.buildings.BuildingService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.turn.resources.ResourceAmount;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ColonizationService {

    @Nonnull
    private final ColonizationRepository repository;

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final BuildingService buildingService;

    /**
     * Holds the colonization which is designated to be displayed to the user.
     */
    @Nullable
    private Colonization colonizationToDisplay;

    public ColonizationService(@Nonnull final ColonizationRepository repository,
                               @Nonnull final UserService userService,
                               @Nonnull final PlanetService planetService,
                               @Nonnull final BuildingService buildingService) {
        Preconditions.checkNotNull(repository, "colonizationRepository shouldn't be null!");
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");
        Preconditions.checkNotNull(buildingService, "buildingService shouldn't be null!");

        this.repository = repository;
        this.userService = userService;
        this.planetService = planetService;
        this.buildingService = buildingService;
    }

    @Nullable
    public Colonization getColonizationToDisplay() {
        return colonizationToDisplay;
    }

    public void setColonizationToDisplay(@Nullable final Colonization colonizationToDisplay) {
        this.colonizationToDisplay = colonizationToDisplay;
    }

    @Nonnull
    public List<Colonization> findAll() {
        return repository.findAll();
    }

    @Nonnull
    public List<Colonization> findAllForUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return repository.findAllForUser(user.getId());
    }

    @Nonnull
    public List<Colonization> findAllForUser(final int idUser) {
        return repository.findAllForUser(idUser);
    }

    @Nullable
    public Colonization find(@Nonnull final Integer idColonization) {
        Preconditions.checkNotNull(idColonization, "idColonization shouldn't be null!");

        return repository.findById(idColonization).orElse(null);
    }

    public Colonization save(@Nonnull final Colonization entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        return repository.save(entity);
    }

    public void delete(@Nonnull final Colonization entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        repository.delete(entity);
    }

    /**
     * Starts a colonizing mission with the fixed duration of 10 ticks.
     * todo ticks based on distance between whatever planets
     *
     * @param user       the user who starts the colonization
     * @param toColonize the planet to colonize
     * @return the created colonization
     */
    @Transactional(rollbackFor = Exception.class)
    public Colonization startColonizingPlanet(@Nonnull final User user, @Nonnull final Planet toColonize) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");
        Preconditions.checkNotNull(toColonize, "toColonize shouldn't be null!");

        final ResourceAmount costs = ColonizationCostCalculator.calculateColonizationCost(toColonize);
        final Planet mainPlanet = planetService.findMainPlanet(user);
        final ResourceDeposit debitorDeposit = mainPlanet.getResourceDeposit();

        final ResourceDeposit c = new ResourceDeposit(EDepositType.COSTS);
        c.setAbsoluteResourceValue(costs.getRealResourceType(), costs.getAmount());
        final PayingPossibleResult payingPossible = debitorDeposit.isPayingPossible(c);
        if (!payingPossible.isValid()) {
            throw new NotifyWebUserException("This colonization is to expensive.", payingPossible);
        }
        debitorDeposit.updateResource(costs.getRealResourceType(), costs.getAmount());
        final Map<EEducationType, Long> requiredCrew = new HashMap<>();
        requiredCrew.put(EEducationType.NONE, 200L);
        requiredCrew.put(EEducationType.MILITARY_MK_I, 50L);
        requiredCrew.put(EEducationType.MILITARY_MK_II, 20L);
        requiredCrew.put(EEducationType.CIVIL_MK_I, 100L);
        requiredCrew.put(EEducationType.CIVIL_MK_II, 200L);
        requiredCrew.put(EEducationType.CIVIL_MK_III, 500L);
        final CrewRequirement crewRequirement = new CrewRequirement(requiredCrew, EDepositType.COSTS);

        if (debitorDeposit.isReducingPopulationPossible(crewRequirement)) {
            debitorDeposit.updatePopulation(crewRequirement);
        } else {
            throw new NotifyUserException("Unfortunately you have not enough population on your home planet.");
        }

        final Colonization colonization = new Colonization(user, toColonize, crewRequirement, 10);
        save(colonization);
        planetService.save(mainPlanet);
        userService.save(user);
        return colonization;
    }

    /**
     * Colonizes a planet for a owner.
     * Currently this implies that the new owner will get all information about the system without buying it especially.
     *
     * @param colonization the running colonization
     * @return the colonized planet
     */
    @Transactional(rollbackFor = Exception.class)
    public Planet colonizePlanet(@Nonnull final Colonization colonization) {
        Preconditions.checkNotNull(colonization, "colonization shouldn't be null!");

        final User owner = colonization.getUser();
        final Planet planet = colonization.getTarget();
        planet.setOwner(owner);
        final ResourceDeposit creditorDeposit = planet.getResourceDeposit();
        final CrewRequirement requiredCrew = colonization.getCosts().getCrewRequirement();
        creditorDeposit.updatePopulation(requiredCrew);
        final Building constructionYard = buildingService.findBuildingByProductionType(EResourceType.CONSTRUCTION);
        final Construction constructedConstructionYard = new Construction(planet, constructionYard, 1);
        // todo add other mandatory buildings: living room and hospital
        planet.getConstructions().add(constructedConstructionYard);
        owner.addKnownStarSystems(planet.getSystem());
        userService.save(owner);
        return planetService.save(planet);
    }

    /**
     * Adds a star system to the user's known systems.
     *
     * @param user       the user wh should know the new system
     * @param starSystem the star system
     */
    @Transactional(rollbackFor = Exception.class)
    public void addToKnownSystems(@Nonnull final User user, @Nonnull final StarSystem starSystem) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");
        Preconditions.checkNotNull(starSystem, "starSystem shouldn't be null!");

        final ResourceAmount costs = ColonizationCostCalculator.calculateInformationCost(starSystem);
        final Planet mainPlanet = planetService.findMainPlanet(user);
        final ResourceDeposit resourceDeposit = mainPlanet.getResourceDeposit();

        PayingPossibleResult payingPossibleResult = validateCostsForSystemInformation(mainPlanet, starSystem);
        if (!payingPossibleResult.isValid()) {
            throw new NotifyWebUserException("Buying this systems information is to expensive for you.", payingPossibleResult);
        }

        resourceDeposit.updateResource(costs.getRealResourceType(), costs.getAmount());
        user.addKnownStarSystems(starSystem);
        userService.save(user);
    }

    /**
     * Checks if the main planet of the user can pay the rent.
     *
     * @param mainPlanet the main planet of the user
     * @param starSystem the star system to buy information for
     * @return <code>true</code> if paying is possible, <code>false</code> otherwise
     */
    public PayingPossibleResult validateCostsForSystemInformation(@Nonnull final Planet mainPlanet, @Nonnull final StarSystem starSystem) {
        Preconditions.checkNotNull(mainPlanet, "mainPlanet shouldn't be null!");
        Preconditions.checkNotNull(starSystem, "starSystem shouldn't be null!");

        final ResourceAmount costs = ColonizationCostCalculator.calculateInformationCost(starSystem);
        final ResourceDeposit resourceDeposit = mainPlanet.getResourceDeposit();
        ResourceDeposit c = new ResourceDeposit(EDepositType.COSTS);
        c.setAbsoluteResourceValue(costs.getRealResourceType(), costs.getAmount());
        return resourceDeposit.isPayingPossible(c);
    }
}
