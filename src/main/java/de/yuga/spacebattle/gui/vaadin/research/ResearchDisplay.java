package de.yuga.spacebattle.gui.vaadin.research;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
import de.yuga.spacebattle.gui.vaadin.buildings.BuildingDisplay;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.HullDisplay;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.ModuleDisplay;

import javax.annotation.Nonnull;

public class ResearchDisplay extends HorizontalLayout {

    public ResearchDisplay(@Nonnull final Research research, final int level) {
        Preconditions.checkNotNull(research, "research shouldn't be null!");

        VerticalLayout mainV = new VerticalLayout();
        HorizontalLayout groupH = new HorizontalLayout();
        HorizontalLayout descH = new HorizontalLayout();

        VerticalLayout levelV = new VerticalLayout();
        VerticalLayout delimiterV = new VerticalLayout();
        VerticalLayout levelCapV = new VerticalLayout();

        Label name = new Label(research.getName());
        Label description = new Label(research.getDescription());
        Label delimiter = new Label("/");
        Label levelLabel = new Label("Level");
        Label levelCapLabel = new Label("Level Cap");
        Label levelValue = new Label(String.valueOf(level));
        Label levelCapValue = new Label(String.valueOf(research.getLevelCap()));

        Accordion accordion = new Accordion();
        ViewHelper.setWidth(accordion, null);
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Unlocks");
        research.getUnlocksBuildings().forEach(building ->
                accordionPanel.addContent(new BuildingDisplay(building)));

        research.getUnlocksHulls().forEach(hull ->
        {
            HullDisplay hullDisplay = new HullDisplay();
            hullDisplay.update(hull);
            accordionPanel.addContent(hullDisplay);
        });

        research.getUnlocksModules().forEach(module -> {
            ModuleDisplay moduleDisplay = new ModuleDisplay();
            moduleDisplay.update(module, null);
            accordionPanel.addContent(moduleDisplay);
        });
        accordion.add(accordionPanel);
        ViewHelper.setWidth(accordion, "250px");

        descH.add(name, description);

        levelV.add(levelLabel, levelValue);
        delimiterV.add(delimiter, delimiter);
        levelCapV.add(levelCapLabel, levelCapValue);
        groupH.add(levelV, delimiterV, levelCapV);
        groupH.setJustifyContentMode(JustifyContentMode.START);
        groupH.setMargin(true);

        HorizontalLayout combinatorH = new HorizontalLayout();
        FlexLayout fl = new FlexLayout();
        fl.setMaxHeight("70%");
        fl.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        fl.add(accordion);

        combinatorH.add(groupH, fl);
        combinatorH.setMargin(true);

        mainV.add(descH, combinatorH);
        add(mainV);
    }
}
