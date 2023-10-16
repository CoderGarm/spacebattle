package de.yuga.spacebattle.backend.services.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.account.UserPoints;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.services.combined.spacecraft.FleetService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.researches.ResearchService;
import de.yuga.spacebattle.backend.services.turn.ColonizationService;
import de.yuga.spacebattle.backend.services.turn.JobService;
import de.yuga.spacebattle.backend.services.turn.mission.MissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;

@Service
public class UserPointsService {

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final ColonizationService colonizationService;

    @Nonnull
    private final ResearchService researchService;

    @Nonnull
    private final JobService jobService;

    @Nonnull
    private final FleetService fleetService;

    @Nonnull
    private final MissionService missionService;

    @Autowired
    public UserPointsService(@Nonnull final UserService userService,
                             @Nonnull final PlanetService planetService,
                             @Nonnull final ResearchService researchService,
                             @Nonnull final JobService jobService,
                             @Nonnull final FleetService fleetService,
                             @Nonnull final ColonizationService colonizationService,
                             @Nonnull final MissionService missionService) {
        this.userService = Preconditions.checkNotNull(userService, "userService must not be empty");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService must not be empty");
        this.colonizationService = Preconditions.checkNotNull(colonizationService, "colonizationService must not be empty");
        this.researchService = Preconditions.checkNotNull(researchService, "researchService must not be empty");
        this.jobService = Preconditions.checkNotNull(jobService, "jobService must not be empty");
        this.fleetService = Preconditions.checkNotNull(fleetService, "fleetService must not be empty");
        this.missionService = Preconditions.checkNotNull(missionService, "missionService must not be empty");
    }


    @Nonnull
    public UserPoints getPoints(final int idUser) {
        final User user = Preconditions.checkNotNull(userService.find(idUser), "user must not be empty");

        return new UserPoints(user)
                .withPlanets(planetService.findAllColonizedBy(user))
                .withColonizations(colonizationService.findAllForUser(user))
                .withJobs(jobService.findAllJobsForUser(user))
                .withFleets(fleetService.findAllFleetsByUser(user))
                .withMothball(fleetService.findPooledWarships(user.getId(), null))
                .withMissions(missionService.findAllMissions(user.getId()))
                .withResearches(researchService.getResearchesForUser(user));
    }
}
