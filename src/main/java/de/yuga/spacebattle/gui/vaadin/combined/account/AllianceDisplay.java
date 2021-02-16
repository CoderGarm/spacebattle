package de.yuga.spacebattle.gui.vaadin.combined.account;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;

import javax.annotation.Nonnull;

public class AllianceDisplay extends VerticalLayout {

    public AllianceDisplay(@Nonnull final Alliance alliance) {
        Preconditions.checkNotNull(alliance, "alliance shouldn't be null!");

        Accordion accordion = new Accordion();
        ViewHelper.setWidth(accordion, null);
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Alliance");

        Label name = new Label(alliance.getName());
        Label code = new Label(alliance.getCode());

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.add(name, code);

        accordionPanel.addContent(horizontalLayout);

        accordion.add(accordionPanel);
        accordion.close();
        add(accordion);


    }
}
