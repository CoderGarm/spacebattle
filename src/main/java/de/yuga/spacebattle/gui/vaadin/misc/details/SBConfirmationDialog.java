package de.yuga.spacebattle.gui.vaadin.misc.details;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.gui.vaadin.misc.SBDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * Confirmation dialog with the same properties of {@link SBDialog}.
 * The submit button is disabled per definition.
 */
public class SBConfirmationDialog extends SBDialog {

    private static final Logger LOGGER = LoggerFactory.getLogger(SBConfirmationDialog.class);

    @Nonnull
    private final Button submit;

    @Nonnull
    private final Button cancel;

    public SBConfirmationDialog(@Nonnull Component content) {
        super(content);

        final Div buttonBar = new Div();
        buttonBar.setClassName("sb-dialog-button-bar " + DRAGGABLE_DRAGGABLE_LEAF_ONLY);
        submit = new Button("Submit");
        submit.setClassName("sb-dialog-submit");
        cancel = new Button("Cancel");
        cancel.setClassName("sb-dialog-cancel");
        buttonBar.add(submit, cancel);
        add(buttonBar);
        submit.setEnabled(false);
    }

    /**
     * Attaches the listener for the submit button.
     *
     * @param clickListener the listener to attach
     * @return the registration
     */
    public Registration addSubmitListener(@Nonnull final ComponentEventListener<ClickEvent<Button>> clickListener) {
        Preconditions.checkNotNull(clickListener, "clickListener shouldn't be null!");

        return submit.addClickListener(clickListener);
    }

    /**
     * Attaches the listener for the cancel button.
     *
     * @param clickListener the listener to attach
     * @return the registration
     */
    public Registration addCancelListener(@Nonnull final ComponentEventListener<ClickEvent<Button>> clickListener) {
        Preconditions.checkNotNull(clickListener, "clickListener shouldn't be null!");

        final Registration r1 = close.addClickListener(clickListener);
        final Registration r2 = cancel.addClickListener(clickListener);
        return Registration.combine(r1, r2);
    }

    public void enableSubmitButton(final boolean isEnabled) {
        submit.setEnabled(isEnabled);
    }
}
