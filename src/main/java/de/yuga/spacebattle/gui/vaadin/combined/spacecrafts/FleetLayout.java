package de.yuga.spacebattle.gui.vaadin.combined.spacecrafts;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.gui.vaadin.combined.spacecrafts.details.FleetStatisticsDisplay;
import de.yuga.spacebattle.gui.vaadin.misc.StatisticsDisplay;
import de.yuga.spacebattle.gui.vaadin.misc.StatsLayout;

import javax.annotation.Nonnull;

public abstract class FleetLayout<GenericSubject> extends VerticalLayout implements StatsLayout<GenericSubject> {

    private final FleetStatisticsDisplay fleetStatisticsDisplay = new FleetStatisticsDisplay();

    @Nonnull
    public FleetStatisticsDisplay getFleetStatsDisplay() {
        return fleetStatisticsDisplay;
    }

    @Nonnull
    @Override
    public StatisticsDisplay getStatisticsComponent() {
        return fleetStatisticsDisplay;
    }
}
