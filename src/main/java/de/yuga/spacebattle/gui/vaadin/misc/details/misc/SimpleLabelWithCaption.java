package de.yuga.spacebattle.gui.vaadin.misc.details.misc;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HtmlContainer;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import com.vaadin.flow.shared.Registration;

import javax.annotation.Nonnull;

@Tag(Tag.DIV)
@CssImport("./styles/views/main/details/simple-label-with-caption.css")
public class SimpleLabelWithCaption extends HtmlContainer implements HasValue<AbstractField.ComponentValueChangeEvent<SimpleLabelWithCaption, String>, String> {

    @Nonnull
    private final Binder<String> binder = new Binder<>();

    final Label label = new Label();

    public SimpleLabelWithCaption(@Nonnull final String caption) {
        Preconditions.checkNotNull(caption, "caption shouldn't be null!");

        addClassName("simple-label-with-caption");

        final Label captionLabel = new Label(caption);
        captionLabel.addClassName("caption-label");

        label.addClassName("simple-label");
        final ReadOnlyHasValue<String> readOnlyHasValue = new ReadOnlyHasValue<>(label::setText);
        binder.forField(readOnlyHasValue).bind(String::intern, null);
        add(captionLabel, label);
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
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<SimpleLabelWithCaption, String>> listener) {
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
