package de.yuga.spacebattle.gui.vaadin.research.details;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
import de.yuga.spacebattle.gui.vaadin.buildings.BuildingDisplay;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.HullDisplay;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.ModuleDisplay;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Vaadin component to display all relevant information about a particular research.
 */
public class ResearchDisplay extends HorizontalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<ResearchDisplay, ResearchLevelDTO>, ResearchLevelDTO> {

    @Nonnull
    private final Binder<ResearchLevelDTO> binder = new Binder<>(ResearchLevelDTO.class);

    @Nonnull
    private AccordionPanel accordionPanel = new AccordionPanel();

    @Nonnull
    private final Accordion accordion;

    public ResearchDisplay() {
        final VerticalLayout mainLayout = new VerticalLayout();

        final HorizontalLayout generalInfoLayout = createGeneralInfoLayout();
        final HorizontalLayout levelInfoLayout = createLevelInfoLayout();
        accordion = createUnlocksAccordion();

        final HorizontalLayout combinatorH = new HorizontalLayout();
        final FlexLayout fl = new FlexLayout();
        fl.setMaxHeight("70%");
        fl.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        fl.add(accordion);

        combinatorH.add(levelInfoLayout, fl);
        combinatorH.setMargin(true);

        mainLayout.add(generalInfoLayout, combinatorH);
        add(mainLayout);
        setVisible(false);
    }

    /**
     * Updates the display of the given research.
     *
     * @param research the parameter
     */
    private void updateResearchDetails(@Nullable final Research research) {
        accordion.remove(accordionPanel);
        accordionPanel = new AccordionPanel();
        if (research != null) {
            accordionPanel.setSummaryText("Unlocks");
            research.getUnlocksBuildings().forEach(building ->
            {
                BuildingDisplay buildingDisplay = new BuildingDisplay();
                buildingDisplay.setValue(building);
                accordionPanel.addContent(buildingDisplay);
            });

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
        } else {
            accordionPanel.setSummaryText("Unlocks nothing");
        }
        accordion.add(accordionPanel);
    }

    /**
     * Simple accordion generation.
     *
     * @return the accordion
     */
    @Nonnull
    private Accordion createUnlocksAccordion() {
        final Accordion accordion = new Accordion();
        ViewHelper.setWidth(accordion, null);

        accordion.add(accordionPanel);
        ViewHelper.setWidth(accordion, "250px");

        return accordion;
    }

    /**
     * Simple layout generation for level and level cap.
     *
     * @return the layout
     */
    @Nonnull
    private HorizontalLayout createLevelInfoLayout() {
        final HorizontalLayout groupH = new HorizontalLayout();
        final VerticalLayout levelV = new VerticalLayout();
        final VerticalLayout delimiterV = new VerticalLayout();
        final VerticalLayout levelCapV = new VerticalLayout();

        final Label delimiter = new Label("/");
        final Label levelLabel = new Label("Level");
        final Label levelValue = new Label();
        final ReadOnlyHasValue<String> levelValueText = new ReadOnlyHasValue<>(levelValue::setText);
        binder.forField(levelValueText).bind(researchLevelWrapper -> String.valueOf(researchLevelWrapper.getLevel()), null);
        levelV.add(levelLabel, levelValue);

        final Label levelCapLabel = new Label("Level Cap");
        final Label levelCapValue = new Label();
        final ReadOnlyHasValue<String> levelCapValueText = new ReadOnlyHasValue<>(levelCapValue::setText);
        binder.forField(levelCapValueText).bind(researchLevelWrapper -> String.valueOf(researchLevelWrapper.getResearch().getLevelCap()), null);

        levelCapV.add(levelCapLabel, levelCapValue);
        delimiterV.add(delimiter, delimiter);
        groupH.add(levelV, delimiterV, levelCapV);
        groupH.setJustifyContentMode(JustifyContentMode.START);
        groupH.setMargin(true);

        return groupH;
    }

    /**
     * Simple layout generation for fields name and description.
     *
     * @return the layout
     */
    @Nonnull
    private HorizontalLayout createGeneralInfoLayout() {
        final HorizontalLayout generalInfoLayout = new HorizontalLayout();
        final Label name = new Label();
        final ReadOnlyHasValue<String> nameText = new ReadOnlyHasValue<>(name::setText);
        binder.forField(nameText).bind(researchLevelWrapper -> researchLevelWrapper.getResearch().getName(), null);

        final Label description = new Label();
        final ReadOnlyHasValue<String> descriptionText = new ReadOnlyHasValue<>(description::setText);
        binder.forField(descriptionText).bind(researchLevelWrapper -> researchLevelWrapper.getResearch().getDescription(), null);
        generalInfoLayout.add(name, description);
        return generalInfoLayout;
    }

    @Override
    public void setValue(@Nullable final ResearchLevelDTO value) {

        binder.readBean(value);
        setVisible(value != null);
        updateResearchDetails(value != null ? value.getResearch() : null);
    }

    @Nullable
    @Override
    public ResearchLevelDTO getValue() {
        // not necessary
        return null;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ResearchDisplay, ResearchLevelDTO>> listener) {
        // not necessary
        return null;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        // not necessary
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        // not necessary
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return false;
    }
}
