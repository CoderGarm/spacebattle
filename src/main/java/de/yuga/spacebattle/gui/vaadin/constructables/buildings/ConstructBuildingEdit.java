package de.yuga.spacebattle.gui.vaadin.constructables.buildings;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
import de.yuga.spacebattle.gui.vaadin.buildings.BuildingDisplay;
import de.yuga.spacebattle.gui.vaadin.events.ESBEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.spring.events.Event;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import javax.annotation.Nonnull;

import static de.yuga.spacebattle.gui.vaadin.events.ESBEvent.CONSTRUCT_BUILDING;

public class ConstructBuildingEdit extends VerticalLayout {

    private final static Logger LOGGER = LoggerFactory.getLogger(ConstructBuildingEdit.class);

    @Nonnull
    private final EventBus.UIEventBus uiEventBus = ViewHelper.getService(EventBus.UIEventBus.class);

    @Nonnull
    private final Building building;

    private final int targetLevel;

    @Nonnull
    private final Button build;

    public ConstructBuildingEdit(@Nonnull final Building building, final int targetLevel) {
        Preconditions.checkNotNull(building, "building shouldn't be null!");

        BuildingDisplay buildingDisplay = new BuildingDisplay(building);
        Label levelValue = new Label("Level: " + targetLevel);

        this.building = building;
        this.targetLevel = targetLevel;
        this.uiEventBus.subscribe(this);

        build = new Button("Build", event -> {
            Button source = event.getSource();
            source.setEnabled(false); // todo buttons fire twice? #2
            uiEventBus.publish(this, CONSTRUCT_BUILDING.name());
            LOGGER.info("build");
            source.setEnabled(true);
        });

        add(buildingDisplay, levelValue, build);
    }

    @EventBusListenerMethod
    protected void onEvent(Event<String> e) {
        if (e.getPayload().equals(ESBEvent.CONSTRUCTION_JOB_STARTED.name())) {
            build.setEnabled(false);
            if (e.getSource() == this) {
                build.setText("Job started");
            }
        }
    }

    @Nonnull
    public Building getBuilding() {
        return building;
    }

    public int getTargetLevel() {
        return targetLevel;
    }
}
