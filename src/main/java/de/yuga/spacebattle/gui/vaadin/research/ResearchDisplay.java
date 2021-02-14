package de.yuga.spacebattle.gui.vaadin.research;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.gui.vaadin.constructables.buildings.BuildingDisplay;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.HullDisplay;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.ModuleDisplay;

import javax.annotation.Nonnull;

public class ResearchDisplay extends VerticalLayout {

    public ResearchDisplay(@Nonnull final Research research) {
        Preconditions.checkNotNull(research, "research shouldn't be null!");

        H5 nameTitle = new H5(research.getName());
        Label description = new Label(research.getDescription());

        H5 unlocksBuildingTitle = new H5("Unlocks building");
        Accordion unlocksBuildingAccordion = new Accordion();
        AccordionPanel unlocksBuildingPanel = new AccordionPanel();
        unlocksBuildingAccordion.add(unlocksBuildingPanel);
        research.getUnlocksBuildings().forEach(building ->
                unlocksBuildingPanel.addContent(new BuildingDisplay(building)));

        H5 unlocksHullTitle = new H5("Unlocks building");
        Accordion unlocksHullAccordion = new Accordion();
        AccordionPanel unlocksHullPanel = new AccordionPanel();
        unlocksHullAccordion.add(unlocksHullPanel);
        research.getUnlocksHulls().forEach(hull -> unlocksHullPanel.addContent(new HullDisplay(hull)));

        H5 unlocksModuleTitle = new H5("Unlocks building");
        Accordion unlocksModuleAccordion = new Accordion();
        AccordionPanel unlocksModulePanel = new AccordionPanel();
        unlocksModuleAccordion.add(unlocksModulePanel);
        research.getUnlocksModules().forEach(module -> unlocksModulePanel.addContent(new ModuleDisplay(module)));


        add(nameTitle, description,
                unlocksBuildingTitle, unlocksBuildingAccordion,
                unlocksHullTitle, unlocksHullAccordion,
                unlocksModuleTitle, unlocksModuleAccordion
        );
    }
}
