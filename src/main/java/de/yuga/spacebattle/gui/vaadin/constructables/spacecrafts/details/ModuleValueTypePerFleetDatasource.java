package de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details;

import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.details.AlignedFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Armor;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.ElectronicWarfare;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Propulsion;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Sidewall;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModule;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.ModuleDataElementDisplay;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.details.EModuleValueDTO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ModuleValueTypePerFleetDatasource {

    @Nonnull
    protected final Map<EModuleType, BigDecimal> effectValueByModuleType;

    @Nonnull
    protected final Map<EModuleType, ModuleDataElementDisplay> displayByModuleType;

    protected ModuleValueTypePerFleetDatasource() {
        effectValueByModuleType = Arrays.stream(EModuleType.values())
                .collect(Collectors.toMap(Function.identity(), value -> BigDecimal.ZERO));

        displayByModuleType = Arrays.stream(EModuleType.values())
                .collect(Collectors.toMap(Function.identity(), eModuleType -> {
                    ModuleDataElementDisplay display = new ModuleDataElementDisplay();
                    display.update(new EModuleValueDTO(eModuleType, BigDecimal.ZERO));
                    return display;
                }));
    }

    /**
     * Clears the full display in order to "show nothing of worth".
     */
    protected void clearValues() {
        Arrays.stream(EModuleType.values()).forEach(eModuleType -> effectValueByModuleType.put(eModuleType, BigDecimal.ZERO));
        this.updateStats();
    }

    /**
     * Will update or clear the display, depending if the param exists.
     *
     * @param shipClasses the ship classes to display
     */
    public void update(@Nullable final Map<ShipClass, Integer> shipClasses) {
        clearValues();

        if (shipClasses != null) {
            shipClasses.keySet().forEach(shipClass -> {
                Integer amount = shipClasses.get(shipClass);
                while (amount > 0) {
                    final Armor armor = shipClass.getArmor();
                    addValueByType(armor, 1, EModuleType.ARMOR);

                    final Propulsion propulsion = shipClass.getPropulsion();
                    addValueByType(propulsion, 1, EModuleType.PROPULSION);
                    if (propulsion != null && propulsion.isFtlCapable()) {
                        addValueByType(propulsion, 1, EModuleType.FTLPROPULSION);
                    }
                    final Sidewall sidewall = shipClass.getSidewall();
                    addValueByType(sidewall, 1, EModuleType.SHIELD);

                    final ElectronicWarfare electronicWarfare = shipClass.getElectronicWarfare();
                    addValueByType(electronicWarfare, 1, EModuleType.ELECTRONIC_WARFARE);
                    // todo define module types or icons for missiles, counter missiles, whatever
                    final Set<AlignedFitting> fittings = shipClass.getFittings();
                    fittings.forEach(fitting -> {
                        addValueByType(fitting.getWeapon(), fitting.getAmount(), EModuleType.WEAPON);
                    });
                    amount--;
                }
            });
        } else {
            Arrays.stream(EModuleType.values()).forEach(eModuleType -> effectValueByModuleType.put(eModuleType, BigDecimal.ZERO));
        }
        this.updateStats();
    }

    /**
     * Adds the effective value by {@link EModuleType} to the stats display.
     * Remember: Speeds will not added.
     *
     * @param module         the module which effect value is used
     * @param amountOfModule how often this module should be counted
     * @param moduleType     the type which the module is for
     */
    private void addValueByType(@Nullable final BaseModule module, final int amountOfModule, @Nullable final EModuleType moduleType) {
        if (module == null || amountOfModule == 0 || moduleType == null) {
            return;
        }

        final int effectiveEffectValue = module.getEffectValue();
        final BigDecimal effectiveEffectValueAsBigD = new BigDecimal(effectiveEffectValue);

        final BigDecimal currentEffectValue = effectValueByModuleType.get(moduleType);

        BigDecimal effectiveResultingValue = BigDecimal.ZERO;
        // it's only possible to reduce speed while the slowest ship defined this parameter
        if (EModuleType.FTLPROPULSION == moduleType || EModuleType.PROPULSION == moduleType) {
            final BigDecimal effectiveEffectValueByModuleAmount = effectiveEffectValueAsBigD.multiply(new BigDecimal(amountOfModule));
            if (currentEffectValue.equals(BigDecimal.ZERO) || effectiveEffectValueByModuleAmount.compareTo(currentEffectValue) < 0) {
                effectiveResultingValue = effectiveEffectValueByModuleAmount;
            }
        } else {
            effectiveResultingValue = currentEffectValue.add(effectiveEffectValueAsBigD.multiply(new BigDecimal(amountOfModule)));
        }
        effectValueByModuleType.put(moduleType, effectiveResultingValue);
    }

    /**
     * Updates the stats display by stored values.
     */
    private void updateStats() {
        displayByModuleType.forEach((eModuleType, moduleDataElementDisplay) -> {
            final BigDecimal effectiveValue = effectValueByModuleType.get(eModuleType);
            moduleDataElementDisplay.update(new EModuleValueDTO(eModuleType, effectiveValue));
        });
    }
}
