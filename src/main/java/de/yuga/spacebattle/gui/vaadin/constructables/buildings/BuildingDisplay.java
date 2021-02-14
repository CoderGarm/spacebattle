package de.yuga.spacebattle.gui.vaadin.constructables.buildings;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.backend.entities.buildings.Building;

import javax.annotation.Nonnull;

public class BuildingDisplay extends VerticalLayout {

    public BuildingDisplay(@Nonnull final Building building) {
        Preconditions.checkNotNull(building, "building shouldn't be null!");

        H5 name = new H5(building.getName());
        Label description = new Label(building.getDescription());

        add(name, description);
    }
}
