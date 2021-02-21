package de.yuga.spacebattle.gui.vaadin.constructables.buildings;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.backend.entities.buildings.Building;

import javax.annotation.Nonnull;
import java.util.Map;

public class ConstructBuildingEditMulti extends VerticalLayout {

    public ConstructBuildingEditMulti(@Nonnull final Map<Building, Integer> targetBuildings) {
        Preconditions.checkNotNull(targetBuildings, "targetBuildings shouldn't be null!");

        targetBuildings.forEach((building, integer) -> {
            add(new ConstructBuildingEdit(building, integer));
        });
    }
}
