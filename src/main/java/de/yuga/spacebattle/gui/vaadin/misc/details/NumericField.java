package de.yuga.spacebattle.gui.vaadin.misc.details;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;

@CssImport("./styles/numeric-field-styles.css")
public class NumericField extends TextField {

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

        Button subtractButton = new Button("-", event -> setNumericValue(numericValue - 1));
        Button addButton = new Button("+", event -> setNumericValue(numericValue + 1));

        getElement().setAttribute("theme", "numeric");
        subtractButton.getElement().setAttribute("theme", "icon");
        addButton.getElement().setAttribute("theme", "icon");

        addToPrefix(subtractButton);
        addToSuffix(addButton);
    }

    public void setNumericValue(int value) {
        numericValue = value;
        setValue(value + "");
    }

    public int getNumericValue() {
        return numericValue;
    }
}
