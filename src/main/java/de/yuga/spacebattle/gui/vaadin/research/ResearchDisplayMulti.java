package de.yuga.spacebattle.gui.vaadin.research;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.backend.entities.researches.Research;

import javax.annotation.Nonnull;
import java.util.Collection;

public class ResearchDisplayMulti extends VerticalLayout {

    public ResearchDisplayMulti(@Nonnull final Collection<Research> researches) {
        Preconditions.checkNotNull(researches, "researches shouldn't be null!");

        H5 researchesTitle = new H5("Researches");
        add(researchesTitle);
        Accordion researchesAccordion = new Accordion();
        AccordionPanel accordionPanel = new AccordionPanel();
        researches.forEach(research -> {
            ResearchDisplay researchDisplay = new ResearchDisplay(research);
            accordionPanel.addContent(researchDisplay);
        });
        researchesAccordion.add(accordionPanel);
        researchesAccordion.close();
        add(researchesAccordion);
    }
}
