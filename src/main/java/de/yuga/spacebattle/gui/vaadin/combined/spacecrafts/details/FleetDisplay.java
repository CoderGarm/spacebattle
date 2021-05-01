package de.yuga.spacebattle.gui.vaadin.combined.spacecrafts.details;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FleetDisplay extends VerticalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<FleetDisplay, Fleet>, Fleet> {

    @Nonnull
    private final Binder<Fleet> binderFleet = new Binder<>(Fleet.class);

    public FleetDisplay() {
        // todo information levels by "own fleet" down to "enemy fleet"
        final Label name = new Label();
        final ReadOnlyHasValue<String> nameText = new ReadOnlyHasValue<>(name::setText);
        binderFleet.forField(nameText).bind(Fleet::getName, null);

        final FleetStatsHorizontalDisplay fleetStatsHorizontalDisplay = new FleetStatsHorizontalDisplay();
        binderFleet.forField(fleetStatsHorizontalDisplay).bind(fleet -> fleet, null);


        add(name, fleetStatsHorizontalDisplay);
    }


    @Override
    public void setValue(@Nullable final Fleet value) {
        binderFleet.setBean(value);
    }

    @Nullable
    @Override
    public Fleet getValue() {
        return binderFleet.getBean();
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<FleetDisplay, Fleet>> listener) {
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
