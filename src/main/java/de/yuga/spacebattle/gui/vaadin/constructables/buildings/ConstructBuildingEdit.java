package de.yuga.spacebattle.gui.vaadin.constructables.buildings;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
import de.yuga.spacebattle.gui.vaadin.buildings.BuildingDisplay;
import de.yuga.spacebattle.gui.vaadin.events.ESBEvent;
import de.yuga.spacebattle.gui.vaadin.orbitals.details.BuildingLevelDTO;
import org.vaadin.spring.events.Event;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static de.yuga.spacebattle.gui.vaadin.events.ESBEvent.CONSTRUCTION_JOB_BUILDING_START;

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
    private final Binder<BuildingLevelDTO> binderLevelWrapper = new Binder<>(BuildingLevelDTO.class);

    public ConstructBuildingEdit() {
        this.uiEventBus.subscribe(this);

        final BuildingDisplay buildingDisplay = new BuildingDisplay();
        binderLevelWrapper.forField(buildingDisplay).bind(BuildingLevelDTO::getBuilding, null);

        final Label levelValue = new Label();
        final ReadOnlyHasValue<String> levelValueReadOnly = new ReadOnlyHasValue<>(levelValue::setText);
        binderLevelWrapper.forField(levelValueReadOnly).bind(BuildingLevelDTO::getLevelString, null);

        build = new Button(BUILD, event -> uiEventBus.publish(this, CONSTRUCTION_JOB_BUILDING_START.name()));

        add(buildingDisplay, levelValue, build);
    }

    @EventBusListenerMethod
    protected void onEvent(Event<String> e) {
        if (e.getPayload().equals(ESBEvent.CONSTRUCTION_JOB_BUILDING_FEEDBACK_STARTED.name())) {
            setReadOnly(false);
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
        binderLevelWrapper.readBean(value);
    }

    @Override
    public BuildingLevelDTO getValue() {
        return binderLevelWrapper.getBean();
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
}
