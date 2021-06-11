package de.yuga.spacebattle.gui.vaadin.spacecrafts;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.details.AmmunitionModuleCountDTO;

import javax.annotation.Nonnull;

public class AmmunitionModuleCountEdit extends HorizontalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<AmmunitionModuleCountEdit, AmmunitionModuleCountDTO>, AmmunitionModuleCountDTO>, HasValidation {

    @Nonnull
    private final Binder<AmmunitionModuleCountDTO> binder = new Binder<>(AmmunitionModuleCountDTO.class);

    @Nonnull
    private final IntegerField amountField = new IntegerField();

    public AmmunitionModuleCountEdit() {

        setClassName("module-display");

        final Label name = new Label();
        final ReadOnlyHasValue<String> moduleNameReadOnly = new ReadOnlyHasValue<>(name::setText);
        binder.forField(moduleNameReadOnly).bind(AmmunitionModuleCountDTO::getName, null);

        final Label description = new Label();
        final ReadOnlyHasValue<String> moduleDescriptionReadOnly = new ReadOnlyHasValue<>(description::setText);
        binder.forField(moduleDescriptionReadOnly).bind(AmmunitionModuleCountDTO::getDescription, null);

        final Label supportsWhat = new Label();
        final ReadOnlyHasValue<String> supportsWhatReadOnly = new ReadOnlyHasValue<>(supportsWhat::setText);
        binder.forField(supportsWhatReadOnly).bind(AmmunitionModuleCountDTO::getSupportsWhatDescription, null);

        amountField.setHasControls(true);
        amountField.setMin(0);
        binder.forField(amountField).bind(AmmunitionModuleCountDTO::getCount, AmmunitionModuleCountDTO::setCount);

        add(amountField, name, description, supportsWhat);
    }

    @Override
    public void setValue(@Nonnull final AmmunitionModuleCountDTO weaponAlignmentCountDTO) {
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
    public AmmunitionModuleCountDTO getValue() {
        AmmunitionModuleCountDTO bean = binder.getBean();
        if (bean == null) {
            throw new NotifySBUserException("this shouldn't be empty - check it");
        }
        return bean;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<AmmunitionModuleCountEdit, AmmunitionModuleCountDTO>> listener) {
        return binder.addValueChangeListener(event -> {
            final AbstractField.ComponentValueChangeEvent<AmmunitionModuleCountEdit, AmmunitionModuleCountDTO> changeEvent =
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
