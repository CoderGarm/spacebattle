package de.yuga.spacebattle.gui.vaadin.combined.spacecrafts.details;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details.ModuleValueTypePerFleetHorizontalDisplay;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FleetStatsHorizontalDisplay extends VerticalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<FleetStatsHorizontalDisplay, Fleet>, Fleet> {

    @Nonnull
    private final Binder<Fleet> binderFleet = new Binder<>(Fleet.class);

    public FleetStatsHorizontalDisplay() {
        final HullByAmountHorizontalDisplay hullByAmountVerticalDisplay = new HullByAmountHorizontalDisplay();
        binderFleet.forField(hullByAmountVerticalDisplay).bind(Fleet::getShips, null);

        final ModuleValueTypePerFleetHorizontalDisplay moduleValueTypeVerticalDisplay = new ModuleValueTypePerFleetHorizontalDisplay();
        binderFleet.forField(moduleValueTypeVerticalDisplay).bind(Fleet::getShips, null);

        add(hullByAmountVerticalDisplay, moduleValueTypeVerticalDisplay);
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
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<FleetStatsHorizontalDisplay, Fleet>> listener) {
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
