package de.yuga.spacebattle.gui.vaadin.research.details;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
import de.yuga.spacebattle.gui.vaadin.misc.details.misc.HasNameAndDescriptionDisplayHorizontal;
import de.yuga.spacebattle.gui.vaadin.misc.details.misc.HasNameAndDescriptionDisplayVertical;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.HullDisplay;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Vaadin component to display all necessary information about a research and a possibility to start a research job.
 */
public class ResearchEdit extends HorizontalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<ResearchEdit, ResearchLevelDTO>, ResearchLevelDTO> {

    @Nonnull
    private static final String RESEARCH = "Do research";

    @Nonnull
    private static final String JOB_IN_PROGRESS = "Job in progress";

    @Nonnull
    private final Binder<ResearchLevelDTO> binder = new Binder<>(ResearchLevelDTO.class);

    @Nonnull
    private AccordionPanel accordionPanel = new AccordionPanel();

    @Nonnull
    private final Accordion accordion;

    @Nullable
    private ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ResearchEdit, ResearchLevelDTO>> listener;

    @Nonnull
    private final Button submit = new Button(RESEARCH);

    public ResearchEdit() {
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
            research.getUnlocksBuildings().forEach(building -> {
                final HasNameAndDescriptionDisplayHorizontal buildingDisplay = new HasNameAndDescriptionDisplayHorizontal();
                buildingDisplay.setValue(building);
                accordionPanel.addContent(buildingDisplay);
            });

            research.getUnlocksHulls().forEach(hull -> {
                final HullDisplay hullDisplay = new HullDisplay();
                hullDisplay.setValue(hull);
                accordionPanel.addContent(hullDisplay);
            });

            research.getUnlocksArmor().forEach(module -> {
                final HasNameAndDescriptionDisplayVertical moduleDisplay = new HasNameAndDescriptionDisplayVertical();
                moduleDisplay.setValue(module);
                accordionPanel.addContent(moduleDisplay);
            });

            research.getUnlocksElectronicWarfare().forEach(module -> {
                final HasNameAndDescriptionDisplayVertical moduleDisplay = new HasNameAndDescriptionDisplayVertical();
                moduleDisplay.setValue(module);
                accordionPanel.addContent(moduleDisplay);
            });

            research.getUnlocksPropulsion().forEach(module -> {
                final HasNameAndDescriptionDisplayVertical moduleDisplay = new HasNameAndDescriptionDisplayVertical();
                moduleDisplay.setValue(module);
                accordionPanel.addContent(moduleDisplay);
            });

            research.getUnlocksSidewall().forEach(module -> {
                final HasNameAndDescriptionDisplayVertical moduleDisplay = new HasNameAndDescriptionDisplayVertical();
                moduleDisplay.setValue(module);
                accordionPanel.addContent(moduleDisplay);
            });

            research.getUnlocksWeapons().forEach(module -> {
                final HasNameAndDescriptionDisplayVertical moduleDisplay = new HasNameAndDescriptionDisplayVertical();
                moduleDisplay.setValue(module);
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
        final VerticalLayout levelV = new VerticalLayout();
        final VerticalLayout delimiterV = new VerticalLayout();
        final VerticalLayout levelCapV = new VerticalLayout();
        final Label delimiter = new Label("/");
        final HorizontalLayout groupH = new HorizontalLayout();
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

        submit.addClickListener(event -> {
            final AbstractField.ComponentValueChangeEvent<ResearchEdit, ResearchLevelDTO> changeEvent =
                    new AbstractField.ComponentValueChangeEvent<>(this, this, getValue(), false);
            listener.valueChanged(changeEvent);
        });
        levelV.add(submit);

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
    public void setValue(ResearchLevelDTO value) {
        binder.setBean(value);
        updateResearchDetails(value.getResearch());

    }

    @Nullable
    @Override
    public ResearchLevelDTO getValue() {
        return binder.getBean();
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ResearchEdit, ResearchLevelDTO>> listener) {
        this.listener = listener;
        return new Registration() {
            @Override
            public void remove() {
                removeListener();
            }
        };
    }

    private void removeListener() {
        this.listener = null;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        submit.setText(readOnly ? JOB_IN_PROGRESS : RESEARCH);
        submit.setEnabled(!readOnly);
    }

    @Override
    public boolean isReadOnly() {
        return !submit.isEnabled();
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
