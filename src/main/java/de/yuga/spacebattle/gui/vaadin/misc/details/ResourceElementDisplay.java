package de.yuga.spacebattle.gui.vaadin.misc.details;

import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import de.yuga.spacebattle.backend.enums.EIconPath;
import de.yuga.spacebattle.backend.enums.EResourceType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ResourceElementDisplay extends HorizontalLayout {

    @Nonnull
    private final Binder<EResourceAmountWrapper> binder = new Binder<>(EResourceAmountWrapper.class);

    @Nullable
    private EResourceAmountWrapper amount;

    public ResourceElementDisplay() {
        final Image titleImage = new Image();
        final ReadOnlyHasValue<String> titleImageSrc = new ReadOnlyHasValue<>(titleImage::setSrc);
        final ReadOnlyHasValue<String> titleImageAlt = new ReadOnlyHasValue<>(titleImage::setAlt);
        final ReadOnlyHasValue<String> titleImageTitle = new ReadOnlyHasValue<>(titleImage::setTitle);
        binder.forField(titleImageSrc).bind(wrapper -> {
            final EResourceType resourceType = wrapper.getResourceType();
            final String directory = resourceType.getDirectory();
            final String iconName = resourceType.getIconName();
            return EIconPath.getPath(directory, iconName);
        }, null);
        binder.forField(titleImageAlt).bind(wrapper -> wrapper.getResourceType().getSingularName(), null);
        binder.forField(titleImageTitle).bind(wrapper -> wrapper.getResourceType().getSingularName(), null);

        final Label amountDisplay = new Label();
        final ReadOnlyHasValue<String> amountDisplayText = new ReadOnlyHasValue<>(amountDisplay::setText);
        binder.forField(amountDisplayText).bind(EResourceAmountWrapper::getAmountWithDiff, null);

        add(titleImage, amountDisplay);
    }

    /**
     * Updates the display with the given values.
     *
     * @param amount the input
     */
    public void update(@Nullable final EResourceAmountWrapper amount) {

        binder.readBean(amount);
        this.amount = amount;
    }
}
