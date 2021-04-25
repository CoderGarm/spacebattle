package de.yuga.spacebattle.gui.vaadin.research;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.data.binder.Binder;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.Constructable;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.services.researches.ResearchService;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
import de.yuga.spacebattle.gui.vaadin.research.details.ResearchDisplay;
import de.yuga.spacebattle.gui.vaadin.research.details.ResearchEditMulti;
import de.yuga.spacebattle.gui.vaadin.research.details.ResearchLevelDTO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    private final ResearchDisplay researchDisplay = new ResearchDisplay();

    public ResearchSelectionEdit() {

        binderPlanet.forField(getResearchOutputDisplay()).bind(user -> user, null);

        final Label currentResearch = new Label("Current research");
        add(currentResearch, researchDisplay);

        final ResearchEditMulti researchDisplayMulti = new ResearchEditMulti();
        binderPlanet.forField(researchDisplayMulti).bind(researchService::getUnlockableResearches, null);

        final Label possibleResearch = new Label("Possible researches");
        add(possibleResearch, researchDisplayMulti);
    }

    @Override
    public void update(@Nullable final User user) {
        binderPlanet.readBean(user);
        if (user == null) {
            researchDisplay.setValue(null);
            return;
        }
        user.getResearchInstitute().ifPresent(planet -> {
            final Construction researchFacility = planet.getConstructionByResource(EResourceType.RESEARCH);
            if (researchFacility == null) {
                throw new NotifySBUserException("This should not work - try to use the correct planet!");
            }
            researchFacility.getJobs().stream().filter(Objects::nonNull).findFirst().ifPresent(job -> {
                Constructable constructable = job.getConstructable();
                Research research = constructable.getResearch();
                Integer targetLevel = constructable.getTargetLevel();
                if (research == null || targetLevel == null) {
                    throw new NotifySBUserException("This should not work - try to use the real research!");
                }
                researchDisplay.setValue(new ResearchLevelDTO(research, targetLevel));
            });
        });
    }

}
