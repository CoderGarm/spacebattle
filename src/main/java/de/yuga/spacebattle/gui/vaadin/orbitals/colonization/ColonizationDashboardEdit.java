package de.yuga.spacebattle.gui.vaadin.orbitals.colonization;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
import de.yuga.spacebattle.gui.vaadin.orbitals.details.OrbitCoordinatesHorizontalDisplay;
import de.yuga.spacebattle.gui.vaadin.orbitals.starmap.ViewBoxDefinition;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This view offers the opportunity to buy data for a system and colonize a planet afterwards.
 */
public class ColonizationDashboardEdit extends ColonizationLayout implements HasValue<AbstractField.ComponentValueChangeEvent<ColonizationDashboardEdit, ColonizationTransportUniverseDTO>, ColonizationTransportUniverseDTO> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ColonizationDashboardEdit.class);

    @Nonnull
    private final UserService userService = ViewHelper.getService(UserService.class);

    @Nonnull
    private final Grid<StarSystemColonizationDTO> grid = new Grid<>();

    @Nullable
    private StarSystem selected;

    @Nullable
    private ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ColonizationDashboardEdit, ColonizationTransportUniverseDTO>> valueChangeListener;

    @Nullable
    private ColonizationTransportUniverseDTO universe;

    public ColonizationDashboardEdit() {
        grid.setColumnReorderingAllowed(true);

        final User loggedInUser = userService.getLoggedInUser();

        final HorizontalLayout control = new HorizontalLayout();

        final Set<Planet> ownedPlanets = loggedInUser.getOwnedPlanets();
        final StarSystemSelect planetSelect = new StarSystemSelect();
        final Set<StarSystem> collect = ownedPlanets.stream().map(Planet::getSystem).collect(Collectors.toSet());
        planetSelect.setItems(collect);
        planetSelect.addValueChangeListener(event -> {
            selected = event.getValue();
            grid.getDataProvider().refreshAll();
        });

        // filtering for known or all systems
        final Checkbox onlyKnown = new Checkbox("Show only known systems");
        onlyKnown.addValueChangeListener(event -> {
            //noinspection unchecked
            final ListDataProvider<StarSystemColonizationDTO> dataProvider =
                    (ListDataProvider<StarSystemColonizationDTO>) grid.getDataProvider();
            dataProvider.clearFilters();
            if (event.getValue()) {
                dataProvider.addFilter(StarSystemColonizationDTO::isKnown);
            } else {
                dataProvider.addFilter(dto -> true);
            }
        });

        final TextField nameColumnFilterField = new TextField();

        final Button resetAllSorting = new Button("Reset filter", event -> {
            grid.sort(null);
            planetSelect.setValue(null);
            nameColumnFilterField.setValue("");
            selected = null;
            grid.getDataProvider().refreshAll();
        });

        control.add(planetSelect, onlyKnown, resetAllSorting);

        nameColumnFilterField.addValueChangeListener(event -> {
            //noinspection unchecked
            final ListDataProvider<StarSystemColonizationDTO> dataProvider =
                    (ListDataProvider<StarSystemColonizationDTO>) grid.getDataProvider();
            dataProvider.addFilter(dto -> {
                final String filter = event.getHasValue().getValue();
                if (StringUtils.isBlank(filter)) {
                    return true;
                }
                return dto.getStarSystemName().toLowerCase().contains(filter.toLowerCase().trim());
            });
            dataProvider.refreshAll();
        });

        nameColumnFilterField.setValueChangeMode(ValueChangeMode.EAGER);
        nameColumnFilterField.setSizeFull();
        nameColumnFilterField.setPlaceholder("Filter");
        nameColumnFilterField.getElement().setAttribute("focus-target", "");

        grid.addClassName("header-grid");
        final Grid.Column<StarSystemColonizationDTO> nameColumn =
                grid.addColumn(StarSystemColonizationDTO::getStarSystemName, "Star system")
                        .setHeader("Star system");

        final Grid.Column<StarSystemColonizationDTO> orbitColumn = grid.addComponentColumn(dto -> {
            final OrbitCoordinatesHorizontalDisplay orbitDisplay = new OrbitCoordinatesHorizontalDisplay();
            orbitDisplay.setValue(dto.getStarSystemOrbit());
            return orbitDisplay;
        });
        orbitColumn.setHeader("Orbit");

        grid.addColumn(dto -> {
            final Orbit baseSystemOrbit = selected != null ? selected.getOrbit() : Orbit.getCenterOrbit();
            final double orbitalDistance = ViewBoxDefinition.getOrbitalDistance(baseSystemOrbit, dto.getStarSystemOrbit());
            BigDecimal bigDecimal = new BigDecimal(orbitalDistance);
            return bigDecimal.setScale(2, RoundingMode.FLOOR);
        }).setComparator((o1, o2) -> {
            // create sorting by distance to either universe center or the selected planet
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }

            final Orbit baseSystemOrbit = selected != null ? selected.getOrbit() : Orbit.getCenterOrbit();
            final Double orbitalDistance1 = ViewBoxDefinition.getOrbitalDistance(baseSystemOrbit, o1.getStarSystemOrbit());
            final Double orbitalDistance2 = ViewBoxDefinition.getOrbitalDistance(baseSystemOrbit, o2.getStarSystemOrbit());
            return orbitalDistance1.compareTo(orbitalDistance2);
        }).setHeader("Distance");

        final HeaderRow filterRow = grid.appendHeaderRow();

        final QuadrantSelector quadrantSelector = new QuadrantSelector();
        quadrantSelector.addValueChangeListener(event -> {
            //noinspection unchecked
            final ListDataProvider<StarSystemColonizationDTO> dataProvider =
                    (ListDataProvider<StarSystemColonizationDTO>) grid.getDataProvider();
            dataProvider.clearFilters();
            dataProvider.addFilter(dto -> {
                final QuadrantSelector.QuadrantSelectorValidator filterValue = event.getValue();
                final Orbit systemOrbit = dto.getStarSystemOrbit();
                return filterValue.contains(systemOrbit);
            });
            dataProvider.refreshAll();
        });
        filterRow.getCell(orbitColumn).setComponent(quadrantSelector);
        filterRow.getCell(nameColumn).setComponent(nameColumnFilterField);

        final Map<Button, BuyColonizationDataConfirmationEdit> confirmationEditMap = new HashMap<>();

        final ComponentRenderer<Component, StarSystemColonizationDTO> detailRenderer = new ComponentRenderer<>(dto -> {
            // if user already has bought the system data
            if (userService.getKnownStarSystems(loggedInUser).contains(dto.getStarSystem())) {
                final PlanetSelectionGrid planetSelectionGrid = new PlanetSelectionGrid();
                planetSelectionGrid.setValue(new ColonizationTransportStarSystemDTO(dto.getStarSystem()));
                planetSelectionGrid.addValueChangeListener(event -> {
                    final ColonizationTransportStarSystemDTO value = event.getValue();
                    if (valueChangeListener != null && value != null) {
                        final Planet colonizationSelection = value.getColonizationSelection();
                        universe.setColonizationSelection(colonizationSelection);
                        valueChangeListener.valueChanged(new AbstractField.ComponentValueChangeEvent<>(this, this, getValue(), true));
                    }
                });
                return planetSelectionGrid;
            } else {
                // if not
                final BuyColonizationDataConfirmationEdit confirmationEdit = new BuyColonizationDataConfirmationEdit();
                confirmationEdit.setValue(dto.getStarSystem());
                confirmationEdit.addSubmitListener(event -> {
                    final Button source = event.getSource();
                    final BuyColonizationDataConfirmationEdit selectedEdit = confirmationEditMap.get(source);
                    if (selectedEdit != null) {
                        final StarSystem value = selectedEdit.getValue();
                        universe.setSelectedForBuyingDataStarSystem(value);
                        if (valueChangeListener != null && value != null) {
                            valueChangeListener.valueChanged(new AbstractField.ComponentValueChangeEvent<>(this, this, getValue(), true));
                        }
                    }
                });
                confirmationEditMap.put(confirmationEdit.getSubmitButton(), confirmationEdit);
                return confirmationEdit;
            }
        });
        grid.setItemDetailsRenderer(detailRenderer);

        add(control, grid);
    }

    @Override
    public void setValue(ColonizationTransportUniverseDTO value) {

        universe = value;
        final User loggedInUser = userService.getLoggedInUser();

        final Set<StarSystem> knownStarSystems = userService.getKnownStarSystems(loggedInUser);
        final Set<StarSystemColonizationDTO> collect = value.getStarSystems().stream()
                .map(starSystem -> new StarSystemColonizationDTO(starSystem, knownStarSystems.contains(starSystem)))
                .collect(Collectors.toSet());

        StarSystemColonizationDTO[] dto = new StarSystemColonizationDTO[collect.size() - 1];
        dto = collect.toArray(dto);
        grid.setItems(dto);
        grid.getDataProvider().refreshAll();
    }

    @Override
    public ColonizationTransportUniverseDTO getValue() {
        return universe;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ColonizationDashboardEdit, ColonizationTransportUniverseDTO>> listener) {
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
