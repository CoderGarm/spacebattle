package de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.binder.ErrorLevel;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.gui.vaadin.NotificationHelper;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
import de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details.ShipClassEditDTO;
import de.yuga.spacebattle.gui.vaadin.events.ESBEvent;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.HullDisplay;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.ModuleMultiEdit;
import de.yuga.spacebattle.gui.vaadin.validators.ShipDataVaadinValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.spring.events.Event;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

import static de.yuga.spacebattle.gui.vaadin.validators.ShipDataVaadinValidator.ShipDataVaadinValidatorField.*;

public class ShipClassEdit extends ShipClassLayout<ShipClass>
        implements HasValue<AbstractField.ComponentValueChangeEvent<ShipClassEdit, ShipClassEditDTO>, ShipClassEditDTO> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShipClassEdit.class);

    @Nonnull
    private final EventBus.UIEventBus uiEventBus = ViewHelper.getService(EventBus.UIEventBus.class);

    @Nonnull
    private final Button submit;

    @Nonnull
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Nonnull
    private final Binder<ShipClassEditDTO> binder = new Binder<>(ShipClassEditDTO.class);

    public ShipClassEdit() {
        setClassName("module-display");

        this.uiEventBus.subscribe(this);

        createHullSvg();

        binder.addValueChangeListener(event -> {
            final ShipClass shipClass = getShipClass();
            getShipClassStatDisplay().setValue(shipClass);
            validateSubmitButton();
        });

        final HorizontalLayout nameLayout = new HorizontalLayout();
        final TextField name = new TextField();
        binder.forField(name)
                .withValidator((value, context) -> {
                    final ShipClass shipClass = getShipClass();
                    shipClass.setName(value);
                    return ShipDataVaadinValidator.check(shipClass, NAME);
                })
                .withValidationStatusHandler(this::openNotification)
                .asRequired()
                .bind(ShipClassEditDTO::getName, ShipClassEditDTO::setName);

        final Label markLabel = new Label();
        final ReadOnlyHasValue<String> markLabelText = new ReadOnlyHasValue<>(markLabel::setText);
        binder.forField(markLabelText).bind(s -> "Mk " + s.getShipClass().getMark(), null);
        nameLayout.add(name, markLabel);

        final HullDisplay hullDisplay = new HullDisplay();
        final ReadOnlyHasValue<Hull> hullSelectedReadOnly = new ReadOnlyHasValue<>(hullDisplay::setValue);
        binder.forField(hullSelectedReadOnly)
                .withValidator((value, context) -> {
                    final ShipClass shipClass = getShipClass();
                    shipClass.setHull(value);
                    return ShipDataVaadinValidator.check(shipClass, HULL);
                })
                .withValidationStatusHandler(this::openNotification)
                .bind(ShipClassEditDTO::getHull, null);

        final ModuleMultiEdit moduleMultiEdit = new ModuleMultiEdit(starShipSvgHelper);
        binder.forField(moduleMultiEdit)
                .withValidator((value, context) -> {
                    final ShipClass shipClass = getShipClass();
                    value.prepareShipClassValidation(shipClass);
                    return ShipDataVaadinValidator.check(shipClass, MODULES);
                })
                .withValidationStatusHandler(this::openNotification)
                .bind(ShipClassEditDTO::getModules, ShipClassEditDTO::setModules);

        submit = new Button("Submit", event -> this.uiEventBus.publish(this, ESBEvent.SHIP_CLASS_SUBMITTED.name()));
        submit.setEnabled(false);

        final Button delete = new Button("Delete class", event -> this.uiEventBus.publish(this, ESBEvent.SHIP_CLASS_DELETION.name()));

        final HorizontalLayout buttonBar = new HorizontalLayout(submit, delete);
        add(nameLayout, hullDisplay, moduleMultiEdit, buttonBar);
    }

    @Override
    protected void addDragStartListener() {
        canvas.addDragStartListener(event -> {

        });
    }

    /**
     * Opens a notification with the validation message and validates the submit button.
     *
     * @param msg the validation message
     */
    private void openNotification(BindingValidationStatus<?> msg) {
        if (msg.getStatus() == BindingValidationStatus.Status.OK) {
            validateSubmitButton();
            return;
        }

        msg.getResult().ifPresent(validationResult ->
                validationResult.getErrorLevel().ifPresent(errorLevel -> {
                    if (errorLevel != ErrorLevel.INFO) {
                        NotificationHelper.notify(validationResult.getErrorMessage(), 3000);
                        submit.setEnabled(false);
                    }
                }));
    }

    /**
     * Checks if the ship class bean is valid.
     * todo check if the current selected ship class is equals to its predecessor in all properties
     */
    private void validateSubmitButton() {
        Set<ConstraintViolation<ShipClass>> validate = validator.validate(getShipClass());
        this.submit.setEnabled(validate.isEmpty());
    }

    /**
     * Returns the constructed ship class - or null if nothing were clicked.
     *
     * @return the ship class
     */
    @Nonnull
    public ShipClass getShipClass() {
        ShipClassEditDTO bean = binder.getBean();
        if (bean == null) {
            throw new NotifySBUserException("You could click on submit, congratulations! Please click only if this class is ready.");
        }
        return bean.getShipClass();
    }

    /**
     * The event receiver which receives events.
     *
     * @param e the event to compute
     */
    @EventBusListenerMethod
    protected void onEvent(Event<String> e) {
    }

    @Override
    @Deprecated(since = "Implementation of create and edit dto.")
    public void updateStatistics(@Nullable final ShipClass value) {
        throw new NotifySBUserException("You shouldn't need to call that, lil buddy!");
    }

    @Override
    public void setValue(@Nonnull final ShipClassEditDTO value) {
        Preconditions.checkNotNull(value, "value shouldn't be null!");

        binder.setBean(value);
    }

    @Override
    public ShipClassEditDTO getValue() {
        return binder.getBean();
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ShipClassEdit, ShipClassEditDTO>> listener) {
        return null;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        // not necessary
    }

    @Override
    public boolean isReadOnly() {
        return false;
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
