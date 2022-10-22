package de.yuga.spacebattle.rest.dto.combined.spacecrafts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.details.AlignedFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.details.SupportFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.*;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModuleWithEffectValue;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.backend.enums.ESupportType;
import de.yuga.spacebattle.backend.enums.EWeaponType;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Schema(description = ".")
public class SpacecraftCapabilities {

    @Nonnull
    @Schema(required = true, description = "The effect values per module type.")
    private final List<CapabilityValue> capabilities = new ArrayList<>();

    @JsonIgnore
    private Map<EModuleType, BigDecimal> effectValueByModuleType;

    public SpacecraftCapabilities() {
    }

    public SpacecraftCapabilities(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        effectValueByModuleType = Arrays.stream(EModuleType.values())
                .collect(Collectors.toMap(Function.identity(), value -> BigDecimal.ZERO));
        setValue(fleet.getShipsByClass());
        final List<CapabilityValue> capabilityValues = effectValueByModuleType.entrySet().stream()
                .map(CapabilityValue::new)
                .collect(Collectors.toList());
        capabilities.addAll(capabilityValues);
    }

    public SpacecraftCapabilities(@Nonnull final ShipClass shipClass) {
        Preconditions.checkNotNull(shipClass, "shipClass must not be empty");

        effectValueByModuleType = Arrays.stream(EModuleType.values())
                .collect(Collectors.toMap(Function.identity(), value -> BigDecimal.ZERO));
        final Map<ShipClass, Integer> shipClasses = new HashMap<>();
        shipClasses.put(shipClass, 1);
        setValue(shipClasses);
        final List<CapabilityValue> capabilityValues = effectValueByModuleType.entrySet().stream()
                .map(CapabilityValue::new)
                .collect(Collectors.toList());
        capabilities.addAll(capabilityValues);
    }

    public SpacecraftCapabilities(@Nonnull final de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthState warshipHealthState) {
        Preconditions.checkNotNull(warshipHealthState, "warshipHealthState must not be empty");

        effectValueByModuleType = Arrays.stream(EModuleType.values())
                .collect(Collectors.toMap(Function.identity(), value -> BigDecimal.ZERO));

        final List<CapabilityValue> capabilityValues = effectValueByModuleType.entrySet().stream()
                .map(CapabilityValue::new)
                .collect(Collectors.toList());
        capabilities.addAll(capabilityValues);
    }

    @Nonnull
    @JsonIgnore
    public Map<EModuleType, BigDecimal> getEffectValueByModuleType() {
        return effectValueByModuleType;
    }

    @Nonnull
    public List<CapabilityValue> getCapabilities() {
        return capabilities;
    }

    @JsonIgnore
    private void setValue(@Nonnull final de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthState warshipHealthState) {
        Preconditions.checkNotNull(warshipHealthState, "warshipHealthState shouldn't be null!");

        final ShipClass shipClass = warshipHealthState.getWarShip().getShipClass();
        final Map<ESupportType, List<SupportFitting>> supportTypeToModule = shipClass.getSupportFittings().stream()
                .collect(Collectors.groupingBy(c -> c.getPassiveModule().getSupportType(),
                        Collectors.mapping(Function.identity(), Collectors.toList())));

        final Set<AlignedFitting> fittings = warshipHealthState.getActiveFittings();
        fittings.forEach(fitting -> {
            final EModuleType moduleType = EModuleType.WEAPON;
            final List<SupportFitting> supportFittings = supportTypeToModule.computeIfAbsent(ESupportType.getByValue(moduleType), k -> new ArrayList<>());
            final EWeaponType weaponType = fitting.getWeaponType();
            if ((EWeaponType.BEAM == weaponType || EWeaponType.POINT_DEFENSE == weaponType) && fitting.getWeapon() != null) {
                calculateValueBySupportFitting(fitting.getWeapon(), fitting.getAmount(), supportFittings, moduleType);
            }
            if ((EWeaponType.MISSILE == weaponType || EWeaponType.COUNTER_MISSILE == weaponType) && fitting.getLauncher() != null) {
                calculateValueBySupportFitting(fitting.getLauncher(), fitting.getAmount(), supportFittings, moduleType);
            }
        });

        final int armorState = warshipHealthState.getArmorState();
        addValueByType(armorState, 1, EModuleType.ARMOR);
        final int elokaState = warshipHealthState.getElokaState();
        addValueByType(elokaState, 1, EModuleType.ELECTRONIC_WARFARE);
        final int sidewallState = warshipHealthState.getSidewallState();
        addValueByType(sidewallState, 1, EModuleType.SHIELD);
        final int propulsionState = warshipHealthState.getPropulsionState();
        addValueByType(propulsionState, 1, EModuleType.PROPULSION);
    }

    @JsonIgnore
    private void setValue(@Nonnull final Map<ShipClass, Integer> shipClasses) {
        Preconditions.checkNotNull(shipClasses, "shipClasses shouldn't be null!");

        shipClasses.keySet().forEach(shipClass -> {
            final Map<ESupportType, List<SupportFitting>> supportTypeToModule = shipClass.getSupportFittings().stream()
                    .collect(Collectors.groupingBy(c -> c.getPassiveModule().getSupportType(),
                            Collectors.mapping(Function.identity(), Collectors.toList())));

            Integer amount = shipClasses.get(shipClass);
            while (amount > 0) {
                final Armor armor = shipClass.getArmor();
                if (armor != null) {
                    final EModuleType moduleType = EModuleType.ARMOR;
                    final List<SupportFitting> supportFittings = supportTypeToModule.computeIfAbsent(ESupportType.getByValue(moduleType), k -> new ArrayList<>());
                    calculateValueBySupportFitting(armor, 1, supportFittings, moduleType);
                }

                final Propulsion propulsion = shipClass.getPropulsion();
                if (propulsion != null) {
                    final EModuleType moduleType = EModuleType.PROPULSION;
                    final List<SupportFitting> supportFittings = supportTypeToModule.computeIfAbsent(ESupportType.getByValue(moduleType), k -> new ArrayList<>());
                    calculateValueBySupportFitting(propulsion, 1, supportFittings, moduleType);
                }
                if (propulsion != null && propulsion.isFtlCapable()) {
                    final EModuleType moduleType = EModuleType.FTLPROPULSION;
                    final List<SupportFitting> supportFittings = supportTypeToModule.computeIfAbsent(ESupportType.getByValue(moduleType), k -> new ArrayList<>());
                    calculateValueBySupportFitting(propulsion, 1, supportFittings, moduleType);
                }

                final Sidewall sidewall = shipClass.getSidewall();
                if (sidewall != null) {
                    final EModuleType moduleType = EModuleType.SHIELD;
                    final List<SupportFitting> supportFittings = supportTypeToModule.computeIfAbsent(ESupportType.getByValue(moduleType), k -> new ArrayList<>());
                    calculateValueBySupportFitting(sidewall, 1, supportFittings, moduleType);
                }

                final ElectronicWarfare electronicWarfare = shipClass.getElectronicWarfare();
                if (electronicWarfare != null) {
                    final EModuleType moduleType = EModuleType.ELECTRONIC_WARFARE;
                    final List<SupportFitting> supportFittings = supportTypeToModule.computeIfAbsent(ESupportType.getByValue(moduleType), k -> new ArrayList<>());
                    calculateValueBySupportFitting(electronicWarfare, 1, supportFittings, moduleType);
                }

                final Set<AlignedFitting> fittings = shipClass.getFittings();
                fittings.forEach(fitting -> {
                    final EModuleType moduleType = EModuleType.WEAPON;
                    final List<SupportFitting> supportFittings = supportTypeToModule.computeIfAbsent(ESupportType.getByValue(moduleType), k -> new ArrayList<>());
                    final EWeaponType weaponType = fitting.getWeaponType();
                    if ((EWeaponType.BEAM == weaponType || EWeaponType.POINT_DEFENSE == weaponType) && fitting.getWeapon() != null) {
                        calculateValueBySupportFitting(fitting.getWeapon(), fitting.getAmount(), supportFittings, moduleType);
                    }
                    if ((EWeaponType.MISSILE == weaponType || EWeaponType.COUNTER_MISSILE == weaponType) && fitting.getLauncher() != null) {
                        calculateValueBySupportFitting(fitting.getLauncher(), fitting.getAmount(), supportFittings, moduleType);
                    }
                });
                amount--;
            }
        });
    }

    /**
     * Calculates and adds the value by type.
     *
     * @param launcher        the module to add
     * @param supportFittings the support fitting if present
     * @param moduleType      the module type
     * @param amount          the amount of modules
     */
    @JsonIgnore
    private void calculateValueBySupportFitting(@Nonnull final Launcher launcher,
                                                final int amount,
                                                @Nonnull final List<SupportFitting> supportFittings,
                                                @Nonnull final EModuleType moduleType) {
        Preconditions.checkNotNull(launcher, "launcher shouldn't be null!");
        Preconditions.checkNotNull(supportFittings, "supportFittings shouldn't be null!");
        Preconditions.checkNotNull(moduleType, "moduleType shouldn't be null!");

        final Missile missile = launcher.getAmmunitionModule().getMissile();
        final Double bonus = supportFittings.stream().map(SupportFitting::getEffectValue).reduce(0D, Double::sum);
        final double absoluteValueAsFactor = bonus != 0 ? 1 + (bonus / 100) : 1;
        final double effectValue = missile.getWarhead().getDamageValue() * absoluteValueAsFactor;
        addValueByType(effectValue, amount, moduleType);
    }

    /**
     * Calculates and adds the value by type.
     *
     * @param baseModuleWithEffectValue the module to add
     * @param supportFittings           the support fitting if present
     * @param moduleType                the module type
     * @param amount                    the amount of modules
     */
    @JsonIgnore
    private void calculateValueBySupportFitting(@Nonnull final BaseModuleWithEffectValue baseModuleWithEffectValue,
                                                final int amount,
                                                @Nonnull final List<SupportFitting> supportFittings,
                                                @Nonnull final EModuleType moduleType) {
        Preconditions.checkNotNull(baseModuleWithEffectValue, "baseModule shouldn't be null!");
        Preconditions.checkNotNull(supportFittings, "supportFittings shouldn't be null!");
        Preconditions.checkNotNull(moduleType, "moduleType shouldn't be null!");

        final Double bonus = supportFittings.stream().map(SupportFitting::getEffectValue).reduce(0D, Double::sum);
        final double absoluteValueAsFactor = bonus != 0 ? 1 + (bonus / 100) : 1;
        final double effectValue = baseModuleWithEffectValue.getEffectValue() * absoluteValueAsFactor;
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
    @JsonIgnore
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
}
