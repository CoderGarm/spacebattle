package de.yuga.spacebattle.backend.calculator;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator;
import de.yuga.spacebattle.backend.combat.round.WarshipHealthState;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import de.yuga.spacebattle.backend.dto.physics.Mass;
import de.yuga.spacebattle.backend.dto.physics.Velocity;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.FleetSnapshot;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.misc.HasEffectValue;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.fittings.AlignedFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.fittings.SupportFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.*;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthStateAccessor;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthStateSnapshot;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.ECapacityAreaType;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.backend.enums.ESupportType;
import de.yuga.spacebattle.backend.enums.EWeaponType;
import de.yuga.spacebattle.backend.enums.physics.*;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.CapabilityValue;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.CapacityValue;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.SpacecraftCapabilities;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.SpacecraftCapacityAreas;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SpacecraftCalculator {

    private static final MathContext MATH_CONTEXT = new MathContext(8, RoundingMode.DOWN);

    private final Set<EModuleType> PROPULSIONS = Set.of(EModuleType.PROPULSION, EModuleType.FTLPROPULSION);

    private Map<EModuleType, BigDecimal> effectValueByModuleType;

    private Map<ECapacityAreaType, CapacityValue> capacities;

    public SpacecraftCapabilities getSpaceCraftCapabilities(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        createBaseData();
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

        createBaseData();
        setValue(Map.of(shipClass, 1));
        return getSpacecraftCapabilities();
    }

    public SpacecraftCapabilities getSpaceCraftCapabilities(@Nonnull final Map<ShipClass, Integer> shipClasses) {
        Preconditions.checkNotNull(shipClasses, "shipClasses must not be empty");

        createBaseData();
        setValue(shipClasses);
        return getSpacecraftCapabilities();
    }

    public SpacecraftCapabilities getSpaceCraftCapabilities(@Nonnull final WarshipHealthStateAccessor warshipHealthState) {
        Preconditions.checkNotNull(warshipHealthState, "warshipHealthState must not be empty");

        createBaseData();
        setValue(warshipHealthState);
        return getSpacecraftCapabilities();
    }

    public SpacecraftCapabilities getSpaceCraftCapabilities(@Nonnull final Collection<de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthStateAccessor> warshipHealthStates) {
        Preconditions.checkNotNull(warshipHealthStates, "warshipHealthStates must not be empty");

        createBaseData();
        warshipHealthStates.forEach(this::setValue);
        return getSpacecraftCapabilities();
    }

    public SpacecraftCapabilities getSpaceCraftCapabilities(@Nonnull final FleetSnapshot fleetSnapshot) {
        Preconditions.checkNotNull(fleetSnapshot, "fleetSnapshot must not be empty");

        createBaseData();
        final Set<WarshipHealthStateSnapshot> ships = fleetSnapshot.getShips();
        ships.forEach(this::setValue);
        return getSpacecraftCapabilities();
    }

    public Set<de.yuga.spacebattle.backend.entities.turn.battle.combat.CapabilityValue> getCapabilityValues(@Nonnull final ShipClass shipClass) {
        Preconditions.checkNotNull(shipClass, "shipClass must not be empty");

        createBaseData();
        final Map<ShipClass, Integer> shipClasses = new HashMap<>();
        shipClasses.put(shipClass, 1);
        setValue(shipClasses);
        return effectValueByModuleType.entrySet().stream()
                .map(de.yuga.spacebattle.backend.entities.turn.battle.combat.CapabilityValue::new)
                .collect(Collectors.toSet());
    }

    public Set<de.yuga.spacebattle.backend.entities.turn.battle.combat.CapabilityValue> getCapabilityValues(@Nonnull final WarshipHealthState warshipHealthState) {
        Preconditions.checkNotNull(warshipHealthState, "warshipHealthState must not be empty");

        createBaseData();
        setValue(warshipHealthState);
        return effectValueByModuleType.entrySet().stream()
                .map(de.yuga.spacebattle.backend.entities.turn.battle.combat.CapabilityValue::new)
                .collect(Collectors.toSet());
    }

    public Set<de.yuga.spacebattle.backend.entities.turn.battle.combat.CapabilityValue> getCapabilityValues(@Nonnull final de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthState healthState) {
        Preconditions.checkNotNull(healthState, "healthState must not be empty");

        createBaseData();
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
        addValueByType(warshipHealthState.getSidewallState(), 1, EModuleType.SIDEWALL);

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

    private void createBaseData() {
        capacities = new HashMap<>();
        effectValueByModuleType = Arrays.stream(EModuleType.values())
                .filter(type -> !PROPULSIONS.contains(type))
                .collect(Collectors.toMap(Function.identity(), value -> BigDecimal.ZERO));
    }

    private void setValue(@Nonnull final WarshipHealthStateAccessor warshipHealthState) {
        Preconditions.checkNotNull(warshipHealthState, "warshipHealthState shouldn't be null!");

        final ShipClass shipClass = warshipHealthState.getWarShip().getShipClass();
        setCapacityValue(shipClass);
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
        addValueByType(warshipHealthState.getStateByAsDouble(EModuleType.SIDEWALL), 1, EModuleType.SIDEWALL);

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
            addToIndividualPropulsionValues(EModuleType.PROPULSION, supportTypeToModule, propulsion, propValue);
            if (propulsion.isFtlCapable()) {
                addToIndividualPropulsionValues(EModuleType.FTLPROPULSION, supportTypeToModule, propulsion, ftlPropValue);
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
                    final EModuleType moduleType = EModuleType.SIDEWALL;
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

        final Missile missile = launcher.getHeaviestMissile();
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
    private void calculateValueBySupportFitting(@Nonnull final HasEffectValue baseModuleWithEffectValue,
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

    @Nonnull
    public SpacecraftCapacityAreas getSpacecraftCapacityAreas(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet must not be empty");

        createBaseData();
        fleet.getAliveShips().forEach(warShip -> setCapacityValue(warShip.getShipClass()));
        return new SpacecraftCapacityAreas().withValues(capacities);
    }

    @Nonnull
    public SpacecraftCapacityAreas getSpacecraftCapacityAreas(@Nonnull final ShipClass shipClass) {
        Preconditions.checkNotNull(shipClass, "shipClass must not be empty");

        createBaseData();
        setCapacityValue(shipClass);
        return new SpacecraftCapacityAreas().withValues(capacities);
    }

    @Nonnull
    public SpacecraftCapacityAreas getSpacecraftCapacityAreas(@Nonnull final Map<ShipClass, Integer> shipClasses) {
        Preconditions.checkNotNull(shipClasses, "shipClasses must not be empty");

        createBaseData();
        shipClasses.forEach((shipClass, amount) -> {
            for (int i = 0; i < amount; i++) {
                setCapacityValue(shipClass);
            }
        });
        return new SpacecraftCapacityAreas().withValues(capacities);
    }

    @Nonnull
    public SpacecraftCapacityAreas getSpacecraftCapacityAreas(@Nonnull final FleetSnapshot fleetSnapshot) {
        Preconditions.checkNotNull(fleetSnapshot, "fleetSnapshot must not be empty");

        createBaseData();
        fleetSnapshot.getShips().forEach(warshipHealthStateSnapshot -> {
            final ShipClass shipClass = warshipHealthStateSnapshot.getWarShip().getShipClass();
            setCapacityValue(shipClass);
        });
        return new SpacecraftCapacityAreas().withValues(capacities);
    }

    private void setCapacityValue(@Nonnull final ShipClass shipClass) {
        Preconditions.checkNotNull(shipClass, "shipClass must not be empty");

        for (final ECapacityAreaType capacityAreaType : ECapacityAreaType.getValuesWithoutOverall()) {
            final CapacityValue orDefault = capacities.getOrDefault(capacityAreaType, new CapacityValue());
            orDefault.setCapacityArea(capacityAreaType);
            orDefault.setTonnage(shipClass.getTonnage(capacityAreaType));
            capacities.put(capacityAreaType, orDefault);
        }
        capacities.put(ECapacityAreaType.OVERALL, new CapacityValue(ECapacityAreaType.OVERALL, SpacecraftTonnageCalculator.getFullTonnage(shipClass)));
    }

    @Nonnull
    public static Acceleration getAcceleration(@Nonnull final Mass tonnage,
                                               @Nonnull final Propulsion propulsion,
                                               @Nonnull final EHyperBand hyperBand,
                                               @Nonnull final Set<SupportFitting> supportFittings) {
        Preconditions.checkNotNull(tonnage, "tonnage must not be empty");
        Preconditions.checkNotNull(propulsion, "propulsion must not be empty");
        Preconditions.checkNotNull(hyperBand, "hyperBand must not be empty");
        Preconditions.checkNotNull(supportFittings, "supportFittings must not be empty");


        final double maxAccelerationOfMilitary = propulsion.getTechnologyType().getMaxAccelerationOfMilitary();
        final EModuleType eModuleType = hyperBand == EHyperBand.NONE ? EModuleType.PROPULSION : EModuleType.FTLPROPULSION;
        final double propulsionSupportFactor = supportFittings.stream()
                .filter(s -> eModuleType == s.getPassiveModule().getSupportType().getModifiedProperty())
                .findAny().stream()
                .map(SupportFitting::getAbsoluteValueAsFactor)
                .reduce(0D, Double::sum);
        final BigDecimal factor = BigDecimal.ONE.add(new BigDecimal(propulsionSupportFactor));
        final BigDecimal accelerationValue = getMathematicallyAcceleration(tonnage, propulsion, hyperBand)
                .multiply(factor, ResourceDeposit.MATH_CONTEXT_INTEGER)
                .multiply(BigDecimal.valueOf(maxAccelerationOfMilitary), DistanceCalculator.MC_HU);
        return new Acceleration(accelerationValue, EAccelerationMetric.G, hyperBand);
    }

    @Nonnull
    public static Velocity getVelocity(@Nonnull final Propulsion propulsion,
                                       @Nonnull final EHyperBand hyperBand) {
        Preconditions.checkNotNull(propulsion, "propulsion must not be empty");
        Preconditions.checkNotNull(hyperBand, "hyperBand must not be empty");

        return new Velocity(hyperBand.getEffectiveTopSpeed(propulsion.getTechnologyType()), EDistanceMetric.M, ETimeMetric.SECOND);
    }

    @Nonnull
    private static BigDecimal getMathematicallyAcceleration(@Nonnull final Mass tonnage,
                                                            @Nonnull final Propulsion propulsion,
                                                            @Nonnull final EHyperBand hyperBand) {
        Preconditions.checkNotNull(tonnage, "tonnage must not be empty");
        Preconditions.checkNotNull(propulsion, "propulsion must not be empty");
        Preconditions.checkNotNull(hyperBand, "hyperBand must not be empty");

        if (propulsion.getHyperBand().getVelocityMultiplier() < hyperBand.getVelocityMultiplier()) {
            return BigDecimal.ZERO;
        }

        //y = 558.1465 - 0.0001075032*x + 7.261618e-11*x^2 - 2.1753440000000002e-17*x^3 + 2.786797e-24*x^4 - 1.275354e-31*x^5
        final BigDecimal a = BigDecimal.valueOf(propulsion.getEffectValue());
        final List<BigDecimal> paramList = List.of(
                BigDecimal.valueOf(-0.0001075032),
                BigDecimal.valueOf(7.261618).scaleByPowerOfTen(-11),
                BigDecimal.valueOf(-2.175344).scaleByPowerOfTen(-17),
                BigDecimal.valueOf(2.786797).scaleByPowerOfTen(-24),
                BigDecimal.valueOf(-1.275354).scaleByPowerOfTen(-31)
        );

        final BigDecimal tons = tonnage.getCoordinateInMetric(EMassMetric.T); // is x
        BigDecimal result = a;
        for (int i = 0; i < paramList.size(); i++) {
            final BigDecimal coefficient = paramList.get(i);
            final BigDecimal inBetween = coefficient.multiply(tons.pow(i + 1), MATH_CONTEXT);
            result = result.add(inBetween);
        }
        result = result.setScale(0, RoundingMode.HALF_EVEN);
        return result.multiply(BigDecimal.valueOf(hyperBand.getVelocityMultiplier()), MATH_CONTEXT);
    }
}
