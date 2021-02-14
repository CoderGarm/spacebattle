package de.yuga.spacebattle.gui.vaadin.orbitals.details;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;

import javax.annotation.Nonnull;
import java.util.Collection;

public class PlanetDisplayMulti extends VerticalLayout {

    public PlanetDisplayMulti(@Nonnull final Collection<Planet> planets) {
        Preconditions.checkNotNull(planets, "planets shouldn't be null!");

        H5 planetsTitle = new H5("Planets");
        add(planetsTitle);
        AccordionPanel accordionPanel = new AccordionPanel();
        planets.forEach(planet -> {
            PlanetDisplay planetDisplay = new PlanetDisplay(planet);
            accordionPanel.addContent(planetDisplay);
        });
        Accordion planetsAccordion = new Accordion();
        planetsAccordion.add(accordionPanel);
        planetsAccordion.close();
        add(planetsAccordion);
    }
}
