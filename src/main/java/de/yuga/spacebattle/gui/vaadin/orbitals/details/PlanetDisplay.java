package de.yuga.spacebattle.gui.vaadin.orbitals.details;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.gui.vaadin.misc.details.ResourceDisplay;

import javax.annotation.Nonnull;

public class PlanetDisplay extends VerticalLayout {

    public PlanetDisplay(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        TextField name = new TextField("Name");
        name.setValue(planet.getName());
        H5 depositsTitle = new H5("Deposits");
        ResourceDisplay deposits = new ResourceDisplay(planet.getResourceDeposit());
        H5 miningFactorsTitle = new H5("Mining factors");
        ResourceDisplay factors = new ResourceDisplay(planet.getResourcefactors());

        add(name, depositsTitle, deposits, miningFactorsTitle, factors);
    }
}
