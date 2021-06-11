package de.yuga.spacebattle.gui.vaadin.spacecrafts;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import de.yuga.spacebattle.backend.entities.spacecrafts.details.AlignedFitting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AlignedFittingDisplay extends HorizontalLayout {

    @Nonnull
    private final Binder<AlignedFitting> binder = new Binder<>();

    public AlignedFittingDisplay() {
        setClassName("module-display");

        final Label name = new Label();
        final ReadOnlyHasValue<String> moduleNameReadOnly = new ReadOnlyHasValue<>(name::setText);
        binder.forField(moduleNameReadOnly).bind(a -> a.getWeapon().getName(), null);

        final Label description = new Label();
        final ReadOnlyHasValue<String> moduleDescriptionReadOnly = new ReadOnlyHasValue<>(description::setText);
        binder.forField(moduleDescriptionReadOnly).bind(a -> a.getWeapon().getDescription(), null);

        final Label amountLabel = new Label();
        final ReadOnlyHasValue<String> amountLabelReadOnly = new ReadOnlyHasValue<>(amountLabel::setText);
        binder.forField(amountLabelReadOnly).bind(a -> String.valueOf(a.getAmount()) + "x", null);

        add(amountLabel, name, description);
    }

    /**
     * Will update the existing display. Or simply clear the full view if the params are null.
     *
     * @param module the module to display
     */
    public void setValue(@Nullable final AlignedFitting module) {
        binder.readBean(module);
    }
}
