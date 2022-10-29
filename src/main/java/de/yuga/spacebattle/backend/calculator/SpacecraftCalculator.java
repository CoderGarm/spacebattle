package de.yuga.spacebattle.backend.calculator;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.round.WarshipHealthState;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.FleetSnapshot;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.details.AlignedFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.details.SupportFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.*;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModuleWithEffectValue;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthStateAccessor;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthStateSnapshot;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.backend.enums.ESupportType;
import de.yuga.spacebattle.backend.enums.EWeaponType;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.CapabilityValue;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.SpacecraftCapabilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SpacecraftCalculator {

    private final Set<EModuleType> PROPULSIONS = Set.of(EModuleType.PROPULSION, EModuleType.FTLPROPULSION);

    private Map<EModuleType, BigDecimal> effectValueByModuleType;

    public SpacecraftCapabilities getSpaceCraftCapabilities(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        effectValueByModuleType = createBaseData();
        final Set<de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthState> warshipHealthStates = fleet.getAliveShips().stream()
                .map(WarShip::getWarshipHealthState)
                .collect(Collectors.toSet());
        for (final de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthState warshipHealthState : warshipHealthStates) {
            setValue(warshipHealthState);
        }
        return getSpacecraftCapabilities();
    }

    public SpacecraftCapabilities getSpaceCraftCapabilities(@Nonnull final ShipClass shipClass) {
        Preconditions.checkNotNull(shipClass, "shipClass must not be empty");

        effectValueByModuleType = createBaseData();
        final Map<ShipClass, Integer> shipClasses = new HashMap<>();
        shipClasses.put(shipClass, 1);
        setValue(shipClasses);
        return getSpacecraftCapabilities();
    }

    public SpacecraftCapabilities getSpaceCraftCapabilities(@Nonnull final Map<ShipClass, Integer> shipClasses) {
        Preconditions.checkNotNull(shipClasses, "shipClasses must not be empty");

        effectValueByModuleType = createBaseData();
        setValue(shipClasses);
        return getSpacecraftCapabilities();
    }

    public SpacecraftCapabilities getSpaceCraftCapabilities(@Nonnull final WarshipHealthStateAccessor warshipHealthState) {
        Preconditions.checkNotNull(warshipHealthState, "warshipHealthState must not be empty");

        effectValueByModuleType = createBaseData();
        setValue(warshipHealthState);
        return getSpacecraftCapabilities();
    }

    public SpacecraftCapabilities getSpaceCraftCapabilities(@Nonnull final Collection<de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthStateAccessor> warshipHealthStates) {
        Preconditions.checkNotNull(warshipHealthStates, "warshipHealthStates must not be empty");

        effectValueByModuleType = createBaseData();
        warshipHealthStates.forEach(this::setValue);
        return getSpacecraftCapabilities();
    }

    public SpacecraftCapabilities getSpaceCraftCapabilities(@Nonnull final FleetSnapshot fleetSnapshot) {
        Preconditions.checkNotNull(fleetSnapshot, "fleetSnapshot must not be empty");

        effectValueByModuleType = createBaseData();
        final Set<WarshipHealthStateSnapshot> ships = fleetSnapshot.getShips();
        ships.forEach(this::setValue);
        return getSpacecraftCapabilities();
    }

    public Set<de.yuga.spacebattle.backend.entities.turn.battle.combat.CapabilityValue> getCapabilityValues(@Nonnull final ShipClass shipClass) {
        Preconditions.checkNotNull(shipClass, "shipClass must not be empty");

        effectValueByModuleType = createBaseData();
        final Map<ShipClass, Integer> shipClasses = new HashMap<>();
        shipClasses.put(shipClass, 1);
        setValue(shipClasses);
        return effectValueByModuleType.entrySet().stream()
                .map(de.yuga.spacebattle.backend.entities.turn.battle.combat.CapabilityValue::new)
                .collect(Collectors.toSet());
    }

    public Set<de.yuga.spacebattle.backend.entities.turn.battle.combat.CapabilityValue> getCapabilityValues(@Nonnull final WarshipHealthState warshipHealthState) {
        Preconditions.checkNotNull(warshipHealthState, "warshipHealthState must not be empty");

        effectValueByModuleType = createBaseData();
        setValue(warshipHealthState);
        return effectValueByModuleType.entrySet().stream()
                .map(de.yuga.spacebattle.backend.entities.turn.battle.combat.CapabilityValue::new)
                .collect(Collectors.toSet());
    }

    public Set<de.yuga.spacebattle.backend.entities.turn.battle.combat.CapabilityValue> getCapabilityValues(@Nonnull final de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthState healthState) {
        Preconditions.checkNotNull(healthState, "healthState must not be empty");

        effectValueByModuleType = createBaseData();
        setValue(healthState);
        return effectValueByModuleType.entrySet().stream()
                .map(de.yuga.spacebattle.backend.entities.turn.battle.combat.CapabilityValue::new)
                .collect(Collectors.toSet());
    }

    private void setValue(@Nonnull final WarshipHealthState warshipHealthState) {
        Preconditions.checkNotNull(warshipHealthState, "warshipHealthState shouldn't be null!");

        final ShipClass shipClass = warshipHealthState.getWarShip().getShipClass();
        final Map<ESupportType, List<SupportFitting>> supportTypeToModule = shipClass.getSupportFittings().stream()
                .collect(Collectors.groupingBy(c -> c.getPassiveModule().getSupportType(),
                        Collectors.mapping(Function.identity(), Collectors.toList())));

        final List<AlignedFitting> fittings = warshipHealthState.getActiveFittings();
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

        addValueByType(warshipHealthState.getArmorState(), 1, EModuleType.ARMOR);
        addValueByType(warshipHealthState.getElokaState(), 1, EModuleType.ELECTRONIC_WARFARE);
        addValueByType(warshipHealthState.getSidewallState(), 1, EModuleType.SHIELD);

        setValueByPropulsion(warshipHealthState, EModuleType.PROPULSION);
        if (warshipHealthState.getWarShip().getShipClass().isFTLCapable()) {
            setValueByPropulsion(warshipHealthState, EModuleType.FTLPROPULSION);
        }
    }

    private SpacecraftCapabilities getSpacecraftCapabilities() {
        final List<CapabilityValue> capabilityValues = effectValueByModuleType.entrySet().stream()
                .map(CapabilityValue::new)
                .collect(Collectors.toList());
        return new SpacecraftCapabilities().withValues(capabilityValues);
    }

    private Map<EModuleType, BigDecimal> createBaseData() {
        return Arrays.stream(EModuleType.values())
                .filter(type -> !PROPULSIONS.contains(type))
                .collect(Collectors.toMap(Function.identity(), value -> BigDecimal.ZERO));
    }

    private void setValue(@Nonnull final WarshipHealthStateAccessor warshipHealthState) {
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

        addValueByType(warshipHealthState.getStateByAsDouble(EModuleType.ARMOR), 1, EModuleType.ARMOR);
        addValueByType(warshipHealthState.getStateByAsDouble(EModuleType.ELECTRONIC_WARFARE), 1, EModuleType.ELECTRONIC_WARFARE);
        addValueByType(warshipHealthState.getStateByAsDouble(EModuleType.SHIELD), 1, EModuleType.SHIELD);

        setValueByPropulsion(warshipHealthState, EModuleType.PROPULSION);
        if (warshipHealthState.getWarShip().getShipClass().isFTLCapable()) {
            setValueByPropulsion(warshipHealthState, EModuleType.FTLPROPULSION);
        }
    }

    private void setValueByPropulsion(@Nonnull final WarshipHealthStateAccessor warshipHealthState,
                                      @Nonnull final EModuleType propulsion) {
        Preconditions.checkNotNull(warshipHealthState, "warshipHealthState must not be empty");
        Preconditions.checkNotNull(propulsion, "propulsion must not be empty");

        final double newVal = warshipHealthState.getStateByAsDouble(propulsion);
        final BigDecimal oldVal = effectValueByModuleType.get(propulsion);
        if (oldVal == null) {
            setValueByType(newVal, propulsion);
        } else {
            final double toSet = Double.min(newVal, oldVal.doubleValue());
            setValueByType(toSet, propulsion);
        }
    }

    private void setValueByPropulsion(@Nonnull final WarshipHealthState warshipHealthState,
                                      @Nonnull final EModuleType propulsion) {
        Preconditions.checkNotNull(warshipHealthState, "warshipHealthState must not be empty");
        Preconditions.checkNotNull(propulsion, "propulsion must not be empty");

        final double newVal = warshipHealthState.getPropulsionState();
        final BigDecimal oldVal = effectValueByModuleType.get(propulsion);
        if (oldVal == null) {
            setValueByType(newVal, propulsion);
        } else {
            final double toSet = Double.min(newVal, oldVal.doubleValue());
            setValueByType(toSet, propulsion);
        }
    }

    private void setValue(@Nonnull final Map<ShipClass, Integer> shipClasses) {
        Preconditions.checkNotNull(shipClasses, "shipClasses shouldn't be null!");

        // it's only possible to reduce speed while the slowest ship defined this parameter
        final List<Double> propValue = new ArrayList<>();
        final List<Double> ftlPropValue = new ArrayList<>();
        shipClasses.keySet().forEach(shipClass -> {
            final Map<ESupportType, List<SupportFitting>> supportTypeToModule = shipClass.getSupportFittings().stream()
                    .collect(Collectors.groupingBy(c -> c.getPassiveModule().getSupportType(),
                            Collectors.mapping(Function.identity(), Collectors.toList())));
            final Propulsion propulsion = shipClass.getPropulsion();
            if (propulsion != null) {
                addToIndividualPropulsionValues(EModuleType.PROPULSION, supportTypeToModule, propulsion, propValue);

                if (propulsion.isFtlCapable()) {
                    addToIndividualPropulsionValues(EModuleType.FTLPROPULSION, supportTypeToModule, propulsion, ftlPropValue);
                }
            }
        });

        final Double lowestPropulsion = propValue.stream().min(Double::compareTo).orElse(0D);
        final Double lowestFTLPropulsion = ftlPropValue.stream().min(Double::compareTo).orElse(0D);
        effectValueByModuleType.put(EModuleType.PROPULSION, new BigDecimal(lowestPropulsion));
        effectValueByModuleType.put(EModuleType.FTLPROPULSION, new BigDecimal(lowestFTLPropulsion));

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

    private static void addToIndividualPropulsionValues(@Nonnull final EModuleType propulsion,
                                                        @Nonnull final Map<ESupportType, List<SupportFitting>> supportTypeToModule,
                                                        @Nonnull final Propulsion propulsionModule,
                                                        @Nonnull final List<Double> propulsionValue) {
        Preconditions.checkNotNull(propulsion, "propulsion must not be empty");
        Preconditions.checkNotNull(supportTypeToModule, "supportTypeToModule must not be empty");
        Preconditions.checkNotNull(propulsionModule, "propulsionModule must not be empty");
        Preconditions.checkNotNull(propulsionValue, "propulsionValue must not be empty");

        final List<SupportFitting> supportFittings = supportTypeToModule.computeIfAbsent(ESupportType.getByValue(propulsion), k -> new ArrayList<>());
        final double bonus = supportFittings.stream().map(SupportFitting::getEffectValue).reduce(0D, Double::sum);
        final double absoluteValueAsFactor = bonus != 0 ? 1 + (bonus / 100) : 1;
        final double effectValue = propulsionModule.getEffectValue() * absoluteValueAsFactor;
        propulsionValue.add(effectValue);
    }

    /**
     * Calculates and adds the value by type.
     *
     * @param launcher        the module to add
     * @param supportFittings the support fitting if present
     * @param moduleType      the module type
     * @param amount          the amount of modules
     */
    private void calculateValueBySupportFitting(@Nonnull final Launcher launcher,
                                                final int amount,
                                                @Nonnull final List<SupportFitting> supportFittings,
                                                @Nonnull final EModuleType moduleType) {
        Preconditions.checkNotNull(launcher, "launcher shouldn't be null!");
        Preconditions.checkNotNull(supportFittings, "supportFittings shouldn't be null!");
        Preconditions.checkNotNull(moduleType, "moduleType shouldn't be null!");

        final Missile missile = launcher.getAmmunitionModule().getMissile();
        final double bonus = supportFittings.stream().map(SupportFitting::getEffectValue).reduce(0D, Double::sum);
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
    private void calculateValueBySupportFitting(@Nonnull final BaseModuleWithEffectValue baseModuleWithEffectValue,
                                                final int amount,
                                                @Nonnull final List<SupportFitting> supportFittings,
                                                @Nonnull final EModuleType moduleType) {
        Preconditions.checkNotNull(baseModuleWithEffectValue, "baseModule shouldn't be null!");
        Preconditions.checkNotNull(supportFittings, "supportFittings shouldn't be null!");
        Preconditions.checkNotNull(moduleType, "moduleType shouldn't be null!");

        final double bonus = supportFittings.stream().map(SupportFitting::getEffectValue).reduce(0D, Double::sum);
        final double absoluteValueAsFactor = bonus != 0 ? 1 + (bonus / 100) : 1;
        final double effectValue = baseModuleWithEffectValue.getEffectValue() * absoluteValueAsFactor;
        addValueByType(effectValue, amount, moduleType);
    }

    /**
     * Adds the effective value by {@link EModuleType} to the stats display.
     * Remember: Speeds will not add.
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
        final BigDecimal effectiveResultingValue = currentEffectValue.add(effectiveEffectValueAsBigD.multiply(new BigDecimal(amountOfModule)));
        effectValueByModuleType.put(moduleType, effectiveResultingValue);
    }

    private void setValueByType(final double effectValue, @Nullable final EModuleType moduleType) {
        if (moduleType == null) {
            return;
        }

        final BigDecimal effectiveResultingValue = new BigDecimal(effectValue);
        effectValueByModuleType.put(moduleType, effectiveResultingValue);
    }
}
