package de.yuga.spacebattle.gui.vaadin.combined.spacecrafts;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.gui.vaadin.combined.spacecrafts.details.FleetStatsDisplay;
import de.yuga.spacebattle.gui.vaadin.misc.StatsLayout;

import javax.annotation.Nonnull;

public abstract class FleetLayout<T> extends VerticalLayout implements StatsLayout<T> {

    private final FleetStatsDisplay fleetStatsDisplay = new FleetStatsDisplay();

    @Nonnull
    public FleetStatsDisplay getFleetStatsDisplay() {
        return fleetStatsDisplay;
    }

    @Nonnull
    @Override
    public Component getStatisticsComponent() {
        return fleetStatsDisplay;
    }
}
