package de.yuga.spacebattle.gui.vaadin.orbitals.details;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.gui.vaadin.misc.details.ResourceDisplay;
import de.yuga.spacebattle.gui.vaadin.misc.details.ResourceOutputDisplay;

import javax.annotation.Nonnull;

public class PlanetResourceDisplay extends VerticalLayout {

    @Nonnull
    private final ResourceOutputDisplay deposits;

    public PlanetResourceDisplay(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        final TextField name = new TextField("Name");
        name.setValue(planet.getName());
        name.setEnabled(false);
        final H5 depositsTitle = new H5("Deposits");
        deposits = new ResourceOutputDisplay(planet);

        final H5 miningFactorsTitle = new H5("Mining factors");
        final ResourceDisplay factors = new ResourceDisplay(planet.getResourceFactors());

        add(name, depositsTitle, deposits, miningFactorsTitle, factors);
    }

    public void updatePlanet(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        deposits.updateResourceDeposit(planet);
    }
}
