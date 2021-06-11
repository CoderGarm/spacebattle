package de.yuga.spacebattle.gui.vaadin.spacecrafts;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import de.yuga.spacebattle.backend.enums.EResolution;
import de.yuga.spacebattle.gui.vaadin.misc.details.misc.ImageContainer;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.details.EModuleValueDTO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModuleDataElementDisplay extends HorizontalLayout {

    @Nonnull
    private final Binder<EModuleValueDTO> binder = new Binder<>(EModuleValueDTO.class);

    public ModuleDataElementDisplay() {
        // todo define module types or icons for missiles, counter missiles, whatever
        final ImageContainer imageContainer = new ImageContainer(EResolution.PX24);
        binder.forField(imageContainer).bind(w -> w, null);

        final Label amountDisplay = new Label();
        final ReadOnlyHasValue<String> amountDisplayText = new ReadOnlyHasValue<>(amountDisplay::setText);
        binder.forField(amountDisplayText).bind(EModuleValueDTO::getValue, null);

        add(imageContainer, amountDisplay);
    }

    /**
     * Updates the display with the given values.
     *
     * @param wrapper the input
     */
    public void update(@Nullable final EModuleValueDTO wrapper) {
        binder.readBean(wrapper);
    }
}
