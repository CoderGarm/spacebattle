package de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.enums.EResolution;
import de.yuga.spacebattle.gui.vaadin.misc.StatisticsDisplay;
import de.yuga.spacebattle.gui.vaadin.misc.details.CostsDisplayVertical;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ShipClassStatisticsDisplay extends StatisticsDisplay implements HasValue<AbstractField.ComponentValueChangeEvent<ShipClassStatisticsDisplay, ShipClass>, ShipClass> {

    @Nonnull
    private final CostsDisplayVertical costsDisplayVertical = new CostsDisplayVertical(EResolution.PX24);

    @Nonnull
    private final ModuleValueTypePerFleetVerticalDisplay shipClassStatistics = new ModuleValueTypePerFleetVerticalDisplay();

    public ShipClassStatisticsDisplay() {
        addSlide("Costs", costsDisplayVertical);
        addSlide("Stats", shipClassStatistics);
    }

    /**
     * Will update or clear the display, depending if the param exists.
     *
     * @param value the ship class to display
     */
    @Override
    public void setValue(@Nonnull final ShipClass value) {
        Preconditions.checkNotNull(value, "value shouldn't be null!");

        final Map<ShipClass, Integer> shipClassAmountHashMap = new HashMap<>();
        shipClassAmountHashMap.put(value, 1);
        shipClassStatistics.setValue(shipClassAmountHashMap);
        costsDisplayVertical.setValue(value.getCostsOverall());
    }

    @Nullable
    @Override
    public ShipClass getValue() {
        return null;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ShipClassStatisticsDisplay, ShipClass>> listener) {
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
