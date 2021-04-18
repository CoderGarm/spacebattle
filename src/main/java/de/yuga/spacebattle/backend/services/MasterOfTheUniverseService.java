package de.yuga.spacebattle.backend.services;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.entities.spacecrafts.Module;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.backend.enums.ERaceType;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.buildings.BuildingService;
import de.yuga.spacebattle.backend.services.combined.account.AllianceService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.ShipClassService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.orbitals.StarSystemService;
import de.yuga.spacebattle.backend.services.researches.ResearchService;
import de.yuga.spacebattle.backend.services.spacecraft.HullService;
import de.yuga.spacebattle.backend.services.spacecraft.ModuleService;
import de.yuga.spacebattle.backend.services.turn.TickService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.gui.vaadin.misc.ViewBoxDefinition.STAR_RADIUS;
import static de.yuga.spacebattle.gui.vaadin.misc.ViewBoxDefinition.UNIVERSE_CENTER_RADIUS;

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

    @Autowired
    public MasterOfTheUniverseService(@Nonnull final TickService tickService,
                                      @Nonnull final UserService userService,
                                      @Nonnull final AllianceService allianceService,
                                      @Nonnull final StarSystemService starsystemService,
                                      @Nonnull final PlanetService planetService,
                                      @Nonnull final BuildingService buildingService,
                                      @Nonnull final ModuleService moduleService,
                                      @Nonnull final HullService hullService,
                                      @Nonnull final ShipClassService shipClassService,
                                      @Nonnull final ResearchService researchService) {
        Preconditions.checkNotNull(tickService, "tickService shouldn't be null!");
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        Preconditions.checkNotNull(allianceService, "allianceService shouldn't be null!");
        Preconditions.checkNotNull(starsystemService, "starsystemService shouldn't be null!");
        Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");
        Preconditions.checkNotNull(buildingService, "buildingService shouldn't be null!");
        Preconditions.checkNotNull(moduleService, "moduleService shouldn't be null!");
        Preconditions.checkNotNull(hullService, "hullService shouldn't be null!");
        Preconditions.checkNotNull(shipClassService, "shipClassService shouldn't be null!");
        Preconditions.checkNotNull(researchService, "researchService shouldn't be null!");

        this.tickService = tickService;
        this.userService = userService;
        this.allianceService = allianceService;
        this.starsystemService = starsystemService;
        this.planetService = planetService;
        this.buildingService = buildingService;
        this.moduleService = moduleService;
        this.hullService = hullService;
        this.shipClassService = shipClassService;
        this.researchService = researchService;
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
                planetService.createPlanet(starSystemName + "-" + i, null, starSystem, orbit);
            }
        });
        LOGGER.info("New star systems populated");
    }

    @Transactional
    void createInitialDataPayload() {
        Alliance a1 = allianceService.createAlliance("Argonauten", "A");
        Alliance a2 = allianceService.createAlliance("111er", "111er");
        LOGGER.info("Alliances created");

        final User u1 = userService.createUser("Flashkid", "test", "mail", ERaceType.HUMAN);
        final User u2 = userService.createUser("Yufiel", "test", "mail2", ERaceType.KANDORIAN);
        LOGGER.info("Users created");

        u1.setAlliance(a1);
        u2.setAlliance(a2);
        userService.save(u1);
        userService.save(u2);
        LOGGER.info("Alliances populated.");

        StarSystem s1 = starsystemService.createStarSystem("Argonaut", 1, 1);
        StarSystem s2 = starsystemService.createStarSystem("111", 2, 2);
        LOGGER.info("Starsystems created");

        Planet p1 = planetService.createPlanet("Argonauten HQ", u1, s1, 1, 1);
        Planet p2 = planetService.createPlanet("111er HQ", u2, s2, 2, 2);
        LOGGER.info("Planets created");

        Research unlockConstructionYard = researchService.createResearch("Construction Yard", "The construction yard research researches the construction yard.", 1, null);
        Research unlockShipyard = researchService.createResearch("Orbitals Construction Yard", "The orbitals Construction Yard research researches the orbitals construction yard.", 1, null);
        Research unlockLaboratoy = researchService.createResearch("Laboratories", "The laboratories research researches laboratories.", 1, null);
        Research unlockBank = researchService.createResearch("Market place", "The Market place research researches Market places.", 1, null);
        Research unlockmetals = researchService.createResearch("Metal works", "The Metal works research researches Metal works.", 1, null);
        Research unlockmecur = researchService.createResearch("Special orbital ores", "The Special orbital ores research researches Special orbital ores.", 1, unlockmetals);
        Research unlockhyperworks = researchService.createResearch("Asynchronous Investigations", "The Asynchronous Investigations research researches Asynchronous Investigations.", 1, unlockmecur);
        Research unlocklaser = researchService.createResearch("Laser", "The Laser research researches Lasers.", 1, null);
        Research unlockarmor = researchService.createResearch("Armor", "The Armor research researches Armors.", 1, null);
        Research unlockshield = researchService.createResearch("Shield", "The Shield research researches Shields.", 1, null);
        Research unlockpropulsion = researchService.createResearch("Speed", "The Speed research researches sublight propulsion.", 1, null);
        Research unlockftl = researchService.createResearch("FTL Speed", "The FTL Speed research researches FTL propulsion.", 1, null);
        Research unlockscanner = researchService.createResearch("Scanner", "The Scanner research researches Scanners.", 1, null);
        Research unlockhull1 = researchService.createResearch("Corvette", "The Corvette research researches Corvettes.", 1, null);
        Research unlockhull2 = researchService.createResearch("Frigate", "The Frigate research researches Frigates.", 1, null);
        Research unlockhull3 = researchService.createResearch("Cruiser", "The Cruiser research researches Cruisers.", 1, unlockhull2);
        LOGGER.info("Researches created");

        Building constructionYard = buildingService.createBuilding("Construction Yard", "The construction yard construct constructions.", 10, EResourceType.CONSTRUCTION, unlockConstructionYard);
        Building shipYard = buildingService.createBuilding("Orbitals Construction Yard", "The construction yard construct orbital constructions.", 10, EResourceType.ORBITALCONSTRUCTION, unlockShipyard);
        Building researchB = buildingService.createBuilding("Research Laboratories", "The lab investigates researches.", 10, EResourceType.RESEARCH, unlockLaboratoy);
        Building bank = buildingService.createBuilding("Market place", "The market makes money.", 10, EResourceType.CREDITS, unlockBank);
        Building metals = buildingService.createBuilding("Metal works", "Metals for progress.", 10, EResourceType.METALORE, unlockmetals);
        Building mecur = buildingService.createBuilding("Special orbital ores", "Better metals for more progress.", 10, EResourceType.MERCURIUM, unlockmecur);
        Building hyperworks = buildingService.createBuilding("Asynchronous Investigations", "The clock works creates time.", 10, EResourceType.HYPERONIUM, unlockhyperworks);
        LOGGER.info("Buildings created");

        p1.getConstructions().add(new Construction(p1, constructionYard, 1));
        p2.getConstructions().add(new Construction(p2, constructionYard, 1));
        planetService.save(p1);
        planetService.save(p2);
        LOGGER.info("Planets populated with Construction Yards.");

        Module laser = moduleService.createModule("Laser Mk I", EModuleType.WEAPON, "A laser", 5, 10, 1, unlocklaser);
        Module armor = moduleService.createModule("Armor Mk I", EModuleType.ARMOR, "An armor", 5, 10, 1, unlockarmor);
        Module shield = moduleService.createModule("Shield Mk I", EModuleType.SHIELD, "A shield", 5, 10, 1, unlockshield);
        Module propulsion = moduleService.createModule("Speed Mk I", EModuleType.PROPULSION, "A drive", 5, 10, 1, unlockpropulsion);
        Module ftl = moduleService.createModule("FTL Speed Mk I", EModuleType.FTLPROPULSION, "A FTL drive", 5, 10, 1, unlockftl);
        Module scanner = moduleService.createModule("Scanner Mk I", EModuleType.SCANNER, "A scanner", 5, 10, 1, unlockscanner);
        LOGGER.info("Modules created");

        Hull hull1 = hullService.createHull("Corvette vessel", 1, 50, "The corvette hull", unlockhull1);
        Hull hull2 = hullService.createHull("Frigate vessel", 1, 100, "The frigate hull", unlockhull2);
        Hull hull3 = hullService.createHull("Cruiser vessel", 1, 150, "The cruiser hull", unlockhull3);
        LOGGER.info("Hulls created");

        ShipClass as1 = shipClassService.createShipClass(u1, "Argonauts corvette", hull1);
        as1 = shipClassService.addModules(as1, laser, armor, shield, propulsion, ftl, scanner);
        ShipClass as2 = shipClassService.createShipClass(u1, "Argonauts frigate", hull2);
        as2 = shipClassService.addModules(as2, laser, armor, shield, propulsion, ftl, scanner, laser, armor, shield, propulsion, ftl);
        ShipClass as3 = shipClassService.createShipClass(u1, "Argonauts cruiser", hull3);
        as3 = shipClassService.addModules(as3, laser, armor, shield, propulsion, ftl, scanner, laser, armor, shield, propulsion, ftl, laser, armor, shield);

        ShipClass ers1 = shipClassService.createShipClass(u2, "111er corvette", hull1);
        ers1 = shipClassService.addModules(ers1, laser, armor, shield, propulsion, ftl, scanner);
        ShipClass ers2 = shipClassService.createShipClass(u2, "111er frigate", hull2);
        ers2 = shipClassService.addModules(ers2, laser, armor, shield, propulsion, ftl, scanner, laser, armor, shield, propulsion, ftl);
        ShipClass ers3 = shipClassService.createShipClass(u2, "111er cruiser", hull3);
        ers3 = shipClassService.addModules(ers3, laser, armor, shield, propulsion, ftl, scanner, laser, armor, shield, propulsion, ftl, laser, armor, shield);
        LOGGER.info("ShipClasses created");

        userService.addUnlockedResearch(u1, unlockConstructionYard);
        userService.addUnlockedResearch(u1, unlockShipyard);
        userService.addUnlockedResearch(u1, unlockLaboratoy);
        userService.addUnlockedResearch(u1, unlockBank);
        userService.addUnlockedResearch(u1, unlockmetals);
        userService.addUnlockedResearch(u1, unlocklaser);
        userService.addUnlockedResearch(u1, unlockarmor);
        userService.addUnlockedResearch(u1, unlockshield);
        userService.addUnlockedResearch(u1, unlockpropulsion);
        userService.addUnlockedResearch(u1, unlockftl);
        userService.addUnlockedResearch(u1, unlockscanner);
        userService.addUnlockedResearch(u1, unlockhull1);
        userService.addUnlockedResearch(u1, unlockhull2);
        userService.addUnlockedResearch(u1, unlockhull3);
        userService.addUnlockedResearch(u2, unlockConstructionYard);
        userService.addUnlockedResearch(u2, unlockShipyard);
        userService.addUnlockedResearch(u2, unlockLaboratoy);
        userService.addUnlockedResearch(u2, unlockBank);
        userService.addUnlockedResearch(u2, unlockmetals);
        userService.addUnlockedResearch(u2, unlocklaser);
        userService.addUnlockedResearch(u2, unlockarmor);
        userService.addUnlockedResearch(u2, unlockshield);
        userService.addUnlockedResearch(u2, unlockpropulsion);
        userService.addUnlockedResearch(u2, unlockftl);
        userService.addUnlockedResearch(u2, unlockscanner);
        userService.addUnlockedResearch(u2, unlockhull1);
        userService.addUnlockedResearch(u2, unlockhull2);
        userService.addUnlockedResearch(u2, unlockhull3);
        LOGGER.info("Researches populated");

        tickService.doTick();
        LOGGER.info("All Data created");
    }
}
