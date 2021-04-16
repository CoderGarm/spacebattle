package de.yuga.spacebattle.gui.vaadin.combined.spacecrafts;

import com.vaadin.flow.data.binder.Binder;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FleetDashboardDisplay extends FleetLayout<Fleet> {

    @Nonnull
    private final Binder<Fleet> binderFleet = new Binder<>(Fleet.class);

    public FleetDashboardDisplay() {

        binderFleet.forField(getFleetStatsDisplay()).bind(fleet -> fleet, null);

        GarrisonDisplay garrisonDisplay = new GarrisonDisplay();
        binderFleet.forField(garrisonDisplay).bind(fleet -> fleet, null);

        add(garrisonDisplay);

    }

    public void update(@Nullable final Fleet planet) {
        binderFleet.readBean(planet);
    }

}
