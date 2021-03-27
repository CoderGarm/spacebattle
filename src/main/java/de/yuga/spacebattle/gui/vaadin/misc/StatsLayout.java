package de.yuga.spacebattle.gui.vaadin.misc;

import com.vaadin.flow.component.Component;
import de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.ShipClassLayout;
import de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details.ShipClassStatDisplay;
import de.yuga.spacebattle.gui.vaadin.misc.details.StatsDrawer;
import de.yuga.spacebattle.gui.vaadin.orbitals.PlanetLayout;
import de.yuga.spacebattle.gui.vaadin.orbitals.details.PlanetResourceDisplay;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Layout definition for view pages with statistics section.
 *
 * @param <T> the type definition of "what the page is for", e.g. {@link PlanetLayout} or {@link ShipClassLayout}
 */
public interface StatsLayout<T> {

    /**
     * Must return the components which should be displayed in the statistics section, {@link StatsDrawer}.
     *
     * @return the component, e.g. {@link PlanetResourceDisplay} or {@link ShipClassStatDisplay}
     */
    @Nonnull
    Component getStatisticsComponent();

    /**
     * Must update the full chain of used components based on the given parameter.
     * The full chain must be null-safe in that way that a null parameter will display a "fresh" view without values,
     * in other words: to clear a view.
     *
     * @param value the object which holds every necessary data
     */
    void update(@Nullable T value);
}
