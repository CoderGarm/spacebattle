package de.yuga.spacebattle.gui.vaadin.combined.spacecrafts.details;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class FleetMergeEdit extends HorizontalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<FleetMergeEdit, Set<Fleet>>, Set<Fleet>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FleetMergeEdit.class);

    private final Set<Fleet> availableFleets = new HashSet<>();

    private final MultiSelectListBox<String> multiSelect = new MultiSelectListBox<>();

    public FleetMergeEdit() {
        final Label title = new Label("Merge with fleets");
        add(title, multiSelect);
    }

    @Override
    public void setValue(Set<Fleet> value) {
        if (value == null) {
            value = new HashSet<>();
        }
        availableFleets.clear();
        availableFleets.addAll(value);
        final Set<String> fleetNamesToAdd = value.stream().map(Fleet::getName).collect(Collectors.toSet());

        multiSelect.setItems(fleetNamesToAdd);
    }

    @Override
    public Set<Fleet> getValue() {
        final Set<String> names = multiSelect.getSelectedItems();
        return availableFleets.stream().filter(fleet -> names.contains(fleet.getName())).collect(Collectors.toSet());
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<FleetMergeEdit, Set<Fleet>>> listener) {
        return null;
    }

    public Registration addChangeListener(@Nonnull final ValueChangeListener listener) {
        Preconditions.checkNotNull(listener, "listener shouldn't be null!");

        return multiSelect.addValueChangeListener(listener);
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
