package de.yuga.spacebattle.gui.vaadin.turn.resource;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import de.yuga.spacebattle.backend.enums.EResolution;
import de.yuga.spacebattle.gui.vaadin.misc.details.misc.ImageContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ResourceElementDisplay extends HorizontalLayout {

    @Nonnull
    private final Binder<ResourceDetailDTO> binder = new Binder<>();

    public ResourceElementDisplay(@Nonnull final EResolution resolution) {
        Preconditions.checkNotNull(resolution, "resolution shouldn't be null!");

        final ImageContainer imageContainer = new ImageContainer(resolution);
        binder.forField(imageContainer).bind(w -> w, null);

        final Label amountDisplay = new Label();
        final ReadOnlyHasValue<String> amountDisplayText = new ReadOnlyHasValue<>(amountDisplay::setText);
        binder.forField(amountDisplayText).bind(ResourceDetailDTO::getAmountAsString, null);

        add(imageContainer, amountDisplay);
    }

    /**
     * Updates the display with the given values.
     *
     * @param amount the input
     */
    public void setValue(@Nullable final ResourceDetailDTO amount) {
        binder.readBean(amount);
    }
}
