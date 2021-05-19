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
import de.yuga.spacebattle.gui.vaadin.spacecrafts.details.WeaponAlignmentDTO;

import javax.annotation.Nonnull;

public class WeaponCountEdit extends HorizontalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<WeaponCountEdit, WeaponAlignmentDTO>, WeaponAlignmentDTO>, HasValidation {

    @Nonnull
    private final Binder<WeaponAlignmentDTO> binder = new Binder<>(WeaponAlignmentDTO.class);

    @Nonnull
    private final NumericField amountField = new NumericField();

    public WeaponCountEdit() {

        setClassName("module-display");

        final Label name = new Label();
        final ReadOnlyHasValue<String> moduleNameReadOnly = new ReadOnlyHasValue<>(name::setText);
        binder.forField(moduleNameReadOnly).bind(WeaponAlignmentDTO::getWeaponName, null);

        final Label description = new Label();
        final ReadOnlyHasValue<String> moduleDescriptionReadOnly = new ReadOnlyHasValue<>(description::setText);
        binder.forField(moduleDescriptionReadOnly).bind(WeaponAlignmentDTO::getWeaponDescription, null);

        binder.forField(amountField).bind(WeaponAlignmentDTO::getCount, WeaponAlignmentDTO::setCount);

        amountField.addClassName("numeric-before-amount");
        add(amountField, name, description);
    }

    @Override
    public void setValue(@Nonnull final WeaponAlignmentDTO weaponAlignmentDTO) {
        Preconditions.checkNotNull(weaponAlignmentDTO, "moduleCountWrapper shouldn't be null!");

        binder.setBean(weaponAlignmentDTO);
    }

    /**
     * Returns the wrapper which should contain the original module itself and the possibly modified values.
     *
     * @return the wrapper
     */
    @Nonnull
    @Override
    public WeaponAlignmentDTO getValue() {
        WeaponAlignmentDTO bean = binder.getBean();
        if (bean == null) {
            throw new NotifySBUserException("this shouldn't be empty - check it");
        }
        return bean;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<WeaponCountEdit, WeaponAlignmentDTO>> listener) {
        return binder.addValueChangeListener(event -> {
            final AbstractField.ComponentValueChangeEvent<WeaponCountEdit, WeaponAlignmentDTO> changeEvent =
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
