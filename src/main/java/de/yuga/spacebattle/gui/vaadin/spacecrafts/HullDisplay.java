package de.yuga.spacebattle.gui.vaadin.spacecrafts;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;

import javax.annotation.Nonnull;

public class HullDisplay extends HorizontalLayout {

    public HullDisplay(@Nonnull final Hull hull) {
        Preconditions.checkNotNull(hull, "hull shouldn't be null!");

        Label name = new Label(hull.getName());
        Label description = new Label(hull.getDescription());

        add(name, description);
    }
}
