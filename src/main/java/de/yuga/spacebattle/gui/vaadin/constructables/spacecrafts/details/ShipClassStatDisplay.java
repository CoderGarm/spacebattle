package de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.gui.vaadin.misc.details.CostsDisplay;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ShipClassStatDisplay extends HorizontalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<ShipClassStatDisplay, ShipClass>, ShipClass> {

    @Nonnull
    private final CostsDisplay costsDisplay;

    @Nonnull
    private final ModuleValueTypeVerticalDisplay moduleValueTypeVerticalDisplay;

    public ShipClassStatDisplay() {
        costsDisplay = new CostsDisplay();
        moduleValueTypeVerticalDisplay = new ModuleValueTypeVerticalDisplay();

        add(costsDisplay, moduleValueTypeVerticalDisplay);
    }

    /**
     * Clears the full display in order to "show nothing of worth".
     */
    private void clearValues() {
        costsDisplay.clear();
        costsDisplay.update();
        moduleValueTypeVerticalDisplay.update(null);
    }

    /**
     * Will update or clear the display, depending if the param exists.
     *
     * @param shipClass the ship class to display
     */
    public void update(@Nullable final ShipClass shipClass) {
        clearValues();

        if (shipClass != null) {
            final Map<ShipClass, Integer> shipClassAmountHashMap = new HashMap<>();
            shipClassAmountHashMap.put(shipClass, 1);
            moduleValueTypeVerticalDisplay.update(shipClassAmountHashMap);
            costsDisplay.addCosts(shipClass.getCosts());
            if (shipClass.getHull() != null) {
                costsDisplay.addCosts(shipClass.getHull().getCosts());
            }
        } else {
            costsDisplay.clear();
        }
        costsDisplay.update();
    }

    @Override
    public void setValue(@Nullable final ShipClass value) {
        this.update(value);
    }

    @Nullable
    @Override
    public ShipClass getValue() {
        return null;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ShipClassStatDisplay, ShipClass>> listener) {
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
