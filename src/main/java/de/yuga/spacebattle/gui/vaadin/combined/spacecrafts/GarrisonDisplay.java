package de.yuga.spacebattle.gui.vaadin.combined.spacecrafts;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.turn.Move;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GarrisonDisplay extends HorizontalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<GarrisonDisplay, Fleet>, Fleet> {

    @Nonnull
    private final Binder<Fleet> binderFleet = new Binder<>(Fleet.class);

    public GarrisonDisplay() {

        final Label label = new Label();
        final ReadOnlyHasValue<String> amountDisplayText = new ReadOnlyHasValue<>(label::setText);
        binderFleet.forField(amountDisplayText).bind(this::getOrbit, null);
        add(label);
    }

    private String getOrbit(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        final FleetOrbit fleetOrbit = fleet.getOrbit();
        if (fleetOrbit == null) {
            final Move move = fleet.getMove();
            if (move == null) {
                throw new NotifySBUserException("Buddy, you should do something with your fleet, really.");
            }
            final FleetOrbit startOrbit = move.getStartOrbit();
            final FleetOrbit targetOrbit = move.getTargetOrbit();
            final int moveDoneAtZero = move.getMoveDoneAtZero();
            return "This fleet moves from " + getStringForOrbit(startOrbit)
                    + " to " + getStringForOrbit(targetOrbit)
                    + " and will arrive in " + moveDoneAtZero + " ticks.";
        }

        final String stringForOrbit = getStringForOrbit(fleetOrbit);
        return "This fleet is in orbit of the " + stringForOrbit;
    }

    @Nonnull
    private String getStringForOrbit(@Nonnull final FleetOrbit fleetOrbit) {
        Preconditions.checkNotNull(fleetOrbit, "fleetOrbit shouldn't be null!");

        final StarSystem system = fleetOrbit.getSystem();
        final Planet planet = fleetOrbit.getPlanet();

        final String systemName = system.getName();
        final String planetName = planet.getName();
        return "system " + systemName + " and it's planet " + planetName;
    }


    @Override
    public void setValue(@Nonnull final Fleet value) {
        Preconditions.checkNotNull(value, "value shouldn't be null!");

        binderFleet.readBean(value);
    }

    @Nullable
    @Override
    public Fleet getValue() {
        // not necessary
        return null;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<GarrisonDisplay, Fleet>> listener) {
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
