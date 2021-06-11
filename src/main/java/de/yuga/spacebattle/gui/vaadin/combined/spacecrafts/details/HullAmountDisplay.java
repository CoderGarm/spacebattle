package de.yuga.spacebattle.gui.vaadin.combined.spacecrafts.details;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.enums.EResolution;
import de.yuga.spacebattle.gui.vaadin.misc.details.misc.ImageContainer;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HullAmountDisplay extends HorizontalLayout {

    @Nonnull
    private final Binder<HullAmountWrapper> binder = new Binder<>(HullAmountWrapper.class);

    @Nullable
    private HullAmountWrapper wrapper;

    public HullAmountDisplay() {
        // todo hull icons per class
        final ImageContainer imageContainer = new ImageContainer(EResolution.PX24);
        binder.forField(imageContainer).bind(w -> w, null);

        final Label hullDisplay = new Label();
        final ReadOnlyHasValue<String> hullDisplayText = new ReadOnlyHasValue<>(hullDisplay::setText);
        binder.forField(hullDisplayText).bind(w -> getSign(w.getHull()), null);

        final Label amountDisplay = new Label();
        final ReadOnlyHasValue<String> amountDisplayText = new ReadOnlyHasValue<>(amountDisplay::setText);
        binder.forField(amountDisplayText).bind(HullAmountWrapper::getValue, null);

        add(imageContainer, hullDisplay, amountDisplay);
    }

    /**
     * Simple plus signs by hull level.
     *
     * @param hull the param to convert
     * @return the string representation of the hull
     */
    private String getSign(@Nonnull final Hull hull) {
        Preconditions.checkNotNull(hull, "hull shouldn't be null!");

        return StringUtils.repeat("+", hull.getLevel());
    }

    /**
     * Updates the display with the given values.
     *
     * @param wrapper the input
     */
    public void setValue(@Nullable final HullAmountWrapper wrapper) {

        binder.readBean(wrapper);
        this.wrapper = wrapper;
    }
}
