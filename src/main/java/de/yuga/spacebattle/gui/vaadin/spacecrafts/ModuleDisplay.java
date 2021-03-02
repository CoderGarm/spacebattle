package de.yuga.spacebattle.gui.vaadin.spacecrafts;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import de.yuga.spacebattle.backend.entities.spacecrafts.Module;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModuleDisplay extends HorizontalLayout {

    @Nonnull
    private final Binder<Module> binderModule = new Binder<>(Module.class);

    @Nonnull
    private final Binder<Integer> binderAmount = new Binder<>(Integer.class);

    public ModuleDisplay() {
        ViewHelper.setWidth(this, null);

        Label name = new Label();
        final ReadOnlyHasValue<String> moduleNameReadOnly = new ReadOnlyHasValue<>(name::setText);
        binderModule.forField(moduleNameReadOnly).bind(Module::getName, null);

        Label description = new Label();
        final ReadOnlyHasValue<String> moduleDescriptionReadOnly = new ReadOnlyHasValue<>(description::setText);
        binderModule.forField(moduleDescriptionReadOnly).bind(Module::getDescription, null);

        Label amountL = new Label();
        final ReadOnlyHasValue<String> amountReadOnly = new ReadOnlyHasValue<>(amountL::setText);
        binderAmount.forField(amountReadOnly).bind(integer -> integer != null ? "Amount: " + integer : "", null);
        add(name, description, amountL);
    }

    /**
     * Will update the existing display. Or simply clear the full view if the params are null.
     *
     * @param module the module to display
     * @param amount the amount of module type to display
     */
    public void update(@Nullable final Module module, @Nullable final Integer amount) {
        binderModule.readBean(module);
        binderAmount.readBean(amount);
    }
}
