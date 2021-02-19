package de.yuga.spacebattle.gui.vaadin.constructables;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.Constructable;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.gui.vaadin.constructables.buildings.BuildingDisplay;
import de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.ShipClassDisplay;
import de.yuga.spacebattle.gui.vaadin.research.ResearchDisplay;

import javax.annotation.Nonnull;

public class ConstructableDisplay extends VerticalLayout {

    public ConstructableDisplay(@Nonnull final Constructable constructable) {
        Preconditions.checkNotNull(constructable, "constructable shouldn't be null!");

        EResourceType resourceType = constructable.getResourceType();
        Integer targetLevel = constructable.getTargetLevel();

        switch (resourceType) {
            case RESEARCH:
                Research research = constructable.getResearch();
                if (research == null) {
                    throw new NotifySBUserException("The research cannot be null here.");
                }
                if (targetLevel == null) {
                    throw new NotifySBUserException("The targetLevel cannot be null here.");
                }
                ResearchDisplay researchDisplay = new ResearchDisplay(research, targetLevel);
                add(researchDisplay);
                break;
            case CONSTRUCTION:
                Building building = constructable.getBuilding();
                if (building == null) {
                    throw new NotifySBUserException("The building cannot be null here.");
                }
                if (targetLevel == null) {
                    throw new NotifySBUserException("The targetLevel cannot be null here.");
                }
                BuildingDisplay buildingDisplay = new BuildingDisplay(building);
                Label levelValue = new Label("Target Level: " + targetLevel);
                add(levelValue, buildingDisplay);
                break;
            case ORBITALCONSTRUCTION:
                ShipClass shipClass = constructable.getShipClass();
                Integer amountShips = constructable.getAmountShips();
                if (shipClass == null) {
                    throw new NotifySBUserException("The shipClass cannot be null here.");
                }
                if (amountShips == null) {
                    throw new NotifySBUserException("The amountShips cannot be null here.");
                }
                ShipClassDisplay shipClassDisplay = new ShipClassDisplay(shipClass, amountShips);
                add(shipClassDisplay);
                break;
        }
    }
}
