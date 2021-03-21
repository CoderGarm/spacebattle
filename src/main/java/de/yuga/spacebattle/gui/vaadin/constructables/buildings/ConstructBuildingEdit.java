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
import de.yuga.spacebattle.gui.vaadin.orbitals.details.BuildingLevelWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.spring.events.Event;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import javax.annotation.Nonnull;

import static de.yuga.spacebattle.gui.vaadin.events.ESBEvent.CONSTRUCTION_JOB_BUILDING_START;

public class ConstructBuildingEdit extends VerticalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<ConstructBuildingEdit, BuildingLevelWrapper>, BuildingLevelWrapper> {

    private final static Logger LOGGER = LoggerFactory.getLogger(ConstructBuildingEdit.class);

    @Nonnull
    private final EventBus.UIEventBus uiEventBus = ViewHelper.getService(EventBus.UIEventBus.class);

    @Nonnull
    private BuildingLevelWrapper buildingLevelWrapper;

    @Nonnull
    private final Button build;

    @Nonnull
    private final Binder<BuildingLevelWrapper> binderLevelWrapper = new Binder<>(BuildingLevelWrapper.class);

    public ConstructBuildingEdit() {

        BuildingDisplay buildingDisplay = new BuildingDisplay();
        binderLevelWrapper.forField(buildingDisplay).bind(BuildingLevelWrapper::getBuilding, null);

        Label levelValue = new Label();
        final ReadOnlyHasValue<String> levelValueReadOnly = new ReadOnlyHasValue<>(levelValue::setText);
        binderLevelWrapper.forField(levelValueReadOnly).bind(BuildingLevelWrapper::getLevelString, null);

        this.uiEventBus.subscribe(this);

        build = new Button("Build", event -> {
            uiEventBus.publish(this, CONSTRUCTION_JOB_BUILDING_START.name());
            LOGGER.info("build");
        });

        add(buildingDisplay, levelValue, build);
    }

    @EventBusListenerMethod
    protected void onEvent(Event<String> e) {
        if (e.getPayload().equals(ESBEvent.CONSTRUCTION_JOB_BUILDING_FEEDBACK_STARTED.name())) {
            build.setEnabled(false);
            if (e.getSource() == this) {
                build.setText("Job started");
            }
        }
    }

    @Nonnull
    public Building getBuilding() {
        return buildingLevelWrapper.getBuilding();
    }

    public int getTargetLevel() {
        return buildingLevelWrapper.getLevel();
    }

    @Override
    public void setValue(BuildingLevelWrapper value) {
        this.buildingLevelWrapper = value;
        binderLevelWrapper.readBean(value);
    }

    @Override
    public BuildingLevelWrapper getValue() {
        return null;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ConstructBuildingEdit, BuildingLevelWrapper>> listener) {
        // not necessary
        return null;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        // not necessary
    }

    @Override
    public boolean isReadOnly() {
        return false;
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
