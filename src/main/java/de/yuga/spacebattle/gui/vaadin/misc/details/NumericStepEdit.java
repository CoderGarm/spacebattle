package de.yuga.spacebattle.gui.vaadin.misc.details;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.shared.Registration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NumericStepEdit extends HorizontalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<NumericStepEdit, Integer>, Integer> {

    private final int step = 1;

    private Integer oldValue = 0;

    private Integer value = 0;

    @Nonnull
    private final Button increase;

    @Nonnull
    private final Button decrease;

    @Nonnull
    private final Label valueLabel = new Label();

    @Nullable
    private ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<NumericStepEdit, Integer>> valueChangeListener;

    public NumericStepEdit() {
        increase = new Button(">>", event -> {
            increase();
        });
        decrease = new Button("<<", event -> {
            decrease();
        });

        getElement().setAttribute("theme", "numeric");
        increase.getElement().setAttribute("theme", "icon");
        decrease.getElement().setAttribute("theme", "icon");


        add(decrease, increase, valueLabel);
    }

    private void decrease() {
        oldValue = value;
        setValue(value - step);
    }

    private void increase() {
        oldValue = value;
        setValue(value + step);
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<NumericStepEdit, Integer>> listener) {

        valueChangeListener = listener;
        return new Registration() {
            @Override
            public void remove() {
                valueChangeListener = null;
            }
        };
    }

    /**
     * Notifies all listeners.
     */
    private void communicate() {
        if (valueChangeListener != null) {
            valueChangeListener.valueChanged(new AbstractField.ComponentValueChangeEvent<>(this, this, oldValue, false));
        }
    }

    @Override
    public void setValue(Integer value) {
        this.value = value;
        valueLabel.setText(String.valueOf(this.value));
        communicate();
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        decrease.setEnabled(!readOnly);
        increase.setEnabled(!readOnly);
    }

    @Nonnull
    public Button getIncrease() {
        return increase;
    }

    @Nonnull
    public Button getDecrease() {
        return decrease;
    }

    @Override
    public boolean isReadOnly() {
        return !decrease.isEnabled() || !increase.isEnabled();
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
