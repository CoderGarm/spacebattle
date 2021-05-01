package de.yuga.spacebattle.gui.vaadin.combined.spacecrafts.details.fleetsplit;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.gui.vaadin.misc.details.NumericStepEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ShipClassNumericStepperEdit extends HorizontalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<ShipClassNumericStepperEdit, ShipClassCountSplitDTO>, ShipClassCountSplitDTO> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShipClassNumericStepperEdit.class);

    @Nonnull
    private final Binder<ShipClassCountSplitDTO> binder = new Binder<>();

    @Nonnull
    private final NumericStepEdit stepEdit = new NumericStepEdit();

    @Nullable
    private ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ShipClassNumericStepperEdit, ShipClassCountSplitDTO>> valueChangeListener;

    public ShipClassNumericStepperEdit() {
        final Label classNameLabel = new Label();
        final ReadOnlyHasValue<String> classNameLabelReadOnly = new ReadOnlyHasValue<>(classNameLabel::setText);
        binder.forField(classNameLabelReadOnly).bind(ShipClassCountSplitDTO::getName, null);

        final Label referenceAmountLabel = new Label();
        final ReadOnlyHasValue<String> referenceAmountLabelReadOnly = new ReadOnlyHasValue<>(referenceAmountLabel::setText);
        binder.forField(referenceAmountLabelReadOnly).bind(ShipClassCountSplitDTO::getCalculatedReferenceAmount, null);

        binder.forField(stepEdit)
                // validate that the split count is not below zero and not above the reference count
                .withValidator(integer -> getValue() != null && integer >= 0 && integer <= getValue().getReferenceAmountNumeric(), "You cannot exceed the amount limit.")
                .bind(ShipClassCountSplitDTO::getSplitCount, ShipClassCountSplitDTO::setSplitCount);

        binder.addValueChangeListener(event -> {
            // update binder because the referenceAmountLabel has to be updated, too
            final ShipClassCountSplitDTO bean = getValue();
            binder.readBean(bean);
            // validate that there is at least one transferred ship selected
            if (valueChangeListener != null && binder.validate().isOk()) {
                valueChangeListener.valueChanged(new AbstractField.ComponentValueChangeEvent<>(this, this, getValue(), true));
            }
        });

        add(classNameLabel, referenceAmountLabel, stepEdit);
    }

    @Override
    public void setValue(ShipClassCountSplitDTO value) {
        binder.setBean(value);
    }

    @Nullable
    @Override
    public ShipClassCountSplitDTO getValue() {
        return binder.getBean();
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ShipClassNumericStepperEdit, ShipClassCountSplitDTO>> listener) {

        valueChangeListener = listener;
        return new Registration() {
            @Override
            public void remove() {
                valueChangeListener = null;
            }
        };
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        stepEdit.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() {
        return stepEdit.isReadOnly();
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {

    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return false;
    }
}
