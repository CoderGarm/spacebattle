package de.yuga.spacebattle.gui.vaadin.buildings;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.buildings.Building;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuildingDisplay extends HorizontalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<BuildingDisplay, Building>, Building> {

    @Nonnull
    private final Binder<Building> binderBuilding = new Binder<>(Building.class);

    public BuildingDisplay() {

        Label name = new Label();
        final ReadOnlyHasValue<String> nameReadOnly = new ReadOnlyHasValue<>(name::setText);
        binderBuilding.forField(nameReadOnly).bind(Building::getName, null);

        Label description = new Label();
        final ReadOnlyHasValue<String> descriptionReadOnly = new ReadOnlyHasValue<>(description::setText);
        binderBuilding.forField(descriptionReadOnly).bind(Building::getDescription, null);

        add(name, description);
    }

    @Override
    public void setValue(Building value) {
        binderBuilding.readBean(value);
    }

    @Nullable
    @Override
    public Building getValue() {
        return null;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<BuildingDisplay, Building>> listener) {
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
