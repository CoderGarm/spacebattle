package de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.ModuleDataElementDisplay;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ModuleValueTypePerShipDisplayCompact extends Div implements HasValue<AbstractField.ComponentValueChangeEvent<ModuleValueTypePerShipDisplayCompact, ShipClass>, ShipClass> {

    @Nonnull
    private final ModuleValueTypePerFleetDatasource datasource;

    public ModuleValueTypePerShipDisplayCompact() {
        datasource = new ModuleValueTypePerFleetDatasource();
        addClassName("compact-block");

        for (int i = 0; i < EModuleType.values().length; i++) {
            ModuleDataElementDisplay moduleDataElementDisplay = datasource.displayByModuleType.get(EModuleType.values()[i]);
            addStyleAndAdd(moduleDataElementDisplay);
        }
    }

    private void addStyleAndAdd(@Nonnull final Component component) {
        Preconditions.checkNotNull(component, "component shouldn't be null!");

        if (component instanceof HasStyle) {
            ((HasStyle) component).addClassName("block-element");
        }
        add(component);
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
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ModuleValueTypePerShipDisplayCompact, ShipClass>> listener) {
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
