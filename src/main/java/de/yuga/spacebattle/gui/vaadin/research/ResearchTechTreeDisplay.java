package de.yuga.spacebattle.gui.vaadin.research;

import com.vaadin.flow.data.binder.Binder;
import de.yuga.spacebattle.backend.entities.account.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ResearchTechTreeDisplay extends ResearchLayout<User> {

    @Nonnull
    private final Binder<User> binderPlanet = new Binder<>(User.class);

    public ResearchTechTreeDisplay() {

        binderPlanet.forField(getResearchOutputDisplay()).bind(user -> user, null);
    }

    @Override
    public void updateStatistics(@Nullable final User user) {
        binderPlanet.readBean(user);
    }

}
