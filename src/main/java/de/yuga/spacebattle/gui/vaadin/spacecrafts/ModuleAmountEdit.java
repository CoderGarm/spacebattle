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
import de.yuga.spacebattle.gui.vaadin.spacecrafts.details.ModuleAmountWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModuleAmountEdit extends HorizontalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<ModuleAmountEdit, ModuleAmountWrapper>, ModuleAmountWrapper>, HasValidation {

    @Nonnull
    private final Binder<ModuleAmountWrapper> binderModule = new Binder<>(ModuleAmountWrapper.class);

    @Nonnull
    private final NumericField amountField = new NumericField();

    @Nullable
    private ModuleAmountWrapper moduleAmountWrapper;

    public ModuleAmountEdit() {

        Label name = new Label();
        final ReadOnlyHasValue<String> moduleNameReadOnly = new ReadOnlyHasValue<>(name::setText);
        binderModule.forField(moduleNameReadOnly).bind(ModuleAmountWrapper::getModuleName, null);

        Label description = new Label();
        final ReadOnlyHasValue<String> moduleDescriptionReadOnly = new ReadOnlyHasValue<>(description::setText);
        binderModule.forField(moduleDescriptionReadOnly).bind(ModuleAmountWrapper::getModuleDescription, null);

        binderModule.forField(amountField).bind(ModuleAmountWrapper::getAmount, ModuleAmountWrapper::setAmount);

        add(name, description, amountField);
    }

    public void update(@Nonnull final ModuleAmountWrapper moduleAmountWrapper) {
        Preconditions.checkNotNull(moduleAmountWrapper, "moduleAmountWrapper shouldn't be null!");

        if (this.moduleAmountWrapper == null) {
            binderModule.setBean(moduleAmountWrapper);
        }
        binderModule.readBean(moduleAmountWrapper);
        this.moduleAmountWrapper = moduleAmountWrapper;
    }

    @Override
    public void setValue(ModuleAmountWrapper value) {
        this.update(value);
    }

    /**
     * Returns the wrapper which should contain the original module itself and the possibly modified values.
     *
     * @return the wrapper
     */
    @Nullable
    @Override
    public ModuleAmountWrapper getValue() {
        return binderModule.getBean();
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ModuleAmountEdit, ModuleAmountWrapper>> listener) {
        return binderModule.addValueChangeListener(event -> {
            final AbstractField.ComponentValueChangeEvent<ModuleAmountEdit, ModuleAmountWrapper> changeEvent =
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
