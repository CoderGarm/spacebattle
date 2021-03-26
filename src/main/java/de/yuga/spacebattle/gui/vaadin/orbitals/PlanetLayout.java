package de.yuga.spacebattle.gui.vaadin.orbitals;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.gui.vaadin.misc.StatsLayout;
import de.yuga.spacebattle.gui.vaadin.orbitals.details.PlanetResourceDisplay;

import javax.annotation.Nonnull;

public abstract class PlanetLayout<T> extends VerticalLayout implements StatsLayout<T> {

    private final PlanetResourceDisplay planetResourceDisplay = new PlanetResourceDisplay();

    @Nonnull
    public PlanetResourceDisplay getPlanetResourceDisplay() {
        return planetResourceDisplay;
    }

    @Override
    public Component getStatisticsComponent() {
        return planetResourceDisplay;
    }
}
