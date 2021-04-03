package de.yuga.spacebattle.gui.vaadin.spacecrafts;

import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import de.yuga.spacebattle.backend.enums.EIconPath;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.details.EModuleValueDTO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModuleDataElementDisplay extends HorizontalLayout {

    @Nonnull
    private final Binder<EModuleValueDTO> binder = new Binder<>(EModuleValueDTO.class);

    @Nullable
    private EModuleValueDTO wrapper;

    public ModuleDataElementDisplay() {
        final Image titleImage = new Image();
        final ReadOnlyHasValue<String> titleImageSrc = new ReadOnlyHasValue<>(titleImage::setSrc);
        final ReadOnlyHasValue<String> titleImageAlt = new ReadOnlyHasValue<>(titleImage::setAlt);
        final ReadOnlyHasValue<String> titleImageTitle = new ReadOnlyHasValue<>(titleImage::setTitle);
        binder.forField(titleImageSrc).bind(wrapper -> {
            EModuleType moduleType = wrapper.getModuleType();
            final String directory = moduleType.getDirectory();
            final String iconName = moduleType.getIconName();
            return EIconPath.getPath(directory, iconName);
        }, null);
        binder.forField(titleImageAlt).bind(wrapper -> wrapper.getModuleType().getName(), null);
        binder.forField(titleImageTitle).bind(wrapper -> wrapper.getModuleType().getName(), null);

        final Label amountDisplay = new Label();
        final ReadOnlyHasValue<String> amountDisplayText = new ReadOnlyHasValue<>(amountDisplay::setText);
        binder.forField(amountDisplayText).bind(EModuleValueDTO::getValue, null);

        add(titleImage, amountDisplay);
    }

    /**
     * Updates the display with the given values.
     *
     * @param wrapper the input
     */
    public void update(@Nullable final EModuleValueDTO wrapper) {

        binder.readBean(wrapper);
        this.wrapper = wrapper;
    }
}
