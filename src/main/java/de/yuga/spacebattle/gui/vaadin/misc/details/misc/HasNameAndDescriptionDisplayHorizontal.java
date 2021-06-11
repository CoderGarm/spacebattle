package de.yuga.spacebattle.gui.vaadin.misc.details.misc;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.HasNameAndDescription;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Displays the name and the description of a building.
 */
public class HasNameAndDescriptionDisplayHorizontal extends HorizontalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<HasNameAndDescriptionDisplayHorizontal, HasNameAndDescription>, HasNameAndDescription> {

    @Nonnull
    private final Binder<HasNameAndDescription> binder = new Binder<>();

    public HasNameAndDescriptionDisplayHorizontal() {

        final Label name = new Label();
        final ReadOnlyHasValue<String> nameReadOnly = new ReadOnlyHasValue<>(name::setText);
        binder.forField(nameReadOnly).bind(HasNameAndDescription::getName, null);

        final Label description = new Label();
        final ReadOnlyHasValue<String> descriptionReadOnly = new ReadOnlyHasValue<>(description::setText);
        binder.forField(descriptionReadOnly).bind(HasNameAndDescription::getDescription, null);

        add(name, description);
    }

    @Override
    public void setValue(HasNameAndDescription value) {
        binder.readBean(value);
    }

    @Nullable
    @Override
    public HasNameAndDescription getValue() {
        return null;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<HasNameAndDescriptionDisplayHorizontal, HasNameAndDescription>> listener) {
        // not necessary
        return null;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        // not necessary
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        // not necessary
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return false;
    }
}
