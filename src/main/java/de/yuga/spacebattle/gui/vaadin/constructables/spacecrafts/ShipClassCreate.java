package de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.binder.ErrorLevel;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.validators.base.CustomValidatorFactory;
import de.yuga.spacebattle.gui.vaadin.NotificationHelper;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
import de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details.ShipClassCreateDTO;
import de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details.ShipClassEditDTO;
import de.yuga.spacebattle.gui.vaadin.events.ESBEvent;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.HullSelector;
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
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.Set;

import static de.yuga.spacebattle.gui.vaadin.validators.ShipDataVaadinValidator.ShipDataVaadinValidatorField.*;

public class ShipClassCreate extends ShipClassLayout<ShipClass>
        implements HasValue<AbstractField.ComponentValueChangeEvent<ShipClassCreate, ShipClassCreateDTO>, ShipClassCreateDTO> {

    @Nonnull
    private final static Logger LOGGER = LoggerFactory.getLogger(ShipClassCreate.class);

    @Nonnull
    private final EventBus.UIEventBus uiEventBus = ViewHelper.getService(EventBus.UIEventBus.class);

    @Nonnull
    private final Binder<ShipClassCreateDTO> binderShipClass = new Binder<>(ShipClassCreateDTO.class);

    @Nonnull
    private final Validator validator = CustomValidatorFactory.buildCustomValidator();

    @Nonnull
    private final Button submit;

    public ShipClassCreate() {
        this.uiEventBus.subscribe(this);

        binderShipClass.addValueChangeListener(event -> {
            final ShipClass shipClass = getShipClass();
            getShipClassStatDisplay().setValue(shipClass);
        });

        final TextField name = new TextField();
        name.setRequiredIndicatorVisible(true);
        binderShipClass.forField(name)
                .withValidator((value, context) -> {
                    final ShipClass shipClass = getShipClass();
                    shipClass.setName(value);
                    return ShipDataVaadinValidator.check(shipClass, NAME);
                })
                .withValidationStatusHandler(this::openNotification)
                .asRequired()
                .bind(ShipClassEditDTO::getName, ShipClassEditDTO::setName);


        HullSelector hullSelect = new HullSelector();
        binderShipClass.forField(hullSelect)
                .withValidator((value, context) -> {
                    final Hull hull = new ArrayList<>(value).get(0);
                    final ShipClass shipClass = getShipClass();
                    shipClass.setHull(hull);
                    return ShipDataVaadinValidator.check(shipClass, HULL);
                })
                .withValidationStatusHandler(this::openNotification)
                .bind(ShipClassCreateDTO::getPossibleHulls, ShipClassCreateDTO::setHulls);

        final ModuleMultiEdit moduleMultiEdit = new ModuleMultiEdit();
        binderShipClass.forField(moduleMultiEdit)
                .withValidator((value, context) -> {
                    final ShipClass shipClass = getShipClass();
                    shipClass.setModules(value);
                    return ShipDataVaadinValidator.check(shipClass, MODULES);
                })
                .withValidationStatusHandler(this::openNotification)
                .bind(ShipClassEditDTO::getModules, ShipClassEditDTO::setModules);

        submit = new Button("Submit", event -> {
            this.uiEventBus.publish(this, ESBEvent.SHIP_CLASS_SUBMITTED.name());
            event.getSource().setEnabled(false);
            event.getSource().setText("Job started");
        });
        submit.setEnabled(false);

        final Button clear = new Button("Clear display", event -> {
            resetDTO();
            validateSubmitButton();
        });

        final HorizontalLayout buttonBar = new HorizontalLayout(submit, clear);
        add(name, hullSelect, moduleMultiEdit, buttonBar);
    }

    /**
     * Resets the user input in that way that the dto is untouched like freshly fallen snow.
     */
    private void resetDTO() {
        final ShipClassCreateDTO bean = binderShipClass.getBean();
        if (bean != null) {
            bean.setHull(null);
            bean.setName(null);
            bean.resetModules();
            binderShipClass.readBean(bean);
        }
    }

    /**
     * Checks if the ship class bean is valid
     */
    private void validateSubmitButton() {
        final ShipClass shipClass = getShipClass();
        Set<ConstraintViolation<ShipClass>> validate = validator.validate(shipClass);
        this.submit.setEnabled(validate.isEmpty());
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
     * Returns the constructed ship class - or null if nothing was clicked.
     *
     * @return the ship class
     */
    @Nonnull
    public ShipClass getShipClass() {
        ShipClassCreateDTO shipClassBean = binderShipClass.getBean();
        if (shipClassBean == null) {
            throw new NotifySBUserException("You could click on submit, congratulations! Please click only if this class is ready.");
        }
        return shipClassBean.getShipClass();
    }

    /**
     * The event receiver which receives events.
     *
     * @param e the event to compute
     */
    @EventBusListenerMethod
    protected void onEvent(Event<String> e) {
        // not necessary
    }

    @Override
    public void update(ShipClass value) {
        resetDTO();
    }


    @Override
    public void setValue(@Nullable final ShipClassCreateDTO value) {
        if (value == null) {
            binderShipClass.readBean(null);
        } else {
            binderShipClass.setBean(value);
        }
    }

    @Override
    public ShipClassCreateDTO getValue() {
        return binderShipClass.getBean();
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ShipClassCreate, ShipClassCreateDTO>> listener) {
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
