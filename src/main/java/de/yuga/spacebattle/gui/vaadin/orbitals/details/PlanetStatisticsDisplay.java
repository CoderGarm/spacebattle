package de.yuga.spacebattle.gui.vaadin.orbitals.details;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.enums.EResolution;
import de.yuga.spacebattle.gui.vaadin.misc.StatisticsDisplay;
import de.yuga.spacebattle.gui.vaadin.turn.resource.MiningFactorsVerticalDisplay;
import de.yuga.spacebattle.gui.vaadin.turn.resource.ResourceOutputDisplay;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlanetStatisticsDisplay extends StatisticsDisplay implements HasValue<AbstractField.ComponentValueChangeEvent<PlanetStatisticsDisplay, Planet>, Planet> {

    @Nonnull
    private final ResourceOutputDisplay deposits = new ResourceOutputDisplay(EResolution.PX24);

    @Nonnull
    private final MiningFactorsVerticalDisplay factors = new MiningFactorsVerticalDisplay(EResolution.PX24);

    public PlanetStatisticsDisplay() {
        addSlide("Deposit", deposits);
        addSlide("Mining factors", factors);
    }

    @Override
    public void setValue(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        deposits.setValue(planet);
        factors.setValue(planet);
    }

    @Nullable
    @Override
    public Planet getValue() {
        // not necessary
        return null;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<PlanetStatisticsDisplay, Planet>> listener) {
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
