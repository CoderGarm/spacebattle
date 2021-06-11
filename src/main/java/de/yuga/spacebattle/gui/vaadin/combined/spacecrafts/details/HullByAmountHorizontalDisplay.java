package de.yuga.spacebattle.gui.vaadin.combined.spacecrafts.details;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class HullByAmountHorizontalDisplay extends HorizontalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<HullByAmountHorizontalDisplay, Map<ShipClass, Integer>>, Map<ShipClass, Integer>> {

    @Nonnull
    private final Map<Hull, Integer> amountByHullType = new HashMap<>();

    @Nonnull
    private final Map<Hull, HullAmountDisplay> displayByHullType = new HashMap<>();

    public HullByAmountHorizontalDisplay() {
        final Label title = new Label("Composition");
        add(title);
    }

    @Override
    public void setValue(@Nullable final Map<ShipClass, Integer> shipClasses) {
        if (shipClasses == null || shipClasses.isEmpty()) {
            clearView();
            return;
        }

        final Set<Hull> hullSet = shipClasses.keySet().stream().map(ShipClass::getHull).collect(Collectors.toSet());
        amountByHullType.keySet().removeIf(hull -> !hullSet.contains(hull));

        shipClasses.forEach((key, value) -> {
            Integer amount = amountByHullType.get(key.getHull());
            if (amount != null) {
                amount = +value;
            } else {
                amount = value;
            }
            amountByHullType.put(key.getHull(), amount);
        });
        updateDisplays();
    }

    private void clearView() {
        this.removeAll();
        amountByHullType.clear();
        displayByHullType.clear();
    }

    private void updateDisplays() {
        if (amountByHullType.isEmpty()) {
            clearView();
            return;
        }

        displayByHullType.forEach((hull, hullAmountDisplay) -> {
            if (!amountByHullType.containsKey(hull)) {
                remove(hullAmountDisplay);
            }
        });
        displayByHullType.keySet().removeIf(hull -> !amountByHullType.containsKey(hull));

        amountByHullType.forEach((hull, amount) -> {
            HullAmountDisplay hullAmountDisplay = displayByHullType.get(hull);
            if (hullAmountDisplay == null) {
                hullAmountDisplay = new HullAmountDisplay();
                add(hullAmountDisplay);
                displayByHullType.put(hull, hullAmountDisplay);
            }
            hullAmountDisplay.setValue(new HullAmountWrapper(hull, amount));
        });
    }

    @Nullable
    @Override
    public Map<ShipClass, Integer> getValue() {
        return null;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<HullByAmountHorizontalDisplay, Map<ShipClass, Integer>>> listener) {
        // not necessary
        return null;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        // not necessary
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        // not necessary
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return false;
    }
}
