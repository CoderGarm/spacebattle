package de.yuga.spacebattle.gui.vaadin.research;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.data.binder.Binder;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.Constructable;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.researches.ResearchService;
import de.yuga.spacebattle.backend.services.turn.JobService;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
import de.yuga.spacebattle.gui.vaadin.research.details.ResearchDisplay;
import de.yuga.spacebattle.gui.vaadin.research.details.ResearchEditMulti;
import de.yuga.spacebattle.gui.vaadin.research.details.ResearchLevelDTO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * Vaadin component to display all research possibilities which includes the option to start a research job.
 */
public class ResearchSelectionEdit extends ResearchLayout<User> {

    @Nonnull
    private final Binder<User> binderPlanet = new Binder<>(User.class);

    @Nonnull
    private final ResearchService researchService = ViewHelper.getService(ResearchService.class);

    @Nonnull
    private final PlanetService planetService = ViewHelper.getService(PlanetService.class);

    @Nonnull
    private final JobService jobService = ViewHelper.getService(JobService.class);

    @Nonnull
    private final UserService userService = ViewHelper.getService(UserService.class);

    @Nonnull
    private final ResearchDisplay currentResearchDisplay = new ResearchDisplay();

    public ResearchSelectionEdit() {

        binderPlanet.forField(getResearchOutputDisplay()).bind(user -> user, null);

        final Label currentResearch = new Label("Current research");
        add(currentResearch, currentResearchDisplay);

        final ResearchEditMulti researchDisplayMulti = new ResearchEditMulti();
        binderPlanet.forField(researchDisplayMulti).bind(researchService::getUnlockableResearches, null);

        final Label possibleResearch = new Label("Possible researches");
        add(possibleResearch, researchDisplayMulti);
    }

    @Override
    public void update(@Nullable User user) {
        if (user == null) {
            currentResearchDisplay.setValue(null);
            return;
        }
        user = userService.findWithResearches(user);
        binderPlanet.readBean(user);

        final Planet researchPlanet = planetService.findResearchPlanet(user);
        if (researchPlanet != null) {
            final Construction researchFacility = researchPlanet.getConstructionByResource(EResourceType.RESEARCH);
            if (researchFacility == null) {
                throw new NotifySBUserException("This should not work - try to use the correct planet!");
            }
            final List<Job> allResearchJobs = jobService.findAllJobsForConstruction(researchFacility);
            allResearchJobs.stream().filter(Objects::nonNull).findFirst().ifPresent(job -> {
                final Constructable constructable = job.getConstructable();
                final Research research = constructable.getResearch();
                final Integer targetLevel = constructable.getTargetLevel();
                if (research == null || targetLevel == null) {
                    throw new NotifySBUserException("This should not work - try to use the real research!");
                }
                currentResearchDisplay.setValue(new ResearchLevelDTO(research, targetLevel));
            });
        }
    }
}
