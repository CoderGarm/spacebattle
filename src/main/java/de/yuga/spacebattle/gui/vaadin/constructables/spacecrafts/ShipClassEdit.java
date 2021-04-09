package de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.entities.spacecrafts.Module;
import de.yuga.spacebattle.backend.validators.base.CustomValidatorFactory;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
import de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details.ShipClassDTO;
import de.yuga.spacebattle.gui.vaadin.events.ESBEvent;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.HullDisplay;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.ModuleMultiEdit;
import org.vaadin.spring.events.Event;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ShipClassEdit extends ShipClassLayout<ShipClass> {

    @Nonnull
    private final EventBus.UIEventBus uiEventBus = ViewHelper.getService(EventBus.UIEventBus.class);

    @Nonnull
    private final Button submit;

    @Nonnull
    private final Validator validator = CustomValidatorFactory.buildCustomValidator();

    @Nonnull
    private final Binder<ShipClassDTO> binderShipClass = new Binder<>(ShipClassDTO.class);

    private Map<Module, Integer> modulesMap;

    public ShipClassEdit() {
        this.uiEventBus.subscribe(this);

        final TextField name = new TextField();
        binderShipClass.forField(name).bind(ShipClassDTO::getName, ShipClassDTO::setName);

        binderShipClass.forField(getShipClassStatDisplay()).bind(ShipClassDTO::getShipClass, null);

        final HullDisplay hullDisplay = new HullDisplay();
        final ReadOnlyHasValue<Hull> hullSelectedReadOnly = new ReadOnlyHasValue<>(hullDisplay::update);
        binderShipClass.forField(hullSelectedReadOnly).bind(ShipClassDTO::getHull, null);

        final ModuleMultiEdit moduleMultiEdit = new ModuleMultiEdit();
        final ReadOnlyHasValue<Map<Module, Integer>> levelValueReadOnly = new ReadOnlyHasValue<>(moduleMultiEdit::setValue);
        binderShipClass.forField(levelValueReadOnly).bind(ShipClassDTO::getPossibleModules, null);

        binderShipClass.forField(moduleMultiEdit).bind(ShipClassDTO::getModules, ShipClassDTO::setModules);

        binderShipClass.addValueChangeListener(event -> validateSubmitButton());

        submit = new Button("Submit", event -> this.uiEventBus.publish(this, ESBEvent.SHIP_CLASS_SUBMITTED.name()));
        submit.setEnabled(false);

        final Button delete = new Button("Delete class", event -> this.uiEventBus.publish(this, ESBEvent.SHIP_CLASS_DELETION.name()));

        final HorizontalLayout buttonBar = new HorizontalLayout(submit, delete);
        add(name, hullDisplay, moduleMultiEdit, buttonBar);
    }

    /**
     * Sets the available modules to this view.
     *
     * @param modules the modules to select in the UI
     */
    public void setBaseData(@Nonnull final Collection<Module> modules) {
        Preconditions.checkNotNull(modules, "modules shouldn't be null!");

        modulesMap = modules.stream().collect(Collectors.toMap(Function.identity(), val -> 0));
        final ShipClassDTO bean = binderShipClass.getBean();
        if (bean != null) {
            bean.setPossibleModules(modulesMap);
            binderShipClass.readBean(bean);
        }
    }

    /**
     * Checks if the ship class bean is valid
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
        ShipClassDTO bean = binderShipClass.getBean();
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
    public void update(@Nullable final ShipClass value) {
        ShipClassDTO shipClassDTO = null;
        if (value != null) {
            shipClassDTO = new ShipClassDTO(value);
            shipClassDTO.setPossibleModules(modulesMap);
        }
        if (binderShipClass.getBean() == null) {
            binderShipClass.setBean(shipClassDTO);
        } else {
            binderShipClass.readBean(shipClassDTO);
        }
    }
}
