package de.yuga.spacebattle.gui.vaadin.spacecrafts;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.gui.vaadin.misc.details.NumericField;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.details.ModuleCountWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModuleCountEdit extends HorizontalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<ModuleCountEdit, ModuleCountWrapper>, ModuleCountWrapper>, HasValidation {

    @Nonnull
    private final Binder<ModuleCountWrapper> binderModule = new Binder<>(ModuleCountWrapper.class);

    @Nonnull
    private final NumericField amountField = new NumericField();

    @Nullable
    private ModuleCountWrapper moduleCountWrapper;

    public ModuleCountEdit() {

        Label name = new Label();
        final ReadOnlyHasValue<String> moduleNameReadOnly = new ReadOnlyHasValue<>(name::setText);
        binderModule.forField(moduleNameReadOnly).bind(ModuleCountWrapper::getModuleName, null);

        Label description = new Label();
        final ReadOnlyHasValue<String> moduleDescriptionReadOnly = new ReadOnlyHasValue<>(description::setText);
        binderModule.forField(moduleDescriptionReadOnly).bind(ModuleCountWrapper::getModuleDescription, null);

        binderModule.forField(amountField).bind(ModuleCountWrapper::getCount, ModuleCountWrapper::setCount);

        amountField.addClassName("numeric-before-amount");
        add(amountField, name, description);
    }

    public void update(@Nonnull final ModuleCountWrapper moduleCountWrapper) {
        Preconditions.checkNotNull(moduleCountWrapper, "moduleCountWrapper shouldn't be null!");

        if (this.moduleCountWrapper == null) {
            binderModule.setBean(moduleCountWrapper);
        }
        binderModule.readBean(moduleCountWrapper);
        this.moduleCountWrapper = moduleCountWrapper;
    }

    @Override
    public void setValue(ModuleCountWrapper value) {
        this.update(value);
    }

    /**
     * Returns the wrapper which should contain the original module itself and the possibly modified values.
     *
     * @return the wrapper
     */
    @Nullable
    @Override
    public ModuleCountWrapper getValue() {
        return binderModule.getBean();
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ModuleCountEdit, ModuleCountWrapper>> listener) {
        return binderModule.addValueChangeListener(event -> {
            final AbstractField.ComponentValueChangeEvent<ModuleCountEdit, ModuleCountWrapper> changeEvent =
                    new AbstractField.ComponentValueChangeEvent<>(this, this, getValue(), false);
            listener.valueChanged(changeEvent);
        });
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        amountField.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() {
        return amountField.isReadOnly();
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        amountField.setRequiredIndicatorVisible(requiredIndicatorVisible);
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return amountField.isRequiredIndicatorVisible();
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        amountField.setErrorMessage(errorMessage);
    }

    @Override
    public String getErrorMessage() {
        return amountField.getErrorMessage();
    }

    @Override
    public void setInvalid(boolean invalid) {
        amountField.setInvalid(invalid);
    }

    @Override
    public boolean isInvalid() {
        return amountField.isInvalid();
    }
}
