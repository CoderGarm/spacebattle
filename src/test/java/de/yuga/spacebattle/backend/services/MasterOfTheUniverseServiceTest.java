package de.yuga.spacebattle.backend.services;

import de.yuga.spacebattle.SpringBootProdProfile;
import de.yuga.spacebattle.backend.calculator.colonization.ColonizationCostCalculator;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.account.forum.Forum;
import de.yuga.spacebattle.backend.entities.account.forum.ForumMessage;
import de.yuga.spacebattle.backend.entities.account.forum.ForumThread;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.buildings.ProductionType;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.researches.ResearchLevel;
import de.yuga.spacebattle.backend.entities.turn.Colonization;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthState;
import de.yuga.spacebattle.backend.entities.turn.resources.PayingPossibleResult;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.*;
import de.yuga.spacebattle.backend.services.account.ForumService;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.buildings.BuildingService;
import de.yuga.spacebattle.backend.services.constructables.buildings.ConstructionService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.WarShipService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.researches.ResearchService;
import de.yuga.spacebattle.backend.services.turn.ColonizationService;
import de.yuga.spacebattle.backend.services.turn.JobService;
import de.yuga.spacebattle.backend.services.turn.TickService;
import de.yuga.spacebattle.backend.services.turn.battle.combat.WarshipHealthStateService;
import de.yuga.spacebattle.backend.transformer.BuildingCsvTransformer;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static de.yuga.spacebattle.backend.transformer.CSVTransformer.CSV_SEPARATOR;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootProdProfile
//@ActiveProfiles("dev")
@Disabled("not needed for unit or integration testing")
public class MasterOfTheUniverseServiceTest {

    @Autowired
    private MasterOfTheUniverseService masterOfTheUniverseService;

    @Autowired
    private ForumService forumService;

    @Autowired
    private UserService userService;

    @Autowired
    private TickService tickService;

    @Autowired
    private JobService jobService;

    @Autowired
    private ConstructionService constructionService;

    @Autowired
    private ResearchService researchService;

    @Autowired
    private PlanetService planetService;

    @Autowired
    private BuildingService buildingService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private ColonizationService colonizationService;

    @Autowired
    private WarShipService warShipService;

    @Autowired
    private WarshipHealthStateService healthStateService;

    @Test
    void tick() {
        tickService.doTick();
    }

    @Test
    void runBattle() {
        final String random = random();
        final User entity = new User(random, "12457aA!", random + "@de", EWebUserRole.USER);
        final User saved = userService.save(entity);

        final List<Research> researchesWithoutPrecondition = researchService.getResearchesWithoutPrecondition();
        researchService.addResearch(saved, researchesWithoutPrecondition);

        final Planet planet = colonizationService.findPlanetForNewUser();
        planetService.save(planet);
        final Colonization colonization = new Colonization(saved, planet, ColonizationCostCalculator.getCrewRequirementForColonization(), 0);
        colonizationService.colonizePlanet(colonization);

        masterOfTheUniverseService.createFleetForUser(saved);
        final WarShip opponentForUser = masterOfTheUniverseService.createOpponentFleetForUser(saved);
        final WarshipHealthState warshipHealthState = opponentForUser.getWarshipHealthState();
        warshipHealthState.getCapabilities().forEach(cap -> cap.setValue(cap.getValue().divide(BigDecimal.valueOf(3), MathContext.DECIMAL32)));

        warShipService.save(opponentForUser);
        tickService.doTick();

        System.out.println("Login: " + random);
    }

    @Test
    void runSecondBattle() {
        final String username = "fluqhsjqda";
        final User saved = userService.findByUsername(username).get().getUser();

        final WarShip opponentForUser = masterOfTheUniverseService.createOpponentFleetForUser(saved);

        warShipService.save(opponentForUser);
        tickService.doTick();

        System.out.println("Login: " + username);
    }

    private String random() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        return buffer.toString();
    }

    @Test
    public void createInitialData() {
        masterOfTheUniverseService.createInitialData();
    }

    @Test
    void testCreateForums() {
        masterOfTheUniverseService.createForums();
    }

    @Test
    void createSomeForums() {

        final List<User> users = userService.findAll();

        final List<Forum> forums = forumService.findAll();
        forums.forEach(forum -> {
            final User user = users.stream().filter(forum::isUserAllowed).findFirst().orElse(null);
            assertNotNull(user);

            final ForumThread forumThread = new ForumThread(forum, "Thread in " + forum.getTitle(), "Description in " + forum.getDescription());
            final ForumThread save = forumService.save(forumThread);
            final ForumMessage forumMessage = new ForumMessage(save, user, "Hello World!");
            forumService.save(forumMessage);
        });
    }

    @Test
    void convertTest() {
        final Building b = buildingService.find(1);
        final String result = new BuildingCsvTransformer("en").convert(b);
        assertNotNull(result);
        assertFalse(result.isBlank());
        assertNotEquals("" + result.charAt(result.length() - 1), CSV_SEPARATOR);
    }

    @Test
    void readMap() {
        final List<MasterOfTheUniverseService.Coords> coords = resourceService.readStarSystems();
        assertNotNull(coords);
    }

    @Test
    void createThread() {
        final Forum forum = forumService.findForumById(1);
        assertNotNull(forum);
        final ForumThread forumThread = new ForumThread(forum, "Thread in " + forum.getTitle(), "Description in " + forum.getDescription());
        forumService.save(forumThread);
    }

    @Test
    void benchmarkTimeToShipyard() {
        prepareDatabase();

        final User user = userService.find(1);
        assertNotNull(user);
        Planet mainPlanet = planetService.findMainPlanet(user);

        final boolean isConstructionPossible = isConstructionPossibleOnPlanet(mainPlanet.getId());
        assertTrue(isConstructionPossible);

        final Set<ResearchLevel> researchesForUser = researchService.getResearchesForUser(user.getId());

        final ProductionType researchPT = new ProductionType(EResourceType.RESEARCH, EProductionCategory.PRODUCE, null);
        final List<Building> laboratories = buildingService.findBuildingByProductionType(researchPT);
        final Building laboratory = laboratories.get(0);
        final Research labResearch = laboratory.getUnlockedThrough();
        final boolean hasLaboratoryResearch = researchesForUser.stream().anyMatch(f -> f.getResearch().equals(labResearch));
        assertTrue(hasLaboratoryResearch);

        final ProductionType shipyardPT = new ProductionType(EResourceType.ORBITAL_CONSTRUCTION, EProductionCategory.PRODUCE, null);
        final List<Building> shipyards = buildingService.findBuildingByProductionType(shipyardPT);
        final Building shipyard = shipyards.get(0);
        final Research shipyardResearch = shipyard.getUnlockedThrough();
        final boolean hasShipyardResearch = researchesForUser.stream().anyMatch(f -> f.getResearch().equals(shipyardResearch));
        assertTrue(hasShipyardResearch);

        final ProductionType collegePT = new ProductionType(EResourceType.POPULATION, EProductionCategory.REFINEMENT, ERefinementSequence.EDUCATION_CIVIL_II);
        final List<Building> colleges = buildingService.findBuildingByProductionType(collegePT);
        final Building college = colleges.get(0);
        final Research collegeResearch = college.getUnlockedThrough();
        final boolean hasCollegeResearch = researchesForUser.stream().anyMatch(f -> f.getResearch().equals(collegeResearch));
        assertTrue(hasCollegeResearch);

        long jobDoneAtZero = runGroundConstruction(mainPlanet, college);
        jobDoneAtZero += runGroundConstruction(mainPlanet, laboratory);

        mainPlanet = planetService.findMainPlanet(user);
        final ResourceDeposit resourceDeposit = mainPlanet.getResourceDeposit();

        final ResourceDeposit costs = new ResourceDeposit(EDepositType.COSTS);
        final ResourceDeposit shipyardCosts = shipyard.getCosts();
        final ResourceDeposit laboratoryCosts = laboratory.getCosts();
        final ResourceDeposit collegeCosts = college.getCosts();
        costs.add(shipyardCosts);
        costs.add(laboratoryCosts);
        //costs.add(collegeCosts);

        try {
            final int ticks = mainPlanet.calculateTicksToCollect(costs);
            System.out.println("Tick to done: " + ticks + jobDoneAtZero);
        } catch (final NotifyWebUserException e) {
            final PayingPossibleResult payingPossibleResult = e.getPayingPossibleResult();
            if (payingPossibleResult == null) {
                e.printStackTrace();
                return;
            }
            assertNotNull(payingPossibleResult.getResult());
            System.out.println(String.join(",", payingPossibleResult.getResult()));
        }

        final boolean researchPossible = isResearchPossible(user);


        final Map<Building, Integer> upgradeableConstructions = getUpgradableConstructions(mainPlanet.getId());


    }

    private long runGroundConstruction(final Planet mainPlanet, final Building college) {
        final Job constructionYardJob = jobService.createConstructionYardJob(mainPlanet.getId(), college.getId());
        final long jobDoneAtZero = constructionYardJob.getJobDoneAtZero();

        assertTrue(jobDoneAtZero >= 0);

        for (long i = jobDoneAtZero; i >= 0; i--) {
            tickService.doTick();
        }
        return jobDoneAtZero;
    }

    private boolean isResearchPossible(final User user) {
        final Planet researchPlanet = planetService.findResearchPlanet(user);
        if (researchPlanet == null) {
            return false;
        }
        final Construction facility = researchPlanet.getConstructionByResource(EResourceType.RESEARCH)
                .stream().findFirst().orElse(null);
        if (facility == null) {
            return false;
        }
        return facility.getJobs().isEmpty();
    }

    private Map<Building, Integer> getUpgradableConstructions(final int idPlanet) {
        final Planet planet = planetService.find(idPlanet);
        assertNotNull(planet);
        assertNotNull(planet.getOwner());
        final Set<ResearchLevel> researchesForUser = researchService.getResearchesForUser(planet.getOwner().getId());
        return constructionService.getUpgradeableConstructions(planet, researchesForUser);
    }

    private boolean isConstructionPossibleOnPlanet(final int idPlanet) {
        final Planet planet = planetService.find(idPlanet);
        assertNotNull(planet);
        return planet.isConstructionPossible();
    }

    private void prepareDatabase() {
        List<User> all = userService.findAll();
        assertTrue(all.isEmpty());
        masterOfTheUniverseService.createInitialDataPayload();

        all = userService.findAll();
        assertNotNull(all);
        assertEquals(3, all.size());
    }
}
