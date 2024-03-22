package de.yuga.spacebattle.backend.services;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.SpringBootProdProfile;
import de.yuga.spacebattle.backend.combat.BattleLogger;
import de.yuga.spacebattle.backend.dto.physics.Mass;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.account.forum.Forum;
import de.yuga.spacebattle.backend.entities.account.forum.ForumMessage;
import de.yuga.spacebattle.backend.entities.account.forum.ForumThread;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.FleetSnapshot;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.entities.turn.battle.BattleReport;
import de.yuga.spacebattle.backend.entities.turn.battle.LossRole;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthStateSnapshot;
import de.yuga.spacebattle.backend.enums.physics.EMassMetric;
import de.yuga.spacebattle.backend.services.account.ForumService;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.buildings.BuildingService;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.constructables.OperationalService;
import de.yuga.spacebattle.backend.services.constructables.buildings.ConstructionService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.WarShipService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.researches.ResearchService;
import de.yuga.spacebattle.backend.services.spacecraft.BattleService;
import de.yuga.spacebattle.backend.services.turn.ColonizationService;
import de.yuga.spacebattle.backend.services.turn.JobService;
import de.yuga.spacebattle.backend.services.turn.TickRunnerService;
import de.yuga.spacebattle.backend.services.turn.battle.BattleReportService;
import de.yuga.spacebattle.backend.services.turn.battle.combat.WarshipHealthStateService;
import de.yuga.spacebattle.backend.transformer.BuildingCsvTransformer;
import de.yuga.spacebattle.rest.dto.misc.Coords;
import de.yuga.spacebattle.rest.dto.misc.wormhole.Junction;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Autowired
    private BattleReportService battleReportService;

    @Autowired
    private FleetService fleetService;

    @Autowired
    private BattleLogger battleLogger;

    @Test
    void t() {
        final Set<Junction> junctions = resourceService.readWormholes();
        assertNotNull(junctions);
    }

    @Test
    void warHarvestAssessment() {

        final Set<BattleReport> reps = battleReportService.findAllBetweenTick(244, 257)
                .stream().filter(br -> br.getParticipatingUsers().stream().anyMatch(u -> u.getId() == 15))
                .collect(Collectors.toSet());

        final Map<Owner, List<LossRole>> lossRoles = new HashMap<>();
        final Map<Owner, List<WarShip>> involved = new HashMap<>();

        for (final BattleReport battleReport : reps) {
            final FleetSnapshot pirate = battleReport.getParticipatingFleets().stream().filter(f -> f.getOwner().getNpcOwner() != null).findFirst().orElse(null);
            final FleetSnapshot intercept = battleReport.getParticipatingFleets().stream().filter(f -> f.getOwner().getHumanOwner() != null).findFirst().orElse(null);

            if (pirate == null || intercept == null) {
                continue;
            }

            battleReport.getLossRole().forEach(lossRole -> {
                final List<LossRole> orDefault = lossRoles.getOrDefault(lossRole.getOwner(), new ArrayList<>());
                orDefault.add(lossRole);
                lossRoles.put(lossRole.getOwner(), orDefault);
            });

            List<WarShip> orDefault = involved.getOrDefault(pirate.getOwner(), new ArrayList<>());
            orDefault.addAll(pirate.getShips().stream().map(WarshipHealthStateSnapshot::getWarShip).collect(Collectors.toList()));
            involved.put(pirate.getOwner(), orDefault);

            orDefault = involved.getOrDefault(intercept.getOwner(), new ArrayList<>());
            orDefault.addAll(intercept.getShips().stream().map(WarshipHealthStateSnapshot::getWarShip).collect(Collectors.toList()));
            involved.put(intercept.getOwner(), orDefault);
        }

        final Set<Owner> owners = new HashSet<>(lossRoles.keySet());
        owners.addAll(involved.keySet());

        Mass playerLoss = new Mass(0, EMassMetric.KT);

        System.out.println("<div class=\"headline hl6\">Loss Role</div>");
        System.out.println("<div class=\"main-loss-list\">");
        for (final Owner owner : owners.stream().filter(o -> o.getHumanOwner() != null).collect(Collectors.toList())) {
            final List<LossRole> lossRolesByOwner = lossRoles.getOrDefault(owner, new ArrayList<>());

            final List<String> lostNames = lossRolesByOwner.stream().map(LossRole::getWarShipName).collect(Collectors.toList());
            if (!lostNames.isEmpty()) {
                final Map<ShipClass, List<LossRole>> lossesByClass = lossRolesByOwner.stream().collect(Collectors.groupingBy(LossRole::getShipClass,
                        Collectors.mapping(Function.identity(), Collectors.toList())));

                final List<LossRole> collect = lossesByClass.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
                for (final LossRole lossRole : collect) {
                    playerLoss = playerLoss.add(lossRole.getShipClass().getTonnage());
                }

                System.out.println("<div class=\"loss-element\">");
                System.out.println("<div class=\"loss-title\">" + owner.getRPGName() + "</div>");
                System.out.println("<div class=\"loss-list\">");
                lossesByClass.forEach((shipClass, losses) -> {
                    losses.forEach(lossRole -> System.out.println("<span><span class=\"ship\"><b>" + lossRole.getWarShipName() + "</b></span>, " + getClass(shipClass) + "</span>"));
                });
                System.out.println("</div>");
                System.out.println("</div>");
            }
        }
        System.out.println("</div>");
        System.out.println("\n");

        Mass pirateLoss = new Mass(0, EMassMetric.KT);
        final Owner owner = owners.stream().filter(o -> o.getNpcOwner() != null).findFirst().orElseThrow(NullPointerException::new);
        final List<LossRole> lossRoles1 = lossRoles.get(owner);
        for (final LossRole lossRole : lossRoles1) {
            pirateLoss = pirateLoss.add(lossRole.getShipClass().getTonnage());
        }
        System.out.println("Player Losses: " + playerLoss.getInMetricWithScale(EMassMetric.MT));
        System.out.println("Pirate Losses: " + pirateLoss.getInMetricWithScale(EMassMetric.MT));
    }

    @Nonnull
    private String getClass(@Nonnull final ShipClass shipClass) {
        Preconditions.checkNotNull(shipClass, "shipClass must not be empty");

        final int mass = shipClass.getTonnage().getCoordinateInMetric(EMassMetric.KT).intValue();
        return "<span class=\"class-complement\">"
                + "<i>" + shipClass.getName() + " Flt. " + shipClass.getFlight() + "</i>, " + shipClass.getShipClassType() + ", " + mass + " " + EMassMetric.KT
                + ", " + shipClass.getCosts().getCrewRequirement().getSumOfPopulation() + " Officers and Enlisted"
                + "</span>";
    }

    @Test
    void tick() {
        for (int i = 1; i <= 1; i++) {
            tickService.doTick();
        }
    }

    @Test
    void runBattle() {

        final int idPlanet = 112;
        fleetService.deleteAll();
        masterOfTheUniverseService.createFlashsFleet(idPlanet);
        masterOfTheUniverseService.createYufielsFleet(idPlanet);
        masterOfTheUniverseService.transformationNeeded = false;

        final Planet planet = planetService.find(idPlanet);
        final Tick today = tickService.getToday();

        battleLogger.setChartActive(true);

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
