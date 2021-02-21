package de.yuga.spacebattle.gui.vaadin.buildings;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import de.yuga.spacebattle.backend.entities.buildings.Building;

import javax.annotation.Nonnull;

public class BuildingDisplay extends HorizontalLayout {

    public BuildingDisplay(@Nonnull final Building building) {
        Preconditions.checkNotNull(building, "building shouldn't be null!");

        Label name = new Label(building.getName());
        Label description = new Label(building.getDescription());

        add(name, description);
    }
}
