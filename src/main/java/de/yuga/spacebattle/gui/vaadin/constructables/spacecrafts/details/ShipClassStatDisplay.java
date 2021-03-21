package de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.Module;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.backend.enums.ERaceType;
import de.yuga.spacebattle.gui.vaadin.misc.details.CostsDisplay;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.ModuleDataElementDisplay;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.details.EModuleAmountWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ShipClassStatDisplay extends HorizontalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<ShipClassStatDisplay, ShipClass>, ShipClass> {

    @Nonnull
    private final CostsDisplay costsDisplay;

    @Nonnull
    private final Map<EModuleType, BigDecimal> amountByModuleType;

    @Nonnull
    private final Map<EModuleType, ModuleDataElementDisplay> displayByModuleType;

    public ShipClassStatDisplay() {
        costsDisplay = new CostsDisplay();

        amountByModuleType = Arrays.stream(EModuleType.values())
                .collect(Collectors.toMap(Function.identity(), value -> BigDecimal.ZERO));

        final Label title = new Label("Data");
        final VerticalLayout statLayout = new VerticalLayout();
        displayByModuleType = Arrays.stream(EModuleType.values())
                .collect(Collectors.toMap(Function.identity(), eModuleType -> {
                    ModuleDataElementDisplay display = new ModuleDataElementDisplay();
                    display.update(new EModuleAmountWrapper(eModuleType, BigDecimal.ZERO));
                    return display;
                }));

        Component[] statLabels = new Component[displayByModuleType.size()];
        statLabels = displayByModuleType.values().toArray(statLabels);
        statLayout.add(title);
        statLayout.add(statLabels);
        add(costsDisplay, statLayout);
    }

    /**
     * Clears the full display in order to "show nothing of worth".
     */
    private void clearValues() {
        this.updateStats();
        this.costsDisplay.clear();
        this.costsDisplay.update();
    }

    /**
     * Will update or clear the display, depending if the param exists.
     *
     * @param shipClass the ship class to display
     */
    public void update(@Nullable final ShipClass shipClass) {
        clearValues();

        if (shipClass != null) {
            this.costsDisplay.addCosts(shipClass.getCosts());
            if (shipClass.getHull() != null) {
                this.costsDisplay.addCosts(shipClass.getHull().getCosts());
            }

            final Map<Module, Integer> modules = shipClass.getModules();
            modules.keySet().forEach(module -> {
                final Integer amountOfModule = modules.get(module);

                addValueByType(shipClass.getOwner().getRaceType(), module, amountOfModule);

                for (int i = 1; i <= amountOfModule; i++) {
                    this.costsDisplay.addCosts(module.getCosts());
                }
            });
        } else {
            this.costsDisplay.clear();
        }
        this.costsDisplay.update();
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
            moduleDataElementDisplay.update(new EModuleAmountWrapper(eModuleType, effectiveValue));
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
