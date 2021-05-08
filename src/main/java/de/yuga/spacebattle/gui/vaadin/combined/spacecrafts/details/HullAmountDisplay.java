package de.yuga.spacebattle.gui.vaadin.combined.spacecrafts.details;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HullAmountDisplay extends HorizontalLayout {

    @Nonnull
    private final Binder<HullAmountWrapper> binder = new Binder<>(HullAmountWrapper.class);

    @Nullable
    private HullAmountWrapper wrapper;

    public HullAmountDisplay() {
        /*
        final Image titleImage = new Image();
        final ReadOnlyHasValue<String> titleImageSrc = new ReadOnlyHasValue<>(titleImage::setSrc);
        final ReadOnlyHasValue<String> titleImageAlt = new ReadOnlyHasValue<>(titleImage::setAlt);
        final ReadOnlyHasValue<String> titleImageTitle = new ReadOnlyHasValue<>(titleImage::setTitle);
        binder.forField(titleImageSrc).bind(wrapper -> {
            final Hull moduleType = wrapper.getHull(); // todo hull icons per class
            final String directory = moduleType.getDirectory();
            final String iconName = moduleType.getIconName();
            return EIconPath.getPath(directory, iconName);
        }, null);
        binder.forField(titleImageAlt).bind(wrapper -> wrapper.getHull().getName(), null);
        binder.forField(titleImageTitle).bind(wrapper -> wrapper.getHull().getName(), null);
        */

        final Label hullDisplay = new Label();
        final ReadOnlyHasValue<String> hullDisplayText = new ReadOnlyHasValue<>(hullDisplay::setText);
        binder.forField(hullDisplayText).bind(w -> getSign(w.getHull()), null);

        final Label amountDisplay = new Label();
        final ReadOnlyHasValue<String> amountDisplayText = new ReadOnlyHasValue<>(amountDisplay::setText);
        binder.forField(amountDisplayText).bind(HullAmountWrapper::getValue, null);

        add(/*titleImage, */hullDisplay, amountDisplay);
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
    public void update(@Nullable final HullAmountWrapper wrapper) {

        binder.readBean(wrapper);
        this.wrapper = wrapper;
    }
}
