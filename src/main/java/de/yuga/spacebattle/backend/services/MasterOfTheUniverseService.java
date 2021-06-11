package de.yuga.spacebattle.backend.services;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.details.AlignedFitting;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.details.AmmunitionFitting;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.details.SupportFitting;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.*;
import de.yuga.spacebattle.backend.enums.*;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.buildings.BuildingService;
import de.yuga.spacebattle.backend.services.combined.account.AllianceService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.ShipClassService;
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

import static de.yuga.spacebattle.gui.vaadin.orbitals.starmap.ViewBoxDefinition.STAR_RADIUS;
import static de.yuga.spacebattle.gui.vaadin.orbitals.starmap.ViewBoxDefinition.UNIVERSE_CENTER_RADIUS;

/**
 * The master of all. Do do all the dev-stuff which could be removed or placed somewhere else.
 */
@Service
public class MasterOfTheUniverseService {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MasterOfTheUniverseService.class);

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
        this.colonizationService = colonizationService;
    }

    /**
     * Create the minimal base data for the current stage of development.
     *
     * @return an empty list on a failure
     */
    public ResponseEntity<?> createInitialData() {
        List<User> all = userService.findAll();
        if (!all.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The database was already initialized!");
        }
        createInitialDataPayload();
        all = userService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(all);
    }

    /**
     * Generates a random int between the given borders.
     *
     * @param min the lower bound
     * @param max the upper bound
     * @return the random number
     */
    public int getRandomInt(final int min, final int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    /**
     * Generates a random double between the given borders.
     *
     * @param min the lower bound
     * @param max the upper bound
     * @return the random number
     */
    public double getRandomDouble(final double min, final double max) {
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
        return new Orbit(xCoordinate, yCoordinate);
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
    public void createMorePopularizedStarSystems() {
        final List<StarSystem> allSystems = starsystemService.findAll();
        final Set<Orbit> knownOrbits = allSystems.stream().map(StarSystem::getOrbit).collect(Collectors.toSet());

        final List<Orbit> newOrbits = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            newOrbits.add(generateSystemPosition());
        }
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
                planetService.createPlanet(starSystemName + "-" + i, starSystem, orbit);
            }
        });
        LOGGER.info("New star systems populated");
    }

    @SuppressWarnings({"deprecation", "unused"})
    @Transactional(rollbackFor = Exception.class)
    void createInitialDataPayload() {
        Alliance a1 = allianceService.createAlliance("Argonauten", "A");
        Alliance a2 = allianceService.createAlliance("111er", "111er");
        LOGGER.info("Alliances created");

        final User u1 = userService.createUser("Flashkid", "test", "mail");
        final User u2 = userService.createUser("Yufiel", "test", "mail2");
        LOGGER.info("Users created");

        u1.setAlliance(a1);
        u2.setAlliance(a2);
        userService.save(u1);
        userService.save(u2);
        LOGGER.info("Alliances populated.");

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
        Research unlocksConstructionYard = researchService.createResearch("Construction Yard", "The construction yard research researches the construction yard.", 1, null);
        Research unlocksShipyard = researchService.createResearch("Orbitals Construction Yard", "The orbitals Construction Yard research researches the orbitals construction yard.", 1, null);
        Research unlocksLaboratory = researchService.createResearch("Laboratories", "The laboratories research researches laboratories.", 1, null);
        Research unlocksBank = researchService.createResearch("Market place", "The Market place research researches Market places.", 1, null);
        Research unlocksMetals = researchService.createResearch("Metal works", "The Metal works research researches Metal works.", 1, null);
        Research unlocksMecur = researchService.createResearch("Special orbital ores", "The Special orbital ores research researches Special orbital ores.", 1, unlocksMetals);
        Research unlocksHyperworks = researchService.createResearch("Asynchronous Investigations", "The Asynchronous Investigations research researches Asynchronous Investigations.", 1, unlocksMecur);

        // modules
        Research unlockLaser = researchService.createResearch("Laser", "The Laser research researches ...", 1, null);
        Research unlockMissiles = researchService.createResearch("Missile", "The Missile research researches ...", 1, null);
        Research unlockCounterMissiles = researchService.createResearch("Counter Missile", "The Counter Missile research researches ...", 1, null);
        Research unlockPointDefense = researchService.createResearch("Point Defense", "The point defense research researches ...", 1, null);
        Research unlockArmor = researchService.createResearch("Armor", "The Armor research researches ...", 1, null);
        Research unlockShield = researchService.createResearch("Shield", "The Shield research researches ...", 1, null);
        Research unlockPropulsion = researchService.createResearch("Speed", "The Speed research researches sub light ...", 1, null);
        Research unlockFTLPropulsion = researchService.createResearch("FTL Speed", "The FTL Speed research researches FTL ...", 1, null);
        Research unlockElectronicWarfare = researchService.createResearch("Electronic Warfare", "The ElWa research researches electronic warfare.", 1, null);
        Research unlocksRocketAmmunition = researchService.createResearch("Rocket Ammunition", "a bunch of rockets.", 1, null);
        Research unlocksPointDefenseAmmunition = researchService.createResearch("Point Defense Ammunition", "a bunch of bullets.", 1, null);
        Research unlocksCounterRocketAmmunition = researchService.createResearch("Counter Rocket Ammunition", "another bunch of rockets.", 1, null);
        Research unlockPassive = researchService.createResearch("Armor improvement I", "Improves the armor improvement module", 1, null);

        // hulls
        Research unlockHull1 = researchService.createResearch("Corvette", "The Corvette research researches Corvettes.", 1, null);
        Research unlockHull2 = researchService.createResearch("Frigate", "The Frigate research researches Frigates.", 1, null);
        Research unlockHull3 = researchService.createResearch("Cruiser", "The Cruiser research researches Cruisers.", 1, unlockHull2);
        LOGGER.info("Researches created");

        Building constructionYard = buildingService.createBuilding("Construction Yard", "The construction yard construct constructions.", 10, EResourceType.CONSTRUCTION, unlocksConstructionYard);
        Building shipYard = buildingService.createBuilding("Orbitals Construction Yard", "The construction yard construct orbital constructions.", 10, EResourceType.ORBITALCONSTRUCTION, unlocksShipyard);
        Building researchB = buildingService.createBuilding("Research Laboratories", "The lab investigates researches.", 10, EResourceType.RESEARCH, unlocksLaboratory);
        Building bank = buildingService.createBuilding("Market place", "The market makes money.", 10, EResourceType.CREDITS, unlocksBank);
        Building metalsWorks = buildingService.createBuilding("Metal works", "Metals for progress.", 10, EResourceType.METALORE, unlocksMetals);
        Building specialOre = buildingService.createBuilding("Special orbital ores", "Better metals for more progress.", 10, EResourceType.MERCURIUM, unlocksMecur);
        Building hyperWorks = buildingService.createBuilding("Asynchronous Investigations", "The clock works creates time.", 10, EResourceType.HYPERONIUM, unlocksHyperworks);
        LOGGER.info("Buildings created");

        colonizePlanet(u1, p11, constructionYard);
        colonizePlanet(u1, p12, constructionYard);
        colonizePlanet(u1, p13, constructionYard);
        colonizePlanet(u1, p14, constructionYard);
        colonizePlanet(u2, p21, constructionYard);
        colonizePlanet(u2, p22, constructionYard);
        colonizePlanet(u2, p23, constructionYard);
        colonizePlanet(u2, p24, constructionYard);
        LOGGER.info("Planets colonized");

        addBuilding(p11, shipYard);
        addBuilding(p11, researchB);
        addBuilding(p21, shipYard);
        addBuilding(p21, researchB);
        planetService.save(p11);
        planetService.save(p21);
        LOGGER.info("Planets populated with Construction Yards.");

        Armor armor = moduleService.createArmor("Armor Mk I", "An armor", unlockArmor, 5, 10, 1);
        Propulsion propulsion = moduleService.createPropulsion("Speed Mk I", "A drive", unlockPropulsion, 5, 10, 1, false);
        Propulsion propulsionFTL = moduleService.createPropulsion("FTL Speed Mk I", "A FTL drive", unlockFTLPropulsion, 10, 15, 1, true);
        ElectronicWarfare electronicWarfare = moduleService.createElectronicWarfare("Scanner Mk I", "A scanner", unlockElectronicWarfare, 5, 10, 1);
        Sidewall sidewall = moduleService.createSidewall("Shield Mk I", "A shield", unlockShield, 5, 10, 1);

        AmmunitionModule rocketAmmunition = moduleService.createAmmunitionModule("Rocket Ammunition", "A bunch of rockets.", unlocksRocketAmmunition, 5, 10, 1);
        AmmunitionModule pointDefAmmunition = moduleService.createAmmunitionModule("Point Defense Ammunition", "A bunch of bullets.", unlocksPointDefenseAmmunition, 5, 10, 1);
        AmmunitionModule counterRocketAmmunition = moduleService.createAmmunitionModule("Counter Rocket Ammunition", "Another bunch of rockets.", unlocksCounterRocketAmmunition, 5, 10, 1);

        Weapon laserWeapon = moduleService.createWeapon("Laser Mk I", "A laser", unlockLaser, null, 5, 10, 1, 1000, null, EDamageType.DART, EWeaponType.BEAM, EAlignmentType.HUNTING_ALIGNMENT);
        Weapon counterMissile = moduleService.createWeapon("Counter Missile Mk I", "A counter missile", unlockCounterMissiles, counterRocketAmmunition, 5, 10, 1, 1000, null, EDamageType.DART, EWeaponType.COUNTER_MISSILE, EAlignmentType.BATTLE_ALIGNMENT);
        Weapon pointDefense = moduleService.createWeapon("Point Defense Mk I", "A point defense", unlockPointDefense, pointDefAmmunition, 5, 10, 1, 1000, null, EDamageType.DART, EWeaponType.POINT_DEFENSE, EAlignmentType.BATTLE_ALIGNMENT);
        Weapon missile = moduleService.createWeapon("Missile Mk I", "A missile", unlockMissiles, rocketAmmunition, 5, 10, 1, 1000, 0.05, EDamageType.EXPLOSION, EWeaponType.MISSILE, EAlignmentType.BATTLE_ALIGNMENT);

        PassiveModule passiveModule = moduleService.createPassiveModule("Improves armor", "Increases the amount of armor", unlockPassive, ESupportType.ARMOR, ECalculationType.ADD, 5, 10, 1);
        LOGGER.info("Modules created");

        Hull hull1 = hullService.createHull("Corvette vessel", 1, 50, 15, 15, 35, "The corvette hull", unlockHull1, EHullType.FG);
        Hull hull2 = hullService.createHull("Frigate vessel", 1, 100, 35, 35, 55, "The frigate hull", unlockHull2, EHullType.FG);
        Hull hull3 = hullService.createHull("Cruiser vessel", 1, 150, 45, 45, 75, "The cruiser hull", unlockHull3, EHullType.CC);
        LOGGER.info("Hulls created");

        ShipClass as3 = new ShipClass(u1, "Argonauts cruiser", hull3, null);
        createFitting(armor, propulsionFTL, electronicWarfare, sidewall, laserWeapon, pointDefense, missile, new PassiveModule[]{passiveModule}, new AmmunitionModule[]{rocketAmmunition, pointDefAmmunition, counterRocketAmmunition}, as3);

        ShipClass ers3 = new ShipClass(u2, "111er cruiser", hull3, null);
        createFitting(armor, propulsionFTL, electronicWarfare, sidewall, laserWeapon, pointDefense, missile, new PassiveModule[]{passiveModule}, new AmmunitionModule[]{rocketAmmunition, pointDefAmmunition, counterRocketAmmunition}, ers3);
        LOGGER.info("ShipClasses created");


        addUnlockedResearches(u1, unlocksConstructionYard, unlocksShipyard, unlocksLaboratory, unlocksBank, unlocksMetals, unlockLaser, unlockArmor, unlockShield, unlockPropulsion, unlockFTLPropulsion, unlockElectronicWarfare, unlockMissiles, unlockPointDefense, unlockCounterMissiles, unlocksRocketAmmunition, unlocksCounterRocketAmmunition, unlocksPointDefenseAmmunition, unlockPassive, unlockHull1, unlockHull2, unlockHull3);
        addUnlockedResearches(u2, unlocksConstructionYard, unlocksShipyard, unlocksLaboratory, unlocksBank, unlocksMetals, unlockLaser, unlockArmor, unlockShield, unlockPropulsion, unlockFTLPropulsion, unlockElectronicWarfare, unlockMissiles, unlockPointDefense, unlockCounterMissiles, unlocksRocketAmmunition, unlocksCounterRocketAmmunition, unlocksPointDefenseAmmunition, unlockPassive, unlockHull1, unlockHull2, unlockHull3);
        LOGGER.info("Researches populated");

        tickService.doTick();
        LOGGER.info("All Data created");
    }

    @Deprecated(since = "productive environment")
    public Planet colonizePlanet(@Nonnull final User owner, @Nonnull final Planet planet, Building constructionYard) {
        planet.setOwner(owner);
        final Construction constructedConstructionYard = new Construction(planet, constructionYard, 1);
        planet.getConstructions().add(constructedConstructionYard);
        owner.addKnownStarSystems(planet.getSystem());
        return planet;
    }

    @Deprecated(since = "productive environment")
    private void createFitting(Armor armor,
                               Propulsion propulsionFTL,
                               ElectronicWarfare electronicWarfare,
                               Sidewall sidewall,
                               Weapon laserWeapon,
                               Weapon pointDefense,
                               Weapon missile,
                               PassiveModule[] passiveModules,
                               AmmunitionModule[] ammunitionModules,
                               ShipClass shipClass) {
        shipClass.setArmor(armor);
        shipClass.setSidewall(sidewall);
        shipClass.setPropulsion(propulsionFTL);
        shipClass.setElectronicWarfare(electronicWarfare);
        Set<AlignedFitting> fittings = new HashSet<>();
        fittings.add(new AlignedFitting(EWeaponAlignment.BOW, laserWeapon, 1));
        fittings.add(new AlignedFitting(EWeaponAlignment.BROADSIDE, missile, 1));
        fittings.add(new AlignedFitting(EWeaponAlignment.STERN, pointDefense, 1));
        shipClass.setFittings(fittings);

        Set<SupportFitting> supportFittings = Arrays.stream(passiveModules).map(af -> new SupportFitting(af, 1)).collect(Collectors.toSet());
        shipClass.setSupportFittings(supportFittings);

        Set<AmmunitionFitting> ammunitionFittings = Arrays.stream(ammunitionModules).map(af -> new AmmunitionFitting(af, 1)).collect(Collectors.toSet());
        shipClass.setAmmunitionFittings(ammunitionFittings);

        shipClassService.save(shipClass);
    }

    @Deprecated(since = "productive environment")
    private void addUnlockedResearches(User u1, Research... researches) {
        userService.addUnlockedResearch(u1, researches);
    }

    @Deprecated(since = "productive environment")
    private void addBuilding(Planet p11, Building building) {
        p11.getConstructions().add(new Construction(p11, building, 1));
    }
}
