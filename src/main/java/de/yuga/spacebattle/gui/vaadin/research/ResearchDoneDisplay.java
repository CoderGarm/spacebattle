package de.yuga.spacebattle.gui.vaadin.research;

import com.vaadin.flow.data.binder.Binder;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.gui.vaadin.research.details.ResearchDisplayMulti;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Vaadin component to display every researched research.
 */
public class ResearchDoneDisplay extends ResearchLayout<User> {

    @Nonnull
    private final Binder<User> binderPlanet = new Binder<>(User.class);

    public ResearchDoneDisplay() {

        binderPlanet.forField(getResearchOutputDisplay()).bind(user -> user, null);

        final ResearchDisplayMulti researchDisplayMulti = new ResearchDisplayMulti();
        binderPlanet.forField(researchDisplayMulti).bind(User::getResearches, null);
        add(researchDisplayMulti);
    }

    @Override
    public void updateStatistics(@Nullable final User user) {
        binderPlanet.readBean(user);
    }

}
