package de.yuga.spacebattle.backend.services;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.account.forum.Forum;
import de.yuga.spacebattle.backend.entities.account.forum.ForumMessage;
import de.yuga.spacebattle.backend.entities.account.forum.ForumThread;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.buildings.ProductionType;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
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
import de.yuga.spacebattle.backend.entities.spacecrafts.details.AlignedFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.details.AmmunitionFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.details.SupportFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.*;
import de.yuga.spacebattle.backend.entities.turn.Colonization;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.*;
import de.yuga.spacebattle.backend.enums.physics.EAccelerationMetric;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import de.yuga.spacebattle.backend.enums.physics.EHyperBand;
import de.yuga.spacebattle.backend.services.account.ForumService;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.buildings.BuildingService;
import de.yuga.spacebattle.backend.services.combined.account.AllianceService;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.constructables.buildings.ConstructionService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.ShipClassService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.WarShipService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.orbitals.StarSystemService;
import de.yuga.spacebattle.backend.services.researches.ResearchService;
import de.yuga.spacebattle.backend.services.spacecraft.HullService;
import de.yuga.spacebattle.backend.services.spacecraft.ModuleService;
import de.yuga.spacebattle.backend.services.turn.ColonizationService;
import de.yuga.spacebattle.backend.services.turn.TickService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The master of all. Do all the dev-stuff which could be removed or placed somewhere else.
 */
@Service
public class MasterOfTheUniverseService {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MasterOfTheUniverseService.class);

    /**
     * The radius of a star in px which is displayed in the canvas for a star system.
     */
    public static final int STAR_RADIUS = 30;

    /**
     * The radius of a universe center in px which is displayed in the canvas for the universe.
     */
    public static final int UNIVERSE_CENTER_RADIUS = 50;

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
    private final ConstructionService constructionService;

    @Nonnull
    private final FleetService fleetService;

    @Nonnull
    private final WarShipService warShipService;

    @Nonnull
    private final ForumService forumService;

    @Nonnull
    private final ColonizationService colonizationService;

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
                                      @Nonnull final ConstructionService constructionService,
                                      @Nonnull final FleetService fleetService,
                                      @Nonnull final WarShipService warShipService,
                                      @Nonnull final ForumService forumService,
                                      @Nonnull final ColonizationService colonizationService) {
        Preconditions.checkNotNull(tickService, "tickService shouldn't be null!");
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        Preconditions.checkNotNull(allianceService, "allianceService shouldn't be null!");
        Preconditions.checkNotNull(starSystemService, "starSystemService shouldn't be null!");
        Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");
        Preconditions.checkNotNull(buildingService, "buildingService shouldn't be null!");
        Preconditions.checkNotNull(moduleService, "moduleService shouldn't be null!");
        Preconditions.checkNotNull(hullService, "hullService shouldn't be null!");
        Preconditions.checkNotNull(shipClassService, "shipClassService shouldn't be null!");
        Preconditions.checkNotNull(researchService, "researchService shouldn't be null!");
        Preconditions.checkNotNull(constructionService, "constructionService shouldn't be null!");
        Preconditions.checkNotNull(fleetService, "fleetService shouldn't be null!");
        Preconditions.checkNotNull(warShipService, "warShipService shouldn't be null!");
        Preconditions.checkNotNull(forumService, "forumService shouldn't be null!");
        Preconditions.checkNotNull(colonizationService, "colonizationService shouldn't be null!");

        this.tickService = tickService;
        this.userService = userService;
        this.allianceService = allianceService;
        this.starsystemService = starSystemService;
        this.planetService = planetService;
        this.buildingService = buildingService;
        this.moduleService = moduleService;
        this.hullService = hullService;
        this.shipClassService = shipClassService;
        this.researchService = researchService;
        this.constructionService = constructionService;
        this.fleetService = fleetService;
        this.warShipService = warShipService;
        this.forumService = forumService;
        this.colonizationService = colonizationService;
    }

    /**
     * Create the minimal base data for the current stage of development.
     */
    public void createInitialData() {
        List<User> all = userService.findAll();
        if (!all.isEmpty()) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The database was already initialized!");
            return;
        }
        createInitialDataPayload();
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
    private double getRandomDouble(final double min, final double max) {
        return ((Math.random() * (max - min)) + min);
    }

    /**
     * Generates a random orbit position for star system where a elliptical placing is not mandatory.
     * It has an inner boundary to keep a circle around the middle clear.
     *
     * @return the orbit
     */
    private Orbit generateSystemPosition() {
        final int min = -999;
        final int max = 1001;
        return createOrbit(UNIVERSE_CENTER_RADIUS * 3, min, max, false);
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
     * Creates an orbit the the given parameters.
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
        final EDistanceMetric distanceMetric = planetaryOrbit ? Planet.PLANET_STANDARD_METRIC : StarSystem.STAR_SYSTEM_STANDARD_METRIC;
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

    /**
     * Generates up to 100 new star systems with planets.
     */
    @Transactional
    protected void createMorePopularizedStarSystems() {
        final List<StarSystem> allSystems = starsystemService.findAll();
        final Set<Orbit> knownOrbits = allSystems.stream().map(StarSystem::getOrbit).collect(Collectors.toSet());

        final Set<Orbit> created = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            created.add(generateSystemPosition());
        }
        final List<Orbit> newOrbits = new ArrayList<>(created);
        newOrbits.removeAll(knownOrbits);
        final Set<StarSystem> newStarSystems = newOrbits.stream().map(orbit -> {
            final long timeInMillis = Calendar.getInstance().getTimeInMillis();
            return starsystemService.createStarSystem("System-" + timeInMillis + newOrbits.indexOf(orbit), orbit);
        }).collect(Collectors.toSet());
        LOGGER.info("New star systems generated");

        newStarSystems.forEach(starSystem -> {
            final String starSystemName = starSystem.getName();
            final int randomNumber = getRandomInt(0, 5);
            final List<Orbit> newPlanetaryOrbits = new ArrayList<>();
            for (int i = 0; i <= randomNumber; i++) {
                Orbit orbit = generatePlanetaryOrbit();
                while (newPlanetaryOrbits.contains(orbit)) {
                    orbit = generatePlanetaryOrbit();
                }
                newPlanetaryOrbits.add(orbit);
                //noinspection deprecation
                planetService.createPlanet(starSystemName + "-" + i, starSystem, orbit);
            }
        });
        LOGGER.info("New star systems populated");
    }

    void createForums() {
        final List<Alliance> alliances = allianceService.findAll();

        final List<Forum> toStore = new ArrayList<>();
        final Forum mainUsersForum = new Forum(EWebUserRole.USER, "Users forum", "A Forum for all users.");
        toStore.add(mainUsersForum);
        final Forum adminsForum = new Forum(EWebUserRole.ADMIN, "Admins forum", "A forum for the game admins.");
        toStore.add(adminsForum);
        final List<Forum> allianceForums = alliances.stream()
                .map(alliance -> new Forum(alliance, alliance.getCode() + " Forum", "The " + alliance.getName() + "'s forum."))
                .collect(Collectors.toList());
        toStore.addAll(allianceForums);

        forumService.saveAll(toStore);

        final List<User> users = userService.findAll();

        final List<Forum> forums = forumService.findAll();
        forums.forEach(forum -> {
            final User user = users.stream().filter(forum::isUserAllowed).findFirst().orElse(null);
            assert user != null : "The user should be present!";

            final ForumThread forumThread = new ForumThread(forum, "Thread in " + forum.getTitle(), "Description in " + forum.getDescription());
            final ForumThread save = forumService.save(forumThread);
            final ForumMessage forumMessage = new ForumMessage(save, user, "Hello World!");
            forumService.save(forumMessage);
        });


        LOGGER.info("Forums created");
    }

    @SuppressWarnings({"deprecation", "unused"})
    void createInitialDataPayload() {

        final User u1 = userService.createUser("Flashkid", "12457aA!", "mail", EWebUserRole.ADMIN, EGameUserRole.ALLIANCE_ADMIN);
        final User u2 = userService.createUser("Yufiel", "12457aA!", "mail2", EWebUserRole.USER, EGameUserRole.ALLIANCE_ADMIN);
        final User u3 = userService.createUser("Applicant", "12457aA!", "mail3", EWebUserRole.USER);
        LOGGER.info("Users created");

        Alliance a1 = allianceService.createAlliance("Argonauten", "A", u1, u3);
        Alliance a2 = allianceService.createAlliance("111er", "111er", u2);
        LOGGER.info("Alliances created");
        LOGGER.info("Alliances populated.");

        createForums();

        StarSystem s1 = starsystemService.createStarSystem("Argonaut", 150, -75);
        StarSystem s2 = starsystemService.createStarSystem("111", 150, -150);
        LOGGER.info("Star systems created");

        Planet p11 = planetService.createPlanet("Argonauten HQ 1", s1, 1300, -1000);
        Planet p12 = planetService.createPlanet("Argonauten HQ 2", s1, -1300, 1000);
        Planet p13 = planetService.createPlanet("Argonauten HQ 3", s1, -500, -600);
        Planet p14 = planetService.createPlanet("Argonauten HQ 4", s1, 700, 500);

        Planet p21 = planetService.createPlanet("111er HQ 1", s2, 1300, -1000);
        Planet p22 = planetService.createPlanet("111er HQ 2", s2, -1300, 1000);
        Planet p23 = planetService.createPlanet("111er HQ 3", s2, -500, -600);
        Planet p24 = planetService.createPlanet("111er HQ 4", s2, 700, 500);
        LOGGER.info("Planets created");

        createMorePopularizedStarSystems();

        // buildings
        Research livingStuff = research("Eternal live", "How to buy wine.", 1, ETechLevel.TECH_I, null);
        Research unlocksConstructionYard = research("Construction Yard", "The construction yard research researches the construction yard.", 1, ETechLevel.TECH_I, null);
        Research unlocksShipyard = research("Orbitals Construction Yard", "The orbitals Construction Yard research researches the orbitals construction yard.", 1, ETechLevel.TECH_I, null);
        Research unlocksLaboratory = research("Laboratories", "The laboratories research researches laboratories.", 1, ETechLevel.TECH_I, null);
        Research unlocksBank = research("Market place", "The Market place research researches Market places.", 1, ETechLevel.TECH_I, null);
        Research unlocksMetals = research("Metal works", "The Metal works research researches Metal works.", 1, ETechLevel.TECH_I, null);
        Research unlocksMecur = research("Special orbital ores", "The Special orbital ores research researches Special orbital ores.", 1, ETechLevel.TECH_I, unlocksMetals);
        Research unlocksHyperWorks = research("Asynchronous Investigations", "The Asynchronous Investigations research researches Asynchronous Investigations.", 1, ETechLevel.TECH_I, unlocksMecur);

        // modules
        Research unlockLaser = research("Laser", "The Laser research researches ...", 1, ETechLevel.TECH_I, null);
        Research unlockMissiles = research("Missile", "The Missile research researches ...", 1, ETechLevel.TECH_I, null);
        Research unlockCounterMissiles = research("Counter Missile", "The Counter Missile research researches ...", 1, ETechLevel.TECH_I, null);
        Research unlockPointDefense = research("Point Defense", "The point defense research researches ...", 1, ETechLevel.TECH_I, null);
        Research unlockArmor = research("Armor", "The Armor research researches ...", 1, ETechLevel.TECH_I, null);
        Research unlockShield = research("Shield", "The Shield research researches ...", 1, ETechLevel.TECH_I, null);
        Research unlockPropulsion = research("Speed", "The Speed research researches sub light ...", 1, ETechLevel.TECH_I, null);
        Research unlockFTLPropulsion = research("FTL Speed", "The FTL Speed research researches FTL ...", 1, ETechLevel.TECH_I, null);
        Research unlockElectronicWarfare = research("Electronic Warfare", "The EW research researches electronic warfare.", 1, ETechLevel.TECH_I, null);
        Research unlocksRocketAmmunition = research("Rocket Ammunition", "a bunch of rockets.", 1, ETechLevel.TECH_I, null);
        Research unlocksPointDefenseAmmunition = research("Point Defense Ammunition", "a bunch of bullets.", 1, ETechLevel.TECH_I, null);
        Research unlocksCounterRocketAmmunition = research("Counter Rocket Ammunition", "another bunch of rockets.", 1, ETechLevel.TECH_I, null);
        Research unlockPassive = research("Armor improvement I", "Improves the armor improvement module", 1, ETechLevel.TECH_I, null);

        // hulls
        Research unlockHull1 = research("Corvette", "The Corvette research researches Corvettes.", 1, ETechLevel.TECH_I, null);
        Research unlockHull2 = research("Frigate", "The Frigate research researches Frigates.", 1, ETechLevel.TECH_I, null);
        Research unlockHull3 = research("Cruiser", "The Cruiser research researches Cruisers.", 1, ETechLevel.TECH_I, unlockHull2);
        LOGGER.info("Researches created");

        Building constructionYard = building("Construction Yard", "The construction yard construct constructions.", 100, 10, ETechLevel.TECH_I, new ProductionType(EResourceType.CONSTRUCTION, EProductionCategory.PRODUCE, null), EEducationType.COLLEGE, unlocksConstructionYard);
        Building orbitalsConstructionYard = building("Orbitals Construction Yard", "The construction yard construct orbital constructions.", 100, 10, ETechLevel.TECH_I, new ProductionType(EResourceType.ORBITAL_CONSTRUCTION, EProductionCategory.PRODUCE, null), EEducationType.COLLEGE, unlocksShipyard);
        Building researchB = building("Research Laboratories", "The lab investigates researches.", 100, 10, ETechLevel.TECH_I, new ProductionType(EResourceType.RESEARCH, EProductionCategory.PRODUCE, null), EEducationType.UNIVERSITY, unlocksLaboratory);
        Building bank = building("Market place", "The market makes money.", 100, 10, ETechLevel.TECH_I, new ProductionType(EResourceType.CREDITS, EProductionCategory.PRODUCE, null), EEducationType.COLLEGE, unlocksBank);
        Building metalsWorks = building("Metal works", "Metals for progress.", 100, 10, ETechLevel.TECH_I, new ProductionType(EResourceType.METALORE, EProductionCategory.PRODUCE, null), EEducationType.COLLEGE, unlocksMetals);
        Building orbitalOres = building("Special orbital ores", "Heavier metals for more progress.", 100, 10, ETechLevel.TECH_II, new ProductionType(EResourceType.RARE_ELEMENTS, EProductionCategory.PRODUCE, null), EEducationType.UNIVERSITY, unlocksMecur);
        Building investigations = building("Asynchronous Investigations", "Rare elements for the future.", 100, 10, ETechLevel.TECH_III, new ProductionType(EResourceType.HEAVY_METALS, EProductionCategory.PRODUCE, null), EEducationType.UNIVERSITY, unlocksHyperWorks);

        Building livingRoom = building("Living room", "Everyone needs a home", 200, 15, ETechLevel.TECH_I, new ProductionType(EResourceType.POPULATION, EProductionCategory.CAPACITY, null), EEducationType.COLLEGE, livingStuff);
        Building hospital = building("Hospital", "Everyone needs a doctor", 1, 10, ETechLevel.TECH_I, new ProductionType(EResourceType.POPULATION, EProductionCategory.PRODUCE, null), EEducationType.UNIVERSITY, livingStuff);
        Building elementarySchool = building("Elementary schools", "a school", 1000, 10, ETechLevel.TECH_I, new ProductionType(EResourceType.POPULATION, EProductionCategory.REFINEMENT, ERefinementSequence.EDUCATION_CIVIL_I), EEducationType.UNIVERSITY, livingStuff);
        Building secondarySchool = building("Secondary schools", "another school", 1000, 10, ETechLevel.TECH_I, new ProductionType(EResourceType.POPULATION, EProductionCategory.REFINEMENT, ERefinementSequence.EDUCATION_CIVIL_II), EEducationType.UNIVERSITY, livingStuff);
        Building university = building("University", "a university", 1000, 10, ETechLevel.TECH_I, new ProductionType(EResourceType.POPULATION, EProductionCategory.REFINEMENT, ERefinementSequence.EDUCATION_CIVIL_III), EEducationType.UNIVERSITY, livingStuff);
        Building enlistedSchool = building("Teams Rank School", "for the guys which are loud", 200, 10, ETechLevel.TECH_I, new ProductionType(EResourceType.POPULATION, EProductionCategory.REFINEMENT, ERefinementSequence.EDUCATION_MILITARY_I), EEducationType.ENLISTED, livingStuff);
        Building militaryAcademy = building("Military Academy", "for the guys which are silent", 100, 10, ETechLevel.TECH_I, new ProductionType(EResourceType.POPULATION, EProductionCategory.REFINEMENT, ERefinementSequence.EDUCATION_MILITARY_II), EEducationType.OFFICER, livingStuff);
        LOGGER.info("Buildings created");

        colonizePlanet(u1, p11);
        colonizePlanet(u2, p21);
        LOGGER.info("Planets colonized and populated. Constructions were build.");

        Map<EEducationType, Long> militaryCrew = new HashMap<>();
        militaryCrew.put(EEducationType.ENLISTED, 20L);
        militaryCrew.put(EEducationType.OFFICER, 10L);

        Armor armor = moduleService.createArmor("Armor Mk I", "An armor", unlockArmor, 5, 3000, ETechLevel.TECH_I, new CrewRequirement(militaryCrew, EDepositType.COSTS));
        Propulsion propulsion = moduleService.createPropulsion("Speed Mk I", "A drive", unlockPropulsion, 5, 500, ETechLevel.TECH_I, EHyperBand.NONE, new CrewRequirement(militaryCrew, EDepositType.COSTS));
        Propulsion propulsionFTL = moduleService.createPropulsion("FTL Speed Mk I", "A FTL drive", unlockFTLPropulsion, 10, 500, ETechLevel.TECH_I, EHyperBand.DELTA, new CrewRequirement(militaryCrew, EDepositType.COSTS));
        ElectronicWarfare electronicWarfare = moduleService.createElectronicWarfare("Scanner Mk I", "A scanner", unlockElectronicWarfare, 5, 100, new Distance(2.669, EDistanceMetric.LS), ETechLevel.TECH_I, new CrewRequirement(militaryCrew, EDepositType.COSTS));
        Sidewall sidewall = moduleService.createSidewall("Shield Mk I", "A shield", unlockShield, 5, 15000, ETechLevel.TECH_I, new CrewRequirement(militaryCrew, EDepositType.COSTS));

        AmmunitionModule shipKillerAmmunition = moduleService.createAmmunitionModule("Rocket Ammunition", "A bunch of rockets.", unlocksRocketAmmunition, 5, 10, ETechLevel.TECH_I, new CrewRequirement(militaryCrew, EDepositType.COSTS));
        MissileMotor shipKillerMotor = moduleService.createMissileMotor("Ship Killer Motor Mk I", "Ship Killer Motor Mk I", 180, ETechLevel.TECH_I, new Acceleration(46000, EAccelerationMetric.G), 20, 100);
        Warhead nuclearShipKillerWarHead = moduleService.createWarhead("Nuclear ship killer war head", "Nuclear ship killer war head", 1000, ETechLevel.TECH_I, new Distance(0.00017, EDistanceMetric.LS), EWarheadType.EXPLOSION, 100);
        Missile shipKillerMissile = moduleService.createMissile("Nuclear ship killer missile Mk I", "Nuclear ship killer missile Mk I", 100, 100, 100, ETechLevel.TECH_I, nuclearShipKillerWarHead, List.of(shipKillerMotor), unlockMissiles, shipKillerAmmunition);
        Launcher shipKillerLauncher = moduleService.createLauncher("Ship killer launcher Mk I", "The launcher for ship killers", unlockMissiles, shipKillerAmmunition, 100, ETechLevel.TECH_I, EAlignmentType.CHASE_ALIGNMENT, new CrewRequirement(militaryCrew, EDepositType.COSTS), EWeaponType.MISSILE, Set.of(shipKillerMissile));

        AmmunitionModule counterRocketAmmunition = moduleService.createAmmunitionModule("Counter Rocket Ammunition", "Another bunch of rockets.", unlocksCounterRocketAmmunition, 5, 10, ETechLevel.TECH_I, new CrewRequirement(militaryCrew, EDepositType.COSTS));
        MissileMotor counterMissileMotor = moduleService.createMissileMotor("Counter Motor Mk I", "Counter Motor Mk I", 30, ETechLevel.TECH_I, new Acceleration(96000, EAccelerationMetric.G), 80, 10);
        Warhead counterWarHead = moduleService.createWarhead("Counter war head", "Counter war head", 1, ETechLevel.TECH_I, Distance.ZERO, EWarheadType.COUNTER_MISSILE, 10);
        Missile counterMissile = moduleService.createMissile("Counter missile Mk I", "Counter missile Mk I", 10, 10, 10, ETechLevel.TECH_I, counterWarHead, List.of(counterMissileMotor), unlockCounterMissiles, counterRocketAmmunition);
        Launcher counterMissileLauncher = moduleService.createLauncher("Counter missile launcher Mk I", "The launcher for counter missiles", unlockCounterMissiles, counterRocketAmmunition, 100, ETechLevel.TECH_I, EAlignmentType.CHASE_ALIGNMENT, new CrewRequirement(militaryCrew, EDepositType.COSTS), EWeaponType.COUNTER_MISSILE, Set.of(counterMissile));

        Weapon laserWeapon = moduleService.createWeapon("Laser Mk I", "A laser", unlockLaser, 5, 1000, ETechLevel.TECH_I, new Distance(1.3343, EDistanceMetric.LS), 1, EWeaponType.BEAM, EAlignmentType.CHASE_ALIGNMENT, new CrewRequirement(militaryCrew, EDepositType.COSTS));
        Weapon pointDefense = moduleService.createWeapon("Point Defense Mk I", "A point defense", unlockPointDefense, 5, 1, ETechLevel.TECH_I, new Distance(1.3343, EDistanceMetric.LS), 1, EWeaponType.POINT_DEFENSE, EAlignmentType.BATTLE_ALIGNMENT, new CrewRequirement(militaryCrew, EDepositType.COSTS));

        PassiveModule passiveModule = moduleService.createPassiveModule("Improves armor", "Increases the amount of armor", unlockPassive, ESupportType.ARMOR, ECalculationType.ADD, 5, 10, ETechLevel.TECH_I, new CrewRequirement(militaryCrew, EDepositType.COSTS));
        LOGGER.info("Modules created");

        Hull hull1 = hullService.createHull("Corvette vessel", 80000, 50, 15, 15, 35, ETechLevel.TECH_I, "The corvette hull", unlockHull1, EHullType.FG, new CrewRequirement(militaryCrew, EDepositType.COSTS));
        Hull hull2 = hullService.createHull("Frigate vessel", 80000, 100, 35, 35, 55, ETechLevel.TECH_I, "The frigate hull", unlockHull2, EHullType.FG, new CrewRequirement(militaryCrew, EDepositType.COSTS));
        Hull hull3 = hullService.createHull("Cruiser vessel", 80000, 150, 45, 45, 75, ETechLevel.TECH_I, "The cruiser hull", unlockHull3, EHullType.CC, new CrewRequirement(militaryCrew, EDepositType.COSTS));
        LOGGER.info("Hulls created");

        ShipClass as3 = new ShipClass(u1, "Argonauts cruiser", hull3, null);
        as3 = createFitting(armor, propulsionFTL, electronicWarfare, sidewall, laserWeapon, pointDefense, new Launcher[]{shipKillerLauncher, counterMissileLauncher}, new PassiveModule[]{passiveModule}, new AmmunitionModule[]{shipKillerAmmunition, counterRocketAmmunition}, as3);

        ShipClass ers3 = new ShipClass(u2, "111er cruiser", hull3, null);
        ers3 = createFitting(armor, propulsionFTL, electronicWarfare, sidewall, laserWeapon, pointDefense, new Launcher[]{shipKillerLauncher, counterMissileLauncher}, new PassiveModule[]{passiveModule}, new AmmunitionModule[]{shipKillerAmmunition, counterRocketAmmunition}, ers3);
        LOGGER.info("ShipClasses created");

        addUnlockedResearches(u1);
        addUnlockedResearches(u2);
        LOGGER.info("Researches populated");

        Fleet f1 = createFleet(u1, p11, "Argonaut Home Fleet");
        Fleet f2 = createFleet(u2, p21, "111er Home Fleet");
        LOGGER.info("Fleets created");

        warShipService.save(new WarShip("Hotspur", p11, f1, as3));
        warShipService.save(new WarShip("Invictus", p11, f1, as3));
        warShipService.save(new WarShip("Invincible", p11, f1, as3));

        warShipService.save(new WarShip("Argonauts Smasher", p21, f2, ers3));
        warShipService.save(new WarShip("Wild Fire", p21, f2, ers3));
        warShipService.save(new WarShip("Thunder Hawk", p21, f2, ers3));
        LOGGER.info("Warships created");
        LOGGER.info("Fleets populated");

        tickService.doTick();
        LOGGER.info("First tick is done");
        LOGGER.info("All Data created");
    }

    @Nonnull
    protected Building building(final String name,
                                final String description,
                                final int baseValue,
                                final int amountOfWorkers,
                                final ETechLevel techLevel,
                                final ProductionType productionType,
                                final EEducationType educationType,
                                final Research unlockedBy) {
        return buildingService.createBuilding(name, description, baseValue, techLevel, productionType, educationType, amountOfWorkers, unlockedBy);
    }

    @Nonnull
    protected Research research(final String name, final String description, final int levelCap, final ETechLevel techLevel, final Research unlockedBy) {
        return researchService.createResearch(name, description, levelCap, techLevel, unlockedBy);
    }

    @Nonnull
    protected Fleet createFleet(User user, Planet planet, String name) {
        FleetOrbit fo1 = new FleetOrbit(planet.getOrbit(), planet.getSystem());
        Fleet f1 = new Fleet(name, user, fo1);
        return fleetService.save(f1);
    }

    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated(since = "productive environment")
    protected Planet colonizePlanet(@Nonnull final User owner, @Nonnull final Planet planet) {
        // first the guys, then the buildings
        final ResourceDeposit resourceDeposit = planet.getResourceDeposit();
        populateNewColonization(resourceDeposit);

        final Colonization colonization = new Colonization(owner, planet, resourceDeposit.getCrewRequirement(), 0);
        return colonizationService.colonizePlanet(colonization);
    }

    public static void populateNewColonization(@Nonnull final ResourceDeposit resourceDeposit) {
        Preconditions.checkNotNull(resourceDeposit, "resourceDeposit shouldn't be null!");

        resourceDeposit.setAbsolutePopulation(EEducationType.NONE, 200);
        resourceDeposit.setAbsolutePopulation(EEducationType.NONE, 200);
        resourceDeposit.setAbsolutePopulation(EEducationType.SCHOOL, 200);
        resourceDeposit.setAbsolutePopulation(EEducationType.COLLEGE, 500);
        resourceDeposit.setAbsolutePopulation(EEducationType.UNIVERSITY, 1000);
        resourceDeposit.setAbsolutePopulation(EEducationType.ENLISTED, 50);
        resourceDeposit.setAbsolutePopulation(EEducationType.OFFICER, 20);
    }

    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated(since = "productive environment")
    protected ShipClass createFitting(Armor armor,
                                      Propulsion propulsionFTL,
                                      ElectronicWarfare electronicWarfare,
                                      Sidewall sidewall,
                                      Weapon laserWeapon,
                                      Weapon pointDefense,
                                      Launcher[] missiles,
                                      PassiveModule[] passiveModules,
                                      AmmunitionModule[] ammunitionModules,
                                      ShipClass shipClass) {
        shipClass.setArmor(armor);
        shipClass.setSidewall(sidewall);
        shipClass.setPropulsion(propulsionFTL);
        shipClass.setElectronicWarfare(electronicWarfare);
        Set<AlignedFitting> fittings = new HashSet<>();
        fittings.add(new AlignedFitting(EWeaponAlignment.BOW, laserWeapon, 1));
        Arrays.stream(missiles).forEach(missile -> fittings.add(new AlignedFitting(EWeaponAlignment.STERN, missile, 1)));
        fittings.add(new AlignedFitting(EWeaponAlignment.BROADSIDE, pointDefense, 1));
        shipClass.setFittings(fittings);

        Set<SupportFitting> supportFittings = Arrays.stream(passiveModules).map(af -> new SupportFitting(af, 1)).collect(Collectors.toSet());
        shipClass.setSupportFittings(supportFittings);

        Set<AmmunitionFitting> ammunitionFittings = Arrays.stream(ammunitionModules).map(af -> new AmmunitionFitting(af, 1)).collect(Collectors.toSet());
        shipClass.setAmmunitionFittings(ammunitionFittings);

        return shipClassService.save(shipClass);
    }

    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated(since = "productive environment")
    private void addUnlockedResearches(User user) {
        final List<Research> researchesWithoutPrecondition = researchService.getResearchesWithoutPrecondition();
        researchService.addResearch(user, researchesWithoutPrecondition);
    }
}
