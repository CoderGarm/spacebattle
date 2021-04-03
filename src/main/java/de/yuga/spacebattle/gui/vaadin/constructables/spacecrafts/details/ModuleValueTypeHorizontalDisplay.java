package de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.Module;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.backend.enums.ERaceType;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.ModuleDataElementDisplay;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.details.EModuleValueDTO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ModuleValueTypeHorizontalDisplay extends HorizontalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<ModuleValueTypeHorizontalDisplay, ShipClass>, ShipClass> {

    @Nonnull
    private final Map<EModuleType, BigDecimal> amountByModuleType;

    @Nonnull
    private final Map<EModuleType, ModuleDataElementDisplay> displayByModuleType;

    public ModuleValueTypeHorizontalDisplay() {
        amountByModuleType = Arrays.stream(EModuleType.values())
                .collect(Collectors.toMap(Function.identity(), value -> BigDecimal.ZERO));

        displayByModuleType = Arrays.stream(EModuleType.values())
                .collect(Collectors.toMap(Function.identity(), eModuleType -> {
                    ModuleDataElementDisplay display = new ModuleDataElementDisplay();
                    display.update(new EModuleValueDTO(eModuleType, BigDecimal.ZERO));
                    return display;
                }));

        for (int i = 0; i < EModuleType.values().length; i++) {
            ModuleDataElementDisplay moduleDataElementDisplay = displayByModuleType.get(EModuleType.values()[i]);
            add(moduleDataElementDisplay);
        }
    }

    /**
     * Clears the full display in order to "show nothing of worth".
     */
    private void clearValues() {
        Arrays.stream(EModuleType.values()).forEach(eModuleType -> amountByModuleType.put(eModuleType, BigDecimal.ZERO));
        this.updateStats();
    }

    /**
     * Will update or clear the display, depending if the param exists.
     *
     * @param shipClass the ship class to display
     */
    public void update(@Nullable final ShipClass shipClass) {
        clearValues();

        if (shipClass != null) {
            final Map<Module, Integer> modules = shipClass.getModules();
            modules.keySet().forEach(module -> {
                final Integer amountOfModule = modules.get(module);
                addValueByType(shipClass.getOwner().getRaceType(), module, amountOfModule);
            });
        } else {
            Arrays.stream(EModuleType.values()).forEach(eModuleType -> amountByModuleType.put(eModuleType, BigDecimal.ZERO));
        }
        this.updateStats();
    }

    /**
     * Adds the effective value by {@link EModuleType} to the stats display.
     *
     * @param raceType       the race type to calculate the effective value
     * @param module         the module which effect value is used
     * @param amountOfModule how often this module should be counted
     */
    private void addValueByType(@Nullable final ERaceType raceType, @Nullable final Module module, final int amountOfModule) {
        if (raceType == null || module == null || amountOfModule == 0) {
            return;
        }
        final EModuleType moduleType = module.getModuleType();
        final int effectiveEffectValue = module.getEffectiveEffectValue(raceType);
        final BigDecimal currentEffectValue = amountByModuleType.get(moduleType);
        amountByModuleType.put(moduleType, currentEffectValue.add(new BigDecimal(effectiveEffectValue)).multiply(new BigDecimal(amountOfModule)));
    }

    /**
     * Updates the stats display by stored values.
     */
    private void updateStats() {
        displayByModuleType.forEach((eModuleType, moduleDataElementDisplay) -> {
            final BigDecimal effectiveValue = amountByModuleType.get(eModuleType);
            moduleDataElementDisplay.update(new EModuleValueDTO(eModuleType, effectiveValue));
        });
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
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ModuleValueTypeHorizontalDisplay, ShipClass>> listener) {
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
