package de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.enums.EResolution;
import de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details.ModuleValueTypePerShipDisplayCompact;
import de.yuga.spacebattle.gui.vaadin.misc.details.CostsDisplayCompact;
import de.yuga.spacebattle.gui.vaadin.misc.details.misc.ImageContainer;
import de.yuga.spacebattle.gui.vaadin.misc.details.misc.SimpleLabelWithCaption;
import de.yuga.spacebattle.gui.vaadin.misc.details.misc.TooltipDisplay;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.details.ShipClassCountDTO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@CssImport("./styles/views/main/details/ship-count-edit.css")
public class ShipClassCountEdit extends VerticalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<ShipClassCountEdit, ShipClassCountDTO>, ShipClassCountDTO>, HasValidation {

    @Nonnull
    private final Binder<ShipClassCountDTO> binder = new Binder<>();

    @Nonnull
    private final IntegerField amountField = new IntegerField();

    @Nullable
    private ShipClassCountDTO shipClassCountDTO;

    public ShipClassCountEdit() {
        addClassName("ship-frame");

        final ImageContainer imageContainer = new ImageContainer(EResolution.PX64);
        imageContainer.addClassName("ship-image");
        binder.forField(imageContainer).bind(dto -> dto, null);

        final HorizontalLayout nameContainer = new HorizontalLayout();
        final SimpleLabelWithCaption name = new SimpleLabelWithCaption("Name");
        binder.forField(name).bind(ShipClassCountDTO::getName, null);

        final SimpleLabelWithCaption mark = new SimpleLabelWithCaption("Mark");
        binder.forField(mark).bind(ShipClassCountDTO::getMark, null);
        nameContainer.add(name, mark, imageContainer);

        final HorizontalLayout hullContainer = new HorizontalLayout();
        final SimpleLabelWithCaption hull = new SimpleLabelWithCaption("Hull class");
        binder.forField(hull).bind(ShipClassCountDTO::getHullClass, null);

        final SimpleLabelWithCaption hullDesc = new SimpleLabelWithCaption("Hull description");
        binder.forField(hullDesc).bind(ShipClassCountDTO::getHullDescription, null);

        hullContainer.add(hull, hullDesc);

        final ModuleValueTypePerShipDisplayCompact stats = new ModuleValueTypePerShipDisplayCompact();
        stats.addClassName("stats");
        binder.forField(stats).bind(ShipClassCountDTO::getShipClass, null);

        final CostsDisplayCompact costsDisplay = new CostsDisplayCompact(EResolution.PX24);
        costsDisplay.addClassName("costs");
        binder.forField(costsDisplay).bind(dto -> dto.getShipClass().getCostsOverall(), null);
        final Button costs = new Button("Costs");
        new TooltipDisplay(this, costs, costsDisplay);

        amountField.setHasControls(true);
        amountField.setMin(0);
        binder.forField(amountField).bind(ShipClassCountDTO::getCountNumeric, ShipClassCountDTO::setCountNumeric);

        add(amountField, nameContainer, hullContainer, stats, costs);
    }

    @Override
    public void setValue(ShipClassCountDTO value) {
        Preconditions.checkNotNull(value, "shipClassCountWrapper shouldn't be null!");

        if (this.shipClassCountDTO == null) {
            binder.setBean(value);
        }
        binder.readBean(value);
        this.shipClassCountDTO = value;
    }

    /**
     * Returns the wrapper which should contain the original module itself and the possibly modified values.
     *
     * @return the wrapper
     */
    @Nullable
    @Override
    public ShipClassCountDTO getValue() {
        return binder.getBean();
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ShipClassCountEdit, ShipClassCountDTO>> listener) {
        return binder.addValueChangeListener(event -> {
            final AbstractField.ComponentValueChangeEvent<ShipClassCountEdit, ShipClassCountDTO> changeEvent =
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
