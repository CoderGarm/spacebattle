package de.yuga.spacebattle.backend.services.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.colonization.ColonizationCostCalculator;
import de.yuga.spacebattle.backend.calculator.resource.PopulationControlCalculator;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.buildings.ProductionType;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.turn.Colonization;
import de.yuga.spacebattle.backend.entities.turn.resources.PayingPossibleResult;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.enums.EProductionCategory;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.repositories.turn.ColonizationRepository;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.buildings.BuildingService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.orbitals.StarSystemService;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.turn.resources.ResourceAmount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit.MATH_CONTEXT_MORE_PRECISION;

@Service
public class ColonizationService {

    @Nonnull
    private final static Logger LOGGER = LoggerFactory.getLogger(ColonizationService.class);

    @Nonnull
    private final ColonizationRepository repository;

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final StarSystemService starSystemService;

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
                               @Nonnull final StarSystemService starSystemService,
                               @Nonnull final BuildingService buildingService) {
        Preconditions.checkNotNull(repository, "colonizationRepository shouldn't be null!");
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");
        Preconditions.checkNotNull(starSystemService, "starSystemService shouldn't be null!");
        Preconditions.checkNotNull(buildingService, "buildingService shouldn't be null!");

        this.repository = repository;
        this.userService = userService;
        this.planetService = planetService;
        this.starSystemService = starSystemService;
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
        final CrewRequirement crewRequirement = getCrewRequirementForColonization();

        if (debitorDeposit.isReducingPopulationPossible(crewRequirement)) {
            debitorDeposit.updatePopulation(crewRequirement);
        } else {
            throw new NotifyWebUserException("Unfortunately you have not enough population on your home planet.");
        }

        final Colonization colonization = new Colonization(user, toColonize, crewRequirement, 10);
        save(colonization);
        planetService.save(mainPlanet);
        userService.save(user);
        return colonization;
    }

    @Nonnull
    private static CrewRequirement getCrewRequirementForColonization() {
        final Map<EEducationType, Long> requiredCrew = new HashMap<>();
        requiredCrew.put(EEducationType.NONE, 200L);
        requiredCrew.put(EEducationType.ENLISTED, 50L);
        requiredCrew.put(EEducationType.OFFICER, 20L);
        requiredCrew.put(EEducationType.SCHOOL, 100L);
        requiredCrew.put(EEducationType.COLLEGE, 200L);
        requiredCrew.put(EEducationType.UNIVERSITY, 500L);
        return new CrewRequirement(requiredCrew, EDepositType.COSTS);
    }

    /**
     * Colonizes a planet for an owner.
     * Currently, this implies that the new owner will get all information about the system without buying it especially.
     *
     * @param colonization the running colonization
     * @return the colonized planet
     */
    @Transactional(rollbackFor = Exception.class)
    public Planet colonizePlanet(@Nonnull final Colonization colonization) {
        Preconditions.checkNotNull(colonization, "colonization shouldn't be null!");
        Preconditions.checkState(colonization.getDoneAtZero() == 0, "colonization cannot be done if the ship isn't in the orbit!");

        final User owner = colonization.getUser();
        final Planet planet = colonization.getTarget();
        planet.setOwner(owner);
        final ResourceDeposit creditorDeposit = planet.getResourceDeposit();
        final CrewRequirement requiredCrew = colonization.getCosts().getCrewRequirement();
        // set crew from the ship to the planet
        creditorDeposit.updatePopulation(requiredCrew);

        final long miningFactor = planet.getMiningFactors().getMiningFactorByType(EResourceType.POPULATION);

        final List<Building> basicBuildings = buildingService.findBasicBuildings();
        basicBuildings.forEach(building -> {
            final int level;
            final ProductionType productionType = building.getProductionType();
            final boolean idPopulationCapacity = EResourceType.POPULATION == productionType.getProductionTarget() && EProductionCategory.CAPACITY == productionType.getProductionCategory();
            if (idPopulationCapacity) {
                // calculate which level must a capacity construction have to suit all the people
                level = detectPopCapStartingLevel(creditorDeposit, building, miningFactor);
            } else {
                level = 1;
            }
            final Construction constructedConstructionYard = new Construction(planet, building, level);
            planet.getConstructions().add(constructedConstructionYard);
        });
        owner.addKnownStarSystems(planet.getSystem());
        userService.save(owner);
        return planetService.save(planet);
    }

    private static int detectPopCapStartingLevel(@Nonnull final ResourceDeposit creditorDeposit, @Nonnull final Building building, final long miningFactor) {
        Preconditions.checkNotNull(creditorDeposit, "creditorDeposit must not be empty");
        Preconditions.checkNotNull(building, "building must not be empty");

        final int baseValue = building.getBaseValue();
        final BigDecimal increasingFactorPerLevel = BigDecimal.ONE.add(building.getIncreasingFactorPerLevel());
        final long sumOfPopulation = creditorDeposit.getCrewRequirement().getSumOfPopulation();

        final BigDecimal virtualSumOfPops = PopulationControlCalculator.getVirtualAmountOfPops(miningFactor, sumOfPopulation);
        final BigDecimal levelTo = virtualSumOfPops
                .divide(new BigDecimal(baseValue).multiply(increasingFactorPerLevel), MATH_CONTEXT_MORE_PRECISION)
                .add(BigDecimal.ONE);
        // be nice and add two levels - buildings on higher levels are not cheap
        return levelTo.intValue() + 2;
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
        final User withKnownStarSystems = userService.getWithKnownStarSystems(user);
        final Planet mainPlanet = planetService.findMainPlanet(user);
        final ResourceDeposit resourceDeposit = mainPlanet.getResourceDeposit();

        PayingPossibleResult payingPossibleResult = validateCostsForSystemInformation(mainPlanet, starSystem);
        if (!payingPossibleResult.isValid()) {
            throw new NotifyWebUserException("Buying this systems information is to expensive for you.", payingPossibleResult);
        }

        resourceDeposit.updateResource(costs.getRealResourceType(), costs.getAmount());
        assert withKnownStarSystems != null;
        withKnownStarSystems.addKnownStarSystems(starSystem);
        userService.save(withKnownStarSystems);
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

    /**
     * Tries to find a free planet which is as far as possible located from other colonized systems.
     *
     * @return the planet which is as far as possible away from other colonized systems
     */
    @Nonnull
    public Planet findPlanetForNewUser() {
        final List<StarSystem> allColonized = starSystemService.findAllColonized();

        final List<StarSystem> allColonizable = starSystemService.findAllColonizable();
        // remove systems which are already colonized
        allColonizable.removeIf(s -> s.getPlanets().stream().anyMatch(p -> !p.isColonizable()));
        if (allColonizable.isEmpty()) {
            LOGGER.warn("THERE ARE NO FREE PLANETS! It would be great if we could create new systems and planets!");
            throw new NotifyWebUserException("Sorry, but we have to make some more universe - ours is done for now.");
        }

        final Map<Orbit, StarSystem> colonizableByOrbit = allColonizable.stream().collect(Collectors.toMap(StarSystem::getOrbit, Function.identity()));
        final Map<Orbit, StarSystem> colonizedByOrbit = allColonized.stream().collect(Collectors.toMap(StarSystem::getOrbit, Function.identity()));
        final List<OrbitalDistanceMarker> marker = new ArrayList<>();
        colonizableByOrbit.keySet().forEach(colonizable -> colonizedByOrbit.keySet().forEach(colonized -> marker.add(new OrbitalDistanceMarker(colonizable, colonized))));
        marker.sort(Comparator.comparing(o -> o.distance));

        final OrbitalDistanceMarker biggestDistance = marker.get(marker.size() - 1);
        final StarSystem starSystem = colonizableByOrbit.get(biggestDistance.first);
        final List<Planet> planets = new ArrayList<>(starSystem.getPlanets());
        if (planets.size() == 1) {
            return planets.get(0);
        }
        final int randomIndex = ThreadLocalRandom.current().nextInt(0, planets.size() - 1);
        return planets.get(randomIndex);
    }

    private static class OrbitalDistanceMarker {

        Orbit first;
        Orbit second;
        Distance distance;

        public OrbitalDistanceMarker(final Orbit first, final Orbit second) {
            this.first = first;
            this.second = second;
            this.distance = first.getDistance(second);
        }
    }
}
