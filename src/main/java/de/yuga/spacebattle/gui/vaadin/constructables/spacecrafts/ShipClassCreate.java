package de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.binder.ErrorLevel;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.entities.spacecrafts.Module;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.validators.base.CustomValidatorFactory;
import de.yuga.spacebattle.gui.vaadin.NotificationHelper;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
import de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details.ShipClassWrapper;
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
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.gui.vaadin.validators.ShipDataVaadinValidator.ShipDataVaadinValidatorField.*;

public class ShipClassCreate extends ShipClassLayout<ShipClass> {

    @Nonnull
    private final static Logger LOGGER = LoggerFactory.getLogger(ShipClassCreate.class);

    @Nonnull
    private final UserService userService = ViewHelper.getService(UserService.class);

    @Nonnull
    private final EventBus.UIEventBus uiEventBus = ViewHelper.getService(EventBus.UIEventBus.class);

    @Nonnull
    private final Binder<ShipClassWrapper> binderShipClass = new Binder<>(ShipClassWrapper.class);

    @Nonnull
    private final HullSelector hullSelect;

    @Nonnull
    private ShipClass shipClass;

    @Nonnull
    private final Validator validator = CustomValidatorFactory.buildCustomValidator();

    @Nonnull
    private final Button submit;

    @Nonnull
    private Collection<Hull> hulls = new HashSet<>();

    @Nonnull
    private Collection<Module> modules = new HashSet<>();

    public ShipClassCreate() {
        this.uiEventBus.subscribe(this);

        shipClass = createStub();

        binderShipClass.forField(getShipClassStatDisplay()).bind(ShipClassWrapper::getShipClass, null);

        final TextField name = new TextField();
        name.setRequiredIndicatorVisible(true);
        binderShipClass.forField(name)
                .withValidator((value, context) -> {
                    shipClass.setName(value);
                    return ShipDataVaadinValidator.check(shipClass, NAME);
                })
                .withValidationStatusHandler(this::openNotification)
                .asRequired()
                .bind(ShipClassWrapper::getName, ShipClassWrapper::setName);


        hullSelect = new HullSelector();
        binderShipClass.forField(hullSelect)
                .withValidator((value, context) -> {
                    Hull hull = new ArrayList<>(value).get(0);
                    shipClass.setHull(hull);
                    return ShipDataVaadinValidator.check(shipClass, HULL);
                })
                .withValidationStatusHandler(this::openNotification)
                .bind(ShipClassWrapper::getPossibleHulls, ShipClassWrapper::setHull);

        final ModuleMultiEdit moduleMultiEdit = new ModuleMultiEdit();
        final ReadOnlyHasValue<Map<Module, Integer>> levelValueReadOnly = new ReadOnlyHasValue<>(moduleMultiEdit::setPossibleModules);
        binderShipClass.forField(levelValueReadOnly).bind(ShipClassWrapper::getPossibleModules, null);

        binderShipClass.forField(moduleMultiEdit)
                .withValidator((value, context) -> {
                    shipClass.setModules(value);
                    return ShipDataVaadinValidator.check(shipClass, MODULES);
                })
                .withValidationStatusHandler(this::openNotification)
                .bind(ShipClassWrapper::getPossibleModules, ShipClassWrapper::setModules);

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

        final User loggedIn = userService.getLoggedInUser();
        if (loggedIn == null) {
            throw new NotifySBUserException("oha, this should not be possible.");
        }
        shipClass.setOwner(loggedIn);
        shipClass.setHull(new Hull());
        final ShipClassWrapper shipClassWrapper = new ShipClassWrapper(shipClass);
        shipClassWrapper.setPossibleModules(modules.stream().collect(Collectors.toMap(Function.identity(), val -> 0)));
        shipClassWrapper.setPossibleHulls(hulls);
        binderShipClass.setBean(shipClassWrapper);
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
     * Defines the amount of base data which the user could use.
     *
     * @param hulls   the hulls to select in the UI
     * @param modules the modules to select in the UI
     */
    public void setBaseData(@Nonnull final Collection<Hull> hulls, @Nonnull final Collection<Module> modules) {
        Preconditions.checkNotNull(hulls, "hulls shouldn't be null!");
        Preconditions.checkNotNull(modules, "modules shouldn't be null!");

        this.hulls = hulls;
        this.modules = modules;
        ShipClassWrapper shipClassWrapper = binderShipClass.getBean();
        if (shipClassWrapper == null) {
            shipClassWrapper = new ShipClassWrapper(this.shipClass);
        }
        shipClassWrapper.setPossibleHulls(hulls);
        shipClassWrapper.setPossibleModules(modules.stream().collect(Collectors.toMap(Function.identity(), val -> 0)));
        binderShipClass.readBean(shipClassWrapper);
    }


    /**
     * Returns the constructed ship class - or null if nothing was clicked.
     *
     * @return the ship class
     */
    @Nonnull
    public ShipClass getShipClass() {
        ShipClassWrapper shipClassBean = binderShipClass.getBean();
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

    @Override
    public void update(ShipClass value) {
        // nothing to do
    }
}
