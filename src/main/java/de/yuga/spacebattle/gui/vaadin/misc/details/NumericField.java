package de.yuga.spacebattle.gui.vaadin.misc.details;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;

@CssImport("./styles/numeric-field-styles.css")
public class NumericField extends TextField {

    @Nonnull
    private final Button subtractButton;

    @Nonnull
    private final Button addButton;

    private int numericValue;

    public NumericField() {
        this(0);
    }

    public NumericField(final int value) {
        setNumericValue(value);

        setPattern("-?[0-9]*");
        setPreventInvalidInput(true);

        addChangeListener(event -> {
            String text = event.getSource().getValue();
            if (StringUtils.isNumeric(text)) {
                setNumericValue(Integer.parseInt(text));
            } else {
                setNumericValue(0);
            }
        });

        subtractButton = new Button("-", event -> setNumericValue(numericValue - 1));
        addButton = new Button("+", event -> setNumericValue(numericValue + 1));

        getElement().setAttribute("theme", "numeric");
        subtractButton.getElement().setAttribute("theme", "icon");
        addButton.getElement().setAttribute("theme", "icon");

        addToPrefix(subtractButton);
        addToSuffix(addButton);
    }

    @Override
    public void setValue(String value) {
        setNumericValue(Integer.parseInt(value));
    }

    public void setNumericValue(int value) {
        numericValue = value;
        super.setValue(value + "");
    }

    public int getNumericValue() {
        return numericValue;
    }

    /**
     * Stupid pseudo-overriding the readOnly setter to access buttons.
     *
     * @param readOnly the value
     */
    public void setReadonlyForButtons(boolean readOnly) {
        subtractButton.setEnabled(!readOnly);
        addButton.setEnabled(!readOnly);
    }
}
