package de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.Module;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.HullDisplay;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.ModuleDisplay;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class ShipClassDisplay extends VerticalLayout {

    public ShipClassDisplay(@Nonnull final ShipClass shipClass, @Nullable final Integer amount) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");

        Label name = new Label(shipClass.getName());
        HullDisplay hullDisplay = new HullDisplay(shipClass.getHull());

        Map<Module, Integer> modules = shipClass.getModules();
        Accordion accordion = new Accordion();
        ViewHelper.setWidth(accordion, null);
        AccordionPanel accordionPanel = new AccordionPanel();
        accordionPanel.setSummaryText("Modules");
        modules.keySet().forEach(module -> {
            ModuleDisplay moduleDisplay = new ModuleDisplay(module, modules.get(module));
            accordionPanel.addContent(moduleDisplay);
        });
        accordion.add(accordionPanel);
        accordion.close();


        add(name);
        if (amount != null) {
            Label amountL = new Label("Amount: " + amount);
            add(amountL);
        }
        add(hullDisplay);
        add(accordion);
    }
}
