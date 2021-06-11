package de.yuga.spacebattle.gui.vaadin.spacecrafts;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.details.AmmunitionModuleCountDTO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AmmunitionFittingDisplay extends HorizontalLayout {

    @Nonnull
    private final Binder<AmmunitionModuleCountDTO> binder = new Binder<>();

    public AmmunitionFittingDisplay() {
        setClassName("module-display");

        final Label name = new Label();
        final ReadOnlyHasValue<String> moduleNameReadOnly = new ReadOnlyHasValue<>(name::setText);
        binder.forField(moduleNameReadOnly).bind(AmmunitionModuleCountDTO::getName, null);

        final Label description = new Label();
        final ReadOnlyHasValue<String> moduleDescriptionReadOnly = new ReadOnlyHasValue<>(description::setText);
        binder.forField(moduleDescriptionReadOnly).bind(AmmunitionModuleCountDTO::getDescription, null);

        final Label supportsWhat = new Label();
        final ReadOnlyHasValue<String> supportsWhatReadOnly = new ReadOnlyHasValue<>(supportsWhat::setText);
        binder.forField(supportsWhatReadOnly).bind(AmmunitionModuleCountDTO::getSupportsWhatDescription, null);

        final Label amountLabel = new Label();
        final ReadOnlyHasValue<String> amountLabelReadOnly = new ReadOnlyHasValue<>(amountLabel::setText);
        binder.forField(amountLabelReadOnly).bind(a -> a.getCount() + "x", null);

        add(amountLabel, name, description, supportsWhat);
    }

    /**
     * Will update the existing display. Or simply clear the full view if the params are null.
     *
     * @param module the module to display
     */
    public void setValue(@Nullable final AmmunitionModuleCountDTO module) {
        binder.readBean(module);
    }
}
