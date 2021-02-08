package de.yuga.spacebattle.restapi.impl;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.entities.account.User;
import de.yuga.spacebattle.entities.buildings.Building;
import de.yuga.spacebattle.entities.combined.account.Alliance;
import de.yuga.spacebattle.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.entities.orbitals.Planet;
import de.yuga.spacebattle.entities.orbitals.Starsystem;
import de.yuga.spacebattle.entities.researches.Research;
import de.yuga.spacebattle.entities.spacecrafts.Hull;
import de.yuga.spacebattle.entities.spacecrafts.Module;
import de.yuga.spacebattle.enums.EModuleType;
import de.yuga.spacebattle.enums.ERaceType;
import de.yuga.spacebattle.enums.EResourceType;
import de.yuga.spacebattle.logic.account.UserService;
import de.yuga.spacebattle.logic.buildings.BuildingService;
import de.yuga.spacebattle.logic.combined.account.AllianceService;
import de.yuga.spacebattle.logic.constructables.spacecraft.ShipClassService;
import de.yuga.spacebattle.logic.orbitals.PlanetService;
import de.yuga.spacebattle.logic.orbitals.StarsystemService;
import de.yuga.spacebattle.logic.researches.ResearchService;
import de.yuga.spacebattle.logic.spacecraft.HullService;
import de.yuga.spacebattle.logic.spacecraft.ModuleService;
import de.yuga.spacebattle.logic.turn.TickService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import java.util.List;

@RestController
@RequestMapping(value = "/sb")
public class DefaultApiImpl {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DefaultApiImpl.class);

    @Nonnull
    private final TickService tickService;

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final AllianceService allianceService;

    @Nonnull
    private final StarsystemService starsystemService;

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
    public DefaultApiImpl(@Nonnull final TickService tickService,
                          @Nonnull final UserService userService,
                          @Nonnull final AllianceService allianceService,
                          @Nonnull final StarsystemService starsystemService,
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

    @GetMapping(value = "/createInitialData")
    @ResponseBody
    public ResponseEntity<?> createInitialData() {
        List<Alliance> allianceList = allianceService.findAll();
        if (!allianceList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The database was already initialized!");
        }
        createInitialDataPayload();
        final List<User> all = userService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(all);
    }

    @Transactional
    void createInitialDataPayload() {
        Alliance a1 = allianceService.createAlliance("Argonauten", "A");
        Alliance a2 = allianceService.createAlliance("111er", "111er");
        LOGGER.info(a1.toString());
        LOGGER.info(a2.toString());
        LOGGER.info("Alliances created");

        final User u1 = userService.createUser("Flashkid", "test", ERaceType.HUMAN);
        final User u2 = userService.createUser("Yufiel", "test", ERaceType.KANDORIAN);
        LOGGER.info(u1.toString());
        LOGGER.info(u2.toString());
        LOGGER.info("Users created");

        a1 = allianceService.addMember(a1, u1);
        a2 = allianceService.addMember(a2, u2);
        LOGGER.info(a1.toString());
        LOGGER.info(a2.toString());
        LOGGER.info("Alliances populated.");

        Starsystem s1 = starsystemService.createStarsystem("Argonaut", 1, 1);
        Starsystem s2 = starsystemService.createStarsystem("111", 2, 2);
        LOGGER.info(s1.toString());
        LOGGER.info(s2.toString());
        LOGGER.info("Starsystems created");

        Planet p1 = planetService.createPlanet("Argonauten HQ", u1, s1, 1, 1);
        Planet p2 = planetService.createPlanet("111er HQ", u2, s2, 2, 2);
        LOGGER.info(p1.toString());
        LOGGER.info(p2.toString());
        LOGGER.info("Planets created");

        Research unlockConstructionYard = researchService.createResearch("Construction Yard", "The construction yard research researches the construction yard.", 1);
        Research unlockShipyard = researchService.createResearch("Orbitals Construction Yard", "The orbitals Construction Yard research researches the orbitals construction yard.", 1);
        Research unlockLaboratoy = researchService.createResearch("Laboratories", "The laboratories research researches laboratories.", 1);
        Research unlockBank = researchService.createResearch("Market place", "The Market place research researches Market places.", 1);
        Research unlockmetals = researchService.createResearch("Metal works", "The Metal works research researches Metal works.", 1);
        Research unlockmecur = researchService.createResearch("Special orbital ores", "The Special orbital ores research researches Special orbital ores.", 1);
        Research unlockhyperworks = researchService.createResearch("Asynchronous Investigations", "The Asynchronous Investigations research researches Asynchronous Investigations.", 1);
        Research unlocklaser = researchService.createResearch("Laser", "The Laser research researches Lasers.", 1);
        Research unlockarmor = researchService.createResearch("Armor", "The Armor research researches Armors.", 1);
        Research unlockshield = researchService.createResearch("Shield", "The Shield research researches Shields.", 1);
        Research unlockpropulsion = researchService.createResearch("Speed", "The Speed research researches sublight propulsion.", 1);
        Research unlockftl = researchService.createResearch("FTL Speed", "The FTL Speed research researches FTL propulsion.", 1);
        Research unlockscanner = researchService.createResearch("Scanner", "The Scanner research researches Scanners.", 1);
        Research unlockhull1 = new Research("Corvette", "The Corvette research researches Corvettes.", 1);
        Research unlockhull2 = new Research("Frigate", "The Frigate research researches Frigates.", 1);
        Research unlockhull3 = new Research("Cruiser", "The Cruiser research researches Cruisers.", 1);
        LOGGER.info(unlockConstructionYard.toString());
        LOGGER.info(unlockShipyard.toString());
        LOGGER.info(unlockLaboratoy.toString());
        LOGGER.info(unlockBank.toString());
        LOGGER.info(unlockmetals.toString());
        LOGGER.info(unlockmecur.toString());
        LOGGER.info(unlockhyperworks.toString());
        LOGGER.info(unlocklaser.toString());
        LOGGER.info(unlockarmor.toString());
        LOGGER.info(unlockshield.toString());
        LOGGER.info(unlockpropulsion.toString());
        LOGGER.info(unlockftl.toString());
        LOGGER.info(unlockscanner.toString());
        LOGGER.info(unlockhull1.toString());
        LOGGER.info(unlockhull2.toString());
        LOGGER.info(unlockhull3.toString());
        LOGGER.info("Researches created");

        Building constructionYard = buildingService.createBuilding("Construction Yard", "The construction yard construct constructions.", 10, EResourceType.CONSTRUCTION, unlockConstructionYard);
        Building shipYard = buildingService.createBuilding("Orbitals Construction Yard", "The construction yard construct orbital constructions.", 10, EResourceType.ORBITALCONSTRUCTION, unlockShipyard);
        Building researchB = buildingService.createBuilding("Research Laboratories", "The lab investigates researches.", 10, EResourceType.RESEARCH, unlockLaboratoy);
        Building bank = buildingService.createBuilding("Market place", "The market makes money.", 10, EResourceType.CREDITS, unlockBank);
        Building metals = buildingService.createBuilding("Metal works", "Metals for progress.", 10, EResourceType.METALORE, unlockmetals);
        Building mecur = buildingService.createBuilding("Special orbital ores", "Better metals for more progress.", 10, EResourceType.MERCURIUM, unlockmecur);
        Building hyperworks = buildingService.createBuilding("Asynchronous Investigations", "The clock works creates time.", 10, EResourceType.HYPERONIUM, unlockhyperworks);
        LOGGER.info(constructionYard.toString());
        LOGGER.info(shipYard.toString());
        LOGGER.info(researchB.toString());
        LOGGER.info(bank.toString());
        LOGGER.info(metals.toString());
        LOGGER.info(mecur.toString());
        LOGGER.info(hyperworks.toString());
        LOGGER.info("Buildings created");

        p1.getConstructions().add(new Construction(p1, constructionYard, 1));
        p2.getConstructions().add(new Construction(p2, constructionYard, 1));
        planetService.save(p1);
        planetService.save(p2);
        LOGGER.info(p1.toString());
        LOGGER.info(p2.toString());
        LOGGER.info("Planets populated with Construction Yards.");

        Module laser = moduleService.createModule("Laser Mk I", EModuleType.WEAPON, "A laser", 5, 10, 1, unlocklaser);
        Module armor = moduleService.createModule("Armor Mk I", EModuleType.ARMOR, "An armor", 5, 10, 1, unlockarmor);
        Module shield = moduleService.createModule("Shield Mk I", EModuleType.SHIELD, "A shield", 5, 10, 1, unlockshield);
        Module propulsion = moduleService.createModule("Speed Mk I", EModuleType.PROPULSION, "A drive", 5, 10, 1, unlockpropulsion);
        Module ftl = moduleService.createModule("FTL Speed Mk I", EModuleType.FTLPROPULSION, "A FTL drive", 5, 10, 1, unlockftl);
        Module scanner = moduleService.createModule("Scanner Mk I", EModuleType.SCANNER, "A scanner", 5, 10, 1, unlockscanner);
        LOGGER.info(laser.toString());
        LOGGER.info(armor.toString());
        LOGGER.info(shield.toString());
        LOGGER.info(propulsion.toString());
        LOGGER.info(ftl.toString());
        LOGGER.info(scanner.toString());
        LOGGER.info("Modules created");

        Hull hull1 = hullService.createHull("Corvette vessel", 1, 50, "The corvette hull", unlockhull1);
        Hull hull2 = hullService.createHull("Frigate vessel", 1, 100, "The frigate hull", unlockhull2);
        Hull hull3 = hullService.createHull("Cruiser vessel", 1, 150, "The cruiser hull", unlockhull3);
        LOGGER.info(hull1.toString());
        LOGGER.info(hull2.toString());
        LOGGER.info(hull3.toString());
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
        LOGGER.info(as1.toString());
        LOGGER.info(as2.toString());
        LOGGER.info(as3.toString());
        LOGGER.info(ers1.toString());
        LOGGER.info(ers2.toString());
        LOGGER.info(ers3.toString());
        LOGGER.info("ShipClasses created");
    }
}
