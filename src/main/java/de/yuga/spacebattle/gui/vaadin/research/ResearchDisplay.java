package de.yuga.spacebattle.gui.vaadin.research;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayoutVariant;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
import de.yuga.spacebattle.gui.vaadin.constructables.buildings.BuildingDisplay;
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
        VerticalLayout delmimiterV = new VerticalLayout();
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
                accordionPanel.addContent(new HullDisplay(hull)));

        research.getUnlocksModules().forEach(module ->
                accordionPanel.addContent(new ModuleDisplay(module)));
        accordion.add(accordionPanel);

        descH.add(name, description);

        levelV.add(levelLabel, levelValue);
        delmimiterV.add(delimiter, delimiter);
        levelCapV.add(levelCapLabel, levelCapValue);
        groupH.add(levelV, delmimiterV, levelCapV);
        groupH.setMaxWidth("50%");
        accordion.setMaxWidth("50%");

        SplitLayout split = new SplitLayout();
        split.setOrientation(SplitLayout.Orientation.VERTICAL);
        split.setSplitterPosition(110);
        split.addThemeVariants(SplitLayoutVariant.LUMO_MINIMAL);

        split.addToPrimary(groupH, accordion);
        //levelH.addToPrimary(groupH);
        //levelH.addToSecondary(accordion);

        mainV.add(descH, split);

        add(mainV);
    }
}
