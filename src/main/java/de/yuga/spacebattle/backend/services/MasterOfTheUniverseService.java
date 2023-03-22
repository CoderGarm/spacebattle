package de.yuga.spacebattle.backend.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.FittingUtils;
import de.yuga.spacebattle.backend.calculator.colonization.ColonizationCostCalculator;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.account.forum.Forum;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.buildings.ProductionType;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.i18n.Translatable;
import de.yuga.spacebattle.backend.entities.i18n.Translation;
import de.yuga.spacebattle.backend.entities.misc.HasName;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.MissileMotor;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Warhead;
import de.yuga.spacebattle.backend.entities.spacecrafts.fittings.AlignedFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.fittings.AmmunitionFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.fittings.SupportFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.*;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModule;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModuleWithEffectValue;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.NamedTechLevel;
import de.yuga.spacebattle.backend.entities.turn.Colonization;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.enums.*;
import de.yuga.spacebattle.backend.enums.physics.EAccelerationMetric;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import de.yuga.spacebattle.backend.enums.physics.EHyperBand;
import de.yuga.spacebattle.backend.services.account.ForumService;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.buildings.BuildingService;
import de.yuga.spacebattle.backend.services.combined.account.AllianceService;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.ShipClassService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.WarShipService;
import de.yuga.spacebattle.backend.services.i18n.TranslatableService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.orbitals.StarSystemService;
import de.yuga.spacebattle.backend.services.researches.ResearchService;
import de.yuga.spacebattle.backend.services.spacecraft.BattleService;
import de.yuga.spacebattle.backend.services.spacecraft.HullService;
import de.yuga.spacebattle.backend.services.spacecraft.ModuleService;
import de.yuga.spacebattle.backend.services.turn.ColonizationService;
import de.yuga.spacebattle.backend.services.turn.TickService;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
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

    public static final ProductionType CONSTRUCTION_YARD_PT = new ProductionType(EResourceType.CONSTRUCTION, EProductionCategory.PRODUCE, null);
    public static final ProductionType SHIPYARD_PT = new ProductionType(EResourceType.ORBITAL_CONSTRUCTION, EProductionCategory.PRODUCE, null);
    public static final ProductionType RESEARCH_LAB_PT = new ProductionType(EResourceType.RESEARCH, EProductionCategory.PRODUCE, null);
    public static final ProductionType MARKET_PT = new ProductionType(EResourceType.CREDITS, EProductionCategory.PRODUCE, null);
    public static final ProductionType METAL_WORKS = new ProductionType(EResourceType.METALORE, EProductionCategory.PRODUCE, null);
    public static final ProductionType HEAVY_METALS_WORK_PT = new ProductionType(EResourceType.RARE_ELEMENTS, EProductionCategory.PRODUCE, null);
    public static final ProductionType RARE_ELEMENTS_PT = new ProductionType(EResourceType.HEAVY_METALS, EProductionCategory.PRODUCE, null);
    public static final ProductionType LIVING_PT = new ProductionType(EResourceType.POPULATION, EProductionCategory.CAPACITY, null);
    public static final ProductionType DOCTOR_PT = new ProductionType(EResourceType.POPULATION, EProductionCategory.PRODUCE, null);
    public static final ProductionType ELEMENTARY_SCHOOL_PT = new ProductionType(EResourceType.POPULATION, EProductionCategory.REFINEMENT, ERefinementSequence.EDUCATION_CIVIL_I);
    public static final ProductionType SECONDARY_SCHOOL_PT = new ProductionType(EResourceType.POPULATION, EProductionCategory.REFINEMENT, ERefinementSequence.EDUCATION_CIVIL_II);
    public static final ProductionType UNIVERSITY_PT = new ProductionType(EResourceType.POPULATION, EProductionCategory.REFINEMENT, ERefinementSequence.EDUCATION_CIVIL_III);
    public static final ProductionType MILITARY_I_PT = new ProductionType(EResourceType.POPULATION, EProductionCategory.REFINEMENT, ERefinementSequence.EDUCATION_MILITARY_I);
    public static final ProductionType MILITARY_II_PT = new ProductionType(EResourceType.POPULATION, EProductionCategory.REFINEMENT, ERefinementSequence.EDUCATION_MILITARY_II);

    private final static Map<EEducationType, Long> XXXS_CREW = Map.of(
            EEducationType.ENLISTED, 1L);

    private final static Map<EEducationType, Long> XXS_CREW = Map.of(
            EEducationType.ENLISTED, 3L);

    private final static Map<EEducationType, Long> XS_CREW = Map.of(
            EEducationType.ENLISTED, 5L,
            EEducationType.OFFICER, 1L);

    private final static Map<EEducationType, Long> S_CREW = Map.of(
            EEducationType.ENLISTED, 8L,
            EEducationType.OFFICER, 2L);

    private final static Map<EEducationType, Long> M_CREW = Map.of(
            EEducationType.ENLISTED, 12L,
            EEducationType.OFFICER, 3L);

    private final static Map<EEducationType, Long> L_CREW = Map.of(
            EEducationType.ENLISTED, 18L,
            EEducationType.OFFICER, 6L);

    private final static Map<EEducationType, Long> XL_CREW = Map.of(
            EEducationType.ENLISTED, 20L,
            EEducationType.OFFICER, 9L);

    private final static Map<EEducationType, Long> XXL_CREW = Map.of(
            EEducationType.ENLISTED, 300L,
            EEducationType.OFFICER, 100L);

    private final static Map<EEducationType, Long> XXXL_CREW = Map.of(
            EEducationType.ENLISTED, 500L,
            EEducationType.OFFICER, 180L);

    public static final String FLASHKID = "Flashkid";

    @Nonnull
    private final Validator validator;

    @Nonnull
    private final TickService tickService;

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
    private final HullService hullService;

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
    private final TranslatableService translatableService;

    @Nonnull
    private final ResourceService resourceService;

    @Autowired
    public MasterOfTheUniverseService(@Nonnull final TickService tickService,
                                      @Nonnull final UserService userService,
                                      @Nonnull final AllianceService allianceService,
                                      @Nonnull final StarSystemService starSystemService,
                                      @Nonnull final PlanetService planetService,
                                      @Nonnull final BuildingService buildingService,
                                      @Nonnull final ModuleService moduleService,
                                      @Nonnull final HullService hullService,
                                      @Nonnull final ShipClassService shipClassService,
                                      @Nonnull final ResearchService researchService,
                                      @Nonnull final FleetService fleetService,
                                      @Nonnull final WarShipService warShipService,
                                      @Nonnull final ForumService forumService,
                                      @Nonnull final ColonizationService colonizationService,
                                      @Nonnull final BattleService battleService,
                                      @Nonnull final TranslatableService translatableService,
                                      @Nonnull final ResourceService resourceService) {
        this.tickService = Preconditions.checkNotNull(tickService, "tickService shouldn't be null!");
        this.userService = Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        this.allianceService = Preconditions.checkNotNull(allianceService, "allianceService shouldn't be null!");
        this.starsystemService = Preconditions.checkNotNull(starSystemService, "starSystemService shouldn't be null!");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");
        this.buildingService = Preconditions.checkNotNull(buildingService, "buildingService shouldn't be null!");
        this.moduleService = Preconditions.checkNotNull(moduleService, "moduleService shouldn't be null!");
        this.hullService = Preconditions.checkNotNull(hullService, "hullService shouldn't be null!");
        this.shipClassService = Preconditions.checkNotNull(shipClassService, "shipClassService shouldn't be null!");
        this.researchService = Preconditions.checkNotNull(researchService, "researchService shouldn't be null!");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService shouldn't be null!");
        this.warShipService = Preconditions.checkNotNull(warShipService, "warShipService shouldn't be null!");
        this.forumService = Preconditions.checkNotNull(forumService, "forumService shouldn't be null!");
        this.colonizationService = Preconditions.checkNotNull(colonizationService, "colonizationService shouldn't be null!");
        this.battleService = Preconditions.checkNotNull(battleService, "battleService must not be empty");
        this.translatableService = Preconditions.checkNotNull(translatableService, "translatableService must not be empty");
        this.resourceService = Preconditions.checkNotNull(resourceService, "resourceService must not be empty");
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @PostConstruct
    @SuppressWarnings("ConstantConditions")
    public void transform() {
        validateUniverse();
        LOGGER.info("---------------------------- transforming the universe ----------------------------");
        final boolean transformationNeeded = false;
        if (transformationNeeded) {
            LOGGER.info("---------------------------- done transforming -------------------------------");
        } else {
            LOGGER.info("---------------------------- nothing to transform ----------------------------");
        }
    }

    private void validateUniverse() {
        LOGGER.info("---------------------------- validating the universe -----------------------------");
        final boolean initiationNeeded = tickService.findAll().isEmpty();
        if (initiationNeeded) {
            LOGGER.info("---------------------------- creating the universe ----------------------------");
            createInitialDataPayload();
            LOGGER.info("---------------------------- done creating ------------------------------------");
        }
        LOGGER.info("---------------------------- done validating --------------------------------------");
    }

    @SuppressWarnings({"unused"})
    void createInitialDataPayload() {

        Research unlocksConstructionYard = research("Construction Yard", "The construction yard research researches the construction yard.", 1, ETechLevel.TECH_I, null);
        Building constructionYard = building("Construction Yard", "The construction yard construct constructions.", 100, 10, EEducationType.COLLEGE, ETechLevel.TECH_I, CONSTRUCTION_YARD_PT, unlocksConstructionYard);

        Research unlocksShipyard = research("Orbitals Construction Yard", "The orbitals Construction Yard research researches the orbitals construction yard.", 1, ETechLevel.TECH_I, null);
        Building orbitalsConstructionYard = building("Orbitals Construction Yard", "The construction yard construct orbital constructions.", 100, 10, EEducationType.COLLEGE, ETechLevel.TECH_I, SHIPYARD_PT, unlocksShipyard);

        Research unlocksLaboratory = research("Laboratories", "The laboratories research researches laboratories.", 1, ETechLevel.TECH_I, null);
        Building researchB = building("Research Laboratories", "The lab investigates researches.", 10, 10, EEducationType.UNIVERSITY, ETechLevel.TECH_I, RESEARCH_LAB_PT, unlocksLaboratory);

        Research unlocksBank = research("Market place", "The Market place research researches Market places.", 1, ETechLevel.TECH_I, null);
        Building bank = building("Market place", "The market makes money.", 100, 10, EEducationType.COLLEGE, ETechLevel.TECH_I, MARKET_PT, unlocksBank);

        Research unlocksMetals = research("Metal works", "The Metal works research researches Metal works.", 1, ETechLevel.TECH_I, null);
        Building metalsWorks = building("Metal works", "Metals for progress.", 250, 10, EEducationType.COLLEGE, ETechLevel.TECH_I, METAL_WORKS, unlocksMetals);

        Research unlocksMecur = research("Special orbital ores", "The Special orbital ores research researches Special orbital ores.", 1, ETechLevel.TECH_I, unlocksMetals);
        Building orbitalOres = building("Special orbital ores", "Heavier metals for more progress.", 200, 10, EEducationType.UNIVERSITY, ETechLevel.TECH_II, HEAVY_METALS_WORK_PT, unlocksMecur);

        Research unlocksHyperWorks = research("Asynchronous Investigations", "The Asynchronous Investigations research researches Asynchronous Investigations.", 1, ETechLevel.TECH_I, unlocksMecur);
        Building investigations = building("Asynchronous Investigations", "Rare elements for the future.", 100, 10, EEducationType.UNIVERSITY, ETechLevel.TECH_III, RARE_ELEMENTS_PT, unlocksHyperWorks);

        Research livingStuff = research("Eternal live", "How to buy wine.", 1, ETechLevel.TECH_I, null);
        Building livingRoom = building("Living room", "Everyone needs a home", 1000, 15, EEducationType.COLLEGE, ETechLevel.TECH_I, LIVING_PT, livingStuff);
        Building hospital = building("Hospital", "Everyone needs a doctor", 50, 10, EEducationType.UNIVERSITY, ETechLevel.TECH_I, DOCTOR_PT, livingStuff);
        Building elementarySchool = building("Elementary schools", "a school", 100, 10, EEducationType.UNIVERSITY, ETechLevel.TECH_I, ELEMENTARY_SCHOOL_PT, livingStuff);
        Building secondarySchool = building("Secondary schools", "another school", 100, 10, EEducationType.UNIVERSITY, ETechLevel.TECH_I, SECONDARY_SCHOOL_PT, livingStuff);
        Building university = building("University", "a university", 100, 10, EEducationType.UNIVERSITY, ETechLevel.TECH_I, UNIVERSITY_PT, livingStuff);
        Building enlistedSchool = building("Teams Rank School", "for the guys which are loud", 50, 10, EEducationType.ENLISTED, ETechLevel.TECH_I, MILITARY_I_PT, livingStuff);
        Building militaryAcademy = building("Military Academy", "for the guys which are silent", 50, 10, EEducationType.OFFICER, ETechLevel.TECH_I, MILITARY_II_PT, livingStuff);
        LOGGER.info("Buildings created");

        //noinspection OptionalGetWithoutIsPresent
        final User flashkid = userService.findByUsername(FLASHKID).get().getUser();
        final User pirate = userService.createUser(DEFEATED_OPPONENT, "12457aA!", "mail3", EWebUserRole.USER);
        LOGGER.info("Users created");

        final Alliance a1 = allianceService.createAlliance("Argonauten", "A", flashkid);
        LOGGER.info("Alliance created");

        createForums();
        LOGGER.info("Forums created");

        final List<Coords> coords = resourceService.readStarSystems();
        createStarSystems(coords);
        final List<StarSystem> starSystems = starsystemService.findAll();

        StarSystem s1 = starSystems.stream().filter(s -> s.getName().equals("Manticore")).findFirst().orElseThrow(() -> new NotifyWebUserException("The star systems should be present."));
        LOGGER.info("Star systems created");

        final Planet p11 = new ArrayList<>(s1.getPlanets()).get(0);
        LOGGER.info("Planets created");

        colonizePlanet(flashkid, p11);
        LOGGER.info("Planets colonized and populated. Constructions were build.");

        createArmors();
        LOGGER.info("armors created");

        createPropulsions();
        LOGGER.info("propulsions created");

        createEloka();
        LOGGER.info("eloka created");

        createSidewalls();
        LOGGER.info("sidewall created");

        createWeapons();
        LOGGER.info("weapons created");

        createMissiles();
        LOGGER.info("missiles created");

        createPassiveModules();
        LOGGER.info("support modules created");
        LOGGER.info("Modules created");

        createHulls();
        LOGGER.info("Hulls created");

        addUnlockedResearches(flashkid);
        LOGGER.info("Researches populated");

        createShipClass(flashkid, EHullType.CA);
        createShipClass(pirate, EHullType.CL);
        LOGGER.info("ShipClass created");

        createFleetForUser(flashkid);
        createOpponentFleetForUser(flashkid);
        LOGGER.info("Fleets created");
        LOGGER.info("Warships created");
        LOGGER.info("Fleets populated");

        amendTranslations();
        LOGGER.info("Translations amended.");

        tickService.doTick();
        LOGGER.info("First tick is done");
        LOGGER.info("All Data created");
    }

    private void createArmors() {
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

        moduleService.createArmor(baseModule, 1, 3000, 3, EHullType.CL);
        moduleService.createArmor(baseModule, 3, 5000, 5, EHullType.CA);
        moduleService.createArmor(baseModule, 6, 13000, 8, EHullType.BC);
        moduleService.createArmor(baseModule, 9, 28000, 9, EHullType.BB);
        moduleService.createArmor(baseModule, 13, 190000, 10, EHullType.DN);
        moduleService.createArmor(baseModule, 18, 360000, 12, EHullType.SD);

    }

    private void createPropulsions() {
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

        moduleService.createPropulsion(impellerDrive, 1, 558, 7, EHyperBand.NONE, ETechnologyType.CIVIL);
        moduleService.createPropulsion(impellerDrive, 2, 558, 8, EHyperBand.NONE, ETechnologyType.MILITARY);

        final NamedTechLevel warshawskiSail = moduleService.createBaseModule("Warshawski-Sail",
                "The Warshawski sail was a gravitic technology, and a key component to interstellar travel in the Post Diaspora era. Allows faster-than-light travel.",
                research, ETechLevel.TECH_I, Propulsion.class);
        warshawskiSail.getName().updateOrCreate(Translation.SECOND_LANGUAGE, "Warshawski-Segel");
        warshawskiSail.getDescription().updateOrCreate(Translation.SECOND_LANGUAGE, "Die Segel sind Teil des Impellerantriebs eines Schiffes und werden von den Alpha-Emittern erzeugt. Sie ermöglichen die Reise mit scheinbarer Überlichtgeschwindigkeit.");
        moduleService.save(warshawskiSail);

        moduleService.createPropulsion(warshawskiSail, 1, 558, 13, EHyperBand.ALPHA, ETechnologyType.CIVIL);
        moduleService.createPropulsion(warshawskiSail, 2, 558, 16, EHyperBand.ALPHA, ETechnologyType.MILITARY);
        moduleService.createPropulsion(warshawskiSail, 2, 558, 14, EHyperBand.BETA, ETechnologyType.CIVIL);
        moduleService.createPropulsion(warshawskiSail, 3, 558, 17, EHyperBand.BETA, ETechnologyType.MILITARY);
        moduleService.createPropulsion(warshawskiSail, 2, 558, 15, EHyperBand.GAMMA, ETechnologyType.CIVIL);
        moduleService.createPropulsion(warshawskiSail, 4, 558, 18, EHyperBand.GAMMA, ETechnologyType.MILITARY);
        moduleService.createPropulsion(warshawskiSail, 3, 558, 16, EHyperBand.DELTA, ETechnologyType.CIVIL);
        moduleService.createPropulsion(warshawskiSail, 6, 558, 19, EHyperBand.DELTA, ETechnologyType.MILITARY);
        moduleService.createPropulsion(warshawskiSail, 7, 558, 20, EHyperBand.EPSILON, ETechnologyType.MILITARY);
        moduleService.createPropulsion(warshawskiSail, 8, 558, 21, EHyperBand.ZETA, ETechnologyType.MILITARY);
        moduleService.createPropulsion(warshawskiSail, 9, 558, 22, EHyperBand.ETA, ETechnologyType.MILITARY);
        moduleService.createPropulsion(warshawskiSail, 10, 558, 23, EHyperBand.THETA, ETechnologyType.MILITARY);
    }

    private void createEloka() {
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

        moduleService.createElectronicWarfare(namedTechLevel, 1, 30, 3, EHullType.LAC, effectiveRange);
        moduleService.createElectronicWarfare(namedTechLevel, 2, 60, 4, EHullType.VT, effectiveRange);
        moduleService.createElectronicWarfare(namedTechLevel, 3, 70, 5, EHullType.FG, effectiveRange);
        moduleService.createElectronicWarfare(namedTechLevel, 4, 75, 6, EHullType.DD, effectiveRange);
        moduleService.createElectronicWarfare(namedTechLevel, 5, 100, 7, EHullType.CL, effectiveRange);
        moduleService.createElectronicWarfare(namedTechLevel, 6, 150, 8, EHullType.CA, effectiveRange);
        moduleService.createElectronicWarfare(namedTechLevel, 7, 700, 9, EHullType.BC, effectiveRange);
        moduleService.createElectronicWarfare(namedTechLevel, 8, 750, 9, EHullType.BB, effectiveRange);
        moduleService.createElectronicWarfare(namedTechLevel, 9, 5000, 10, EHullType.DN, effectiveRange);
        moduleService.createElectronicWarfare(namedTechLevel, 10, 7000, 10, EHullType.SD, effectiveRange);
    }

    private void createSidewalls() {
        Research research = research("Sidewall",
                "The sidewall was the main passive protection of a warship against all sorts of weapons fire.", 10, ETechLevel.TECH_I, null);
        amendTranslation(research, "Seitenschild", "Seitenschilde sind die wichtigste passive Verteidigung gegen alle Arten von Waffenfeuer.");
        researchService.save(research);

        final NamedTechLevel namedTechLevel = moduleService.createBaseModule("Sidewall",
                "The sidewall was the main passive protection of a warship against all sorts of weapons fire.",
                research, ETechLevel.TECH_I, Sidewall.class);
        amendTranslation(namedTechLevel, "Seitenschild", "Seitenschilde sind die wichtigste passive Verteidigung gegen alle Arten von Waffenfeuer.");
        moduleService.save(namedTechLevel);

        moduleService.createSidewall(namedTechLevel, 1, 1000, 2, EHullType.LAC);
        moduleService.createSidewall(namedTechLevel, 2, 6000, 3, EHullType.VT);
        moduleService.createSidewall(namedTechLevel, 3, 8000, 4, EHullType.FG);
        moduleService.createSidewall(namedTechLevel, 4, 11000, 5, EHullType.DD);
        moduleService.createSidewall(namedTechLevel, 5, 15000, 6, EHullType.CL);
        moduleService.createSidewall(namedTechLevel, 6, 21000, 7, EHullType.CA);
        moduleService.createSidewall(namedTechLevel, 7, 60000, 8, EHullType.BC);
        moduleService.createSidewall(namedTechLevel, 8, 75000, 9, EHullType.BB);
        moduleService.createSidewall(namedTechLevel, 9, 375000, 10, EHullType.DN);
        moduleService.createSidewall(namedTechLevel, 10, 700000, 11, EHullType.SD);
    }

    private void amendTranslation(@Nonnull final HasName hasName, @Nonnull final String name, @Nonnull final String description) {
        Preconditions.checkNotNull(hasName, "hasName must not be empty");
        Preconditions.checkNotNull(name, "name must not be empty");
        Preconditions.checkNotNull(description, "description must not be empty");

        hasName.getName().updateOrCreate(Translation.SECOND_LANGUAGE, name);
        hasName.getDescription().updateOrCreate(Translation.SECOND_LANGUAGE, description);
    }

    private void createWeapons() {
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

        final Distance closePDDistance = new Distance(0.6343, EDistanceMetric.LS);
        final Distance beamDistance = new Distance(1.3343, EDistanceMetric.LS);
        moduleService.createWeapon(namedTechLevel, 1, 1, 1, EHullType.LAC, closePDDistance, 2, EWeaponType.POINT_DEFENSE, new CrewRequirement(XXXS_CREW, EDepositType.COSTS));
        moduleService.createWeapon(namedTechLevel, 1, 2, 1, EHullType.LAC, closePDDistance, 4, EWeaponType.POINT_DEFENSE, new CrewRequirement(XXXS_CREW, EDepositType.COSTS));

        moduleService.createWeapon(namedTechLevel, 4, 1, 1, EHullType.LAC, beamDistance, 3, EWeaponType.POINT_DEFENSE, new CrewRequirement(XXXS_CREW, EDepositType.COSTS));
        moduleService.createWeapon(namedTechLevel, 4, 2, 1, EHullType.LAC, beamDistance, 6, EWeaponType.POINT_DEFENSE, new CrewRequirement(XXXS_CREW, EDepositType.COSTS));

        moduleService.createWeapon(namedTechLevel, 1, 2, 1, EHullType.CL, closePDDistance, 6, EWeaponType.POINT_DEFENSE, new CrewRequirement(XXS_CREW, EDepositType.COSTS));
        moduleService.createWeapon(namedTechLevel, 1, 3, 1, EHullType.CL, closePDDistance, 12, EWeaponType.POINT_DEFENSE, new CrewRequirement(XXS_CREW, EDepositType.COSTS));

        moduleService.createWeapon(namedTechLevel, 4, 2, 1, EHullType.CL, beamDistance, 10, EWeaponType.POINT_DEFENSE, new CrewRequirement(XXXS_CREW, EDepositType.COSTS));
        moduleService.createWeapon(namedTechLevel, 4, 3, 1, EHullType.CL, beamDistance, 16, EWeaponType.POINT_DEFENSE, new CrewRequirement(XXXS_CREW, EDepositType.COSTS));

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

        moduleService.createWeapon(namedTechLevel, 1, 1, 75, EHullType.LAC, beamDistance, 1, EWeaponType.BEAM, new CrewRequirement(XXXS_CREW, EDepositType.COSTS));
        moduleService.createWeapon(namedTechLevel, 1, 2, 50, EHullType.LAC, beamDistance, 1, EWeaponType.BEAM, new CrewRequirement(XXXS_CREW, EDepositType.COSTS));

        moduleService.createWeapon(namedTechLevel, 2, 3, 85, EHullType.DD, beamDistance, 1, EWeaponType.BEAM, new CrewRequirement(XXS_CREW, EDepositType.COSTS));
        moduleService.createWeapon(namedTechLevel, 2, 5, 70, EHullType.DD, beamDistance, 1, EWeaponType.BEAM, new CrewRequirement(XS_CREW, EDepositType.COSTS));

        moduleService.createWeapon(namedTechLevel, 3, 5, 115, EHullType.CL, beamDistance, 1, EWeaponType.BEAM, new CrewRequirement(XS_CREW, EDepositType.COSTS));
        moduleService.createWeapon(namedTechLevel, 3, 8, 90, EHullType.CL, beamDistance, 1, EWeaponType.BEAM, new CrewRequirement(S_CREW, EDepositType.COSTS));

        moduleService.createWeapon(namedTechLevel, 4, 9, 160, EHullType.CA, beamDistance, 1, EWeaponType.BEAM, new CrewRequirement(S_CREW, EDepositType.COSTS));
        moduleService.createWeapon(namedTechLevel, 4, 16, 110, EHullType.CA, beamDistance, 1, EWeaponType.BEAM, new CrewRequirement(M_CREW, EDepositType.COSTS));

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

        moduleService.createWeapon(namedTechLevel, 1, 9, 350, EHullType.CL, beamDistance, 1, EWeaponType.BEAM, new CrewRequirement(XS_CREW, EDepositType.COSTS));
        moduleService.createWeapon(namedTechLevel, 1, 14, 200, EHullType.CL, beamDistance, 1, EWeaponType.BEAM, new CrewRequirement(S_CREW, EDepositType.COSTS));

        moduleService.createWeapon(namedTechLevel, 2, 11, 400, EHullType.CA, beamDistance, 1, EWeaponType.BEAM, new CrewRequirement(S_CREW, EDepositType.COSTS));
        moduleService.createWeapon(namedTechLevel, 2, 18, 240, EHullType.CA, beamDistance, 1, EWeaponType.BEAM, new CrewRequirement(M_CREW, EDepositType.COSTS));

        moduleService.createWeapon(namedTechLevel, 3, 21, 500, EHullType.BC, beamDistance, 1, EWeaponType.BEAM, new CrewRequirement(M_CREW, EDepositType.COSTS));
        moduleService.createWeapon(namedTechLevel, 3, 36, 350, EHullType.BC, beamDistance, 1, EWeaponType.BEAM, new CrewRequirement(L_CREW, EDepositType.COSTS));

        moduleService.createWeapon(namedTechLevel, 4, 37, 850, EHullType.BB, beamDistance, 1, EWeaponType.BEAM, new CrewRequirement(M_CREW, EDepositType.COSTS));
        moduleService.createWeapon(namedTechLevel, 4, 64, 500, EHullType.BB, beamDistance, 1, EWeaponType.BEAM, new CrewRequirement(L_CREW, EDepositType.COSTS));

        moduleService.createWeapon(namedTechLevel, 5, 51, 1100, EHullType.DN, beamDistance, 1, EWeaponType.BEAM, new CrewRequirement(L_CREW, EDepositType.COSTS));
        moduleService.createWeapon(namedTechLevel, 5, 98, 750, EHullType.DN, beamDistance, 1, EWeaponType.BEAM, new CrewRequirement(XL_CREW, EDepositType.COSTS));

        moduleService.createWeapon(namedTechLevel, 6, 75, 1450, EHullType.SD, beamDistance, 1, EWeaponType.BEAM, new CrewRequirement(L_CREW, EDepositType.COSTS));
        moduleService.createWeapon(namedTechLevel, 6, 122, 900, EHullType.SD, beamDistance, 1, EWeaponType.BEAM, new CrewRequirement(XL_CREW, EDepositType.COSTS));
    }

    private void createMissiles() {
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

        final Missile counterMissile = moduleService.createMissile(counterMissileNTL, 1, 5, 5, EHullType.LAC, new Warhead(counterProjectionRange, EWarheadType.COUNTER_MISSILE, 1), counterMotor);
        final Missile lacMissile = moduleService.createMissile(missileNTL, 1, 5, 5, EHullType.LAC, new Warhead(damageProjectionRange, EWarheadType.EXPLOSION, 20), shipKillerMotor);
        final Missile ddMissile = moduleService.createMissile(missileNTL, 2, 30, 20, EHullType.DD, new Warhead(damageProjectionRange, EWarheadType.EXPLOSION, 100), shipKillerMotor);
        final Missile caMissile = moduleService.createMissile(missileNTL, 4, 70, 50, EHullType.CA, new Warhead(damageProjectionRange, EWarheadType.EXPLOSION, 400), shipKillerMotor);
        final Missile dnMissile = moduleService.createMissile(missileNTL, 6, 100, 80, EHullType.DN, new Warhead(damageProjectionRange, EWarheadType.EXPLOSION, 800), shipKillerMotor);

        final NamedTechLevel launcherNTL = moduleService.createBaseModule("Missile launcher",
                "Originally, missile tubes merely housed the missile prior to launch. Missiles would receive pre-programmed instructions from their ship, and would then use reaction thrusters to move out beyond the ship's impeller wedge before activating their own. This severely limited the fire rate.",
                research, ETechLevel.TECH_I, Missile.class);
        amendTranslation(launcherNTL, "Raketenwerfer", "Raketenwerfer bestehen in der einfachsten Ausführung aus einem Startrohr, in dem die abschussbereite Rakete gelagert wird, und einer Zielvorrichtung. Bordraketenwerfer von Raumschiffen verfügen normalerweise über ein einzelnes Startrohr mit einem Nachladesystem, mit dem Raketen aus internen Magazinen nachgeladen werden können, bis die Munition erschöpft ist.");
        moduleService.save(launcherNTL);

        moduleService.createLauncher(launcherNTL, 1, 1, EHullType.LAC, new CrewRequirement(XXXS_CREW, EDepositType.COSTS), EWeaponType.COUNTER_MISSILE, Set.of(counterMissile));
        moduleService.createLauncher(launcherNTL, 1, 2, EHullType.LAC, new CrewRequirement(XXS_CREW, EDepositType.COSTS), EWeaponType.COUNTER_MISSILE, Set.of(counterMissile));

        moduleService.createLauncher(launcherNTL, 1, 2, EHullType.LAC, new CrewRequirement(XXXS_CREW, EDepositType.COSTS), EWeaponType.MISSILE, Set.of(lacMissile));
        moduleService.createLauncher(launcherNTL, 1, 4, EHullType.LAC, new CrewRequirement(XXS_CREW, EDepositType.COSTS), EWeaponType.MISSILE, Set.of(lacMissile));

        moduleService.createLauncher(launcherNTL, 2, 7, EHullType.DD, new CrewRequirement(XXS_CREW, EDepositType.COSTS), EWeaponType.MISSILE, Set.of(ddMissile));
        moduleService.createLauncher(launcherNTL, 2, 13, EHullType.DD, new CrewRequirement(XS_CREW, EDepositType.COSTS), EWeaponType.MISSILE, Set.of(ddMissile));

        moduleService.createLauncher(launcherNTL, 4, 15, EHullType.CA, new CrewRequirement(S_CREW, EDepositType.COSTS), EWeaponType.MISSILE, Set.of(caMissile));
        moduleService.createLauncher(launcherNTL, 4, 27, EHullType.CA, new CrewRequirement(M_CREW, EDepositType.COSTS), EWeaponType.MISSILE, Set.of(caMissile));

        moduleService.createLauncher(launcherNTL, 6, 41, EHullType.DN, new CrewRequirement(M_CREW, EDepositType.COSTS), EWeaponType.MISSILE, Set.of(dnMissile));
        moduleService.createLauncher(launcherNTL, 6, 76, EHullType.DN, new CrewRequirement(M_CREW, EDepositType.COSTS), EWeaponType.MISSILE, Set.of(dnMissile));
    }

    private void createPassiveModules() {
        Research unlocksPassive = research("Armor improvement I", "Improves the armor improvement module", 1, ETechLevel.TECH_I, null);
        PassiveModule passiveModule = moduleService.createPassiveModule("Armor increasement Mk I", "Increases the armor value.", unlocksPassive,
                ESupportType.ARMOR, ECalculationType.ADD, 10, 10, EHullType.CL, ETechLevel.TECH_I, new CrewRequirement(XS_CREW, EDepositType.COSTS));
        passiveModule.getName().updateOrCreate(Translation.SECOND_LANGUAGE, "Verstärkung der Panzerung Mk I");
        passiveModule.getDescription().updateOrCreate(Translation.SECOND_LANGUAGE, "Verstärkt die Panzerung.");
        moduleService.save(passiveModule);

        passiveModule = moduleService.createPassiveModule("Sidewall increasement Mk I", "Increases the sidewall value.", unlocksPassive,
                ESupportType.ARMOR, ECalculationType.ADD, 10, 6, EHullType.FR, ETechLevel.TECH_I, new CrewRequirement(S_CREW, EDepositType.COSTS));
        passiveModule.getName().updateOrCreate(Translation.SECOND_LANGUAGE, "Verstärkung des Seitenschilds Mk I");
        passiveModule.getDescription().updateOrCreate(Translation.SECOND_LANGUAGE, "Verstärkt den Seitenschild.");
        moduleService.save(passiveModule);

        passiveModule = moduleService.createPassiveModule("Sidewall increasement Mk I", "Increases the sidewall value.", unlocksPassive,
                ESupportType.SIDEWALL, ECalculationType.ADD, 10, 6, EHullType.FR, ETechLevel.TECH_I, new CrewRequirement(S_CREW, EDepositType.COSTS));
        passiveModule.getName().updateOrCreate(Translation.SECOND_LANGUAGE, "Verstärkung des Seitenschilds Mk I");
        passiveModule.getDescription().updateOrCreate(Translation.SECOND_LANGUAGE, "Verstärkt den Seitenschild.");
        moduleService.save(passiveModule);

        passiveModule = moduleService.createPassiveModule("Electronic warfare increasement Mk I", "Increases the electronic warfare value.", unlocksPassive,
                ESupportType.ELECTRONIC_WARFARE, ECalculationType.ADD, 2, 5, EHullType.FR, ETechLevel.TECH_I, new CrewRequirement(S_CREW, EDepositType.COSTS));
        passiveModule.getName().updateOrCreate(Translation.SECOND_LANGUAGE, "Verstärkung der Eloka Mk I");
        passiveModule.getDescription().updateOrCreate(Translation.SECOND_LANGUAGE, "Verstärkt die Eloka.");
        moduleService.save(passiveModule);
    }

    private void createHulls() {
        Research research = research("Corvette", "The Corvette research researches Corvettes.", 1, ETechLevel.TECH_I, null);
        amendTranslation(research, "Raketen", "Eine Rakete ist ein selbst angetriebener Flugkörper, der im Raumkampf hauptsächlich als Waffe eingesetzt wird.");
        researchService.save(research);

        final NamedTechLevel ntl = moduleService.createBaseModule("Missile",
                "A missile was a self-propelled guided projectile used as a weapon. By the 20th Century PD, impeller drive-propelled missiles were the most common weapons of naval warfare.",
                research, ETechLevel.TECH_I, Hull.class);
        amendTranslation(ntl, "Raketen", "Eine Rakete ist ein selbst angetriebener Flugkörper, der im Raumkampf hauptsächlich als Waffe eingesetzt wird.");
        moduleService.save(ntl);

        hullService.createHull(ntl, "Small Freighter hull", 1, 2000, 2000, 0, 0, 0, EHullType.FR, new CrewRequirement(S_CREW, EDepositType.COSTS));
        hullService.createHull(ntl, "Big Freighter hull", 1, 5000, 5000, 0, 0, 0, EHullType.FR, new CrewRequirement(M_CREW, EDepositType.COSTS));

        hullService.createHull(ntl, "Light attack vessel", 1, 20, 4, 10, 3, 3, EHullType.LAC, new CrewRequirement(S_CREW, EDepositType.COSTS));
        hullService.createHull(ntl, "Corvette vessel", 1, 30, 6, 5, 5, 14, EHullType.VT, new CrewRequirement(M_CREW, EDepositType.COSTS));
        hullService.createHull(ntl, "Frigate vessel", 1, 50, 18, 6, 6, 20, EHullType.FG, new CrewRequirement(M_CREW, EDepositType.COSTS));
        hullService.createHull(ntl, "Destroyer vessel", 1, 65, 24, 8, 8, 24, EHullType.DD, new CrewRequirement(M_CREW, EDepositType.COSTS));


        hullService.createHull(ntl, "Cruiser vessel", 1, 80, 30, 10, 10, 30, EHullType.CL, new CrewRequirement(M_CREW, EDepositType.COSTS));
        hullService.createHull(ntl, "Heavy cruiser vessel", 1, 200, 60, 30, 30, 80, EHullType.CA, new CrewRequirement(L_CREW, EDepositType.COSTS));
        hullService.createHull(ntl, "Battlecruiser vessel", 1, 700, 280, 90, 90, 240, EHullType.BC, new CrewRequirement(XXL_CREW, EDepositType.COSTS));
        hullService.createHull(ntl, "Battleship vessel", 1, 2000, 800, 200, 200, 800, EHullType.BB, new CrewRequirement(XXXL_CREW, EDepositType.COSTS));
        hullService.createHull(ntl, "Dreadnought vessel", 1, 5000, 1900, 400, 400, 2300, EHullType.DN, new CrewRequirement(XXXL_CREW, EDepositType.COSTS));
        hullService.createHull(ntl, "Superdreadnought vessel", 1, 8000, 3320, 640, 640, 3400, EHullType.SD, new CrewRequirement(XXL_CREW, EDepositType.COSTS));
    }

    private ShipClass createShipClass(@Nonnull final User user, @Nonnull final EHullType hullType) {
        Preconditions.checkNotNull(user, "user must not be empty");
        Preconditions.checkNotNull(hullType, "hullType must not be empty");

        final boolean shipIsPresent = shipClassService.findAllLatestByOwner(user).stream()
                .map(ShipClass::getHull)
                .filter(Objects::nonNull)
                .map(Hull::getHullType)
                .anyMatch(h -> h == hullType);
        if (shipIsPresent) {
            throw new NotifyWebUserException("You already have your starter ship. Don't be greedy!");
        }

        final List<Hull> byHullType = hullService.findByHullType(hullType);
        final Hull hull = byHullType.stream()
                .sorted(Comparator.comparingInt(Hull::getOverallConstructionCapacity))
                .reduce((o1, o2) -> o1)
                .orElseThrow(() -> new NotifyWebUserException("There was no hull found for your request."));
        final String randomWarshipName = resourceService.getRandomWarshipName();
        return createFitting(new ShipClass(user, randomWarshipName, hull, null));
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
            final Orbit orbit = new Orbit(new Distance(coord.x, STAR_SYSTEM_STANDARD_METRIC), new Distance(coord.y, STAR_SYSTEM_STANDARD_METRIC));
            return starsystemService.createStarSystem(coord.name, orbit);
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

    private void amendTranslations() {
        final String de = "de";
        final List<Translatable> all = translatableService.findAll();
        all.forEach(tr -> {
            final String inEN = tr.getTranslation(Translation.DEFAULT_LANGUAGE);
            final String inDe = tr.getTranslation(de);
            if (StringUtils.isBlank(inDe)) {
                final String germanTranslation = translatableService.provideTranslation(inEN);
                if (StringUtils.isBlank(germanTranslation)) {
                    throw new NotifyWebUserException("Oh this must not happen. There is something missing");
                }
                tr.updateOrCreate(de, germanTranslation);
            }
        });
        translatableService.saveAll(all);
    }

    @Nonnull
    protected Building building(final String name,
                                final String description,
                                final int baseValue,
                                final int amountOfWorkers,
                                final EEducationType educationType,
                                final ETechLevel techLevel,
                                final ProductionType productionType,
                                final Research unlockedBy) {
        return buildingService.createBuilding(name, description, baseValue, techLevel, productionType, educationType, amountOfWorkers, unlockedBy);
    }

    @Nonnull
    protected Research research(final String name, final String description, final int levelCap, final ETechLevel techLevel, final Research unlockedBy) {
        return researchService.createResearch(name, description, levelCap, techLevel, unlockedBy);
    }

    @Nonnull
    protected Fleet createFleet(User user, Planet planet, String name) {
        FleetOrbit fleetOrbit = new FleetOrbit(planet.getOrbit(), planet.getSystem());
        Fleet fleet = new Fleet(name, user, fleetOrbit);
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

    @SuppressWarnings("ConstantConditions")
    protected ShipClass createFitting(@Nonnull final ShipClass shipClass) {
        Preconditions.checkNotNull(shipClass, "shipClass must not be empty");

        final Hull hull = shipClass.getHull();
        assert hull != null : "If this is wrong, then everything is broken!";
        int cc = hull.getConstructionCapacity();
        int ccBow = hull.getConstructionCapacityBow();
        int ccStern = hull.getConstructionCapacityStern();
        int ccBroadsides = hull.getConstructionCapacityBroadsides();
        final EHullType hullType = hull.getHullType();

        final Armor armor = moduleService.findAllArmors().stream()
                .filter(a -> hullType.suitsHullType(a.getHullType()))
                .reduce((o1, o2) -> o2).orElse(null);

        final Sidewall sidewall = moduleService.findAllSidewalls().stream()
                .filter(e -> hullType.suitsHullType(e.getHullType()))
                .reduce((o1, o2) -> o2).orElse(null);

        final ElectronicWarfare electronicWarfare = moduleService.findAllElectronicWarfare().stream()
                .filter(e -> hullType.suitsHullType(e.getHullType()))
                .reduce((o1, o2) -> o2).orElse(null);

        final Propulsion propulsion = moduleService.findAllPropulsions().stream()
                .filter(p -> p.getHyperBand() == EHyperBand.ALPHA && p.getTechnologyType() == ETechnologyType.MILITARY)
                .findFirst().orElse(null);

        final List<Weapon> weapons = moduleService.findAllWeapons();

        final Weapon beam = weapons.stream().filter(w2 -> w2.getWeaponType() == EWeaponType.BEAM && hullType.suitsHullType(w2.getHullType()))
                .sorted(Comparator.comparingInt(Weapon::getDamageValue))
                .reduce((o1, o2) -> o2)
                .orElse(null);

        final Weapon pd = weapons.stream().filter(w -> w.getWeaponType() == EWeaponType.POINT_DEFENSE && hullType.suitsHullType(w.getHullType()))
                .reduce((o1, o2) -> o2)
                .orElse(null);

        final List<Launcher> allLaunchers = moduleService.findAllLaunchers();
        final Launcher missile = allLaunchers.stream().filter(w1 -> w1.getWeaponType() == EWeaponType.MISSILE && hullType.suitsHullType(w1.getHullType()))
                .sorted(Comparator.comparingLong(l -> l.getHeaviestMissile().getDamageValue()))
                .reduce((o1, o2) -> o2)
                .orElse(null);

        final Launcher counter = allLaunchers.stream().filter(w -> w.getWeaponType() == EWeaponType.COUNTER_MISSILE && hullType.suitsHullType(w.getHullType()))
                .reduce((o1, o2) -> o2)
                .orElse(null);

        final List<PassiveModule> passiveModules = sortByValue(sortByValue(moduleService.findAllPassiveModules(), hullType), hullType);

        cc -= propulsion.getUseCapacity(hull);
        shipClass.setPropulsion(propulsion);

        if (armor != null) {
            cc -= armor.getUseCapacity(hull);
            shipClass.setArmor(armor);
        }

        if (sidewall != null) {
            cc -= sidewall.getUseCapacity(hull);
            shipClass.setSidewall(sidewall);
        }

        if (electronicWarfare != null) {
            cc -= electronicWarfare.getUseCapacity(hull);
            shipClass.setElectronicWarfare(electronicWarfare);
        }

        final Set<AlignedFitting> fittings = new HashSet<>();
        for (final EWeaponAlignment alignment : EWeaponAlignment.values()) {

            int presentCapacity;
            switch (alignment) {
                default:
                case BROADSIDE:
                    presentCapacity = ccBroadsides;
                    break;
                case BOW:
                    presentCapacity = ccBow;
                    break;
                case STERN:
                    presentCapacity = ccStern;
                    break;
            }

            int amountBeam = 0;
            int amountMissile = 0;
            int amountPD = 0;
            int amountCounter = 0;
            boolean hasChanged;
            while (presentCapacity >= 0) {
                hasChanged = false;
                if (presentCapacity >= beam.getUseCapacity()) {
                    amountBeam++;
                    presentCapacity -= beam.getUseCapacity();
                    hasChanged = true;
                }
                if (presentCapacity >= missile.getUseCapacity()) {
                    amountMissile++;
                    presentCapacity -= missile.getUseCapacity();
                    hasChanged = true;
                }
                if (presentCapacity >= counter.getUseCapacity()) {
                    amountCounter++;
                    presentCapacity -= counter.getUseCapacity();
                    hasChanged = true;
                }
                if (presentCapacity >= pd.getUseCapacity()) {
                    amountPD++;
                    presentCapacity -= pd.getUseCapacity();
                    hasChanged = true;
                }
                if (!hasChanged) {
                    break;
                }
            }
            if (amountBeam > 0) {
                fittings.add(new AlignedFitting(alignment, beam, amountBeam));
            }
            if (amountMissile > 0) {
                fittings.add(new AlignedFitting(alignment, missile, amountMissile));
            }
            if (amountCounter > 0) {
                fittings.add(new AlignedFitting(alignment, counter, amountCounter));
            }
            if (amountPD > 0) {
                fittings.add(new AlignedFitting(alignment, pd, amountPD));
            }
        }

        if (fittings.isEmpty()) {
            throw new NotifyWebUserException("You must fight, bitch!");
        }
        shipClass.setFittings(fittings);

        final Map<Launcher, Integer> attackMissilesToAmount = fittings.stream()
                .filter(FittingUtils.ATTACK_MISSILES)
                .collect(Collectors.groupingBy(AlignedFitting::getLauncher,
                        Collectors.mapping(AlignedFitting::getAmount, Collectors.reducing(0, Integer::sum))));

        final Map<Launcher, Integer> counterMissilesToAmount = fittings.stream()
                .filter(FittingUtils.COUNTER_MISSILES)
                .collect(Collectors.groupingBy(AlignedFitting::getLauncher,
                        Collectors.mapping(AlignedFitting::getAmount, Collectors.reducing(0, Integer::sum))));

        final Integer amountAttack = attackMissilesToAmount.values().stream().reduce(0, Integer::sum);
        final Integer amountCounter = counterMissilesToAmount.values().stream().reduce(0, Integer::sum);

        final int fullAmount = amountAttack + amountCounter;
        final double piece = (double) cc / (double) fullAmount;
        double freeCapForAttackAmmo = piece * amountAttack;
        double freeCapForCounterAmmo = piece * amountCounter;

        cc = addAmmunition(shipClass, cc, attackMissilesToAmount, freeCapForAttackAmmo);
        cc = addAmmunition(shipClass, cc, counterMissilesToAmount, freeCapForCounterAmmo);

        final int shotsLoad = shipClass.getAmmunitionFittings().stream().map(AmmunitionFitting::getAmount).reduce(0, Integer::sum);
        if (shotsLoad == 0) {
            throw new NotifyWebUserException("You must fight, bitch! Take a stone!");
        }

        final int neededCapacityForSupport = passiveModules.stream().map(PassiveModule::getUseCapacity).reduce(0, Integer::sum);
        final int amountOfSupportSets = (cc / neededCapacityForSupport) - 1;
        if (amountOfSupportSets > 0) {
            final Set<SupportFitting> supportFittings = passiveModules.stream().map(af -> new SupportFitting(af, amountOfSupportSets)).collect(Collectors.toSet());
            shipClass.setSupportFittings(supportFittings);
        }

        final Set<ConstraintViolation<ShipClass>> validate = validator.validate(shipClass);
        if (!validate.isEmpty()) {
            throw new NotifyWebUserException("The provided class is not valid.", validate);
        }

        return shipClassService.save(shipClass);
    }

    private int addAmmunition(@Nonnull final ShipClass shipClass,
                              final int capacity,
                              @Nonnull final Map<Launcher, Integer> launcherToLauncherAmount,
                              double freeCapForAmmo) {
        Preconditions.checkNotNull(shipClass, "shipClass must not be empty");
        Preconditions.checkNotNull(launcherToLauncherAmount, "launcherToLauncherAmount must not be empty");

        int capClone = capacity;

        final double capacityForSingleShotFromAllLaunchers = launcherToLauncherAmount.entrySet().stream().map(l -> {
            final Missile missile = new ArrayList<>(l.getKey().getAllowedMissiles()).get(0);
            final Integer amountOfLaunchers = l.getValue();
            //noinspection UnnecessaryLocalVariable
            final double capacityForSingleShot = missile.getUsedCapacity() * amountOfLaunchers;
            return capacityForSingleShot;
        }).reduce(0D, Double::sum);

        final int shotsAvailablePerLauncher = (int) ((freeCapForAmmo) / capacityForSingleShotFromAllLaunchers);

        capClone -= (shotsAvailablePerLauncher * capacityForSingleShotFromAllLaunchers);

        launcherToLauncherAmount.forEach((launcher, amountOfLauncher) -> {
            final Missile missile = new ArrayList<>(launcher.getAllowedMissiles()).get(0);
            shipClass.addAmmunitionFitting(new AmmunitionFitting(missile, amountOfLauncher * shotsAvailablePerLauncher));
        });
        return capClone;
    }

    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated(since = "productive environment")
    private void addUnlockedResearches(User user) {
        final List<Research> researchesWithoutPrecondition = researchService.getResearchesWithoutPrecondition();
        researchService.addResearch(user, researchesWithoutPrecondition);
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
            e.printStackTrace();
            throw new NotifyWebUserException(e.getMessage());
        }
    }

    public void createFleetForUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user must not be empty");

        final List<ShipClass> classList = shipClassService.findAllByOwner(user);
        ShipClass ship = classList.stream()
                .filter(s -> s.getHull() != null)
                .sorted(Comparator.comparingInt(o -> o.getHull().getOverallConstructionCapacity()))
                .reduce((o1, o2) -> o2).orElse(null);
        if (ship == null) {
            ship = createShipClass(user, EHullType.CA);
        }
        final Planet homePlanet = planetService.findMainPlanet(user);
        final Fleet fleet = createFleet(user, homePlanet, "Homefleet");

        final List<String> randomWarshipName = resourceService.getRandomWarshipName(3);
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

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public WarShip createOpponentFleetForUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user must not be empty");

        final User opponent = userService.findByUsername(DEFEATED_OPPONENT).get().getUser();

        final Planet homePlanet = planetService.findMainPlanet(user);
        final Fleet opponentsFleet = createFleet(opponent, homePlanet, "Pirates bane");

        final List<ShipClass> classList = shipClassService.findAllLatestByOwner(opponent);
        final List<ShipClass> shipClasses = classList.stream()
                .filter(s -> s.getHull() != null)
                .filter(s -> s.getHull().getHullType() == EHullType.CL)
                .sorted(Comparator.comparingInt(o -> o.getHull().getOverallConstructionCapacity()))
                .collect(Collectors.toList());
        final ShipClass ship = shipClasses.stream().reduce((o1, o2) -> o2).orElseThrow(() -> new NotifyWebUserException("No ship class found."));

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

    private <M extends BaseModuleWithEffectValue> List<M> sortByValue(@Nonnull final List<M> modules, @Nonnull final EHullType hullType) {
        Preconditions.checkNotNull(modules, "modules must not be empty");
        Preconditions.checkNotNull(hullType, "hullType must not be empty");

        final List<M> result = getMatchingByHullType(modules, hullType);
        return result.stream().sorted(Comparator.comparingInt(BaseModuleWithEffectValue::getEffectValue)).collect(Collectors.toList());
    }

    @Nonnull
    private static <M extends BaseModule> List<M> getMatchingByHullType(final @Nonnull List<M> modules, final @Nonnull EHullType hullType) {
        final Map<Integer, List<M>> modulesByHullTypeOrdinal = modules.stream().collect(Collectors.groupingBy(m -> m.getHullType().ordinal(),
                Collectors.mapping(m -> m, Collectors.toList())));
        List<M> result = modulesByHullTypeOrdinal.getOrDefault(hullType.ordinal(), new ArrayList<>());
        if (result.isEmpty()) {
            for (int i = hullType.ordinal() - 1; i >= 0; i--) {
                // get from smaller hulls
                result = modulesByHullTypeOrdinal.getOrDefault(i, new ArrayList<>());
                if (!result.isEmpty()) {
                    break;
                }
            }
            if (result.isEmpty()) {
                for (int i = hullType.ordinal() + 1; i < EHullType.values().length; i++) {
                    // get from bigger hulls
                    result = modulesByHullTypeOrdinal.getOrDefault(i, new ArrayList<>());
                    if (!result.isEmpty()) {
                        break;
                    }
                }
            }
        }
        return result;
    }

    public static class CoordsBlob extends ArrayList<MasterOfTheUniverseService.Coords> {

        public CoordsBlob(@Nonnull final List<Coords> coords) {
            Preconditions.checkNotNull(coords, "coords must not be empty");

            super.addAll(coords);
        }
    }


    public static class Coords {
        @JsonProperty
        int x;
        @JsonProperty
        int y;
        @JsonProperty
        String name;

        /**
         * Reads the cartesian coordinates and flips the y-axis in order to display the coords directly to the screen.
         */
        public Coords(final String[] split) {
            this.name = split[0];
            this.x = Integer.parseInt(split[1].replace("x", "").replaceAll(" ", ""));
            this.y = Integer.parseInt(split[2].replace("y", "").replaceAll(" ", "")) * -1;
        }
    }
}
