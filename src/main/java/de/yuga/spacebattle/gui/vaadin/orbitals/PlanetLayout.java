package de.yuga.spacebattle.gui.vaadin.orbitals;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.gui.vaadin.misc.StatisticsDisplay;
import de.yuga.spacebattle.gui.vaadin.misc.StatsLayout;
import de.yuga.spacebattle.gui.vaadin.orbitals.details.PlanetStatisticsDisplay;

import javax.annotation.Nonnull;

public abstract class PlanetLayout<GenericSubject> extends VerticalLayout implements StatsLayout<GenericSubject> {

    private final PlanetStatisticsDisplay planetStatisticsDisplay = new PlanetStatisticsDisplay();

    @Nonnull
    public PlanetStatisticsDisplay getPlanetResourceDisplay() {
        return planetStatisticsDisplay;
    }

    @Nonnull
    @Override
    public StatisticsDisplay getStatisticsComponent() {
        return planetStatisticsDisplay;
    }
}
