package de.yuga.spacebattle.gui.vaadin.orbitals.details;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlanetDisplay extends VerticalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<PlanetDisplay, Planet>, Planet> {

    @Nonnull
    private final Binder<Planet> binder = new Binder<>(Planet.class);

    public PlanetDisplay() {

        final Label name = new Label();
        final ReadOnlyHasValue<String> nameText = new ReadOnlyHasValue<>(name::setText);
        binder.forField(nameText).bind(Planet::getName, null);

        final OrbitCoordinatesVerticalDisplay orbitCoordinatesVerticalDisplay = new OrbitCoordinatesVerticalDisplay();
        binder.forField(orbitCoordinatesVerticalDisplay).bind(Planet::getOrbit, null);

        add(name, orbitCoordinatesVerticalDisplay);
    }

    @Override
    public void setValue(@Nullable final Planet value) {
        binder.setBean(value);
    }

    @Override
    public Planet getValue() {
        final Planet bean = binder.getBean();
        Preconditions.checkArgument(bean != null, "The planet must not be null!");
        return bean;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<PlanetDisplay, Planet>> listener) {
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
