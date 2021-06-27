package de.yuga.spacebattle.gui.vaadin.combined.spacecrafts.details.fleetsplit;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.BeforeLeaveObserver;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.gui.vaadin.misc.details.SimpleValueChangeEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class FleetSplitEdit extends VerticalLayout implements BeforeLeaveObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(FleetSplitEdit.class);

    @Nonnull
    private final SplitFleetIntoThatEdit splitEdit = new SplitFleetIntoThatEdit();

    @Nonnull
    private final HorizontalLayout content = new HorizontalLayout();

    @Nullable
    private ValueChangeListener<SimpleValueChangeEvent> valueChangeListener;

    /**
     * Holds every registered listener for this and it's children component. But you have to register them manually.
     * Every {@link Registration} will be removed if it's part of this list and the component will be left.
     */
    private final List<Registration> registrationList = new ArrayList<>();

    public FleetSplitEdit() {
        final Label title = new Label("Split fleet");
        add(title, content);
    }

    public void setValue(@Nullable final Fleet value) {
        if (value == null) {
            return;
        }

        final FleetSplitDTO fleetDTO = new FleetSplitDTO(value);
        splitEdit.setValue(fleetDTO);

        content.removeAll();

        Registration valueChangeListener = splitEdit.addValueChangeListener(event -> {
            if (this.valueChangeListener != null) {
                this.valueChangeListener.valueChanged(new SimpleValueChangeEvent());
            }
        });
        registrationList.add(valueChangeListener);
        content.add(splitEdit);
    }

    @Nullable
    public FleetSplitDTO getValue() {
        return splitEdit.getValue();
    }

    public Registration addValueChangeListener(@Nonnull final ValueChangeListener<SimpleValueChangeEvent> listener) {
        Preconditions.checkNotNull(listener, "listener shouldn't be null!");

        valueChangeListener = listener;
        Registration valueChangeListenerR = splitEdit.addValueChangeListener(event -> {
            valueChangeListener.valueChanged(new SimpleValueChangeEvent());
        });
        registrationList.add(valueChangeListenerR);
        return new Registration() {
            @Override
            public void remove() {
                valueChangeListener = null;
                valueChangeListenerR.remove();
            }
        };
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        registrationList.forEach(Registration::remove);
    }

    /**
     * Checks if the user input creates a splitted fleet.
     *
     * @return <code>true</code> if the split is valid, <code>false</code> otherwise
     */
    public boolean isValid() {
        final FleetSplitDTO value = getValue();
        if (value == null) {
            return false;
        }

        if (StringUtils.isBlank(value.getName())) {
            return false;
        }
        final ShipClassCountSplitDTO dto = value.getShips().stream().filter(s -> s.getSplitCount() > 0).findAny().orElse(null);
        if (dto == null) {
            return false;
        }

        return true;
    }
}
