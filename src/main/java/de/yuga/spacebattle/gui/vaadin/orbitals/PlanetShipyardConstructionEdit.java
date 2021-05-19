package de.yuga.spacebattle.gui.vaadin.orbitals;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
import de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.ShipClassCountEdit;
import de.yuga.spacebattle.gui.vaadin.events.ESBEvent;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.details.ShipClassCountDTO;
import org.vaadin.spring.events.Event;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class PlanetShipyardConstructionEdit extends PlanetLayout<Planet> implements HasValue<AbstractField.ComponentValueChangeEvent<PlanetShipyardConstructionEdit, Planet>, Planet> {

    @Nonnull
    private static final String BUILD = "Build";

    @Nonnull
    private static final String JOB_IN_PROGRESS = "Job in progress";

    @Nonnull
    private final EventBus.UIEventBus uiEventBus = ViewHelper.getService(EventBus.UIEventBus.class);

    @Nonnull
    private final Map<ShipClass, ShipClassCountEdit> componentsMap = new HashMap<>();

    @Nonnull
    private final Binder<Planet> binderPlanet = new Binder<>(Planet.class);

    @Nonnull
    private final VerticalLayout verticalLayout = new VerticalLayout();

    @Nonnull
    private final Button submit;

    @Nonnull
    private final Button clear;

    @Nonnull
    private Map<ShipClass, Integer> shipJobPayload = new HashMap<>();

    public PlanetShipyardConstructionEdit() {

        uiEventBus.subscribe(this);
        binderPlanet.forField(getPlanetResourceDisplay()).bind(planet -> planet, null);

        final Label title = new Label("Shipyard");
        submit = new Button(BUILD, event -> {
            shipJobPayload = componentsMap.values().stream()
                    .map(ShipClassCountEdit::getValue)
                    .filter(Objects::nonNull)
                    .filter(dto -> dto.getCountNumeric() > 0)
                    .map(ShipClassCountDTO::getAsEntry)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            uiEventBus.publish(this, ESBEvent.ORBITAL_CONSTRUCTION_JOB_BUILDING_START.name());
        });

        clear = new Button("Clear display", event -> {
            componentsMap.values().forEach(shipClassCountEdit -> {
                ShipClassCountDTO value = shipClassCountEdit.getValue();
                if (value == null) {
                    throw new NotifySBUserException("There should be an accountable amount. Talk to the admin.");
                }
                value.setCountNumeric(0);
                shipClassCountEdit.setValue(value);
            });
            validateSubmitButton();
        });

        final HorizontalLayout buttonBar = new HorizontalLayout(submit, clear);
        add(title, verticalLayout, buttonBar);
        validateSubmitButton();
    }

    private void validateSubmitButton() {
        componentsMap.values().stream().map(ShipClassCountEdit::getValue)
                .filter(Objects::nonNull)
                .filter(dto -> dto.getCountNumeric() > 0)
                .findFirst()
                .ifPresentOrElse(shipClassCountDTO -> setReadOnly(false), () -> submit.setEnabled(false));
    }

    public void update(@Nullable final Planet planet) {
        binderPlanet.readBean(planet);
        if (planet == null) {
            clear();
            return;
        }
        planet.getConstructions().stream()
                .filter(construction -> construction.getBuilding().getResourceType() == EResourceType.ORBITALCONSTRUCTION)
                .findFirst()
                .ifPresentOrElse(this::createConstructionSelection, this::clear);
    }

    @Override
    public void setValue(Planet value) {
        update(value);
    }

    @Nullable
    @Override
    public Planet getValue() {
        return binderPlanet.getBean();
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<PlanetShipyardConstructionEdit, Planet>> listener) {
        return null;
    }

    @Override
    public void clear() {
        componentsMap.values().forEach(verticalLayout::remove);
        componentsMap.clear();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        submit.setEnabled(!readOnly);
        final String text = !readOnly ? BUILD : JOB_IN_PROGRESS;
        submit.setText(text);
        clear.setEnabled(!readOnly);
        componentsMap.values().forEach(shipClassCountEdit -> shipClassCountEdit.setReadOnly(readOnly));
    }

    @Override
    public boolean isReadOnly() {
        return !submit.isEnabled();
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        // not necessary
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return false;
    }

    /**
     * Creates the editable views with every job option by this {@link Construction}.
     *
     * @param construction the construction which includes the job options
     */
    private void createConstructionSelection(@Nonnull final Construction construction) {
        Preconditions.checkNotNull(construction, "construction shouldn't be null!");

        final User user = construction.getPlanet().getOwner();
        if (user == null) {
            throw new NotifySBUserException("You should be logged in here.");
        }
        final Set<ShipClass> shipClasses = user.getShipClasses();

        shipClasses.forEach(building -> {
            final ShipClassCountEdit shipClassCountEdit;
            if (componentsMap.containsKey(building)) {
                shipClassCountEdit = componentsMap.get(building);
            } else {
                shipClassCountEdit = new ShipClassCountEdit();
                shipClassCountEdit.addValueChangeListener(event -> validateSubmitButton());
                componentsMap.put(building, shipClassCountEdit);
            }
            shipClassCountEdit.setValue(new ShipClassCountDTO(building, 0));
            shipClassCountEdit.setReadOnly(!construction.getJobs().isEmpty());
        });
        setReadOnly(!construction.getJobs().isEmpty());
        componentsMap.values().forEach(verticalLayout::add);
        add(verticalLayout);
    }

    @Nonnull
    public Map<ShipClass, Integer> getShipJobPayload() {
        return shipJobPayload;
    }

    /**
     * The event receiver which receives events.
     *
     * @param e the event to compute
     */
    @EventBusListenerMethod
    protected void onEvent(Event<String> e) {
        if (e.getPayload().equals(ESBEvent.ORBITAL_CONSTRUCTION_JOB_BUILDING_FEEDBACK_STARTED.getName())) {
            setReadOnly(false);
        }

    }
}
