package de.yuga.spacebattle.gui.vaadin.research;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.gui.vaadin.misc.StatsLayout;
import de.yuga.spacebattle.gui.vaadin.research.details.ResearchOutputDisplay;

import javax.annotation.Nonnull;

/**
 * General layout for research pages.
 *
 * @param <T> the generic type of basic information
 */
public abstract class ResearchLayout<T> extends VerticalLayout implements StatsLayout<T> {

    private final ResearchOutputDisplay researchOutputDisplay = new ResearchOutputDisplay();

    @Nonnull
    public ResearchOutputDisplay getResearchOutputDisplay() {
        return researchOutputDisplay;
    }

    @Nonnull
    @Override
    public Component getStatisticsComponent() {
        return researchOutputDisplay;
    }
}
