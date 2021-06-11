package de.yuga.spacebattle.gui.vaadin.misc.details.misc;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.enums.EResolution;

import javax.annotation.Nonnull;

public class ImageContainer extends Div implements HasValue<AbstractField.ComponentValueChangeEvent<ImageContainer, ImageMapper>, ImageMapper> {

    @Nonnull
    private final Binder<ImageMapper> binder = new Binder<>();

    public ImageContainer(@Nonnull final EResolution resolution) {
        Preconditions.checkNotNull(resolution, "resolution shouldn't be null!");

        final Image titleImage = new Image();
        final ReadOnlyHasValue<String> titleImageSrc = new ReadOnlyHasValue<>(titleImage::setSrc);
        final ReadOnlyHasValue<String> titleImageAlt = new ReadOnlyHasValue<>(titleImage::setAlt);
        final ReadOnlyHasValue<String> titleImageTitle = new ReadOnlyHasValue<>(titleImage::setTitle);
        binder.forField(titleImageSrc).bind(i -> i.getPath(resolution), null);
        binder.forField(titleImageAlt).bind(ImageMapper::getAlternativeText, null);
        binder.forField(titleImageTitle).bind(ImageMapper::getTitleText, null);
        add(titleImage);
    }

    @Override
    public void setValue(ImageMapper value) {
        binder.readBean(value);
    }

    @Override
    public ImageMapper getValue() {
        return binder.getBean();
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ImageContainer, ImageMapper>> listener) {
        return null;
    }

    @Override
    public void setReadOnly(boolean readOnly) {

    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {

    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return false;
    }
}
