package de.yuga.spacebattle.gui.vaadin.spacecrafts;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HullDisplay extends VerticalLayout {

    @Nonnull
    private final Binder<Hull> hullBinder = new Binder<>(Hull.class);

    public HullDisplay() {

        final HorizontalLayout payload = new HorizontalLayout();
        final Label name = new Label();
        final ReadOnlyHasValue<String> nameReadOnly = new ReadOnlyHasValue<>(name::setText);
        hullBinder.forField(nameReadOnly).bind(Hull::getName, null);

        final Label description = new Label();
        final ReadOnlyHasValue<String> descriptionReadOnly = new ReadOnlyHasValue<>(description::setText);
        hullBinder.forField(descriptionReadOnly).bind(Hull::getDescription, null);

        final Label constructionCapacity = new Label();
        final ReadOnlyHasValue<String> constructionCapacityReadOnly = new ReadOnlyHasValue<>(constructionCapacity::setText);
        hullBinder.forField(constructionCapacityReadOnly).bind(hull -> "Construction capacity: " + String.valueOf(hull.getConstructionCapacity()), null);

        payload.add(description, constructionCapacity);
        add(name, payload);
    }

    /**
     * Will update the display or clear all fields.
     *
     * @param hull the hull to display
     */
    public void update(@Nullable final Hull hull) {

        hullBinder.readBean(hull);
    }
}
