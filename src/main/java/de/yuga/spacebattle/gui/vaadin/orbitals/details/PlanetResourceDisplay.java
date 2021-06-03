package de.yuga.spacebattle.gui.vaadin.orbitals.details;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.enums.EResolution;
import de.yuga.spacebattle.gui.vaadin.misc.details.ResourceOutputDisplay;
import de.yuga.spacebattle.gui.vaadin.misc.details.ResourceVerticalDisplay;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlanetResourceDisplay extends HorizontalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<PlanetResourceDisplay, Planet>, Planet> {

    @Nonnull
    private final ResourceOutputDisplay deposits;

    @Nonnull
    private final ResourceVerticalDisplay factors;

    public PlanetResourceDisplay() {
        deposits = new ResourceOutputDisplay(EResolution.PX32);
        factors = new ResourceVerticalDisplay(EResolution.PX32);
        add(deposits, factors);
    }

    public void update(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        deposits.updateResourceDeposit(planet);
        factors.updateResourceDeposit(planet.getResourceFactors());
    }

    @Override
    public void setValue(Planet value) {
        this.update(value);
    }

    @Nullable
    @Override
    public Planet getValue() {
        // not necessary
        return null;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<PlanetResourceDisplay, Planet>> listener) {
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
