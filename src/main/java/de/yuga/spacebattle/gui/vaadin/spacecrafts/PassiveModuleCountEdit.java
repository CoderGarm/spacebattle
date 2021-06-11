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
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.gui.vaadin.misc.details.NumericField;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.details.PassiveModuleCountDTO;

import javax.annotation.Nonnull;

public class PassiveModuleCountEdit extends HorizontalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<PassiveModuleCountEdit, PassiveModuleCountDTO>, PassiveModuleCountDTO>, HasValidation {

    @Nonnull
    private final Binder<PassiveModuleCountDTO> binder = new Binder<>(PassiveModuleCountDTO.class);

    @Nonnull
    private final NumericField amountField = new NumericField();

    public PassiveModuleCountEdit() {

        setClassName("module-display");

        final Label name = new Label();
        final ReadOnlyHasValue<String> moduleNameReadOnly = new ReadOnlyHasValue<>(name::setText);
        binder.forField(moduleNameReadOnly).bind(PassiveModuleCountDTO::getName, null);

        final Label description = new Label();
        final ReadOnlyHasValue<String> moduleDescriptionReadOnly = new ReadOnlyHasValue<>(description::setText);
        binder.forField(moduleDescriptionReadOnly).bind(PassiveModuleCountDTO::getDescription, null);

        final Label supportsWhat = new Label();
        final ReadOnlyHasValue<String> supportsWhatReadOnly = new ReadOnlyHasValue<>(supportsWhat::setText);
        binder.forField(supportsWhatReadOnly).bind(PassiveModuleCountDTO::getSupportsWhatDescription, null);

        binder.forField(amountField).bind(PassiveModuleCountDTO::getCount, PassiveModuleCountDTO::setCount);

        amountField.addClassName("numeric-before-amount");
        add(amountField, name, description, supportsWhat);
    }

    @Override
    public void setValue(@Nonnull final PassiveModuleCountDTO weaponAlignmentCountDTO) {
        Preconditions.checkNotNull(weaponAlignmentCountDTO, "moduleCountWrapper shouldn't be null!");

        binder.setBean(weaponAlignmentCountDTO);
    }

    /**
     * Returns the wrapper which should contain the original module itself and the possibly modified values.
     *
     * @return the wrapper
     */
    @Nonnull
    @Override
    public PassiveModuleCountDTO getValue() {
        PassiveModuleCountDTO bean = binder.getBean();
        if (bean == null) {
            throw new NotifySBUserException("this shouldn't be empty - check it");
        }
        return bean;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<PassiveModuleCountEdit, PassiveModuleCountDTO>> listener) {
        return binder.addValueChangeListener(event -> {
            final AbstractField.ComponentValueChangeEvent<PassiveModuleCountEdit, PassiveModuleCountDTO> changeEvent =
                    new AbstractField.ComponentValueChangeEvent<>(this, this, getValue(), false);
            listener.valueChanged(changeEvent);
        });
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        amountField.setReadOnly(readOnly);
        amountField.setReadonlyForButtons(readOnly);
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
