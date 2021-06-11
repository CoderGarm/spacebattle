package de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.ModuleDataElementDisplay;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class ModuleValueTypePerFleetVerticalDisplay extends VerticalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<ModuleValueTypePerFleetVerticalDisplay, Map<ShipClass, Integer>>, Map<ShipClass, Integer>> {

    @Nonnull
    private final ModuleValueTypePerFleetDatasource datasource;

    public ModuleValueTypePerFleetVerticalDisplay() {
        datasource = new ModuleValueTypePerFleetDatasource();

        for (int i = 0; i < EModuleType.values().length; i++) {
            final ModuleDataElementDisplay moduleDataElementDisplay = datasource.displayByModuleType.get(EModuleType.values()[i]);
            moduleDataElementDisplay.addClassName("statistics-tight");
            add(moduleDataElementDisplay);
        }
    }

    @Override
    public void setValue(@Nullable final Map<ShipClass, Integer> value) {
        datasource.setValue(value);
    }

    @Nullable
    @Override
    public Map<ShipClass, Integer> getValue() {
        return null;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ModuleValueTypePerFleetVerticalDisplay, Map<ShipClass, Integer>>> listener) {
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
