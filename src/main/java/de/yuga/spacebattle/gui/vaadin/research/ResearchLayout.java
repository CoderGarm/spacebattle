package de.yuga.spacebattle.gui.vaadin.research;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.gui.vaadin.misc.StatisticsDisplay;
import de.yuga.spacebattle.gui.vaadin.misc.StatsLayout;
import de.yuga.spacebattle.gui.vaadin.research.details.ResearchStatisticsDisplay;

import javax.annotation.Nonnull;

/**
 * General layout for research pages.
 *
 * @param <GenericSubject> the generic type of basic information
 */
public abstract class ResearchLayout<GenericSubject> extends VerticalLayout implements StatsLayout<GenericSubject> {

    private final ResearchStatisticsDisplay researchStatisticsDisplay = new ResearchStatisticsDisplay();

    @Nonnull
    public ResearchStatisticsDisplay getResearchOutputDisplay() {
        return researchStatisticsDisplay;
    }

    @Nonnull
    @Override
    public StatisticsDisplay getStatisticsComponent() {
        return researchStatisticsDisplay;
    }
}
