package de.yuga.spacebattle.gui.vaadin.research;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;

import javax.annotation.Nonnull;
import java.util.Map;

public class ResearchDisplayMulti extends VerticalLayout {

    public ResearchDisplayMulti(@Nonnull final Map<Research, Integer> researches) {
        Preconditions.checkNotNull(researches, "researches shouldn't be null!");

        Accordion accordion = new Accordion();
        ViewHelper.setWidth(accordion, null);
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Researches");
        researches.forEach((research, level) -> {
            ResearchDisplay researchDisplay = new ResearchDisplay(research, level);
            accordionPanel.addContent(researchDisplay);
        });
        accordion.add(accordionPanel);
        accordion.close();
        add(accordion);
    }
}
