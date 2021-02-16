package de.yuga.spacebattle.gui.vaadin.spacecrafts;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.backend.entities.spacecrafts.Module;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;

import javax.annotation.Nonnull;

public class ModuleDisplay extends VerticalLayout {

    public ModuleDisplay(@Nonnull final Module module) {
        Preconditions.checkNotNull(module, "module shouldn't be null!");

        ViewHelper.setWidth(this, null);

        H5 name = new H5(module.getName());
        Label description = new Label(module.getDescription());

        add(name, description);
    }
}
