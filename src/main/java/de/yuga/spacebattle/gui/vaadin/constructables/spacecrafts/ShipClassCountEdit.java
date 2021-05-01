package de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details.ModuleValueTypePerShipHorizontalDisplay;
import de.yuga.spacebattle.gui.vaadin.misc.details.NumericField;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.details.ShipClassCountDTO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ShipClassCountEdit extends VerticalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<ShipClassCountEdit, ShipClassCountDTO>, ShipClassCountDTO>, HasValidation {

    @Nonnull
    private final Binder<ShipClassCountDTO> binderShipClass = new Binder<>(ShipClassCountDTO.class);

    @Nonnull
    private final NumericField amountField = new NumericField();

    @Nullable
    private ShipClassCountDTO shipClassCountDTO;

    public ShipClassCountEdit() {
        final Label name = new Label();
        final ReadOnlyHasValue<String> moduleNameReadOnly = new ReadOnlyHasValue<>(name::setText);
        binderShipClass.forField(moduleNameReadOnly).bind(ShipClassCountDTO::getName, null);

        final ModuleValueTypePerShipHorizontalDisplay moduleValueTypeVerticalDisplay = new ModuleValueTypePerShipHorizontalDisplay();
        binderShipClass.forField(moduleValueTypeVerticalDisplay).bind(ShipClassCountDTO::getShipClass, null);

        binderShipClass.forField(amountField).bind(ShipClassCountDTO::getCount, ShipClassCountDTO::setCount);

        amountField.addClassName("numeric-before-amount");
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.add(amountField, name);
        add(horizontalLayout, moduleValueTypeVerticalDisplay);
    }

    public void update(@Nonnull final ShipClassCountDTO shipClassCountDTO) {
        Preconditions.checkNotNull(shipClassCountDTO, "shipClassCountWrapper shouldn't be null!");

        if (this.shipClassCountDTO == null) {
            binderShipClass.setBean(shipClassCountDTO);
        }
        binderShipClass.readBean(shipClassCountDTO);
        this.shipClassCountDTO = shipClassCountDTO;
    }

    @Override
    public void setValue(ShipClassCountDTO value) {
        this.update(value);
    }

    /**
     * Returns the wrapper which should contain the original module itself and the possibly modified values.
     *
     * @return the wrapper
     */
    @Nullable
    @Override
    public ShipClassCountDTO getValue() {
        return binderShipClass.getBean();
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ShipClassCountEdit, ShipClassCountDTO>> listener) {
        return binderShipClass.addValueChangeListener(event -> {
            final AbstractField.ComponentValueChangeEvent<ShipClassCountEdit, ShipClassCountDTO> changeEvent =
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
