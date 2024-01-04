package de.yuga.spacebattle.backend.services;

import de.yuga.spacebattle.SpringBootProdProfile;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.account.forum.Forum;
import de.yuga.spacebattle.backend.entities.account.forum.ForumMessage;
import de.yuga.spacebattle.backend.entities.account.forum.ForumThread;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.services.account.ForumService;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.buildings.BuildingService;
import de.yuga.spacebattle.backend.services.constructables.OperationalService;
import de.yuga.spacebattle.backend.services.constructables.buildings.ConstructionService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.WarShipService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.researches.ResearchService;
import de.yuga.spacebattle.backend.services.spacecraft.BattleService;
import de.yuga.spacebattle.backend.services.turn.ColonizationService;
import de.yuga.spacebattle.backend.services.turn.JobService;
import de.yuga.spacebattle.backend.services.turn.TickRunnerService;
import de.yuga.spacebattle.backend.services.turn.battle.combat.WarshipHealthStateService;
import de.yuga.spacebattle.backend.transformer.BuildingCsvTransformer;
import de.yuga.spacebattle.rest.dto.misc.Coords;
import de.yuga.spacebattle.rest.dto.misc.wormhole.Junction;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Random;
import java.util.Set;

import static de.yuga.spacebattle.backend.transformer.CSVTransformer.CSV_SEPARATOR;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootProdProfile
@Disabled("not needed for unit or integration testing")
public class MasterOfTheUniverseServiceTest {

    @Autowired
    private MasterOfTheUniverseService masterOfTheUniverseService;

    @Autowired
    private ForumService forumService;

    @Autowired
    private UserService userService;

    @Autowired
    private TickRunnerService tickService;

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

    @Autowired
    private OperationalService operationalService;

    @Autowired
    private BattleService battleService;

    @Test
    void t() {
        final Set<Junction> junctions = resourceService.readWormholes();
        assertNotNull(junctions);
    }

    @Test
    void tick() {
        for (int i = 1; i <= 1; i++) {
            tickService.doTick();
        }
    }

    @Test
    void runBattle() {
        final Planet planet = planetService.find(112);
        final Tick today = tickService.getToday();
        battleService.runBattleAtPlanet(today, planet);
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
        final List<Coords> coords = resourceService.readStarSystems();
        assertNotNull(coords);
    }

    @Test
    void createThread() {
        final Forum forum = forumService.findForumById(1);
        assertNotNull(forum);
        final ForumThread forumThread = new ForumThread(forum, "Thread in " + forum.getTitle(), "Description in " + forum.getDescription());
        forumService.save(forumThread);
    }
}
