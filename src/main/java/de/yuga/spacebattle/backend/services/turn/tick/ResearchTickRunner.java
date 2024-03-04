package de.yuga.spacebattle.backend.services.turn.tick;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.research.EmpireResearchCapability;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.turn.Constructable;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.researches.ResearchService;
import de.yuga.spacebattle.backend.services.turn.JobService;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

@Service
public class ResearchTickRunner implements TickRunner {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(ResearchTickRunner.class);
    private final UserService userService;

    @Nullable
    private Tick today;

    @Nonnull
    private final JobService jobService;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final ResearchService researchService;

    @Autowired
    public ResearchTickRunner(@Nonnull final JobService jobService,
                              @Nonnull final PlanetService planetService,
                              @Nonnull final ResearchService researchService, final UserService userService) {
        this.jobService = Preconditions.checkNotNull(jobService, "jobService shouldn't be null!");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");
        this.researchService = Preconditions.checkNotNull(researchService, "researchService shouldn't be null!");
        this.userService = userService;
    }

    @Override
    public void tick(@Nonnull final Tick today) {
        this.today = Preconditions.checkNotNull(today, "today must not be empty");

        LOGGER.info("Tick planets");
        tickPlanets();
    }


    private void log(@Nonnull final Planet planet, @Nonnull final String msg) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(msg, "msg must not be empty");

        LOGGER.info("[Planet #{}] {}", planet.getId(), msg);
    }

    private void log(@Nonnull final Planet planet, @Nonnull final Job job, @Nonnull final String msg) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(job, "job must not be empty");
        Preconditions.checkNotNull(msg, "msg must not be empty");

        LOGGER.info("[Planet #{} - #{}] [Job #{}] {}", planet.getId(), planet.getName(), job.getId(), msg);
    }

    /**
     * Runs the tick for all planets.
     */
    private void tickPlanets() {
        Preconditions.checkNotNull(today, "today must not be empty");

        final Set<Integer> users = userService.findAllUserIDs();
        for (final Integer idUser : users) {
            final Job researchJob = jobService.findAllResearchJobsForUser(idUser).stream()
                    .findFirst()
                    .orElse(null);
            if (researchJob == null) {
                continue;
            }
            tickResearch(idUser, researchJob);
        }
    }

    private void tickResearch(final int idUser, @Nonnull final Job researchJob) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(researchJob, "researchJob must not be empty");

        final Planet researchPlanet = planetService.findResearchPlanet(idUser);
        assert researchPlanet != null;
        final long empireWideResearchPointsLeftOver = planetService.getEmpireWideResearchPoints(idUser).getEmpireWideResearchPointsLeftOver();
        final long usedPoints = tickJob(researchJob, empireWideResearchPointsLeftOver);
        if (!researchJob.isFinished()) {
            jobService.save(researchJob);
            planetService.reduceResearchPoints(idUser, usedPoints);
            log(researchPlanet, researchJob, "Shifting job for tick after " + today + ".");
            return;
        }
        completeResearch(researchPlanet, researchJob, usedPoints, today);
        jobService.save(researchJob);
    }

    private void completeResearch(@Nonnull final Planet planet, @Nonnull final Job job, final long usedPoints, @Nonnull final Tick today) {
        Preconditions.checkNotNull(planet, "planet must not be empty");
        Preconditions.checkNotNull(job, "job must not be empty");
        Preconditions.checkNotNull(today, "today must not be empty");

        log(planet, job, "Start processing research job.");
        job.setFinished(today);
        final Constructable constructable = job.getConstructable();
        final User owner = planet.getHumanOwner();
        if (owner == null) {
            throw new NotifyWebUserException("There must be a planet's owner.");
        }
        final Research research = constructable.getResearch();
        if (research == null) {
            throw new NotifyWebUserException("Oh fuck, this should not happen while research whatever!");
        }
        researchService.addResearch(owner, List.of(research));
        planetService.reduceResearchPoints(owner.getId(), usedPoints);
        log(planet, job, "Done processing research job.");
    }

    /**
     * Counts down the remaining {@link Job#getPointsLeft()}.
     *
     * @param job the {@link Job} to do
     * @return the used points will be returned
     */
    private long tickJob(@Nonnull final Job job, final long points) {
        Preconditions.checkNotNull(job, "job shouldn't be null!");

        return job.tick(points);
    }

    @Nonnull
    public Job tickInstaResearch(@Nonnull final Job job, @Nonnull final EmpireResearchCapability capability, @Nonnull final Tick today) {
        Preconditions.checkNotNull(job, "job must not be empty");
        Preconditions.checkNotNull(capability, "capability must not be empty");
        Preconditions.checkNotNull(today, "today must not be empty");

        final Planet planet = planetService.find(job.getFacility().getPlanet());
        Preconditions.checkNotNull(planet, "planet must not be empty");

        if (job.getPointsLeft() <= capability.getEmpireWideResearchPointsLeftOver()) {
            final long usedPoints = tickJob(job, capability.getEmpireWideResearchPointsLeftOver());
            completeResearch(job.getFacility().getPlanet(), job, usedPoints, today);
            return jobService.save(job);
        }
        return job;
    }
}
