package de.yuga.spacebattle.gui.vaadin.misc.details.misc;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HtmlContainer;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import com.vaadin.flow.shared.Registration;

import javax.annotation.Nonnull;

@Tag(Tag.DIV)
public class SimpleLabel extends HtmlContainer implements HasValue<AbstractField.ComponentValueChangeEvent<SimpleLabel, String>, String> {

    @Nonnull
    private final Binder<String> binder = new Binder<>();

    public SimpleLabel() {
        final Label label = new Label();
        label.addClassName("simple-label");
        final ReadOnlyHasValue<String> readOnlyHasValue = new ReadOnlyHasValue<>(label::setText);
        binder.forField(readOnlyHasValue).bind(String::intern, null);
        add(label);
    }

    @Override
    public void setValue(String value) {
        binder.readBean(value);
    }

    @Override
    public String getValue() {
        return binder.getBean();
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<SimpleLabel, String>> listener) {
        return null;
    }

    @Override
    public void setReadOnly(boolean readOnly) {

    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {

    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return false;
    }
}
