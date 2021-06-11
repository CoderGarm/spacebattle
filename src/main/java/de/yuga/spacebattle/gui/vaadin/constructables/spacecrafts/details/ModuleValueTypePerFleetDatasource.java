package de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.details.AlignedFitting;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.details.SupportFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Armor;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.ElectronicWarfare;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Propulsion;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Sidewall;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModule;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.backend.enums.ESupportType;
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
                final Set<SupportFitting> supportFittings = shipClass.getSupportFittings();
                final Map<ESupportType, SupportFitting> supportTypeToModule = supportFittings.stream()
                        .collect(Collectors.toMap(e -> e.getPassiveModule().getSupportType(), Function.identity()));

                Integer amount = shipClasses.get(shipClass);
                while (amount > 0) {
                    final Armor armor = shipClass.getArmor();
                    if (armor != null) {
                        final EModuleType moduleType = EModuleType.ARMOR;
                        final SupportFitting supportFitting = supportTypeToModule.get(ESupportType.getByValue(moduleType));
                        calculateValueBySupportFitting(armor, 1, supportFitting, moduleType);
                    }

                    final Propulsion propulsion = shipClass.getPropulsion();
                    if (propulsion != null) {
                        final EModuleType moduleType = EModuleType.PROPULSION;
                        final SupportFitting supportFitting = supportTypeToModule.get(ESupportType.getByValue(moduleType));
                        calculateValueBySupportFitting(propulsion, 1, supportFitting, moduleType);
                    }
                    if (propulsion != null && propulsion.isFtlCapable()) {
                        final EModuleType moduleType = EModuleType.FTLPROPULSION;
                        final SupportFitting supportFitting = supportTypeToModule.get(ESupportType.getByValue(moduleType));
                        calculateValueBySupportFitting(propulsion, 1, supportFitting, moduleType);
                    }

                    final Sidewall sidewall = shipClass.getSidewall();
                    if (sidewall != null) {
                        final EModuleType moduleType = EModuleType.SHIELD;
                        final SupportFitting supportFitting = supportTypeToModule.get(ESupportType.getByValue(moduleType));
                        calculateValueBySupportFitting(sidewall, 1, supportFitting, moduleType);
                    }

                    final ElectronicWarfare electronicWarfare = shipClass.getElectronicWarfare();
                    if (electronicWarfare != null) {
                        final EModuleType moduleType = EModuleType.ELECTRONIC_WARFARE;
                        final SupportFitting supportFitting = supportTypeToModule.get(ESupportType.getByValue(moduleType));
                        calculateValueBySupportFitting(electronicWarfare, 1, supportFitting, moduleType);
                    }

                    final Set<AlignedFitting> fittings = shipClass.getFittings();
                    fittings.forEach(fitting -> {
                        final EModuleType moduleType = EModuleType.WEAPON;
                        final SupportFitting supportFitting = supportTypeToModule.get(ESupportType.getByValue(moduleType));
                        calculateValueBySupportFitting(fitting.getWeapon(), fitting.getAmount(), supportFitting, moduleType);
                    });
                    // todo add ammunition salvo amount
                    amount--;
                }
            });
        } else {
            Arrays.stream(EModuleType.values()).forEach(eModuleType -> effectValueByModuleType.put(eModuleType, BigDecimal.ZERO));
        }
        this.updateStats();
    }

    /**
     * Calculates and adds the value by type.
     *
     * @param baseModule     the module to add
     * @param supportFitting the support fitting if present
     * @param moduleType     the module type
     * @param amount         the amount of modules
     */
    private void calculateValueBySupportFitting(@Nonnull final BaseModule baseModule,
                                                final int amount,
                                                @Nullable final SupportFitting supportFitting,
                                                @Nonnull final EModuleType moduleType) {
        Preconditions.checkNotNull(baseModule, "baseModule shouldn't be null!");
        Preconditions.checkNotNull(moduleType, "moduleType shouldn't be null!");

        final double absoluteValueAsFactor = supportFitting != null ? supportFitting.getAbsoluteValueAsFactor() : 1;
        final double effectValue = baseModule.getEffectValue() * absoluteValueAsFactor;
        addValueByType(effectValue, amount, moduleType);
    }

    /**
     * Adds the effective value by {@link EModuleType} to the stats display.
     * Remember: Speeds will not added.
     *
     * @param effectValue    the effect value
     * @param amountOfModule how often this module should be counted
     * @param moduleType     the type which the module is for
     */
    private void addValueByType(final double effectValue, final int amountOfModule, @Nullable final EModuleType moduleType) {
        if (amountOfModule == 0 || moduleType == null) {
            return;
        }

        final BigDecimal effectiveEffectValueAsBigD = new BigDecimal(effectValue);
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
