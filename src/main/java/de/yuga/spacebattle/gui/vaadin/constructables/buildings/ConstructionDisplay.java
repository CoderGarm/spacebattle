package de.yuga.spacebattle.gui.vaadin.constructables.buildings;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.gui.vaadin.buildings.BuildingDisplay;

import javax.annotation.Nonnull;

public class ConstructionDisplay extends HorizontalLayout {

    public ConstructionDisplay(@Nonnull final Construction construction) {
        Preconditions.checkNotNull(construction, "construction shouldn't be null!");

        Building building = construction.getBuilding();
        int level = construction.getLevel();
        BuildingDisplay buildingDisplay = new BuildingDisplay(building);
        Label levelValue = new Label("Level: " + level);
        add(levelValue, buildingDisplay);
    }
}
