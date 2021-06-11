package de.yuga.spacebattle.gui.vaadin.turn.resource.crew;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.enums.EResolution;
import de.yuga.spacebattle.gui.vaadin.misc.details.misc.ImageContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CrewIconDisplaySingle extends HorizontalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<CrewIconDisplaySingle, CrewIconAmountDTO>, CrewIconAmountDTO> {

    @Nonnull
    private final Binder<CrewIconAmountDTO> binder = new Binder<>();

    public CrewIconDisplaySingle(@Nonnull final EResolution resolution) {
        Preconditions.checkNotNull(resolution, "resolution shouldn't be null!");

        final ImageContainer imageContainer = new ImageContainer(resolution);
        binder.forField(imageContainer).bind(w -> w, null);

        final Label name = new Label();
        final ReadOnlyHasValue<String> nameReadOnly = new ReadOnlyHasValue<>(name::setText);
        binder.forField(nameReadOnly).bind(CrewIconAmountDTO::getAmountAsString, null);

        add(imageContainer, name);
    }

    @Override
    public void setValue(@Nonnull final CrewIconAmountDTO value) {
        Preconditions.checkNotNull(value, "value shouldn't be null!");

        binder.readBean(value);
    }

    @Nullable
    @Override
    public CrewIconAmountDTO getValue() {
        return binder.getBean();
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<CrewIconDisplaySingle, CrewIconAmountDTO>> listener) {
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
