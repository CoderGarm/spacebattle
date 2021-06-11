package de.yuga.spacebattle.gui.vaadin.constructables;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.Constructable;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.ShipClassDisplay;
import de.yuga.spacebattle.gui.vaadin.misc.details.misc.HasNameAndDescriptionDisplayHorizontal;
import de.yuga.spacebattle.gui.vaadin.research.details.ResearchDisplay;
import de.yuga.spacebattle.gui.vaadin.research.details.ResearchLevelDTO;

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
                ResearchDisplay researchDisplay = new ResearchDisplay();
                researchDisplay.setValue(new ResearchLevelDTO(research, targetLevel));
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
                HasNameAndDescriptionDisplayHorizontal hasNameAndDescriptionDisplayHorizontal = new HasNameAndDescriptionDisplayHorizontal();
                hasNameAndDescriptionDisplayHorizontal.setValue(building);
                Label levelValue = new Label("Target Level: " + targetLevel);
                add(levelValue, hasNameAndDescriptionDisplayHorizontal);
                break;
            case ORBITAL_CONSTRUCTION:
                ShipClass shipClass = constructable.getShipClass();
                Integer amountShips = constructable.getAmountShips();
                if (shipClass == null) {
                    throw new NotifySBUserException("The shipClass cannot be null here.");
                }
                if (amountShips == null) {
                    throw new NotifySBUserException("The amountShips cannot be null here.");
                }
                ShipClassDisplay shipClassDisplay = new ShipClassDisplay();
                shipClassDisplay.updateStatistics(shipClass);
                add(shipClassDisplay, new Label("Amount: " + amountShips));
                break;
        }
    }
}
