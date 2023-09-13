package de.yuga.spacebattle.backend.services.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.colonization.ColonizationCostCalculator;
import de.yuga.spacebattle.backend.calculator.resource.PopulationControlCalculator;
import de.yuga.spacebattle.backend.calculator.resource.TickOutputCalculator;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.dto.physics.OrbitalDistanceMarker;
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
import de.yuga.spacebattle.backend.enums.ECalculationType;
import de.yuga.spacebattle.backend.enums.EDepositType;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ColonizationService {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(ColonizationService.class);

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

    public ColonizationService(@Nonnull final ColonizationRepository repository,
                               @Nonnull final UserService userService,
                               @Nonnull final PlanetService planetService,
                               @Nonnull final StarSystemService starSystemService,
                               @Nonnull final BuildingService buildingService) {
        this.repository = Preconditions.checkNotNull(repository, "colonizationRepository shouldn't be null!");
        this.userService = Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");
        this.starSystemService = Preconditions.checkNotNull(starSystemService, "starSystemService shouldn't be null!");
        this.buildingService = Preconditions.checkNotNull(buildingService, "buildingService shouldn't be null!");
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

    @Nonnull
    public Colonization initiateColonization(@Nonnull final User user, @Nonnull final Planet toColonize) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");
        Preconditions.checkNotNull(toColonize, "toColonize shouldn't be null!");

        final ResourceDeposit colonizationCosts = ColonizationCostCalculator.getColonizationCosts(toColonize);

        final Planet mainPlanet = planetService.findMainPlanet(user);
        final ResourceDeposit debitorDeposit = mainPlanet.getResourceDeposit();

        final PayingPossibleResult payingPossible = debitorDeposit.isPayingPossible(colonizationCosts.getCrewRequirement());
        if (!payingPossible.isValid()) {
            return createPlannedColonization(user, toColonize);
        }

        return startColonizingPlanet(user, toColonize);
    }

    @Nonnull
    private Colonization createPlannedColonization(@Nonnull final User owner, @Nonnull final Planet toColonize) {
        Preconditions.checkNotNull(owner, "owner must not be empty");
        Preconditions.checkNotNull(toColonize, "toColonize must not be empty");

        final CrewRequirement crewRequirement = ColonizationCostCalculator.getColonizationCosts(toColonize).getCrewRequirement();
        return save(new Colonization(owner, toColonize, crewRequirement, 10, true));
    }

    /**
     * Starts a colonizing mission with the fixed duration of 10 ticks.
     * todo ticks based on distance between whatever planets
     *
     * @param user       the user who starts the colonization
     * @param toColonize the planet to colonize
     * @return the created colonization
     */
    @Nonnull
    private Colonization startColonizingPlanet(@Nonnull final User user, @Nonnull final Planet toColonize) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");
        Preconditions.checkNotNull(toColonize, "toColonize shouldn't be null!");

        final ResourceDeposit colonizationCosts = ColonizationCostCalculator.getColonizationCosts(toColonize);

        final Planet mainPlanet = planetService.findMainPlanet(user);
        final ResourceDeposit debitorDeposit = mainPlanet.getResourceDeposit();

        final PayingPossibleResult payingPossible = debitorDeposit.isPayingPossible(colonizationCosts.getCrewRequirement());
        if (!payingPossible.isValid()) {
            throw new NotifyWebUserException("This colonization is to expensive.", payingPossible);
        }

        final CrewRequirement crewRequirement = colonizationCosts.getCrewRequirement();
        debitorDeposit.updateCrew(crewRequirement, ECalculationType.SUBTRACT);

        final Colonization colonization = new Colonization(user, toColonize, crewRequirement, 10);
        save(colonization);
        planetService.save(mainPlanet);
        userService.save(user);
        return colonization;
    }

    /**
     * Colonizes a planet for an owner.
     * Currently, this implies that the new owner will get all information about the system without buying it especially.
     *
     * @param colonization the running colonization
     * @return the colonized planet
     */
    @Nonnull
    @Transactional(propagation = Propagation.REQUIRED)
    public Planet colonizePlanet(@Nonnull final Colonization colonization) {
        Preconditions.checkNotNull(colonization, "colonization shouldn't be null!");
        Preconditions.checkState(colonization.getDoneAtZero() == 0, "colonization cannot be done if the ship isn't in the orbit!");

        final User owner = userService.findWithKnownStarSystems(colonization.getUser());
        final Planet planet = colonization.getTarget();
        assert owner != null : "When this happens, the end is near.";
        planet.setOwner(owner);
        final List<Planet> allColonizedBy = planetService.findAllColonizedBy(owner);
        final boolean isMain = allColonizedBy.isEmpty();
        if (isMain) {
            planet.toggleMain();
            planet.getMiningFactors().equalize();
        }
        planet.getResourceDeposit().equalize(isMain);

        final ResourceDeposit creditorDeposit = planet.getResourceDeposit();
        final CrewRequirement requiredCrew = colonization.getCosts().getCrewRequirement();
        // set crew from the ship to the planet
        creditorDeposit.updateCrew(requiredCrew, ECalculationType.ADD);

        final double miningFactor = planet.getMiningFactors().getMiningFactorByType(EResourceType.POPULATION);

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
            planet.getConstructions().add(new Construction(planet, building, level));
        });
        owner.addKnownStarSystems(planet.getSystem());
        userService.save(owner);
        return planetService.save(planet);
    }

    private static int detectPopCapStartingLevel(@Nonnull final ResourceDeposit creditorDeposit, @Nonnull final Building building, final double miningFactor) {
        Preconditions.checkNotNull(creditorDeposit, "creditorDeposit must not be empty");
        Preconditions.checkNotNull(building, "building must not be empty");

        final BigDecimal baseValue = new BigDecimal(building.getBaseValue());
        final BigDecimal increasingFactorPerLevel = building.getIncreasingFactorPerLevel();
        final long sumOfPopulation = creditorDeposit.getCrewRequirement().getSumOfPopulation();

        final int maxLevel = 40;
        int levelTo = maxLevel; // fallback
        final BigDecimal virtualSumOfPops = PopulationControlCalculator.getVirtualAmountOfPops(miningFactor, sumOfPopulation);
        for (int virtualLevel = 1; virtualLevel <= maxLevel; virtualLevel++) {
            final BigDecimal output = TickOutputCalculator.getOutput(baseValue, increasingFactorPerLevel, miningFactor, virtualLevel);
            if (output.compareTo(virtualSumOfPops) > 0) {
                levelTo = virtualLevel;
                break;
            }
        }
        // be nice and add three levels - buildings on higher levels are not cheap
        return levelTo + 4;
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
        final User withKnownStarSystems = userService.findWithKnownStarSystems(user);
        final Planet mainPlanet = planetService.findMainPlanet(user);
        final ResourceDeposit resourceDeposit = mainPlanet.getResourceDeposit();

        PayingPossibleResult payingPossibleResult = validateCostsForSystemInformation(mainPlanet, starSystem);
        if (!payingPossibleResult.isValid()) {
            throw new NotifyWebUserException("Buying this systems information is to expensive for you.", payingPossibleResult);
        }

        resourceDeposit.updateResource(costs.getRealType(), costs.getAmount());
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
    @Nonnull
    public PayingPossibleResult validateCostsForSystemInformation(@Nonnull final Planet mainPlanet, @Nonnull final StarSystem starSystem) {
        Preconditions.checkNotNull(mainPlanet, "mainPlanet shouldn't be null!");
        Preconditions.checkNotNull(starSystem, "starSystem shouldn't be null!");

        final ResourceAmount costs = ColonizationCostCalculator.calculateInformationCost(starSystem);
        final ResourceDeposit resourceDeposit = mainPlanet.getResourceDeposit();
        ResourceDeposit c = new ResourceDeposit(EDepositType.COSTS);
        c.setAbsoluteResourceValue(costs.getRealType(), costs.getAmount());
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

        // remove systems which have a colonization in progress
        final Set<StarSystem> coloInProgress = findAll().stream().map(c -> c.getTarget().getSystem()).collect(Collectors.toSet());
        allColonizable.removeIf(coloInProgress::contains);

        if (allColonizable.isEmpty()) {
            LOGGER.warn("THERE ARE NO FREE PLANETS! It would be great if we could create new systems and planets!");
            throw new NotifyWebUserException("Sorry, but we have to make some more universe - ours is done for now.");
        }

        final Map<Orbit, StarSystem> colonizableByOrbit = allColonizable.stream().collect(Collectors.toMap(StarSystem::getOrbit, Function.identity()));
        final Map<Orbit, StarSystem> colonizedByOrbit = allColonized.stream().collect(Collectors.toMap(StarSystem::getOrbit, Function.identity()));
        final List<OrbitalDistanceMarker> marker = new ArrayList<>();
        colonizableByOrbit.entrySet().removeIf(e -> e.getValue().getPlanets().size() < 3);
        colonizableByOrbit.keySet().forEach(colonizable -> colonizedByOrbit.keySet().forEach(colonized -> marker.add(new OrbitalDistanceMarker(colonizable, colonized))));
        marker.sort(Comparator.comparing(OrbitalDistanceMarker::getDistance));

        StarSystem starSystem = null;
        for (int i = 0; i < marker.size() - 1; i += 2) {
            final StarSystem starSystem1 = colonizableByOrbit.get(marker.get(0).getFirst());
            final StarSystem starSystem2 = colonizableByOrbit.get(marker.get(1).getFirst());
            if (starSystem1 == null || starSystem2 == null) {
                continue;
            }
            final int size1 = starSystem1.getPlanets().size();
            final int size2 = starSystem2.getPlanets().size();
            if (size1 > size2) {
                starSystem = starSystem1;
            } else {
                starSystem = starSystem2;
            }
            break;
        }

        if (starSystem == null) {
            LOGGER.warn("No star system found!");
            throw new NotifyWebUserException("Sorry, but we have to make some more stars in the universe - ours is done for now.");
        }

        final List<Planet> planets = new ArrayList<>(starSystem.getPlanets());
        if (planets.size() == 1) {
            return planets.get(0);
        }
        final int randomIndex = ThreadLocalRandom.current().nextInt(0, planets.size() - 1);
        return planets.get(randomIndex);
    }

    @Nonnull
    public List<Colonization> findAllForSystem(@Nonnull final StarSystem system) {
        Preconditions.checkNotNull(system, "system must not be empty");

        return Objects.requireNonNullElse(repository.findAllForSystem(system.getId()), new ArrayList<>());
    }

    @Nonnull
    public List<Colonization> findAllPlannedForUser(final int idUser) {
        return Objects.requireNonNullElse(repository.findAllPlannedForUser(idUser), new ArrayList<>());
    }

    public void stopPlannedColonization(final int idUser, final int idColonization) {
        final Colonization colonization = repository.findById(idColonization).orElse(null);
        if (colonization == null || colonization.getUser().getId() != idUser) {
            return;
        }
        /* todo payback the paycheck */
        repository.delete(colonization);
    }
}
