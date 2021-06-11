package de.yuga.spacebattle.gui.vaadin.combined.spacecrafts.details;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details.ModuleValueTypePerFleetVerticalDisplay;
import de.yuga.spacebattle.gui.vaadin.misc.StatisticsDisplay;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FleetStatisticsDisplay extends StatisticsDisplay implements HasValue<AbstractField.ComponentValueChangeEvent<FleetStatisticsDisplay, Fleet>, Fleet> {

    @Nonnull
    private final Binder<Fleet> binderFleet = new Binder<>(Fleet.class);

    public FleetStatisticsDisplay() {
        final HullByAmountVerticalDisplay hullByAmountVerticalDisplay = new HullByAmountVerticalDisplay();
        binderFleet.forField(hullByAmountVerticalDisplay).bind(Fleet::getShipsByClass, null);

        final ModuleValueTypePerFleetVerticalDisplay moduleValueTypePerFleetVerticalDisplay = new ModuleValueTypePerFleetVerticalDisplay();
        binderFleet.forField(moduleValueTypePerFleetVerticalDisplay).bind(Fleet::getShipsByClass, null);

        addSlide("Hull composition", hullByAmountVerticalDisplay);
        addSlide("Fleet stats", moduleValueTypePerFleetVerticalDisplay);
    }

    @Override
    public void setValue(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        binderFleet.readBean(fleet);
    }

    @Nullable
    @Override
    public Fleet getValue() {
        // not necessary
        return null;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<FleetStatisticsDisplay, Fleet>> listener) {
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
