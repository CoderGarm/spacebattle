package de.yuga.spacebattle.gui.vaadin.constructables.buildings;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.buildings.ProductionType;
import de.yuga.spacebattle.backend.enums.EResolution;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
import de.yuga.spacebattle.gui.vaadin.buildings.BuildingLevelDTO;
import de.yuga.spacebattle.gui.vaadin.events.ESBEvent;
import de.yuga.spacebattle.gui.vaadin.misc.details.misc.ImageContainer;
import de.yuga.spacebattle.gui.vaadin.misc.details.misc.SimpleLabelWithCaption;
import de.yuga.spacebattle.gui.vaadin.misc.details.misc.TooltipDisplay;
import org.vaadin.spring.events.Event;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static de.yuga.spacebattle.gui.vaadin.events.ESBEvent.CONSTRUCTION_JOB_BUILDING_START;

@CssImport("./styles/views/main/details/construction-edit.css")
public class ConstructBuildingEdit extends VerticalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<ConstructBuildingEdit, BuildingLevelDTO>, BuildingLevelDTO> {

    @Nonnull
    private static final String BUILD = "Build";

    @Nonnull
    private static final String JOB_IN_PROGRESS = "Job in progress";

    @Nonnull
    private final EventBus.UIEventBus uiEventBus = ViewHelper.getService(EventBus.UIEventBus.class);

    @Nullable
    private BuildingLevelDTO buildingLevelDTO;

    @Nonnull
    private final Button build;

    @Nonnull
    private final Binder<BuildingLevelDTO> binder = new Binder<>(BuildingLevelDTO.class);

    public ConstructBuildingEdit() {
        addClassName("construction-frame");
        this.uiEventBus.subscribe(this);

        final ImageContainer imageContainer = new ImageContainer(EResolution.PX64);
        imageContainer.addClassName("construction-image");
        binder.forField(imageContainer).bind(w -> w, null);

        final HorizontalLayout nameContainer = new HorizontalLayout();
        final SimpleLabelWithCaption name = new SimpleLabelWithCaption("Name");
        binder.forField(name).bind(dto -> dto.getBuilding().getName(), null);

        final SimpleLabelWithCaption levelValue = new SimpleLabelWithCaption("Level");
        binder.forField(levelValue).bind(BuildingLevelDTO::getLevelString, null);

        nameContainer.add(name, levelValue, imageContainer);

        final SimpleLabelWithCaption description = new SimpleLabelWithCaption("Description");
        binder.forField(description).bind(dto -> dto.getBuilding().getDescription(), null);

        final Div productionTargetContainer = new Div();
        productionTargetContainer.addClassName("productionTargetContainer");
        final SimpleLabelWithCaption productionTarget = new SimpleLabelWithCaption("Production Target");
        productionTarget.addClassName("to-shrink");
        binder.forField(productionTarget).bind(dto -> dto.getBuilding().getProductionTarget().getSingularName(), null);

        final SimpleLabelWithCaption productionCategory = new SimpleLabelWithCaption("Production Category");
        productionCategory.addClassName("to-shrink");
        binder.forField(productionCategory).bind(dto -> dto.getBuilding().getProductionType().getProductionCategory().name(), null);

        final SimpleLabelWithCaption refinementSequence = new SimpleLabelWithCaption("Refinement Sequence");
        refinementSequence.addClassName("to-shrink");
        binder.forField(refinementSequence).bind(dto -> dto.getBuilding().getProductionType().getRefinementSequenceAsString(), null);
        // todo hide field if no payload
        productionTargetContainer.add(productionTarget, productionCategory, refinementSequence);

        build = new Button(BUILD, event -> uiEventBus.publish(this, CONSTRUCTION_JOB_BUILDING_START.name()));

        final ConstructionCostsAndStatsComparableDisplay comparableDisplay = new ConstructionCostsAndStatsComparableDisplay();
        binder.forField(comparableDisplay).bind(dto -> dto, null);
        new TooltipDisplay(this, build, comparableDisplay);

        add(nameContainer, description, productionTargetContainer, build);
    }

    @EventBusListenerMethod
    protected void onEvent(Event<String> e) {
        if (e.getPayload().equals(ESBEvent.CONSTRUCTION_JOB_BUILDING_FEEDBACK_STARTED.name())) {
            setReadOnly(true);
            if (e.getSource() == this) {
                build.setText("Job started");
            }
        }
    }

    @Nullable
    public Building getBuilding() {
        if (buildingLevelDTO == null) {
            return null;
        }
        return buildingLevelDTO.getBuilding();
    }

    @Nullable
    public Integer getTargetLevel() {
        if (buildingLevelDTO == null) {
            return null;
        }
        return buildingLevelDTO.getLevel();
    }

    @Override
    public void setValue(BuildingLevelDTO value) {
        this.buildingLevelDTO = value;
        binder.readBean(value);
    }

    @Override
    public BuildingLevelDTO getValue() {
        return buildingLevelDTO;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ConstructBuildingEdit, BuildingLevelDTO>> listener) {
        // not necessary
        return null;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        build.setEnabled(!readOnly);
        final String text = !readOnly ? BUILD : JOB_IN_PROGRESS;
        build.setText(text);
    }

    @Override
    public boolean isReadOnly() {
        return !build.isEnabled();
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        // not necessary
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return false;
    }

    /**
     * Checks if this fits to the given filter.
     *
     * @param filterDTO the filter
     * @return <code>true</code> if the filter fits, <code>false</code> otherwise
     */
    public boolean fitsFilter(@Nonnull final ConstructionFilterDTO filterDTO) {
        Preconditions.checkNotNull(filterDTO, "filterDTO shouldn't be null!");

        final Building building = getBuilding();
        if (building == null) {
            return false;
        }
        final ProductionType productionType = building.getProductionType();
        return filterDTO.fitsFilter(productionType);
    }
}
