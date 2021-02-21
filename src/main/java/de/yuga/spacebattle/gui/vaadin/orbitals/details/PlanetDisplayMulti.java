package de.yuga.spacebattle.gui.vaadin.orbitals.details;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;

import javax.annotation.Nonnull;
import java.util.Collection;

public class PlanetDisplayMulti extends VerticalLayout {

    public PlanetDisplayMulti(@Nonnull final Collection<Planet> planets) {
        Preconditions.checkNotNull(planets, "planets shouldn't be null!");


        Accordion accordion = new Accordion();
        ViewHelper.setWidth(accordion, null);
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Planet");
        planets.forEach(planet -> {
            PlanetResourceDisplay planetResourceDisplay = new PlanetResourceDisplay(planet);
            accordionPanel.addContent(planetResourceDisplay);
        });
        accordion.add(accordionPanel);
        accordion.close();
        add(accordion);
    }
}
