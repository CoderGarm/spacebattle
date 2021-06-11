package de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.ModuleDataElementDisplay;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ModuleValueTypePerShipHorizontalDisplay extends HorizontalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<ModuleValueTypePerShipHorizontalDisplay, ShipClass>, ShipClass> {

    @Nonnull
    private final ModuleValueTypePerFleetDatasource datasource;

    public ModuleValueTypePerShipHorizontalDisplay() {
        datasource = new ModuleValueTypePerFleetDatasource();

        for (int i = 0; i < EModuleType.values().length; i++) {
            ModuleDataElementDisplay moduleDataElementDisplay = datasource.displayByModuleType.get(EModuleType.values()[i]);
            add(moduleDataElementDisplay);
        }
    }

    /**
     * Will update or clear the display, depending if the param exists.
     *
     * @param value the ship class to display
     */
    @Override
    public void setValue(@Nullable final ShipClass value) {
        Map<ShipClass, Integer> map = new HashMap<>();
        map.put(value, 1);
        datasource.setValue(map);
    }

    @Nullable
    @Override
    public ShipClass getValue() {
        return null;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ModuleValueTypePerShipHorizontalDisplay, ShipClass>> listener) {
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
