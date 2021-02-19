package de.yuga.spacebattle.gui.vaadin.spacecrafts;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.backend.entities.spacecrafts.Module;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModuleDisplay extends VerticalLayout {

    public ModuleDisplay(@Nonnull final Module module, @Nullable final Integer amount) {
        Preconditions.checkNotNull(module, "module shouldn't be null!");

        ViewHelper.setWidth(this, null);

        Label name = new Label(module.getName());
        Label description = new Label(module.getDescription());

        add(name, description);

        if (amount != null) {
            Label amountL = new Label("Amount: " + amount);
            add(amountL);
        }
    }
}
