package de.yuga.spacebattle.gui.vaadin.spacecrafts;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;

import javax.annotation.Nonnull;

public class HullDisplay extends VerticalLayout {

    public HullDisplay(@Nonnull final Hull hull) {
        Preconditions.checkNotNull(hull, "hull shouldn't be null!");

        H5 name = new H5(hull.getName());
        Label description = new Label(hull.getDescription());

        add(name, description);
    }
}
