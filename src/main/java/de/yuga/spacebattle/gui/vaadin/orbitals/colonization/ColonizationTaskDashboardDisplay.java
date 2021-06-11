package de.yuga.spacebattle.gui.vaadin.orbitals.colonization;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.turn.Colonization;
import de.yuga.spacebattle.backend.services.turn.ColonizationService;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
import de.yuga.spacebattle.gui.vaadin.orbitals.details.OrbitCoordinatesHorizontalDisplay;
import de.yuga.spacebattle.gui.vaadin.views.StarMapMainView;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.spring.events.Event;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/**
 * This view offers the opportunity to see all running colonizations.
 */
public class ColonizationTaskDashboardDisplay extends ColonizationLayout implements HasValue<AbstractField.ComponentValueChangeEvent<ColonizationTaskDashboardDisplay, ColonizationForUserDTO>, ColonizationForUserDTO> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ColonizationTaskDashboardDisplay.class);

    @Nonnull
    private final EventBus.UIEventBus uiEventBus = ViewHelper.getService(EventBus.UIEventBus.class);

    @Nonnull
    private final ColonizationService colonizationService = ViewHelper.getService(ColonizationService.class);

    @Nonnull
    private final Grid<Colonization> grid = new Grid<>();

    @Nullable
    private ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ColonizationTaskDashboardDisplay, ColonizationForUserDTO>> valueChangeListener;

    @Nullable
    private ColonizationForUserDTO colonizationDTO;

    public ColonizationTaskDashboardDisplay() {
        uiEventBus.subscribe(this);

        grid.setColumnReorderingAllowed(true);

        final TextField nameColumnFilterField = new TextField();

        final Button resetAllSorting = new Button("Reset filter", event -> {
            grid.sort(null);
            nameColumnFilterField.setValue("");
            grid.getDataProvider().refreshAll();
        });

        nameColumnFilterField.addValueChangeListener(event -> {
            //noinspection unchecked
            final ListDataProvider<Colonization> dataProvider =
                    (ListDataProvider<Colonization>) grid.getDataProvider();
            dataProvider.addFilter(dto -> {
                final String filter = event.getHasValue().getValue();
                if (StringUtils.isBlank(filter)) {
                    return true;
                }
                return dto.getTarget().getName().toLowerCase().contains(filter.toLowerCase().trim());
            });
            dataProvider.refreshAll();
        });

        nameColumnFilterField.setValueChangeMode(ValueChangeMode.EAGER);
        nameColumnFilterField.setSizeFull();
        nameColumnFilterField.setPlaceholder("Filter");
        nameColumnFilterField.getElement().setAttribute("focus-target", "");

        grid.addClassName("header-grid");
        final Grid.Column<Colonization> nameColumn =
                grid.addColumn(colo -> colo.getTarget().getName(), "Planet name")
                        .setHeader("Planet name");

        grid.addColumn(Colonization::getDoneAtZero, "Ticks left")
                .setHeader("Ticks left")
                .setWidth("100px")
                .setFlexGrow(0);

        final Grid.Column<Colonization> orbitColumn = grid.addComponentColumn(dto -> {
            final OrbitCoordinatesHorizontalDisplay orbitDisplay = new OrbitCoordinatesHorizontalDisplay();
            orbitDisplay.setValue(dto.getTarget().getOrbit());
            return orbitDisplay;
        });
        orbitColumn.setHeader("Orbit");

        final HeaderRow filterRow = grid.appendHeaderRow();
        filterRow.getCell(nameColumn).setComponent(nameColumnFilterField);

        grid.addItemDoubleClickListener(event -> {
            colonizationService.setColonizationToDisplay(event.getItem());
            grid.getUI().ifPresent(ui -> ui.navigate(StarMapMainView.ROUTE));
        });

        add(resetAllSorting, grid);
    }

    @Override
    public void setValue(ColonizationForUserDTO value) {
        colonizationDTO = value;
        if (value != null && !value.getColonizations().isEmpty()) {
            final Set<Colonization> colonizations = value.getColonizations();
            Colonization[] dto = new Colonization[colonizations.size() - 1];
            dto = colonizations.toArray(dto);
            grid.setItems(dto);
        } else {
            grid.setItems(new HashSet<>());
        }
        grid.getDataProvider().refreshAll();
    }

    @Override
    public ColonizationForUserDTO getValue() {
        return colonizationDTO;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ColonizationTaskDashboardDisplay, ColonizationForUserDTO>> listener) {
        valueChangeListener = listener;
        return (Registration) () -> valueChangeListener = null;
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
