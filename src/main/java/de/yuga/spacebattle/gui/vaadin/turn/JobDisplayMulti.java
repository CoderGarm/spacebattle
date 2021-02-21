package de.yuga.spacebattle.gui.vaadin.turn;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;

import javax.annotation.Nonnull;
import java.util.Set;

public class JobDisplayMulti extends VerticalLayout {

    public JobDisplayMulti(@Nonnull final Set<Job> jobs) {
        Preconditions.checkNotNull(jobs, "jobs shouldn't be null!");

        Accordion accordion = new Accordion();
        ViewHelper.setWidth(accordion, null);
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Jobs");
        jobs.forEach(job -> {
            JobDisplay jobDisplay = new JobDisplay(job);
            accordionPanel.addContent(jobDisplay);
        });
        accordion.add(accordionPanel);
        accordion.close();
        add(accordion);
    }
}
