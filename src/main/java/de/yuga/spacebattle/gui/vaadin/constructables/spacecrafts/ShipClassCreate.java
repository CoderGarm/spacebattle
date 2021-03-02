package de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.binder.ErrorLevel;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.entities.spacecrafts.Module;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.validators.base.CustomValidatorFactory;
import de.yuga.spacebattle.gui.vaadin.NotificationHelper;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
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
import java.util.Collection;
import java.util.Set;

import static de.yuga.spacebattle.gui.vaadin.validators.ShipDataVaadinValidator.ShipDataVaadinValidatorField.*;

public class ShipClassCreate extends ShipClassLayout {

    @Nonnull
    private final static Logger LOGGER = LoggerFactory.getLogger(ShipClassCreate.class);

    @Nonnull
    private final UserService userService = ViewHelper.getService(UserService.class);

    @Nonnull
    private final EventBus.UIEventBus uiEventBus = ViewHelper.getService(EventBus.UIEventBus.class);

    @Nonnull
    private final Binder<ShipClass> binderShipClass = new Binder<>(ShipClass.class);

    @Nonnull
    private final Binder<ShipClass> binderShipClassStats = new Binder<>(ShipClass.class);

    @Nonnull
    private final HullSelector hullSelect;

    @Nonnull
    private ShipClass shipClass;

    @Nonnull
    private final Validator validator = CustomValidatorFactory.buildCustomValidator();

    @Nonnull
    private final Button submit;

    @Nullable
    private Collection<Hull> hulls;

    @Nullable
    private Collection<Module> modules;

    public ShipClassCreate() {
        this.uiEventBus.subscribe(this);

        shipClass = createStub();

        binderShipClassStats.forField(getShipClassStatDisplay()).bind(shipClass -> shipClass, null);

        binderShipClass.addValueChangeListener(event -> binderShipClassStats.readBean(shipClass));

        final TextField name = new TextField();
        name.setRequiredIndicatorVisible(true);
        binderShipClass.forField(name)
                .withValidator((value, context) -> {
                    shipClass.setName(value);
                    return ShipDataVaadinValidator.check(shipClass, NAME);
                })
                .withValidationStatusHandler(this::openNotification)
                .asRequired()
                .bind(ShipClass::getName, ShipClass::setName);


        hullSelect = new HullSelector();
        binderShipClass.forField(hullSelect)
                .withValidator((value, context) -> {
                    Hull hull = new ArrayList<>(value).get(0);
                    shipClass.setHull(hull);
                    return ShipDataVaadinValidator.check(shipClass, HULL);
                })
                .withValidationStatusHandler(this::openNotification)
                .bind(ShipClass::getPossibleHulls, (shipClass1, hulls) -> shipClass.setHull(hullSelect.getValue().get(0)));

        final ModuleMultiEdit moduleMultiEdit = new ModuleMultiEdit();
        binderShipClass.forField(moduleMultiEdit)
                .withValidator((value, context) -> {
                    shipClass.setModules(value);
                    return ShipDataVaadinValidator.check(shipClass, MODULES);
                })
                .withValidationStatusHandler(this::openNotification)
                .bind(ShipClass::getPossibleModules, ShipClass::setModules);

        submit = new Button("Submit", event -> this.uiEventBus.publish(this, ESBEvent.SHIP_CLASS_SUBMITTED.name()));
        submit.setEnabled(false);

        final Button clear = new Button("Clear display", event -> {
            createStub();
            validateSubmitButton();
        });

        final HorizontalLayout buttonBar = new HorizontalLayout(submit, clear);
        add(name, hullSelect, moduleMultiEdit, buttonBar);
    }

    /**
     * Creates a stub ship class which I formerly want to avoid but fuck it's necessary -.-
     *
     * @return the stub class
     */
    private ShipClass createStub() {
        binderShipClass.readBean(null);
        ShipClass shipClass = new ShipClass();

        final User loggedIn = userService.isLoggedIn();
        if (loggedIn == null) {
            throw new NotifySBUserException("oha, this should not be possible.");
        }
        shipClass.setOwner(loggedIn);
        shipClass.setHull(new Hull());
        shipClass.setPossibleModules(modules);
        shipClass.setPossibleHulls(hulls);
        binderShipClass.setBean(shipClass);
        this.shipClass = shipClass;
        return shipClass;
    }

    /**
     * Checks if the ship class bean is valid
     */
    private void validateSubmitButton() {
        Set<ConstraintViolation<ShipClass>> validate = validator.validate(shipClass);
        this.submit.setEnabled(validate.isEmpty());
    }

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
     * Will update the edit display, depending on the shipClass param.
     *
     * @param hulls   the hulls to select in the UI
     * @param modules the modules to select in the UI
     */
    public void update(@Nonnull final Collection<Hull> hulls, @Nonnull final Collection<Module> modules) {
        Preconditions.checkNotNull(hulls, "hulls shouldn't be null!");
        Preconditions.checkNotNull(modules, "modules shouldn't be null!");

        this.hulls = hulls;
        this.modules = modules;
        shipClass.setPossibleHulls(hulls);
        shipClass.setPossibleModules(modules);
        binderShipClass.readBean(shipClass);
    }


    /**
     * Returns the constructed ship class - or null if nothing were clicked.
     *
     * @return the ship class
     */
    @Nonnull
    public ShipClass getShipClass() {
        ShipClass shipClassBean = binderShipClass.getBean();
        if (shipClassBean == null) {
            throw new NotifySBUserException("You could click on submit, congratulations! Please click only if this class is ready.");
        }
        return shipClass;
    }

    /**
     * The event receiver which receives events.
     *
     * @param e the event to compute
     */
    @EventBusListenerMethod
    protected void onEvent(Event<String> e) {
    }
}
