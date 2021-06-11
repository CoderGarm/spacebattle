package de.yuga.spacebattle.gui.vaadin.orbitals.colonization;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.resources.MiningFactors;
import de.yuga.spacebattle.backend.enums.EResolution;
import de.yuga.spacebattle.gui.vaadin.turn.resource.ResourceHorizontalDisplay;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Creates a new {@link Grid} for the planets of a star system and displays their mining resources or the button to colonize a planet.
 */
public class PlanetSelectionGrid extends VerticalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<PlanetSelectionGrid, ColonizationTransportStarSystemDTO>, ColonizationTransportStarSystemDTO> {

    @Nonnull
    private final Grid<Planet> grid = new Grid<>();

    @Nullable
    private ColonizationTransportStarSystemDTO colonizationTransportStarSystemDTO;

    @Nullable
    private ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<PlanetSelectionGrid, ColonizationTransportStarSystemDTO>> valueChangeListener;

    public PlanetSelectionGrid() {
        grid.setColumnReorderingAllowed(true);
        grid.addClassName("header-grid");

        grid.setHeightByRows(true);
        grid.addColumn(Planet::getName)
                .setHeader("Planet's name")
                .setWidth("250px")
                .setFlexGrow(0);

        grid.addComponentColumn(planet -> {
            final ResourceHorizontalDisplay resourceHorizontalDisplay = new ResourceHorizontalDisplay(EResolution.PX16);
            final MiningFactors miningFactors = planet.getMiningFactors();
            resourceHorizontalDisplay.updateResources(miningFactors);
            return resourceHorizontalDisplay;
        }).setHeader("Resource factors");

        final Map<Button, ColonizeConfirmationEdit> colonizePlanetMap = new HashMap<>();

        final ComponentRenderer<Component, Planet> detailRenderer = new ComponentRenderer<>(planet -> {
            final ColonizeConfirmationEdit colonizeConfirmationEdit = new ColonizeConfirmationEdit();
            colonizeConfirmationEdit.setValue(planet);
            colonizeConfirmationEdit.addSubmitListener(event -> {
                final ColonizeConfirmationEdit confirmationEdit = colonizePlanetMap.get(event.getSource());
                if (valueChangeListener != null && confirmationEdit != null) {
                    if (colonizationTransportStarSystemDTO == null) {
                        throw new NotifySBUserException("There is nothing to see here, please go on.");
                    }
                    final Planet toColonize = confirmationEdit.getValue();
                    colonizationTransportStarSystemDTO.setColonizationSelection(toColonize);
                    valueChangeListener.valueChanged(new AbstractField.ComponentValueChangeEvent<>(this, this, getValue(), true));
                }
            });
            colonizePlanetMap.put(colonizeConfirmationEdit.getSubmitButton(), colonizeConfirmationEdit);
            return colonizeConfirmationEdit;
        });
        grid.setItemDetailsRenderer(detailRenderer);

        add(grid);
    }

    @Override
    public void setValue(ColonizationTransportStarSystemDTO value) {
        colonizationTransportStarSystemDTO = value;
        if (value == null) {
            grid.setItems(new HashSet<>());
            return;
        }
        final Set<Planet> planets = value.getStarSystem().getPlanets();
        grid.setItems(planets.stream().filter(Planet::isColonizable));
    }

    @Nullable
    @Override
    public ColonizationTransportStarSystemDTO getValue() {
        return colonizationTransportStarSystemDTO;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<PlanetSelectionGrid, ColonizationTransportStarSystemDTO>> listener) {
        valueChangeListener = listener;
        return (Registration) () -> valueChangeListener = null;
    }

    @Override
    public void setReadOnly(boolean readOnly) {

    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {

    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return false;
    }
}
