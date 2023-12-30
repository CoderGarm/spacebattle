package de.yuga.spacebattle.backend.services;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.colonization.ColonizationCostCalculator;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.dto.spacecraft.Fitting;
import de.yuga.spacebattle.backend.entities.account.NonPlayerCharacter;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.account.forum.Forum;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.buildings.ProductionType;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.OrbitalModule;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.i18n.Translation;
import de.yuga.spacebattle.backend.entities.misc.HasName;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.MissileMotor;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Warhead;
import de.yuga.spacebattle.backend.entities.spacecrafts.fittings.AlignedFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.fittings.AmmunitionFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.fittings.SupportFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.*;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.NamedTechLevel;
import de.yuga.spacebattle.backend.entities.turn.Colonization;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.enums.*;
import de.yuga.spacebattle.backend.enums.physics.EAccelerationMetric;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import de.yuga.spacebattle.backend.enums.physics.EHyperBand;
import de.yuga.spacebattle.backend.services.account.ForumService;
import de.yuga.spacebattle.backend.services.account.NonPlayerCharacterService;
import de.yuga.spacebattle.backend.services.account.OwnerService;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.buildings.BuildingService;
import de.yuga.spacebattle.backend.services.combined.account.AllianceService;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.combined.spacecraft.OrbitalModuleService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.ShipClassService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.WarShipService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.orbitals.StarSystemService;
import de.yuga.spacebattle.backend.services.researches.ResearchService;
import de.yuga.spacebattle.backend.services.spacecraft.BattleService;
import de.yuga.spacebattle.backend.services.spacecraft.ModuleService;
import de.yuga.spacebattle.backend.services.turn.ColonizationService;
import de.yuga.spacebattle.backend.services.turn.TickRunnerService;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.misc.Coords;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.backend.entities.orbitals.StarSystem.STAR_SYSTEM_STANDARD_METRIC;

/**
 * The master of all. Do all the dev-stuff which could be removed or placed somewhere else.
 */
@Service
@SuppressWarnings({"deprecation", "DeprecatedIsStillUsed", "DuplicatedCode"})
public class MasterOfTheUniverseService {

    public static final String BALANCING_ISSUES = "Only for balancing issues.";

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MasterOfTheUniverseService.class);

    /**
     * The radius of a star in px which is displayed in the canvas for a star system.
     */
    public static final int STAR_RADIUS = 30;

    public static final String DEFEATED_OPPONENT = "Defeated Opponent";
    public static final String PIRATE = "Scharteke";

    public static final ProductionType CONSTRUCTION_YARD_PT = new ProductionType(EResourceType.CONSTRUCTION, EProductionCategory.PRODUCE, null);
    public static final ProductionType SHIPYARD_PT = new ProductionType(EResourceType.ORBITAL_CONSTRUCTION, EProductionCategory.PRODUCE, null);
    public static final ProductionType RESEARCH_LAB_PT = new ProductionType(EResourceType.RESEARCH, EProductionCategory.PRODUCE, null);
    public static final ProductionType MARKET_PT = new ProductionType(EResourceType.CREDITS, EProductionCategory.PRODUCE, null);
    public static final ProductionType METAL_WORKS = new ProductionType(EResourceType.METALORE, EProductionCategory.PRODUCE, null);
    public static final ProductionType HEAVY_METALS_WORK_PT = new ProductionType(EResourceType.HEAVY_METALS, EProductionCategory.PRODUCE, null);
    public static final ProductionType RARE_ELEMENTS_PT = new ProductionType(EResourceType.RARE_ELEMENTS, EProductionCategory.PRODUCE, null);
    public static final ProductionType LIVING_PT = new ProductionType(EResourceType.POPULATION, EProductionCategory.CAPACITY, null);
    public static final ProductionType ELEMENTARY_SCHOOL_PT = new ProductionType(EResourceType.POPULATION, EProductionCategory.REFINEMENT, ERefinementSequence.EDUCATION_CIVIL_I);
    public static final ProductionType SECONDARY_SCHOOL_PT = new ProductionType(EResourceType.POPULATION, EProductionCategory.REFINEMENT, ERefinementSequence.EDUCATION_CIVIL_II);
    public static final ProductionType UNIVERSITY_PT = new ProductionType(EResourceType.POPULATION, EProductionCategory.REFINEMENT, ERefinementSequence.EDUCATION_CIVIL_III);
    public static final ProductionType MILITARY_I_PT = new ProductionType(EResourceType.POPULATION, EProductionCategory.REFINEMENT, ERefinementSequence.EDUCATION_MILITARY_I);
    public static final ProductionType MILITARY_II_PT = new ProductionType(EResourceType.POPULATION, EProductionCategory.REFINEMENT, ERefinementSequence.EDUCATION_MILITARY_II);

    private static final Map<EEducationType, Long> XXXS_CREW = Map.of(EEducationType.ENLISTED, 1L);
    private static final Map<EEducationType, Long> XXS_CREW = Map.of(EEducationType.ENLISTED, 3L);
    private static final Map<EEducationType, Long> XS_CREW = Map.of(EEducationType.ENLISTED, 5L, EEducationType.OFFICER, 1L);
    private static final Map<EEducationType, Long> S_CREW = Map.of(EEducationType.ENLISTED, 8L, EEducationType.OFFICER, 1L);
    private static final Map<EEducationType, Long> M_CREW = Map.of(EEducationType.ENLISTED, 12L, EEducationType.OFFICER, 1L);
    private static final Map<EEducationType, Long> L_CREW = Map.of(EEducationType.ENLISTED, 18L, EEducationType.OFFICER, 2L);
    private static final Map<EEducationType, Long> XL_CREW = Map.of(EEducationType.ENLISTED, 20L, EEducationType.OFFICER, 3L);
    private static final Map<EEducationType, Long> XL_ORBITAL_CREW = Map.of(EEducationType.ENLISTED, 48L, EEducationType.OFFICER, 9L);
    private static final Map<EEducationType, Long> XXL_CREW = Map.of(EEducationType.ENLISTED, 300L, EEducationType.OFFICER, 30L);
    private static final Map<EEducationType, Long> XXL_ORBITAL_CREW = Map.of(EEducationType.ENLISTED, 64L, EEducationType.OFFICER, 11L);
    private static final Map<EEducationType, Long> XXXL_CREW = Map.of(EEducationType.ENLISTED, 500L, EEducationType.OFFICER, 50L);
    private static final Map<EEducationType, Long> CIVIL_L_ORBITAL_CREW = Map.of(EEducationType.SCHOOL, 34L, EEducationType.UNIVERSITY, 12L);
    private static final Map<EEducationType, Long> CIVIL_XL_ORBITAL_CREW = Map.of(EEducationType.SCHOOL, 51L, EEducationType.UNIVERSITY, 30L);
    private static final Map<EEducationType, Long> CIVIL_XXL_ORBITAL_CREW = Map.of(EEducationType.SCHOOL, 150L, EEducationType.UNIVERSITY, 100L);

    private static final Map<EEducationType, Long> CONQUERABLE_PLANET = Map.of(
            EEducationType.NONE, 4000L,
            EEducationType.SCHOOL, 5000L,
            EEducationType.UNIVERSITY, 600L,
            EEducationType.ENLISTED, 1800L,
            EEducationType.OFFICER, 400L
    );

    public static final String FLASHKID = "Flashkid";

    @Nonnull
    private final Validator validator;

    @Nonnull
    private final TickRunnerService tickService;

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final AllianceService allianceService;

    @Nonnull
    private final StarSystemService starsystemService;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final BuildingService buildingService;

    @Nonnull
    private final ModuleService moduleService;

    @Nonnull
    private final ShipClassService shipClassService;

    @Nonnull
    private final ResearchService researchService;

    @Nonnull
    private final FleetService fleetService;

    @Nonnull
    private final WarShipService warShipService;

    @Nonnull
    private final ForumService forumService;

    @Nonnull
    private final ColonizationService colonizationService;

    @Nonnull
    private final BattleService battleService;

    @Nonnull
    private final ResourceService resourceService;

    @Nonnull
    private final NonPlayerCharacterService nonPlayerCharacterService;

    @Nonnull
    private final OwnerService ownerService;

    @Nonnull
    private final OrbitalModuleService orbitalModuleService;

    @Nonnull
    private final UserDeleteServiceService userDeleteServiceService;

    @Autowired
    public MasterOfTheUniverseService(@Nonnull final TickRunnerService tickService,
                                      @Nonnull final UserService userService,
                                      @Nonnull final AllianceService allianceService,
                                      @Nonnull final StarSystemService starSystemService,
                                      @Nonnull final PlanetService planetService,
                                      @Nonnull final BuildingService buildingService,
                                      @Nonnull final ModuleService moduleService,
                                      @Nonnull final ShipClassService shipClassService,
                                      @Nonnull final ResearchService researchService,
                                      @Nonnull final FleetService fleetService,
                                      @Nonnull final WarShipService warShipService,
                                      @Nonnull final ForumService forumService,
                                      @Nonnull final ColonizationService colonizationService,
                                      @Nonnull final BattleService battleService,
                                      @Nonnull final ResourceService resourceService,
                                      @Nonnull final NonPlayerCharacterService nonPlayerCharacterService,
                                      @Nonnull final OwnerService ownerService,
                                      @Nonnull final OrbitalModuleService orbitalModuleService,
                                      @Nonnull final UserDeleteServiceService userDeleteServiceService) {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
        this.tickService = Preconditions.checkNotNull(tickService, "tickService shouldn't be null!");
        this.userService = Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        this.allianceService = Preconditions.checkNotNull(allianceService, "allianceService shouldn't be null!");
        this.starsystemService = Preconditions.checkNotNull(starSystemService, "starSystemService shouldn't be null!");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");
        this.buildingService = Preconditions.checkNotNull(buildingService, "buildingService shouldn't be null!");
        this.moduleService = Preconditions.checkNotNull(moduleService, "moduleService shouldn't be null!");
        this.shipClassService = Preconditions.checkNotNull(shipClassService, "shipClassService shouldn't be null!");
        this.researchService = Preconditions.checkNotNull(researchService, "researchService shouldn't be null!");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService shouldn't be null!");
        this.warShipService = Preconditions.checkNotNull(warShipService, "warShipService shouldn't be null!");
        this.forumService = Preconditions.checkNotNull(forumService, "forumService shouldn't be null!");
        this.colonizationService = Preconditions.checkNotNull(colonizationService, "colonizationService shouldn't be null!");
        this.battleService = Preconditions.checkNotNull(battleService, "battleService must not be empty");
        this.resourceService = Preconditions.checkNotNull(resourceService, "resourceService must not be empty");
        this.nonPlayerCharacterService = Preconditions.checkNotNull(nonPlayerCharacterService, "nonPlayerCharacterService must not be empty");
        this.ownerService = Preconditions.checkNotNull(ownerService, "ownerService must not be empty");
        this.orbitalModuleService = Preconditions.checkNotNull(orbitalModuleService, "orbitalModuleService must not be empty");
        this.userDeleteServiceService = Preconditions.checkNotNull(userDeleteServiceService, "userDeleteServiceService must not be empty");
    }

    @PostConstruct
    @SuppressWarnings("ConstantConditions")
    public void transform() {
        validateUniverse();
        LOGGER.info("---------------------------- transforming the universe ----------------------------");
        final boolean transformationNeeded = userService.findByUsername("TanteManfred").isPresent();
        if (transformationNeeded) {
            userDeleteServiceService.deleteAllInactiveUsers();

            final NonPlayerCharacter pirate = nonPlayerCharacterService.findByUsername(MasterOfTheUniverseService.PIRATE);
            final List<Planet> level1 = planetService.findAllColonizedBy(pirate);
            for (final Planet planet : level1) {
                createGuardFleet(planet, 1);
            }

            final List<Planet> toColonize = planetService.findAll(List.of(1330, 1780, 1768, 904, 1732, 1631, 1253, 1067, 1233, 2282, 148, 461, 610, 820, 137, 2341, 2249, 2177, 2214, 2182));
            toColonize
                    .forEach(planet -> colonizationService.colonizePlanet(new Colonization(pirate, planet, new CrewRequirement(CONQUERABLE_PLANET, EDepositType.COSTS), 0)));

            final Map<Integer, List<Integer>> m = Map.of(
                    1, List.of(1330, 1780, 1768, 904),
                    2, List.of(1732, 1631, 1253, 1067),
                    3, List.of(1233, 2282, 148, 461, 610, 820, 137, 2341, 2249, 2177, 2214, 2182)
            );

            m.forEach((strengthLevel, planetIDs) -> {
                final List<Planet> planets = planetService.findAll(planetIDs);

                for (Planet planet : planets) {
                    createGuardFleet(planet, strengthLevel);
                }
            });

            LOGGER.info("---------------------------- done transforming -------------------------------");
        } else {
            LOGGER.info("---------------------------- nothing to transform ----------------------------");
        }
    }

    private void createGuardFleet(@Nonnull final Planet planet, final int strengthLevel) {
        Preconditions.checkNotNull(planet, "planet must not be empty");

        final NonPlayerCharacter owner = planet.getNpcOwner();
        Preconditions.checkNotNull(owner, "owner must not be empty");

        LOGGER.info("Creating guard fleet for the planet {} in {} with strength {}", planet.getName(), planet.getSystem().getName(), strengthLevel);

        final List<ShipClass> classList = shipClassService.findAllLatestByOwner(owner.getId());
        final ShipClass warGoose = classList.stream().filter(s -> s.getName().equals("War Goose")).findFirst().orElseThrow(NullPointerException::new);
        final ShipClass songbird = classList.stream().filter(s -> s.getName().equals("Songbird") && s.getFlight() == 1).findFirst().orElseThrow(NullPointerException::new);

        final Fleet opponentsFleet = createFleet(owner, planet, planet.getName() + " Buccaneers");
        switch (strengthLevel) {
            case 1:
                createShipForFleet(planet, resourceService.getRandomWarshipName(), opponentsFleet, warGoose);
                createShipForFleet(planet, resourceService.getRandomWarshipName(), opponentsFleet, warGoose);
                createShipForFleet(planet, resourceService.getRandomWarshipName(), opponentsFleet, songbird);
                break;
            case 2:
                createShipForFleet(planet, resourceService.getRandomWarshipName(), opponentsFleet, warGoose);
                createShipForFleet(planet, resourceService.getRandomWarshipName(), opponentsFleet, songbird);
                createShipForFleet(planet, resourceService.getRandomWarshipName(), opponentsFleet, songbird);
                break;
            case 3:
                createShipForFleet(planet, resourceService.getRandomWarshipName(), opponentsFleet, warGoose);
                createShipForFleet(planet, resourceService.getRandomWarshipName(), opponentsFleet, warGoose);
                createShipForFleet(planet, resourceService.getRandomWarshipName(), opponentsFleet, warGoose);
                createShipForFleet(planet, resourceService.getRandomWarshipName(), opponentsFleet, songbird);
                createShipForFleet(planet, resourceService.getRandomWarshipName(), opponentsFleet, songbird);
                break;
        }
    }

    private void createShipForFleet(@Nonnull final Planet planet,
                                    @Nonnull final String name,
                                    @Nonnull final Fleet fleet,
                                    @Nonnull final ShipClass shipClass) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(name, "name must not be empty");
        Preconditions.checkNotNull(fleet, "fleet must not be empty");
        Preconditions.checkNotNull(shipClass, "shipClass must not be empty");

        final WarShip warShip = new WarShip(name, planet, fleet, shipClass);
        warShip.setOperational();
        warShipService.save(warShip);
    }

    @SuppressWarnings("unused")
    private void repairAllShips() {
        final Set<WarShip> warShips = warShipService.findAll().stream().filter(w -> !w.isDeleted()).collect(Collectors.toSet());
        warShips.forEach(warShip -> {
            warShip.getWarshipHealthState().repair();
            warShip.getWarshipHealthState().ammoUp();
            warShip.getWarshipHealthState().setFightingCapable(true);
        });
        warShipService.saveAll(warShips);
    }

    private void createPirateShip(@Nonnull final Owner owner,
                                  @Nonnull final Fitting f) {
        Preconditions.checkNotNull(owner, "owner must not be empty");
        Preconditions.checkNotNull(f, "f must not be empty");

        final EShipClassType shipClassType = EShipClassType.VT;
        final ShipClass shipClass = new ShipClass(owner, "War Goose");
        shipClass.setShipClassType(shipClassType);

        final Armor a = f.getByType(shipClassType, f.getArmors());
        final Propulsion p = f.getProp(ETechnologyType.MILITARY);
        final ElectronicWarfare e = f.getByType(shipClassType, f.getEloka());
        final Sidewall s = f.getByType(shipClassType, f.getSidewalls());

        final Weapon beam = f.getWeapon(shipClassType, EWeaponType.BEAM);
        final Weapon pointDefense = f.getWeapon(shipClassType, EWeaponType.POINT_DEFENSE);

        final Map.Entry<Launcher, Missile> shipKiller = f.getLauncher(shipClassType, EWeaponType.MISSILE);
        final Map.Entry<Launcher, Missile> counterMissile = f.getLauncher(shipClassType, EWeaponType.COUNTER_MISSILE);

        fit(shipClass, a, p, e, s,
                Set.of(
                        new AlignedFitting(EWeaponAlignment.BOW, Objects.requireNonNull(beam), 1),
                        new AlignedFitting(EWeaponAlignment.BROADSIDE, Objects.requireNonNull(beam), 2),
                        new AlignedFitting(EWeaponAlignment.STERN, Objects.requireNonNull(beam), 1),
                        new AlignedFitting(EWeaponAlignment.BOW, Objects.requireNonNull(pointDefense), 1),
                        new AlignedFitting(EWeaponAlignment.BROADSIDE, Objects.requireNonNull(pointDefense), 2),
                        new AlignedFitting(EWeaponAlignment.STERN, Objects.requireNonNull(pointDefense), 1),

                        new AlignedFitting(EWeaponAlignment.BOW, Objects.requireNonNull(shipKiller).getKey(), 1),
                        new AlignedFitting(EWeaponAlignment.BROADSIDE, Objects.requireNonNull(shipKiller).getKey(), 2),
                        new AlignedFitting(EWeaponAlignment.STERN, Objects.requireNonNull(shipKiller).getKey(), 1),
                        new AlignedFitting(EWeaponAlignment.BOW, Objects.requireNonNull(counterMissile).getKey(), 1),
                        new AlignedFitting(EWeaponAlignment.BROADSIDE, Objects.requireNonNull(counterMissile).getKey(), 2),
                        new AlignedFitting(EWeaponAlignment.STERN, Objects.requireNonNull(counterMissile).getKey(), 1)
                ),
                Set.of(
                        new AmmunitionFitting(shipKiller.getValue(), 30),
                        new AmmunitionFitting(counterMissile.getValue(), 60)
                ),
                Set.of());

        final List<PassiveModule> passiveModules = f.getPassiveModules();
        final PassiveModule freightModule = passiveModules.stream().filter(pa -> pa.getSupportType() == ESupportType.FREIGHT).findFirst().orElseThrow(NullPointerException::new);
        final PassiveModule passengerModule = passiveModules.stream().filter(pa -> pa.getSupportType() == ESupportType.PASSENGER).findFirst().orElseThrow(NullPointerException::new);
        shipClass.setSupportFittings(Set.of(
                new SupportFitting(freightModule, 1),
                new SupportFitting(passengerModule, 1)));

        final Set<ConstraintViolation<ShipClass>> validate = validator.validate(shipClass);
        if (!validate.isEmpty()) {
            throw new NotifyWebUserException("The provided class is not valid.", validate);
        }
        shipClassService.save(shipClass);
    }

    private void validateUniverse() {
        LOGGER.info("---------------------------- validating the universe -----------------------------");
        final boolean initiationNeeded = tickService.isTickPresent();
        if (initiationNeeded) {
            LOGGER.info("---------------------------- creating the universe ----------------------------");
            createInitialDataPayload();
            LOGGER.info("---------------------------- done creating ------------------------------------");
        }
        LOGGER.info("---------------------------- done validating --------------------------------------");
    }

    void createInitialDataPayload() {
        createBuildings();
        LOGGER.info("Buildings created");

        final List<Coords> coords = resourceService.readStarSystems();
        createStarSystems(coords);
        final List<StarSystem> starSystems = starsystemService.findAll();

        createNPCs();
        LOGGER.info("NPCs created");

        //noinspection OptionalGetWithoutIsPresent
        final User flashkid = userService.findByUsername(FLASHKID).get().getUser();
        Owner pirate = ownerService.findByUsername(DEFEATED_OPPONENT);
        LOGGER.info("Users created");

        allianceService.createAlliance("Argonauten", "A", flashkid);
        LOGGER.info("Alliance created");

        createForums();
        LOGGER.info("Forums created");

        StarSystem s1 = starSystems.stream().filter(s -> s.getName().equals("Yeltsin")).findFirst().orElseThrow(() -> new NotifyWebUserException("The star systems should be present."));
        LOGGER.info("Star systems created");

        final Planet p11 = new ArrayList<>(s1.getPlanets()).get(0);
        LOGGER.info("Planets created");

        colonizePlanet(flashkid, p11);
        LOGGER.info("Planets colonized and populated. Constructions were build.");

        final List<Armor> armors = createArmors();
        LOGGER.info("armors created");

        final List<Propulsion> propulsions = createPropulsions();
        LOGGER.info("propulsions created");

        final List<ElectronicWarfare> eloka = createEloka();
        LOGGER.info("eloka created");

        createOrbitalModules();
        LOGGER.info("Orbital Modules created");

        final List<Sidewall> sidewalls = createSidewalls();
        LOGGER.info("sidewall created");

        final List<Weapon> weapons = createWeapons();
        LOGGER.info("weapons created");

        final Map<Launcher, Missile> missiles = createMissiles();
        LOGGER.info("missiles created");

        final List<PassiveModule> passiveModules = createPassiveModules();
        LOGGER.info("support modules created");
        LOGGER.info("Modules created");

        addUnlockedResearches(flashkid);
        addUnlockedResearches(flashkid);
        LOGGER.info("Researches populated");

        final Fitting fitting = new Fitting(propulsions, armors, eloka, sidewalls, weapons, missiles, passiveModules);
        chansonDestroyer(Objects.requireNonNull(pirate), fitting);

        pirate = nonPlayerCharacterService.createNPC(PIRATE);
        createPirateShip(pirate, fitting);
        LOGGER.info("ShipClass created");

        createFleetForUser(flashkid);
        createOpponentFleetForUser(flashkid);
        LOGGER.info("Fleets created");
        LOGGER.info("Warships created");
        LOGGER.info("Fleets populated");

        tickService.doTick();
        LOGGER.info("First tick is done");
        LOGGER.info("All Data created");
    }

    @SuppressWarnings("DataFlowIssue")
    private void createNPCs() {
        NonPlayerCharacter npc = nonPlayerCharacterService.createNPC("Star Kingdom of Manticore");
        StarSystem sys = starsystemService.findByName("Manticore");
        Planet planet = sys.getPlanets().stream().findFirst().orElse(null);
        colonizeNPC(planet, npc);

        npc = nonPlayerCharacterService.createNPC("Solarian League");
        sys = starsystemService.findByName("Sol");
        planet = sys.getPlanets().stream().findFirst().orElse(null);
        colonizeNPC(planet, npc);

        npc = nonPlayerCharacterService.createNPC("Haven Republic");
        sys = starsystemService.findByName("Haven");
        planet = sys.getPlanets().stream().findFirst().orElse(null);
        colonizeNPC(planet, npc);

        npc = nonPlayerCharacterService.createNPC("Anderman Empire");
        sys = starsystemService.findByName("Gregor");
        planet = sys.getPlanets().stream().findFirst().orElse(null);
        colonizeNPC(planet, npc);

        npc = nonPlayerCharacterService.createNPC("Silesia Confederacy");
        sys = starsystemService.findByName("Silesia");
        planet = sys.getPlanets().stream().filter(Planet::isColonizable).findFirst().orElse(null);
        colonizeNPC(planet, npc);

        npc = nonPlayerCharacterService.createNPC("Midgard Federation");
        sys = starsystemService.findByName("Midgard");
        planet = sys.getPlanets().stream().filter(Planet::isColonizable).findFirst().orElse(null);
        colonizeNPC(planet, npc);

        npc = nonPlayerCharacterService.createNPC("Asgard Association");
        sys = starsystemService.findByName("Asgard");
        planet = sys.getPlanets().stream().filter(Planet::isColonizable).findFirst().orElse(null);
        colonizeNPC(planet, npc);

        npc = nonPlayerCharacterService.createNPC("Rembrandt Trade Union");
        sys = starsystemService.findByName("Rembrandt");
        planet = sys.getPlanets().stream().filter(Planet::isColonizable).findFirst().orElse(null);
        colonizeNPC(planet, npc);

        npc = nonPlayerCharacterService.createNPC("Meroa Trading Association");
        sys = starsystemService.findByName("Meroa");
        planet = sys.getPlanets().stream().filter(Planet::isColonizable).findFirst().orElse(null);
        colonizeNPC(planet, npc);
    }

    private void colonizeNPC(@Nonnull final Planet planet,
                             @Nonnull final NonPlayerCharacter owner) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(owner, "owner must not be empty");

        planet.setOwner(owner);
        planet.toggleMain();
        planetService.save(planet);
    }

    private void createOrbitalModules() {
        /* military modules */

        Research research = moduleService.findAllElectronicWarfare().get(0).getNamedTechLevel().getUnlockedThrough();
        Preconditions.checkNotNull(research, "research must not be empty");
        research = research("Gravitic Anomaly Sensors", "Gravitic sensors are passive sensors which detect the presence of gravity waves and measure the influence of gravity on their surroundings.",
                10, ETechLevel.TECH_II, research);
        amendTranslation(research, "Gravimetrische Anomaliedetektoren", "Gravitationssensoren sind Sensoren zur Ortung von Gravitationsverzerrungen, die durch Impellerantriebe verursacht werden.");
        researchService.save(research);

        OrbitalModule m = orbitalModule("Low-altitude Gravitic Anomaly Detector Array", "Gravity sensors can be used to determine hyperprints more precisely.",
                18000, 1000, L_CREW, ETechLevel.TECH_II, EModuleType.ELECTRONIC_WARFARE, research, 1);
        amendTranslation(m, "Suborbitales Gravimetrisches Anomaliedetektor Array", "Mit Gravitationssensoren können Hyperabdrücke genauer bestimmt werden.");
        orbitalModuleService.save(m);

        m = orbitalModule("Enhanced Low Altitude Gravitic Anomaly Detector Array", "Gravity sensors can be used to determine hyperprints more precisely.",
                26000, 1500, XL_CREW, ETechLevel.TECH_II, EModuleType.ELECTRONIC_WARFARE, research, 3);
        amendTranslation(m, "Verbessertes suborbitales gravimetrisches Anomaliedetektor Array", "Mit Gravitationssensoren können Hyperabdrücke genauer bestimmt werden.");
        orbitalModuleService.save(m);

        m = orbitalModule("Orbital Gravitic Anomaly Detector Array", "Gravity sensors can be used to determine hyperprints more precisely.",
                75000, 4500, XL_ORBITAL_CREW, ETechLevel.TECH_III, EModuleType.ELECTRONIC_WARFARE, research, 5);
        amendTranslation(m, "Orbitales Gravitisches Anomalie Sensor Array", "Mit Gravitationssensoren können Hyperabdrücke genauer bestimmt werden.");
        orbitalModuleService.save(m);

        m = orbitalModule("Enhanced High orbital Gravitic Anomaly Detector Array", "Gravity sensors can be used to determine hyperprints more precisely.",
                125000, 9500, XXL_ORBITAL_CREW, ETechLevel.TECH_III, EModuleType.ELECTRONIC_WARFARE, research, 10);
        amendTranslation(m, "Verbessertes orbitales Gravitisches Anomalie Sensor Array", "Mit Gravitationssensoren können Hyperabdrücke genauer bestimmt werden.");
        orbitalModuleService.save(m);

        /* civil modules */
        research = buildingService.findBuildingByProductionType(LIVING_PT).get(0).getUnlockedThrough();
        Preconditions.checkNotNull(research, "research must not be empty");

        // calculation is 30-40 BRT per passenger like a cruise liner
        m = orbitalModule("Orbital habitat L", "A space habitat is a space station that is not primarily used for industrial or military purposes, but rather as permanent accommodation.",
                70000, 1000, CIVIL_L_ORBITAL_CREW, ETechLevel.TECH_II, EResourceType.POPULATION, research, 4);
        amendTranslation(m, "Weltraumhabitat L", "Ein Weltraumhabitat ist eine Raumstation, die nicht in erster Linie industriellen oder militärischen Zwecken dient, sondern als permanente Unterkunft.");
        orbitalModuleService.save(m);

        m = orbitalModule("Orbital habitat XL", "A space habitat is a space station that is not primarily used for industrial or military purposes, but rather as permanent accommodation.",
                130000, 1900, CIVIL_XL_ORBITAL_CREW, ETechLevel.TECH_II, EResourceType.POPULATION, research, 6);
        amendTranslation(m, "Weltraumhabitat XL", "Ein Weltraumhabitat ist eine Raumstation, die nicht in erster Linie industriellen oder militärischen Zwecken dient, sondern als permanente Unterkunft.");
        orbitalModuleService.save(m);

        m = orbitalModule("Orbital habitat XXL", "A space habitat is a space station that is not primarily used for industrial or military purposes, but rather as permanent accommodation.",
                400000, 4500, CIVIL_XXL_ORBITAL_CREW, ETechLevel.TECH_III, EResourceType.POPULATION, research, 9);
        amendTranslation(m, "Weltraumhabitat XXL", "Ein Weltraumhabitat ist eine Raumstation, die nicht in erster Linie industriellen oder militärischen Zwecken dient, sondern als permanente Unterkunft.");
        orbitalModuleService.save(m);
    }

    private void createBuildings() {
        /* civil constructions */
        Research research = research("Civil facilities", "This contains every kind of technological and industrial knowledge and development.", ETechLevel.TECH_I, null);
        amendTranslation(research, "Zivile Anlagen", "Erforscht und enthält das technische Wissen und Fähigkeiten für zivile Konstruktionen.");
        final Research civilConstructions = researchService.save(research);


        Building b = building("Construction Yard", "Useful to build ground constructions.",
                15000, 10, EEducationType.COLLEGE, ETechLevel.TECH_I, CONSTRUCTION_YARD_PT, research, 1, 0.2);
        amendTranslation(b, "Bauhof", "Nützlich für Gebäude.");
        buildingService.save(b);

        b = building("Financial markets", "A mixture from public investments and tax systems to create income.",
                5000, 10, EEducationType.COLLEGE, ETechLevel.TECH_I, MARKET_PT, research, 1, 0.2);
        amendTranslation(b, "Finanzmärkte", "Eine Mixtur aus öffentlichen Investitionen und Steuern um Einkommen zu generieren.");
        buildingService.save(b);

        b = building("Metal works", "Produces the most basic materials, from rubber in shoes to special alloys for spacecrafts.",
                17500, 10, EEducationType.COLLEGE, ETechLevel.TECH_I, METAL_WORKS, research, 1, 0.2);
        amendTranslation(b, "Metallwerke", "Produziert grundlegende Materialien, von Gummi für Schuhe bis zu speziellen Legierungen für die Raumfahrt.");
        buildingService.save(b);


        b = building("Orbital ore factory", "Produces some asteroid-based materials and farms gas from the giants and clouds in the system.",
                2000, 10, EEducationType.UNIVERSITY, ETechLevel.TECH_II, HEAVY_METALS_WORK_PT, research, 7, 0.2);
        amendTranslation(b, "Orbitale Metallwerke", "Baut Ressourcen und Gase ab, die hauptsächlich außerhalb des Planeten zu finden sind.");
        buildingService.save(b);

        b = building("Nanofarm", "Produces rare elements and combines them to specialized molycircs and complex nano structures.",
                1000, 10, EEducationType.UNIVERSITY, ETechLevel.TECH_III, RARE_ELEMENTS_PT, research, 10, 0.2);
        amendTranslation(b, "Nanofarm", "Produziert die notwendigen Rohstoffe für Molycircs und komplexe Nanostrukturen.");
        buildingService.save(b);

        research = research("Healthcare and living", "Improves the way to live and stay alive.", ETechLevel.TECH_I, null);
        amendTranslation(research, "Gesundheit und Wohnen", "Verbessert die Möglichkeiten des allgemeinen Lebens.");
        researchService.save(research);

        b = building("Residential and housing", "Everyone needs a home.", 10000, 150, EEducationType.COLLEGE, ETechLevel.TECH_I, LIVING_PT, research, 1, 0.4);
        amendTranslation(b, "Wohnräume", "Jeder braucht ein zuhause.");
        buildingService.save(b);
        /* civil constructions */

        /* civil education */
        research = research("Scientific and pedagogical methods", "How to teach someone.", ETechLevel.TECH_I, null);
        amendTranslation(research, "Wissenschaftliche und pädagogische Methoden", "Wie man jemanden ausbildet.");
        final Research civilEducation = researchService.save(research);

        b = building("Research Laboratories", "Brings light into the dark.",
                25, 10, EEducationType.UNIVERSITY, ETechLevel.TECH_I, RESEARCH_LAB_PT, research, 1, 0.2);
        amendTranslation(b, "Forschungslabore", "Bringt Licht ins Dunkel.");
        buildingService.save(b);

        b = building("Elementary schools", "The first school.", 100, 10, EEducationType.UNIVERSITY, ETechLevel.TECH_I, ELEMENTARY_SCHOOL_PT, research, 1, 0.4);
        amendTranslation(b, "Grundschule", "Die erste Schule.");
        buildingService.save(b);

        b = building("Secondary schools", "Prepares pupils for the workforce.", 100, 10, EEducationType.UNIVERSITY, ETechLevel.TECH_I, SECONDARY_SCHOOL_PT, research, 1, 0.4);
        amendTranslation(b, "Weiterführende Schule", "Bereitet Schüler auf das Arbeitsleben vor.");
        buildingService.save(b);

        b = building("University", "Prepares and trains people to be researchers.", 100, 10, EEducationType.UNIVERSITY, ETechLevel.TECH_I, UNIVERSITY_PT, research, 1, 0.4);
        amendTranslation(b, "Universität", "Bildet Forscher aus.");
        buildingService.save(b);
        /* civil education */

        /* military constructions */
        research = research("Military facilities", "This contains every kind of technological and military knowledge and development.", ETechLevel.TECH_I, civilConstructions);
        amendTranslation(research, "Militärische Strukturen", "Erforscht und enthält das technische Wissen und Fähigkeiten für militärische Konstruktionen");
        researchService.save(research);

        b = building("Orbitals Construction Yard", "The shipyard constructs off-planet components, ships and space stations.",
                8000, 10, EEducationType.COLLEGE, ETechLevel.TECH_I, SHIPYARD_PT, research, 1, 0.2);
        amendTranslation(b, "Schiffswerft", "Die Schiffswerft konstruiert außerplanetare Bauteile, Schiffe und Raumstationen.");
        buildingService.save(b);
        /* military constructions */

        /* military education */
        research = research("Military history and modern tactics", "The knowledge about the military past will improve the military future.", ETechLevel.TECH_I, civilEducation);
        amendTranslation(research, "Militärhistorie und moderne Taktiken", "Das Wissen über die Vergangenheit wird die Zukunft verbessern.");
        researchService.save(research);

        b = building("Teams Rank School", "Trains ordinary people into crew people.", 30, 10, EEducationType.ENLISTED, ETechLevel.TECH_I, MILITARY_I_PT, research, 1, 0.4);
        amendTranslation(b, "Mannschaftsschule", "Bildet gewöhnliche Leute zu Besatzungsmitgliedern aus.");
        buildingService.save(b);

        b = building("Officer school", "Trains officers.", 10, 10, EEducationType.OFFICER, ETechLevel.TECH_I, MILITARY_II_PT, research, 1, 0.4);
        amendTranslation(b, "Offiziersschule", "Bildet Offiziere aus.");
        buildingService.save(b);
        /* military education */
    }

    private ShipClass chansonDestroyer(@Nonnull final Owner owner,
                                       @Nonnull final Fitting f) {
        Preconditions.checkNotNull(owner, "owner must not be empty");
        Preconditions.checkNotNull(f, "f must not be empty");

        final EShipClassType shipClassType = EShipClassType.DD;
        final ShipClass shipClass = new ShipClass(owner, "Songbird");
        shipClass.setShipClassType(shipClassType);

        final Armor a = f.getByType(shipClassType, f.getArmors());
        final Propulsion p = f.getProp(ETechnologyType.MILITARY);
        final ElectronicWarfare e = f.getByType(shipClassType, f.getEloka());
        final Sidewall s = f.getByType(shipClassType, f.getSidewalls());

        final Weapon beam = f.getWeapon(shipClassType, EWeaponType.BEAM);
        final Weapon pointDefense = f.getWeapon(shipClassType, EWeaponType.POINT_DEFENSE);

        final Map.Entry<Launcher, Missile> shipKiller = f.getLauncher(shipClassType, EWeaponType.MISSILE);
        final Map.Entry<Launcher, Missile> counterMissile = f.getLauncher(shipClassType, EWeaponType.COUNTER_MISSILE);

        fit(shipClass, a, p, e, s,
                Set.of(
                        new AlignedFitting(EWeaponAlignment.BOW, Objects.requireNonNull(beam), 1),
                        new AlignedFitting(EWeaponAlignment.BROADSIDE, Objects.requireNonNull(beam), 6),
                        new AlignedFitting(EWeaponAlignment.STERN, Objects.requireNonNull(beam), 1),
                        new AlignedFitting(EWeaponAlignment.BOW, Objects.requireNonNull(pointDefense), 2),
                        new AlignedFitting(EWeaponAlignment.BROADSIDE, Objects.requireNonNull(pointDefense), 4),
                        new AlignedFitting(EWeaponAlignment.STERN, Objects.requireNonNull(pointDefense), 2),

                        new AlignedFitting(EWeaponAlignment.BOW, Objects.requireNonNull(shipKiller).getKey(), 2),
                        new AlignedFitting(EWeaponAlignment.BROADSIDE, Objects.requireNonNull(shipKiller).getKey(), 6),
                        new AlignedFitting(EWeaponAlignment.STERN, Objects.requireNonNull(shipKiller).getKey(), 2),
                        new AlignedFitting(EWeaponAlignment.BOW, Objects.requireNonNull(counterMissile).getKey(), 2),
                        new AlignedFitting(EWeaponAlignment.BROADSIDE, Objects.requireNonNull(counterMissile).getKey(), 4),
                        new AlignedFitting(EWeaponAlignment.STERN, Objects.requireNonNull(counterMissile).getKey(), 2)
                ),
                Set.of(
                        new AmmunitionFitting(shipKiller.getValue(), 160),
                        new AmmunitionFitting(counterMissile.getValue(), 444)
                ),
                Set.of());

        final List<PassiveModule> passiveModules = f.getPassiveModules();
        final PassiveModule freightModule = passiveModules.stream().filter(pa -> pa.getSupportType() == ESupportType.FREIGHT).findFirst().orElseThrow(NullPointerException::new);
        final PassiveModule passengerModule = passiveModules.stream().filter(pa -> pa.getSupportType() == ESupportType.PASSENGER).findFirst().orElseThrow(NullPointerException::new);
        shipClass.setSupportFittings(Set.of(
                new SupportFitting(freightModule, 2),
                new SupportFitting(passengerModule, 2)));

        final Set<ConstraintViolation<ShipClass>> validate = validator.validate(shipClass);
        if (!validate.isEmpty()) {
            throw new NotifyWebUserException("The provided class is not valid.", validate);
        }
        shipClassService.save(shipClass);
        return shipClass;
    }

    private void fit(@Nonnull final ShipClass shipClass,
                     @Nullable final Armor armor,
                     @Nonnull final Propulsion propulsion,
                     @Nullable final ElectronicWarfare electronicWarfare,
                     @Nullable final Sidewall sidewall,
                     @Nonnull final Set<AlignedFitting> fittings,
                     @Nonnull final Set<AmmunitionFitting> ammunitionFittings,
                     @Nonnull final Set<SupportFitting> supportFittings) {
        Preconditions.checkNotNull(shipClass, "shipClass must not be empty");
        Preconditions.checkNotNull(propulsion, "propulsion must not be empty");
        Preconditions.checkNotNull(fittings, "fittings must not be empty");
        Preconditions.checkNotNull(ammunitionFittings, "ammunitionFittings must not be empty");
        Preconditions.checkNotNull(supportFittings, "supportFittings must not be empty");

        shipClass.setArmor(armor);
        shipClass.setSidewall(sidewall);
        shipClass.setPropulsion(propulsion);
        shipClass.setElectronicWarfare(electronicWarfare);
        shipClass.setFittings(fittings);
        shipClass.setAmmunitionFittings(ammunitionFittings);
        shipClass.setSupportFittings(supportFittings);
    }

    private List<Armor> createArmors() {
        final Research research = research("Armor", "A protection of many layers of armor that alternated between ablative composites that absorbed energy from energy weapons and solid anti-kinetic layers.", 18, ETechLevel.TECH_I, null);
        research.getName().updateOrCreate(Translation.SECOND_LANGUAGE, "Panzerung");
        research.getDescription().updateOrCreate(Translation.SECOND_LANGUAGE, "Eine Schutzhülle aus verschiedenen Lagen von Komposit-Panzerung aus Keramik und Durastahl und hitzeabsorbierenden Materialien.");
        researchService.save(research);

        final NamedTechLevel baseModule = moduleService.createBaseModule("Armor",
                "A protection of many layers of armor that alternated between ablative composites that absorbed energy from energy weapons and solid anti-kinetic layers.",
                research, ETechLevel.TECH_I, Armor.class);
        baseModule.getName().updateOrCreate(Translation.SECOND_LANGUAGE, "Panzerung");
        baseModule.getDescription().updateOrCreate(Translation.SECOND_LANGUAGE, "Eine Schutzhülle aus verschiedenen Lagen von Komposit-Panzerung aus Keramik und Durastahl und hitzeabsorbierenden Materialien.");
        moduleService.save(baseModule);

        final List<Armor> result = new ArrayList<>();
        result.add(moduleService.createArmor(baseModule, 1, 3000, 8000, EShipClassType.CL, CrewRequirement.of(S_CREW)));
        result.add(moduleService.createArmor(baseModule, 3, 5000, 25000, EShipClassType.CA, CrewRequirement.of(M_CREW)));
        result.add(moduleService.createArmor(baseModule, 6, 13000, 70000, EShipClassType.BC, CrewRequirement.of(L_CREW)));
        result.add(moduleService.createArmor(baseModule, 9, 28000, 200000, EShipClassType.BB, CrewRequirement.of(XL_CREW)));
        result.add(moduleService.createArmor(baseModule, 13, 190000, 600000, EShipClassType.DN, CrewRequirement.of(XXL_CREW)));
        result.add(moduleService.createArmor(baseModule, 18, 360000, 750000, EShipClassType.SD, CrewRequirement.of(XXXL_CREW)));
        return result;
    }

    private List<Propulsion> createPropulsions() {
        Research research = research("Impeller drive", "The phased array gravity drive, more commonly known as the impeller drive, was the preeminent sub-light propulsion mechanism for space-faring vessels of the post Diaspora era.", 10, ETechLevel.TECH_I, null);
        research.getName().updateOrCreate(Translation.SECOND_LANGUAGE, "Impellerantrieb");
        research.getDescription().updateOrCreate(Translation.SECOND_LANGUAGE, "Ein reaktionsmittelfreier Unterlichtantrieb, der auf der Beeinflussung von Gravitation basiert.");
        researchService.save(research);

        final NamedTechLevel impellerDrive = moduleService.createBaseModule("Impeller drive",
                "The phased array gravity drive, more commonly known as the impeller drive, was the preeminent sub-light propulsion mechanism for space-faring vessels of the post Diaspora era.",
                research, ETechLevel.TECH_I, Propulsion.class);
        impellerDrive.getName().updateOrCreate(Translation.SECOND_LANGUAGE, "Impellerantrieb");
        impellerDrive.getDescription().updateOrCreate(Translation.SECOND_LANGUAGE, "Ein reaktionsmittelfreier Unterlichtantrieb, der auf der Beeinflussung von Gravitation basiert.");
        moduleService.save(impellerDrive);

        final List<Propulsion> result = new ArrayList<>();
        result.add(moduleService.createPropulsion(impellerDrive, 1, 558, 23, EHyperBand.NONE, ETechnologyType.CIVIL));
        result.add(moduleService.createPropulsion(impellerDrive, 2, 558, 31, EHyperBand.NONE, ETechnologyType.MILITARY));

        final NamedTechLevel warshawskiSail = moduleService.createBaseModule("Warshawski-Sail",
                "The Warshawski sail was a gravitic technology, and a key component to interstellar travel in the Post Diaspora era. Allows faster-than-light travel.",
                research, ETechLevel.TECH_I, Propulsion.class);
        warshawskiSail.getName().updateOrCreate(Translation.SECOND_LANGUAGE, "Warshawski-Segel");
        warshawskiSail.getDescription().updateOrCreate(Translation.SECOND_LANGUAGE, "Die Segel sind Teil des Impellerantriebs eines Schiffes und werden von den Alpha-Emittern erzeugt. Sie ermöglichen die Reise mit scheinbarer Überlichtgeschwindigkeit.");
        moduleService.save(warshawskiSail);

        // todo as far as buy ship parts is possible - revert it
        result.add(moduleService.createPropulsion(warshawskiSail, 1,/*1,*/558, 36, EHyperBand.ALPHA, ETechnologyType.CIVIL));
        result.add(moduleService.createPropulsion(warshawskiSail, 1,/*2,*/558, 41, EHyperBand.ALPHA, ETechnologyType.MILITARY));
        result.add(moduleService.createPropulsion(warshawskiSail, 1,/*2,*/558, 39, EHyperBand.BETA, ETechnologyType.CIVIL));
        result.add(moduleService.createPropulsion(warshawskiSail, 1,/*3,*/558, 51, EHyperBand.BETA, ETechnologyType.MILITARY));
        result.add(moduleService.createPropulsion(warshawskiSail, 1,/*2,*/558, 44, EHyperBand.GAMMA, ETechnologyType.CIVIL));
        result.add(moduleService.createPropulsion(warshawskiSail, 1,/*4,*/558, 59, EHyperBand.GAMMA, ETechnologyType.MILITARY));
        result.add(moduleService.createPropulsion(warshawskiSail, 2,/*3,*/558, 52, EHyperBand.DELTA, ETechnologyType.CIVIL));
        result.add(moduleService.createPropulsion(warshawskiSail, 2,/*6,*/558, 62, EHyperBand.DELTA, ETechnologyType.MILITARY));
        result.add(moduleService.createPropulsion(warshawskiSail, 3,/*7,*/558, 68, EHyperBand.EPSILON, ETechnologyType.MILITARY));
        result.add(moduleService.createPropulsion(warshawskiSail, 4,/*8,*/558, 70, EHyperBand.ZETA, ETechnologyType.MILITARY));
        result.add(moduleService.createPropulsion(warshawskiSail, 5,/*9,*/558, 75, EHyperBand.ETA, ETechnologyType.MILITARY));
        result.add(moduleService.createPropulsion(warshawskiSail, 5,/*10,*/ 558, 80, EHyperBand.THETA, ETechnologyType.MILITARY));
        return result;
    }

    private List<ElectronicWarfare> createEloka() {
        final Distance effectiveRange = new Distance(2.669, EDistanceMetric.LS);

        Research research = research("Electronic Warfare", "The electronic warfare combines sensors and emitters for the electromagnetic and gravimetric spectrum.", 10, ETechLevel.TECH_I, null);
        research.getName().updateOrCreate(Translation.SECOND_LANGUAGE, "Elektronische Kriegsführung");
        research.getDescription().updateOrCreate(Translation.SECOND_LANGUAGE, "Maßnahmen zur elektronischen Kriegsführung beinhalten Sensoren und Emitter über das gesamte elektromagnetische und gravimetrische Spektrum.");
        researchService.save(research);

        final NamedTechLevel namedTechLevel = moduleService.createBaseModule("Electronic Warfare",
                "The electronic warfare combines sensors and emitters for the electromagnetic and gravimetric spectrum.",
                research, ETechLevel.TECH_I, ElectronicWarfare.class);
        namedTechLevel.getName().updateOrCreate(Translation.SECOND_LANGUAGE, "Elektronische Kriegsführung");
        namedTechLevel.getDescription().updateOrCreate(Translation.SECOND_LANGUAGE, "Maßnahmen zur elektronischen Kriegsführung beinhalten Sensoren und Emitter über das gesamte elektromagnetische und gravimetrische Spektrum.");
        moduleService.save(namedTechLevel);

        final List<ElectronicWarfare> result = new ArrayList<>();
        result.add(moduleService.createElectronicWarfare(namedTechLevel, 1, 30, 750, EShipClassType.LAC, CrewRequirement.of(XXXS_CREW), effectiveRange));
        result.add(moduleService.createElectronicWarfare(namedTechLevel, 2, 60, 1500, EShipClassType.VT, CrewRequirement.of(XXS_CREW), effectiveRange));
        result.add(moduleService.createElectronicWarfare(namedTechLevel, 3, 70, 1600, EShipClassType.FG, CrewRequirement.of(XS_CREW), effectiveRange));
        result.add(moduleService.createElectronicWarfare(namedTechLevel, 4, 75, 2000, EShipClassType.DD, CrewRequirement.of(S_CREW), effectiveRange));
        result.add(moduleService.createElectronicWarfare(namedTechLevel, 5, 100, 2700, EShipClassType.CL, CrewRequirement.of(M_CREW), effectiveRange));
        result.add(moduleService.createElectronicWarfare(namedTechLevel, 6, 150, 5600, EShipClassType.CA, CrewRequirement.of(M_CREW), effectiveRange));
        result.add(moduleService.createElectronicWarfare(namedTechLevel, 7, 700, 9000, EShipClassType.BC, CrewRequirement.of(L_CREW), effectiveRange));
        result.add(moduleService.createElectronicWarfare(namedTechLevel, 8, 750, 11000, EShipClassType.BB, CrewRequirement.of(XL_CREW), effectiveRange));
        result.add(moduleService.createElectronicWarfare(namedTechLevel, 9, 5000, 210000, EShipClassType.DN, CrewRequirement.of(XXL_CREW), effectiveRange));
        result.add(moduleService.createElectronicWarfare(namedTechLevel, 10, 7000, 350000, EShipClassType.SD, CrewRequirement.of(XXXL_CREW), effectiveRange));
        return result;
    }

    private List<Sidewall> createSidewalls() {
        Research research = research("Sidewall",
                "The sidewall was the main passive protection of a warship against all sorts of weapons fire.", 10, ETechLevel.TECH_I, null);
        amendTranslation(research, "Seitenschild", "Seitenschilde sind die wichtigste passive Verteidigung gegen alle Arten von Waffenfeuer.");
        researchService.save(research);

        final NamedTechLevel namedTechLevel = moduleService.createBaseModule("Sidewall",
                "The sidewall was the main passive protection of a warship against all sorts of weapons fire.",
                research, ETechLevel.TECH_I, Sidewall.class);
        amendTranslation(namedTechLevel, "Seitenschild", "Seitenschilde sind die wichtigste passive Verteidigung gegen alle Arten von Waffenfeuer.");
        moduleService.save(namedTechLevel);

        final List<Sidewall> result = new ArrayList<>();
        result.add(moduleService.createSidewall(namedTechLevel, 1, 1000, 750, EShipClassType.LAC, CrewRequirement.of(XXXS_CREW)));
        result.add(moduleService.createSidewall(namedTechLevel, 2, 6000, 1500, EShipClassType.VT, CrewRequirement.of(XXS_CREW)));
        result.add(moduleService.createSidewall(namedTechLevel, 3, 8000, 1600, EShipClassType.FG, CrewRequirement.of(XS_CREW)));
        result.add(moduleService.createSidewall(namedTechLevel, 4, 11000, 2000, EShipClassType.DD, CrewRequirement.of(S_CREW)));
        result.add(moduleService.createSidewall(namedTechLevel, 5, 15000, 2700, EShipClassType.CL, CrewRequirement.of(M_CREW)));
        result.add(moduleService.createSidewall(namedTechLevel, 6, 21000, 5600, EShipClassType.CA, CrewRequirement.of(M_CREW)));
        result.add(moduleService.createSidewall(namedTechLevel, 7, 60000, 9000, EShipClassType.BC, CrewRequirement.of(L_CREW)));
        result.add(moduleService.createSidewall(namedTechLevel, 8, 75000, 11000, EShipClassType.BB, CrewRequirement.of(XL_CREW)));
        result.add(moduleService.createSidewall(namedTechLevel, 9, 375000, 210000, EShipClassType.DN, CrewRequirement.of(XXL_CREW)));
        result.add(moduleService.createSidewall(namedTechLevel, 10, 700000, 350000, EShipClassType.SD, CrewRequirement.of(XXXL_CREW)));
        return result;
    }

    private void amendTranslation(@Nonnull final HasName hasName, @Nonnull final String name, @Nonnull final String description) {
        Preconditions.checkNotNull(hasName, "hasName must not be empty");
        Preconditions.checkNotNull(name, "name must not be empty");
        Preconditions.checkNotNull(description, "description must not be empty");

        hasName.getName().updateOrCreate(Translation.SECOND_LANGUAGE, name);
        hasName.getDescription().updateOrCreate(Translation.SECOND_LANGUAGE, description);
    }

    private List<Weapon> createWeapons() {
        // broadside weapons must be 'virtually' present twice and are slightly lighter than their chasing counterparts

        Research research = research("Point Defense",
                "Close-range defense includes all of a ship's defense systems that represent the last active line of defense against incoming fire.",
                4, ETechLevel.TECH_I, null);
        amendTranslation(research, "Nahbereichsabwehr",
                "Unter Nahbereichsabwehr fasst man alle Verteidigungssysteme eines Schiffes zusammen, die die letzte aktive Verteidigungslinie gegen einkommenden Beschuss darstellen.");
        researchService.save(research);

        NamedTechLevel namedTechLevel = moduleService.createBaseModule("Point Defense",
                "Close-range defense includes all of a ship's defense systems that represent the last active line of defense against incoming fire.",
                research, ETechLevel.TECH_I, Weapon.class);
        amendTranslation(namedTechLevel, "Nahbereichsabwehr", "Unter Nahbereichsabwehr fasst man alle Verteidigungssysteme eines Schiffes zusammen, die die letzte aktive Verteidigungslinie gegen einkommenden Beschuss darstellen.");
        moduleService.save(namedTechLevel);

        final Distance closePDDistance = new Distance(643, EDistanceMetric.KM);
        final Distance beamDistance = new Distance(1.3343, EDistanceMetric.LS);
        final List<Weapon> result = new ArrayList<>();
        result.add(moduleService.createWeapon(namedTechLevel, 1, 200, 1, EShipClassType.LAC, closePDDistance, 4, EWeaponType.POINT_DEFENSE, CrewRequirement.of(XXXS_CREW)));
        result.add(moduleService.createWeapon(namedTechLevel, 1, 500, 1, EShipClassType.CL, closePDDistance, 12, EWeaponType.POINT_DEFENSE, CrewRequirement.of(XXS_CREW)));

        result.add(moduleService.createWeapon(namedTechLevel, 4, 400, 1, EShipClassType.LAC, beamDistance, 8, EWeaponType.POINT_DEFENSE, CrewRequirement.of(XXXS_CREW)));
        result.add(moduleService.createWeapon(namedTechLevel, 4, 800, 1, EShipClassType.CL, beamDistance, 16, EWeaponType.POINT_DEFENSE, CrewRequirement.of(XXXS_CREW)));

        research = research("Laser",
                "Lasers were the most common ship-mounted energy weapon. Anti-ship lasers had lenses that ranged from several decimeters to over a meter in diameter and operate in the X-ray range.",
                4, ETechLevel.TECH_I, null);
        amendTranslation(research, "Laser", "Ein Laser ist eine künstliche, gerichtete Strahlungsquelle und eine von zwei gebräuchlichen Energiewaffen.");
        researchService.save(research);

        namedTechLevel = moduleService.createBaseModule("Laser",
                "Lasers were the most common ship-mounted energy weapon. Anti-ship lasers had lenses that ranged from several decimeters to over a meter in diameter and operate in the X-ray range.",
                research, ETechLevel.TECH_I, Weapon.class);
        amendTranslation(namedTechLevel, "Laser", "Ein Laser ist eine künstliche, gerichtete Strahlungsquelle und eine von zwei gebräuchlichen Energiewaffen.");
        moduleService.save(namedTechLevel);

        result.add(moduleService.createWeapon(namedTechLevel, 1, 300, 40, EShipClassType.LAC, beamDistance, 1, EWeaponType.BEAM, CrewRequirement.of(XXXS_CREW)));
        result.add(moduleService.createWeapon(namedTechLevel, 1, 350, 65, EShipClassType.LAC, beamDistance, 1, EWeaponType.BEAM, CrewRequirement.of(XXS_CREW)));

        result.add(moduleService.createWeapon(namedTechLevel, 2, 450, 80, EShipClassType.DD, beamDistance, 1, EWeaponType.BEAM, CrewRequirement.of(XXS_CREW)));
        result.add(moduleService.createWeapon(namedTechLevel, 3, 600, 90, EShipClassType.CL, beamDistance, 1, EWeaponType.BEAM, CrewRequirement.of(XS_CREW)));

        result.add(moduleService.createWeapon(namedTechLevel, 4, 800, 110, EShipClassType.CA, beamDistance, 1, EWeaponType.BEAM, CrewRequirement.of(XS_CREW)));
        result.add(moduleService.createWeapon(namedTechLevel, 4, 1500, 160, EShipClassType.CA, beamDistance, 1, EWeaponType.BEAM, CrewRequirement.of(S_CREW)));

        research = research("Graser",
                "Grasers were lasers operating in the gamma ray range. Considered vastly superior in both strength and size when compared to lasers, grasers were often only seen in small numbers in smaller ships, due to their larger mass.",
                6, ETechLevel.TECH_I, research);
        amendTranslation(research, "Graser", "Ein Laser ist eine künstliche, gerichtete Strahlungsquelle und eine von zwei gebräuchlichen Energiewaffen.");
        researchService.save(research);

        namedTechLevel = moduleService.createBaseModule("Graser",
                "Grasers were lasers operating in the gamma ray range. Considered vastly superior in both strength and size when compared to lasers, grasers were often only seen in small numbers in smaller ships, due to their larger mass.",
                research, ETechLevel.TECH_I, Weapon.class);
        amendTranslation(namedTechLevel, "Graser", "Graser sind wie die Laser lichtschnelle Waffen, die aber im Gegensatz zu diesen nicht im Bereich des Lichts, sondern im Bereich der Gamma-Strahlung operieren.");
        moduleService.save(namedTechLevel);

        result.add(moduleService.createWeapon(namedTechLevel, 1, 1900, 200, EShipClassType.CL, beamDistance, 1, EWeaponType.BEAM, CrewRequirement.of(XXXS_CREW)));
        result.add(moduleService.createWeapon(namedTechLevel, 1, 2600, 350, EShipClassType.CL, beamDistance, 1, EWeaponType.BEAM, CrewRequirement.of(XXS_CREW)));
        result.add(moduleService.createWeapon(namedTechLevel, 2, 3100, 400, EShipClassType.CA, beamDistance, 1, EWeaponType.BEAM, CrewRequirement.of(XS_CREW)));
        result.add(moduleService.createWeapon(namedTechLevel, 3, 3600, 500, EShipClassType.BC, beamDistance, 1, EWeaponType.BEAM, CrewRequirement.of(M_CREW)));
        result.add(moduleService.createWeapon(namedTechLevel, 4, 6600, 850, EShipClassType.BB, beamDistance, 1, EWeaponType.BEAM, CrewRequirement.of(M_CREW)));
        result.add(moduleService.createWeapon(namedTechLevel, 5, 9800, 1100, EShipClassType.DN, beamDistance, 1, EWeaponType.BEAM, CrewRequirement.of(L_CREW)));
        result.add(moduleService.createWeapon(namedTechLevel, 6, 12200, 1450, EShipClassType.SD, beamDistance, 1, EWeaponType.BEAM, CrewRequirement.of(L_CREW)));
        return result;
    }

    private Map<Launcher, Missile> createMissiles() {
        // broadside weapons must be 'virtually' present twice

        Research research = research("Missile systems",
                "A missile was a self-propelled guided projectile used as a weapon. By the 20th Century PD, impeller drive-propelled missiles were the most common weapons of naval warfare.",
                10, ETechLevel.TECH_I, null);
        amendTranslation(research, "Raketensysteme", "Eine Rakete ist ein selbst angetriebener Flugkörper, der im Raumkampf hauptsächlich als Waffe eingesetzt wird.");
        researchService.save(research);

        final NamedTechLevel missileNTL = moduleService.createBaseModule("Missile",
                "A missile was a self-propelled guided projectile used as a weapon. By the 20th Century PD, impeller drive-propelled missiles were the most common weapons of naval warfare.",
                research, ETechLevel.TECH_I, Missile.class);
        amendTranslation(missileNTL, "Raketen", "Eine Rakete ist ein selbst angetriebener Flugkörper, der im Raumkampf hauptsächlich als Waffe eingesetzt wird.");
        moduleService.save(missileNTL);

        final NamedTechLevel counterMissileNTL = moduleService.createBaseModule("Counter missile",
                "A missile was a self-propelled guided projectile used as a weapon. By the 20th Century PD, impeller drive-propelled missiles were the most common weapons of naval warfare.",
                research, ETechLevel.TECH_I, Missile.class);
        amendTranslation(counterMissileNTL, "Anti-Raketen", "Eine Rakete ist ein selbst angetriebener Flugkörper, der im Raumkampf hauptsächlich als Waffe eingesetzt wird.");
        moduleService.save(counterMissileNTL);

        final Distance counterProjectionRange = new Distance(1, EDistanceMetric.KM);
        final Acceleration counterAcceleration = new Acceleration(96000, EAccelerationMetric.G);
        final MissileMotor counterMotor = new MissileMotor(30, 80, counterAcceleration);

        final Distance damageProjectionRange = new Distance(40000, EDistanceMetric.KM);
        final Acceleration shipKillerAcceleration = new Acceleration(46000, EAccelerationMetric.G);
        final MissileMotor shipKillerMotor = new MissileMotor(180, 20, shipKillerAcceleration);

        final Missile counterMissile = moduleService.createMissile(counterMissileNTL, 1, 5, 6, EShipClassType.LAC, new Warhead(counterProjectionRange, EWarheadType.COUNTER_MISSILE, 1), counterMotor);
        final Missile lacMissile = moduleService.createMissile(missileNTL, 1, 5, 20, EShipClassType.LAC, new Warhead(damageProjectionRange, EWarheadType.EXPLOSION, 20), shipKillerMotor);
        final Missile ddMissile = moduleService.createMissile(missileNTL, 2, 30, 60, EShipClassType.DD, new Warhead(damageProjectionRange, EWarheadType.EXPLOSION, 100), shipKillerMotor);
        final Missile caMissile = moduleService.createMissile(missileNTL, 4, 70, 80, EShipClassType.CA, new Warhead(damageProjectionRange, EWarheadType.EXPLOSION, 400), shipKillerMotor);
        final Missile dnMissile = moduleService.createMissile(missileNTL, 6, 100, 120, EShipClassType.DN, new Warhead(damageProjectionRange, EWarheadType.EXPLOSION, 800), shipKillerMotor);

        final NamedTechLevel launcherNTL = moduleService.createBaseModule("Missile launcher",
                "Originally, missile tubes merely housed the missile prior to launch. Missiles would receive pre-programmed instructions from their ship, and would then use reaction thrusters to move out beyond the ship's impeller wedge before activating their own. This severely limited the fire rate.",
                research, ETechLevel.TECH_I, Missile.class);
        amendTranslation(launcherNTL, "Raketenwerfer", "Raketenwerfer bestehen in der einfachsten Ausführung aus einem Startrohr, in dem die abschussbereite Rakete gelagert wird, und einer Zielvorrichtung. Bordraketenwerfer von Raumschiffen verfügen normalerweise über ein einzelnes Startrohr mit einem Nachladesystem, mit dem Raketen aus internen Magazinen nachgeladen werden können, bis die Munition erschöpft ist.");
        moduleService.save(launcherNTL);

        final Map<Launcher, Missile> result = new HashMap<>();

        result.put(moduleService.createLauncher(launcherNTL, 1, 350, EShipClassType.LAC, CrewRequirement.of(XXXS_CREW), EWeaponType.COUNTER_MISSILE, Set.of(counterMissile)), counterMissile);
        result.put(moduleService.createLauncher(launcherNTL, 1, 450, EShipClassType.LAC, CrewRequirement.of(XXXS_CREW), EWeaponType.MISSILE, Set.of(lacMissile)), lacMissile);
        result.put(moduleService.createLauncher(launcherNTL, 2, 800, EShipClassType.DD, CrewRequirement.of(XS_CREW), EWeaponType.MISSILE, Set.of(ddMissile)), ddMissile);
        result.put(moduleService.createLauncher(launcherNTL, 4, 1000, EShipClassType.CA, CrewRequirement.of(M_CREW), EWeaponType.MISSILE, Set.of(caMissile)), caMissile);
        result.put(moduleService.createLauncher(launcherNTL, 6, 2600, EShipClassType.DN, CrewRequirement.of(L_CREW), EWeaponType.MISSILE, Set.of(dnMissile)), dnMissile);
        return result;
    }

    private List<PassiveModule> createPassiveModules() {
        Research research = research("Support modules", "Unlocks improvements for combat and non-combat supportive.", 6, ETechLevel.TECH_I, null);
        amendTranslation(research, "Unterstützungsmodule", "Ermöglicht Verbesserungen von Schiffsmodulen.");
        researchService.save(research);

        final List<PassiveModule> result = new ArrayList<>();
        PassiveModule passiveModule = moduleService.createPassiveModule("Freight module", "A simple cargo hold.", research, 1,
                ESupportType.FREIGHT, ECalculationType.ADD, 750, 500, EShipClassType.FR, ETechLevel.TECH_I, CrewRequirement.of(XXS_CREW));
        amendTranslation(passiveModule, "Frachtmodul", "Ein einfaches Frachtmodul.");
        result.add(moduleService.save(passiveModule));

        passiveModule = moduleService.createPassiveModule("Freight module", "A simple cargo hold.", research, 3,
                ESupportType.FREIGHT, ECalculationType.ADD, 2000, 1500, EShipClassType.FR, ETechLevel.TECH_I, CrewRequirement.of(XS_CREW));
        amendTranslation(passiveModule, "Frachtmodul", "Ein einfaches Frachtmodul.");
        result.add(moduleService.save(passiveModule));

        passiveModule = moduleService.createPassiveModule("Freight module", "A simple cargo hold.", research, 5,
                ESupportType.FREIGHT, ECalculationType.ADD, 22000, 15000, EShipClassType.FR, ETechLevel.TECH_I, CrewRequirement.of(S_CREW));
        amendTranslation(passiveModule, "Frachtmodul", "Ein einfaches Frachtmodul.");
        result.add(moduleService.save(passiveModule));

        passiveModule = moduleService.createPassiveModule("Passenger module", "A simple passenger module.", research, 1,
                ESupportType.PASSENGER, ECalculationType.ADD, 300, 25, EShipClassType.FR, ETechLevel.TECH_I, CrewRequirement.of(XXS_CREW));
        amendTranslation(passiveModule, "Passagiermodul", "Ein einfaches Passagiermodul.");
        result.add(moduleService.save(passiveModule));

        passiveModule = moduleService.createPassiveModule("Passenger module", "A simple passenger module.", research, 4,
                ESupportType.PASSENGER, ECalculationType.ADD, 500, 50, EShipClassType.FR, ETechLevel.TECH_I, CrewRequirement.of(XS_CREW));
        amendTranslation(passiveModule, "Passagiermodul", "Ein einfaches Passagiermodul.");
        result.add(moduleService.save(passiveModule));

        passiveModule = moduleService.createPassiveModule("Passenger module", "A simple passenger module.", research, 6,
                ESupportType.PASSENGER, ECalculationType.ADD, 1100, 125, EShipClassType.FR, ETechLevel.TECH_I, CrewRequirement.of(S_CREW));
        amendTranslation(passiveModule, "Passagiermodul", "Ein einfaches Passagiermodul.");
        result.add(moduleService.save(passiveModule));
        return result;
    }

    /**
     * Generates a random int between the given borders.
     *
     * @param min the lower bound
     * @param max the upper bound
     * @return the random number
     */
    private int getRandomInt(final int min, final int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    /**
     * Generates a random double between the given borders.
     *
     * @param min the lower bound
     * @param max the upper bound
     * @return the random number
     */
    @SuppressWarnings("SameParameterValue")
    private double getRandomDouble(final double min, final double max) {
        return ((Math.random() * (max - min)) + min);
    }

    /**
     * Generates a random orbit for an elliptical position.
     * It has an inner boundary to keep a circle around the middle clear.
     *
     * @return the orbit
     */
    private Orbit generatePlanetaryOrbit() {
        final int min = -999;
        final int max = 1001;
        return createOrbit(STAR_RADIUS * 3, min, max, true);
    }

    /**
     * Creates an orbit the given parameters.
     *
     * @param innerCircle    the radius in which no position is valid
     * @param min            the minimum boundary
     * @param max            the maximum boundary
     * @param planetaryOrbit if the generated position must be possible for an acceptable elliptical orbit
     * @return the orbit
     */
    @Nonnull
    @SuppressWarnings("SameParameterValue")
    private Orbit createOrbit(final int innerCircle, int min, int max, final boolean planetaryOrbit) {

        int xCoordinate = getCoordinateWithInnerBound(innerCircle, min, max);

        final double maximumDifference;
        if (planetaryOrbit) {
            // the numeric eccentricity of a planetary orbit with maximal 2 % below or above 1
            final double numEccentricity = getRandomDouble(0.98, 1.02);
            maximumDifference = xCoordinate * numEccentricity;
            min = (int) (xCoordinate - maximumDifference);
            max = (int) (xCoordinate + maximumDifference);
        }
        int yCoordinate = getCoordinateWithInnerBound(innerCircle, min, max);
        final EDistanceMetric distanceMetric = planetaryOrbit ? Planet.PLANET_STANDARD_METRIC : STAR_SYSTEM_STANDARD_METRIC;
        return new Orbit(new Distance(xCoordinate, distanceMetric), new Distance(yCoordinate, distanceMetric));
    }

    /**
     * Creates a value between min and max but not within a rang from zero to inner bound.
     *
     * @param innerBound the radius from zero which is permitted
     * @param min        the min value
     * @param max        the max value
     * @return the first created random value which fits the restrictions
     */
    private int getCoordinateWithInnerBound(final int innerBound, final int min, final int max) {
        int xCoordinate = getRandomInt(min, max);
        while (checkIfRangeInside(xCoordinate, innerBound)) {
            xCoordinate = getRandomInt(min, max);
        }
        return xCoordinate;
    }

    /**
     * Checks if the given position is inside the boundary.
     *
     * @param position the position to check
     * @param boundary the border to check against
     * @return <code>true</code> if the position violates the boundary, <code>false</code> otherwise
     */
    private boolean checkIfRangeInside(final int position, final int boundary) {
        return Math.abs(position) <= Math.abs(boundary);
    }

    protected void createStarSystems(@Nonnull final List<Coords> coords) {
        final Set<StarSystem> newStarSystems = coords.stream().map(coord -> {
            final Orbit orbit = new Orbit(new Distance(coord.getX(), STAR_SYSTEM_STANDARD_METRIC), new Distance(coord.getY(), STAR_SYSTEM_STANDARD_METRIC));
            return starsystemService.createStarSystem(coord.getName(), orbit);
        }).collect(Collectors.toSet());
        LOGGER.info("New star systems generated");

        newStarSystems.forEach(starSystem -> {
            final int randomNumber = getRandomInt(1, 5);
            final List<String> names = resourceService.getRandomPlanetName(randomNumber);
            final List<Orbit> newPlanetaryOrbits = new ArrayList<>();
            for (int i = 0; i < randomNumber; i++) {
                Orbit orbit = generatePlanetaryOrbit();
                while (newPlanetaryOrbits.contains(orbit)) {
                    orbit = generatePlanetaryOrbit();
                }
                newPlanetaryOrbits.add(orbit);
                planetService.createPlanet(names.get(i), starSystem, orbit);
            }
        });
        LOGGER.info("New star systems populated");
    }

    void createForums() {
        final List<Alliance> alliances = allianceService.findAll();

        final List<Forum> toStore = new ArrayList<>();
        final Forum techForum = new Forum(EWebUserRole.USER, "Improvements and releases", "To address questions, features, improvements and ideas.");
        toStore.add(techForum);
        final Forum mainUsersForum = new Forum(EWebUserRole.USER, "Users forum", "A Forum for all users.");
        toStore.add(mainUsersForum);
        final Forum adminsForum = new Forum(EWebUserRole.ADMIN, "Admins forum", "A forum for the game admins.");
        toStore.add(adminsForum);
        final List<Forum> allianceForums = alliances.stream()
                .map(alliance -> new Forum(alliance, alliance.getCode() + " Forum", "The " + alliance.getName() + "'s forum."))
                .collect(Collectors.toList());
        toStore.addAll(allianceForums);

        forumService.saveAll(toStore);
    }

    @Nonnull
    protected Building building(final String name,
                                final String description,
                                final int baseValue,
                                final int amountOfWorkers,
                                final EEducationType educationType,
                                final ETechLevel techLevel,
                                final ProductionType productionType,
                                final Research unlockedBy,
                                final int unlockedThroughLevel,
                                final double increasingFactorPerLevel) {
        return buildingService.createBuilding(name, description, baseValue, techLevel, productionType, educationType, amountOfWorkers, unlockedBy, unlockedThroughLevel, increasingFactorPerLevel);
    }

    @Nonnull
    private OrbitalModule orbitalModule(@Nonnull final String name,
                                        @Nonnull final String description,
                                        final int tonnage,
                                        final int baseValue,
                                        @Nonnull final Map<EEducationType, Long> crew,
                                        @Nonnull final ETechLevel techLevel,
                                        @Nonnull final EModuleType effect,
                                        @Nonnull final Research unlockedBy,
                                        final int unlockedThroughLevel) {
        Preconditions.checkNotNull(name, "name must not be empty");
        Preconditions.checkNotNull(description, "description must not be empty");
        Preconditions.checkNotNull(crew, "crew must not be empty");
        Preconditions.checkNotNull(techLevel, "techLevel must not be empty");
        Preconditions.checkNotNull(effect, "effect must not be empty");
        Preconditions.checkNotNull(unlockedBy, "unlockedBy must not be empty");

        return orbitalModuleService.save(new OrbitalModule(name, description, tonnage, baseValue, new CrewRequirement(crew, EDepositType.COSTS), techLevel, effect, unlockedBy, unlockedThroughLevel));
    }

    @Nonnull
    private OrbitalModule orbitalModule(@Nonnull final String name,
                                        @Nonnull final String description,
                                        final int tonnage,
                                        final int baseValue,
                                        @Nonnull final Map<EEducationType, Long> crew,
                                        @Nonnull final ETechLevel techLevel,
                                        @Nonnull final EResourceType effect,
                                        @Nonnull final Research unlockedBy,
                                        final int unlockedThroughLevel) {
        Preconditions.checkNotNull(name, "name must not be empty");
        Preconditions.checkNotNull(description, "description must not be empty");
        Preconditions.checkNotNull(crew, "crew must not be empty");
        Preconditions.checkNotNull(techLevel, "techLevel must not be empty");
        Preconditions.checkNotNull(effect, "effect must not be empty");
        Preconditions.checkNotNull(unlockedBy, "unlockedBy must not be empty");

        return orbitalModuleService.save(new OrbitalModule(name, description, tonnage, baseValue, new CrewRequirement(crew, EDepositType.COSTS), techLevel, effect, unlockedBy, unlockedThroughLevel));
    }


    private Research research(final String name, final String description, final ETechLevel techLevel, final Research unlockedBy) {
        return researchService.createResearch(name, description, Integer.MAX_VALUE, techLevel, unlockedBy);
    }

    @Nonnull
    protected Research research(final String name, final String description, final int levelCap, final ETechLevel techLevel, final Research unlockedBy) {
        return researchService.createResearch(name, description, levelCap, techLevel, unlockedBy);
    }

    @Nonnull
    protected Fleet createFleet(@Nonnull final Owner user, @Nonnull final Planet planet, @Nonnull final String name) {
        Preconditions.checkNotNull(user, "user must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(name, "name must not be empty");

        final FleetOrbit fleetOrbit = new FleetOrbit(planet);
        final Fleet fleet = new Fleet(name, user, fleetOrbit);
        fleet.setOperational();
        return fleetService.save(fleet);
    }

    @Deprecated(since = "productive environment")
    protected Planet colonizePlanet(@Nonnull final User owner, @Nonnull final Planet planet) {
        // first the guys, then the buildings
        final Colonization colonization = new Colonization(owner, planet, ColonizationCostCalculator.getCrewRequirementForColonization(), 0);
        planetService.save(planet);
        return colonizationService.colonizePlanet(colonization);
    }


    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated(since = "productive environment")
    private void addUnlockedResearches(User user) {
        final List<Research> researchesWithoutPrecondition = researchService.getResearchesWithoutPrecondition();
        for (int i = 1; i <= 1; i++) {
            // add up to all DD-level stuff
            researchService.addResearch(user, researchesWithoutPrecondition);
        }
    }

    @Async("asyncTaskExecutor")
    public void createOpponentAndFightAsync(final User saved) {
        try {
            CompletableFuture.runAsync(() -> {
                createFleetForUser(saved);
                createOpponentFleetForUser(saved);
                runBattleForNewUser(saved);
            }).get();
        } catch (final ExecutionException | InterruptedException e) {
            LOGGER.error("createOpponentAndFightAsync", e);
            throw new NotifyWebUserException(e.getMessage());
        }
    }

    public void createFleetForUser(@Nonnull final Owner user) {
        Preconditions.checkNotNull(user, "user must not be empty");

        final NonPlayerCharacter opponent = nonPlayerCharacterService.findByUsername(DEFEATED_OPPONENT);
        final List<ShipClass> classList = shipClassService.findAllLatestByOwner(Objects.requireNonNull(opponent).getId());
        ShipClass ship = classList.get(0);
        ship = new ShipClass(user, ship);
        ship = shipClassService.save(ship);

        final Planet homePlanet = planetService.findMainPlanet(user);
        final Fleet fleet = createFleet(user, homePlanet, "Homefleet");

        final List<String> randomWarshipName = new ArrayList<>(resourceService.getRandomWarshipName(3));
        WarShip warShip = new WarShip(randomWarshipName.get(0), homePlanet, fleet, ship);
        warShip.setOperational();
        warShipService.save(warShip);
        warShip = new WarShip(randomWarshipName.get(1), homePlanet, fleet, ship);
        warShip.setOperational();
        warShipService.save(warShip);
        warShip = new WarShip(randomWarshipName.get(2), homePlanet, fleet, ship);
        warShip.setOperational();
        warShipService.save(warShip);
    }

    @Nonnull
    public WarShip createOpponentFleetForUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user must not be empty");

        final NonPlayerCharacter opponent = nonPlayerCharacterService.findByUsername(DEFEATED_OPPONENT);
        final List<ShipClass> classList = shipClassService.findAllLatestByOwner(Objects.requireNonNull(opponent).getId());
        final ShipClass ship = classList.get(0);

        final Planet homePlanet = planetService.findMainPlanet(user);
        final Fleet opponentsFleet = createFleet(opponent, homePlanet, "Pirates bane");

        final WarShip warShip = new WarShip("Corsair", homePlanet, opponentsFleet, ship);
        warShip.setOperational();
        return warShipService.save(warShip);
    }

    public void runBattleForNewUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user must not be empty");

        final Tick today = tickService.getToday();
        final Planet homePlanet = planetService.findMainPlanet(user);
        battleService.runBattleAtPlanet(today, homePlanet);
    }

    public static String switchName(@Nonnull final String name) {
        // todo please replace me!
        String result = "";
        result = whenThen("Glyptodon", "A-I-CL", name);
        result = whenThen("Armadillo", "A-I-CA", result);
        result = whenThen("Porcupine", "A-I-BC", result);
        result = whenThen("Seeigel", "A-I-BB", result);
        result = whenThen("Dornenkopf", "A-I-DN", result);
        result = whenThen("Stachelhummer", "A-I-SD", result);

        result = whenThen("Owl", "E-I-LAC", result);
        result = whenThen("Cheetah", "E-I-VT", result);
        result = whenThen("Gargoyle", "E-I-FG", result);
        result = whenThen("Zapper", "E-I-DD", result);
        result = whenThen("Wasteland", "E-I-CL", result);
        result = whenThen("Enclave", "E-I-CA", result);
        result = whenThen("Mojave", "E-I-BC", result);
        result = whenThen("Thumper", "E-I-BB", result);
        result = whenThen("Dragonlance", "E-I-DN", result);
        result = whenThen("Longbottom", "E-I-SD", result);

        result = whenThen("Delta Dart AMM", "L-LACIC", result);
        result = whenThen("FarFire ASM", "L-LACIM", result);
        result = whenThen("Agni ASM", "L-DDIM", result);
        result = whenThen("Holly ASM", "L-CAIM", result);
        result = whenThen("Doombud ASM", "L-DNIM", result);

        result = whenThen("Scoreshot", "D-LACIP", result);
        result = whenThen("StarGuard", "D-CLIP", result);
        result = whenThen("ScatterGun", "D-LACIP", result);
        result = whenThen("CrossBow", "D-CLIP", result);
        result = whenThen("Zeus", "D-LACIB", result);
        result = whenThen("StarFire", "D-LACIB", result);
        result = whenThen("Alacorn", "D-DDIB", result);
        result = whenThen("Ingrid", "D-CLIB", result);
        result = whenThen("ExoStar", "D-CAIB", result);
        result = whenThen("Myrmidon", "D-CAIB", result);
        result = whenThen("Blankenburg", "D-CLIB", result);
        result = whenThen("Martell-X", "D-CLIB", result);
        result = whenThen("Shigunga", "D-CAIB", result);
        result = whenThen("StarSlab", "D-BCIB", result);
        result = whenThen("Padilla", "D-BBIB", result);
        result = whenThen("DavyCrockett", "D-DNIB", result);
        result = whenThen("ChisComp", "D-SDIB", result);

        result = whenThen("Testudo", "S-I-LAC", result);
        result = whenThen("Valiant", "S-I-VT", result);
        result = whenThen("Luxor", "S-I-FG", result);
        result = whenThen("Sentinel", "S-I-DD", result);
        result = whenThen("Sipher", "S-I-CL", result);
        result = whenThen("ArcShield", "S-I-CA", result);
        result = whenThen("Cassius", "S-I-BC", result);
        result = whenThen("Cataphract", "S-I-BB", result);
        result = whenThen("Mycenaean", "S-I-DN", result);
        result = whenThen("Hauberk", "S-I-SD", result);

        result = whenThen("Dart AMM", "M-LAC30-80-I-C", result);
        result = whenThen("Spiculum ASM", "M-LAC180-20-I-E", result);
        result = whenThen("Javelin ASM", "M-DD180-20-I-E", result);
        result = whenThen("Pilum ASM", "M-CA180-20-I-E", result);
        result = whenThen("Manipel ASM", "M-DN180-20-I-E", result);

        result = whenThen("Breen", "P-NI-C", result);
        result = whenThen("CoreTek", "P-NI-M", result);
        result = whenThen("Defiance", "P-AI-C", result);
        result = whenThen("Edasich", "P-AI-M", result);
        result = whenThen("Galas", "P-BI-C", result);
        result = whenThen("Hermes", "P-BI-M", result);
        result = whenThen("Magna", "P-GI-C", result);
        result = whenThen("Pitban", "P-GI-M", result);
        result = whenThen("Shinobi", "P-DI-C", result);
        result = whenThen("Vlar", "P-DI-M", result);
        result = whenThen("Rawlings", "P-EI-M", result);
        result = whenThen("Samarkand", "P-ZI-M", result);
        result = whenThen("Irian", "P-EI-M", result);
        result = whenThen("Kendall", "P-TI-M", result);
        return result;
    }

    private static String whenThen(@Nonnull final String newName, @Nonnull final String oldName, @Nonnull final String name) {
        Preconditions.checkNotNull(newName, "newName must not be empty");
        Preconditions.checkNotNull(oldName, "oldName must not be empty");
        Preconditions.checkNotNull(name, "name must not be empty");

        return name.equals(oldName) ? newName : name;
    }

}
