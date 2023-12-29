package de.yuga.spacebattle.backend.services;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.NonPlayerCharacter;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.OrbitalStructure;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.researches.ResearchLevel;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.turn.Colonization;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.backend.entities.turn.Move;
import de.yuga.spacebattle.backend.entities.turn.battle.BattleReport;
import de.yuga.spacebattle.backend.entities.turn.mission.Mission;
import de.yuga.spacebattle.backend.services.account.ChatService;
import de.yuga.spacebattle.backend.services.account.ForumService;
import de.yuga.spacebattle.backend.services.account.NonPlayerCharacterService;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.OrbitalStructureService;
import de.yuga.spacebattle.backend.services.constructables.spacecraft.ShipClassService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.researches.ResearchService;
import de.yuga.spacebattle.backend.services.turn.ColonizationService;
import de.yuga.spacebattle.backend.services.turn.JobService;
import de.yuga.spacebattle.backend.services.turn.MoveService;
import de.yuga.spacebattle.backend.services.turn.battle.BattleReportService;
import de.yuga.spacebattle.backend.services.turn.mission.MissionService;
import de.yuga.spacebattle.backend.services.turn.resources.MarketplaceService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The master of all. Do all the dev-stuff which could be removed or placed somewhere else.
 */
@Service
public class UserDeleteServiceService {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(UserDeleteServiceService.class);

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final ShipClassService shipClassService;

    @Nonnull
    private final ResearchService researchService;

    @Nonnull
    private final FleetService fleetService;

    @Nonnull
    private final ForumService forumService;

    @Nonnull
    private final ColonizationService colonizationService;

    @Nonnull
    private final BattleReportService battleReportService;

    @Nonnull
    private final NonPlayerCharacterService nonPlayerCharacterService;

    @Nonnull
    private final JobService jobService;

    @Nonnull
    private final ChatService chatService;

    @Nonnull
    private final OrbitalStructureService orbitalStructureService;

    @Nonnull
    private final MoveService moveService;

    @Nonnull
    private final MissionService missionService;

    @Nonnull
    private final MarketplaceService marketplaceService;

    @Autowired
    public UserDeleteServiceService(@Nonnull final UserService userService,
                                    @Nonnull final PlanetService planetService,
                                    @Nonnull final ShipClassService shipClassService,
                                    @Nonnull final ResearchService researchService,
                                    @Nonnull final FleetService fleetService,
                                    @Nonnull final ForumService forumService,
                                    @Nonnull final ColonizationService colonizationService,
                                    @Nonnull final BattleReportService battleReportService,
                                    @Nonnull final NonPlayerCharacterService nonPlayerCharacterService,
                                    @Nonnull final JobService jobService,
                                    @Nonnull final ChatService chatService,
                                    @Nonnull final OrbitalStructureService orbitalStructureService,
                                    @Nonnull final MoveService moveService,
                                    @Nonnull final MissionService missionService,
                                    @Nonnull final MarketplaceService marketplaceService) {
        this.userService = Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");
        this.shipClassService = Preconditions.checkNotNull(shipClassService, "shipClassService shouldn't be null!");
        this.researchService = Preconditions.checkNotNull(researchService, "researchService shouldn't be null!");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService shouldn't be null!");
        this.forumService = Preconditions.checkNotNull(forumService, "forumService shouldn't be null!");
        this.colonizationService = Preconditions.checkNotNull(colonizationService, "colonizationService shouldn't be null!");
        this.battleReportService = Preconditions.checkNotNull(battleReportService, "battleService must not be empty");
        this.nonPlayerCharacterService = Preconditions.checkNotNull(nonPlayerCharacterService, "nonPlayerCharacterService must not be empty");
        this.jobService = Preconditions.checkNotNull(jobService, "jobService must not be empty");
        this.chatService = Preconditions.checkNotNull(chatService, "chatService must not be empty");
        this.orbitalStructureService = Preconditions.checkNotNull(orbitalStructureService, "orbitalStructureService must not be empty");
        this.moveService = Preconditions.checkNotNull(moveService, "moveService must not be empty");
        this.missionService = Preconditions.checkNotNull(missionService, "missionService must not be empty");
        this.marketplaceService = Preconditions.checkNotNull(marketplaceService, "marketplaceService must not be empty");
    }

    public void deleteAllInactiveUsers() {

        LOGGER.info("---------------------------- deleting all inactive users the universe ----------------------------");
        final NonPlayerCharacter pirate = nonPlayerCharacterService.findByUsername(MasterOfTheUniverseService.PIRATE);
        Preconditions.checkNotNull(pirate, "pirate must not be empty");

        final User sandkiste = userService.findByUsername("Sandkiste").get().getUser();
        final User novaBallard = userService.findByUsername("NovaBallard").get().getUser();

        final Set<String> toKeep = Set.of("Flashkid", "yufiel", "endofline", "Corben", "ShannonForaker", "Corben1", "NovaBallard", "GeorgeWeberPhillies", "Puidwen", "Sandkiste", "DF12612", "Wlad1990", "ElGuapo", "Mathias")
                .stream().map(String::toLowerCase).collect(Collectors.toSet());

        final Set<User> toWipe = userService.findAll().stream().filter(u -> !toKeep.contains(u.getUsername().toLowerCase())).collect(Collectors.toSet());

        for (final User user : toWipe) {
            final int idUserToWipe = user.getId();
            LOGGER.info("\t\tDelete user {}, {}", user.getUsername(), idUserToWipe);
            final Owner newOwner = idUserToWipe == 30 ? sandkiste : idUserToWipe == 17 ? novaBallard : pirate;
            changeOwnership(user, newOwner);
            deleteUser(idUserToWipe);
        }
    }

    private void deleteUser(final int idUserToWipe) {
        userService.delete(idUserToWipe);
    }

    private void changeOwnership(@Nonnull final User toWipe, @Nonnull final Owner newOwner) {
        Preconditions.checkNotNull(toWipe, "toWipe must not be empty");
        Preconditions.checkNotNull(newOwner, "newOwner must not be empty");

        LOGGER.info("\tChange owner of planets");
        final List<Planet> planetsToChangeOwner = planetService.findAllColonizedBy(toWipe);
        planetsToChangeOwner.forEach(p -> p.setOwner(newOwner));
        planetService.saveAll(planetsToChangeOwner);

        LOGGER.info("\tChange owner of orbitals");
        final List<OrbitalStructure> orbsToChangeOwner = orbitalStructureService.forDeletionFindAllByOwner(toWipe);
        orbsToChangeOwner.forEach(p -> p.setOwner(newOwner));
        orbitalStructureService.saveAll(orbsToChangeOwner);

        LOGGER.info("\tChange owner of fleets");
        final List<Fleet> fleetsToChangeOwner = fleetService.forDeletionFindAllFleetsByUser(toWipe);
        fleetsToChangeOwner.forEach(p -> p.setOwner(newOwner));
        fleetService.saveAll(fleetsToChangeOwner);

        final List<Mission> missionsToChangeOwner = missionService.forDeletionFindAllByUser(toWipe);
        missionsToChangeOwner.forEach(p -> p.setOwner(newOwner));
        missionService.saveAll(missionsToChangeOwner);

        LOGGER.info("\tChange owner of battle reports");
        final List<BattleReport> repsToChangeOwner = battleReportService.forDeletionFindAllByUser(toWipe);
        repsToChangeOwner.forEach(p -> p.changeParticipant(toWipe, newOwner));
        battleReportService.saveAll(repsToChangeOwner);

        LOGGER.info("\tChange owner of ship classes");
        final List<ShipClass> shipClassesToChangeOwner = shipClassService.forDeletionFindAllByOwner(toWipe);
        shipClassesToChangeOwner.forEach(p -> p.setOwner(newOwner));
        shipClassService.saveAll(shipClassesToChangeOwner);

        LOGGER.info("\tChange owner of fleet moves");
        final List<Move> movesToChangeOwner = moveService.forDeletionFindAllByOwner(toWipe);
        movesToChangeOwner.forEach(p -> p.setOwner(newOwner));
        moveService.saveAll(movesToChangeOwner);

        LOGGER.info("\tDeleting resarch levels");
        final Set<ResearchLevel> researchLevels = researchService.getResearchesForUser(toWipe);
        researchService.deleteAll(researchLevels);

        LOGGER.info("\tChange owner of jobs");
        final List<Job> jobsToChangeOwner = jobService.forDeletionFindAllJobsForUser(toWipe);
        jobsToChangeOwner.forEach(p -> p.setOwner(newOwner));
        jobService.saveAll(jobsToChangeOwner);

        LOGGER.info("\tChange author of forum messages");
        forumService.markAsDeletedForUser(toWipe);

        LOGGER.info("\tDelete chat messages");
        chatService.deleteForUser(toWipe);

        LOGGER.info("\tDelete trade offers");
        marketplaceService.changeOwnership(toWipe, newOwner);

        LOGGER.info("\tDeleting colonizations");
        final List<Colonization> colonizationsToWipe = colonizationService.findAllForUser(toWipe);
        colonizationService.deleteAll(colonizationsToWipe);
    }
}
