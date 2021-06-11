package de.yuga.spacebattle.gui.vaadin.research.details;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModule;
import de.yuga.spacebattle.gui.vaadin.misc.details.misc.HasNameAndDescriptionDisplayHorizontal;
import de.yuga.spacebattle.gui.vaadin.misc.details.misc.HasNameAndDescriptionDisplayVertical;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.HullDisplay;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/**
 * Vaadin component to display all relevant information about a particular research.
 */
public class ResearchDisplay extends HorizontalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<ResearchDisplay, ResearchLevelDTO>, ResearchLevelDTO> {

    @Nonnull
    private final Binder<ResearchLevelDTO> binder = new Binder<>(ResearchLevelDTO.class);

    @Nonnull
    private Details unlocksResearchDetails;

    public ResearchDisplay() {
        final VerticalLayout mainLayout = new VerticalLayout();

        final HorizontalLayout generalInfoLayout = createGeneralInfoLayout();
        final HorizontalLayout levelInfoLayout = createLevelInfoLayout();
        unlocksResearchDetails = createUnlocksDetails();

        final HorizontalLayout combinatorH = new HorizontalLayout();
        final FlexLayout fl = new FlexLayout();
        fl.setMaxHeight("70%");
        fl.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        fl.add(unlocksResearchDetails);

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
        if (research != null) {
            research.getUnlocksBuildings().forEach(building -> {
                HasNameAndDescriptionDisplayHorizontal hasNameAndDescriptionDisplayHorizontal = new HasNameAndDescriptionDisplayHorizontal();
                hasNameAndDescriptionDisplayHorizontal.setValue(building);
                activate(hasNameAndDescriptionDisplayHorizontal);
            });

            research.getUnlocksHulls().forEach(hull -> {
                HullDisplay hullDisplay = new HullDisplay();
                hullDisplay.setValue(hull);
                activate(hullDisplay);
            });

            // todo module individuell darstellen
            final Set<BaseModule> modulesSet = new HashSet<>();

            modulesSet.addAll(research.getUnlocksArmor());
            modulesSet.addAll(research.getUnlocksPropulsion());
            modulesSet.addAll(research.getUnlocksSidewall());
            modulesSet.addAll(research.getUnlocksElectronicWarfare());
            modulesSet.addAll(research.getUnlocksWeapons());

            modulesSet.forEach(module -> {
                final HasNameAndDescriptionDisplayVertical display = new HasNameAndDescriptionDisplayVertical();
                display.setValue(module);
                activate(display);
            });
        } else {
            inactivate();
        }
    }

    @Nonnull
    private Details createUnlocksDetails() {
        unlocksResearchDetails = new Details();
        unlocksResearchDetails.setSummaryText("Unlocks researches");
        unlocksResearchDetails.addClassName("unlocks-researches-detail");
        unlocksResearchDetails.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED);
        return unlocksResearchDetails;
    }


    /**
     * Sets a view and it's detail to usable and viewable for the user.
     *
     * @param content the content
     */
    private void activate(@Nonnull final Component content) {
        Preconditions.checkNotNull(content, "content shouldn't be null!");

        if (!unlocksResearchDetails.isEnabled()) {
            unlocksResearchDetails.setEnabled(true);
        }
        unlocksResearchDetails.addContent(content);
    }

    /**
     * Sets the detail in a not-usable state for the user.
     */
    private void inactivate() {
        unlocksResearchDetails.setSummaryText("Unlocks nothing");
        unlocksResearchDetails.setEnabled(false);
        unlocksResearchDetails.setContent(null);
        unlocksResearchDetails.setOpened(false);
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
