package de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details.ShipClassStatDisplay;
import de.yuga.spacebattle.gui.vaadin.misc.StatsLayout;

import javax.annotation.Nonnull;

public abstract class ShipClassLayout<T> extends VerticalLayout implements StatsLayout<T> {

    private final ShipClassStatDisplay shipClassStatDisplay = new ShipClassStatDisplay();

    @Nonnull
    public ShipClassStatDisplay getShipClassStatDisplay() {
        return shipClassStatDisplay;
    }

    @Override
    public Component getStatisticsComponent() {
        return shipClassStatDisplay;
    }
}
