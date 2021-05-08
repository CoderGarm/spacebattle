package de.yuga.spacebattle.gui.vaadin.orbitals.details;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Displays the most basic data about an orbit like it's coordinates in it's parent map.
 */
public class OrbitCoordinatesDisplay extends VerticalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<OrbitCoordinatesDisplay, Orbit>, Orbit> {

    private static final String X_PREFIX = "X";
    private static final String Y_PREFIX = "Y";

    @Nonnull
    private final Binder<Orbit> binder = new Binder<>(Orbit.class);

    public OrbitCoordinatesDisplay() {

        final Label xOrbit = new Label();
        final ReadOnlyHasValue<String> xOrbitText = new ReadOnlyHasValue<>(xOrbit::setText);
        binder.forField(xOrbitText)
                .bind(orbit -> getOrbitString(X_PREFIX, orbit), null);

        final Label yOrbit = new Label();
        final ReadOnlyHasValue<String> yOrbitText = new ReadOnlyHasValue<>(yOrbit::setText);
        binder.forField(yOrbitText)
                .bind(orbit -> getOrbitString(Y_PREFIX, orbit), null);

        add(xOrbit, yOrbit);
    }


    /**
     * Creates the displayable coordinate string.
     *
     * @param prefix the x- or y- prefix
     * @param orbit  holds the coord to display
     * @return the resulting string
     */
    private String getOrbitString(@Nonnull final String prefix, @Nullable final Orbit orbit) {
        Preconditions.checkNotNull(prefix, "prefix shouldn't be null!");

        Integer coordinate = null;
        if (orbit != null) {
            if (X_PREFIX.equals(prefix)) {
                coordinate = orbit.getXCoordinate();
            } else if (Y_PREFIX.equals(prefix)) {
                coordinate = orbit.getYCoordinate();
            }
        }
        return coordinate != null ? prefix + "-Coordinate: " + coordinate : "";
    }

    @Override
    public void setValue(@Nullable final Orbit value) {
        binder.setBean(value);
    }

    @Override
    public Orbit getValue() {
        return binder.getBean();
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<OrbitCoordinatesDisplay, Orbit>> listener) {
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
