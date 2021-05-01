package de.yuga.spacebattle.gui.vaadin.combined.spacecrafts.details.fleetsplit;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import com.vaadin.flow.shared.Registration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class SplitFleetIntoThatEdit extends VerticalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<SplitFleetIntoThatEdit, FleetSplitDTO>, FleetSplitDTO> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SplitFleetIntoThatEdit.class);

    @Nonnull
    private final TextField nameField = new TextField();

    @Nonnull
    private final Binder<FleetSplitDTO> binder = new Binder<>();

    @Nonnull
    private final Map<ShipClassCountSplitDTO, ShipClassNumericStepperEdit> componentMap = new HashMap<>();

    @Nullable
    private ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<SplitFleetIntoThatEdit, FleetSplitDTO>> valueChangeListener;

    @Nonnull
    private final VerticalLayout fleetAmountLayout = new VerticalLayout();

    /**
     * Holds every registered listener for this and it's children component. But you have to register them manually.
     * Every {@link Registration} will be removed if it's part of this list and the component will be left.
     */
    private final List<Registration> registrationList = new ArrayList<>();

    public SplitFleetIntoThatEdit() {
        final Label baseFleetsName = new Label();
        final ReadOnlyHasValue<String> baseFleetsNameReadOnly = new ReadOnlyHasValue<>(baseFleetsName::setText);
        binder.forField(baseFleetsNameReadOnly).bind(FleetSplitDTO::getBaseFleetName, null);

        binder.forField(nameField)
                .withValidator(StringUtils::isNotBlank, "The name should not be empty, probably.")
                .bind(FleetSplitDTO::getName, FleetSplitDTO::setName);

        final HorizontalLayout nameLayout = new HorizontalLayout();

        nameLayout.add(baseFleetsName, nameField);
        add(nameLayout, fleetAmountLayout);
    }

    @Override
    public void setValue(FleetSplitDTO value) {

        binder.setBean(value);

        final List<ShipClassCountSplitDTO> ships = value.getShips();
        final Set<ShipClassCountSplitDTO> notAnymorePresent = componentMap.keySet().stream().filter(dto -> !ships.contains(dto)).collect(Collectors.toSet());
        notAnymorePresent.forEach(dto -> remove(componentMap.get(dto)));
        componentMap.keySet().removeAll(notAnymorePresent);

        ships.forEach(dto -> {
            ShipClassNumericStepperEdit stepperEdit = componentMap.get(dto);
            if (stepperEdit == null) {
                stepperEdit = new ShipClassNumericStepperEdit();
                componentMap.put(dto, stepperEdit);
            }
            stepperEdit.setValue(dto);
        });

        setListenerAction();

        final List<ShipClassCountSplitDTO> sortedShipClasses = componentMap.keySet().stream()
                .sorted(new ShipClassCountSplitDTOComparator())
                .collect(Collectors.toList());

        sortedShipClasses.forEach(dto -> {
            final ShipClassNumericStepperEdit shipClassNumericStepperEdit = componentMap.get(dto);
            fleetAmountLayout.add(shipClassNumericStepperEdit);
        });

    }

    @Nullable
    @Override
    public FleetSplitDTO getValue() {
        final FleetSplitDTO bean = binder.getBean();
        if (bean == null) {
            return null;
        }
        bean.setShips(new ArrayList<>(componentMap.keySet()));
        return bean;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<SplitFleetIntoThatEdit, FleetSplitDTO>> listener) {
        this.valueChangeListener = listener;
        setListenerAction();
        return new Registration() {
            @Override
            public void remove() {
                valueChangeListener = null;
            }
        };
    }

    private void doAction() {
        if (valueChangeListener != null && binder.validate().isOk()) {
            final FleetSplitDTO value = getValue();
            if (value == null) {
                return;
            }
            // validate that there is at least one ship transferred
            final ShipClassCountSplitDTO dto = value.getShips().stream().filter(s -> s.getSplitCount() > 0).findAny().orElse(null);
            if (dto == null) {
                return;
            }
            this.valueChangeListener.valueChanged(new AbstractField.ComponentValueChangeEvent<>(this, this, value, true));
        }
    }

    private void setListenerAction() {
        registrationList.forEach(Registration::remove);

        Registration valueChangeListener1 = binder.addValueChangeListener(event -> {
            doAction();
        });
        registrationList.add(valueChangeListener1);

        componentMap.values().forEach(stepperEdit -> {
            Registration valueChangeListener = stepperEdit.addValueChangeListener(event -> {
                doAction();
            });
            registrationList.add(valueChangeListener);
        });
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        componentMap.values().forEach(s -> s.setReadOnly(readOnly));
        nameField.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() {
        return nameField.isReadOnly();
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {

    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return false;
    }
}
