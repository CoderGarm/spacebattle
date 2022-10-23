package de.yuga.spacebattle.backend.combat.round;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.FittingUtils;
import de.yuga.spacebattle.backend.combat.dto.DamagePerRangeAndAlignment;
import de.yuga.spacebattle.backend.combat.dto.Historizable;
import de.yuga.spacebattle.backend.combat.dto.HitLog;
import de.yuga.spacebattle.backend.combat.dto.RangeDefinition;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.details.AlignedFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.*;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModuleWithEffectValue;
import de.yuga.spacebattle.backend.enums.EHitArea;
import de.yuga.spacebattle.backend.enums.EWeaponType;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.backend.calculator.FittingUtils.DEFENSIVE_FITTING;

public class WarshipHealthState implements Cloneable {

    /**
     * The war ship which is this state for.
     */
    @Nonnull
    private final WarShip warShip;

    /**
     * The state of the hull in percentages. If zero it is destroyed.
     */
    private int hullState;

    /**
     * The state of the armor in percentages. If zero it is destroyed.
     */
    private int armorState;

    /**
     * The state of the sidewall in percentages. If zero it is destroyed.
     */
    private int sidewallState;

    /**
     * The state of the propulsion system in percentages. If zero it is destroyed.
     */
    private int propulsionState;

    /**
     * The state of the electronic warfare systems in percentages. If zero it is destroyed.
     */
    private int elokaState;

    /**
     * The activity state per weapon system. The value indicates if the weapon is active (<code>true</code>) or not.
     */
    @Nonnull
    private Map<AlignedFitting, Boolean> fittings = new HashMap<>();

    @Nonnull
    private final List<HitLog> hitLog = new ArrayList<>();

    @Nonnull
    private final List<BaseModuleWithEffectValue> modules = new ArrayList<>();

    @Nonnull
    private MissileAmmunitionState missileAmmunitionState;

    public WarshipHealthState(@Nonnull final WarShip warShip) {
        Preconditions.checkNotNull(warShip, "warShip shouldn't be null!");

        this.warShip = warShip;
        final ShipClass shipClass = warShip.getShipClass();
        final Armor armor = shipClass.getArmor();
        final Sidewall sidewall = shipClass.getSidewall();
        final Propulsion propulsion = shipClass.getPropulsion();
        final ElectronicWarfare electronicWarfare = shipClass.getElectronicWarfare();
        this.modules.add(armor);
        this.modules.add(sidewall);
        this.modules.add(propulsion);
        this.modules.add(electronicWarfare);

        final de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthState healthState = warShip.getWarshipHealthState();
        if (healthState == null) {
            armorState = armor != null ? armor.getEffectValue() : 0;
            hullState = armorState;
            sidewallState = sidewall != null ? sidewall.getEffectValue() : 0;
            propulsionState = propulsion != null ? propulsion.getEffectValue() : 0;
            elokaState = electronicWarfare != null ? electronicWarfare.getEffectValue() : 0;
            shipClass.getFittings().forEach(fitting -> fittings.put(fitting, true));
            this.missileAmmunitionState = new MissileAmmunitionState(warShip);
        } else {
            armorState = healthState.getArmorState();
            hullState = healthState.getHullState();
            sidewallState = healthState.getSidewallState();
            propulsionState = healthState.getPropulsionState();
            elokaState = healthState.getElokaState();
            final Set<AlignedFitting> activeFittings = healthState.getActiveFittings();
            shipClass.getFittings().forEach(fitting -> {
                final boolean active = activeFittings.contains(fitting);
                fittings.put(fitting, active);
            });
            this.missileAmmunitionState = new MissileAmmunitionState(healthState);
        }
    }

    @Nonnull
    public WarShip getWarShip() {
        return warShip;
    }

    /**
     * States if the war ship is capable of holding lives.
     *
     * @return <code>true</code> if there is a hull present which could hold an atmosphere, <code>false</code> otherwise
     */
    public boolean isAlive() {
        return hullState > 0;
    }

    /**
     * States if the war ship has any active weapon left.
     *
     * @return <code>true</code> if the ship can fight, <code>false</code> otherwise
     */
    public boolean isFightingCapable() {
        if (!isAlive()) {
            return false;
        }
        if (armorState <= 0 || sidewallState <= 0 || propulsionState <= 0 || elokaState <= 0) return false;
        for (Boolean aBoolean : fittings.values()) {
            if (aBoolean) {
                return true;
            }
        }
        return false;
    }

    @Nonnull
    public List<HitLog> getHitLog() {
        return hitLog;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WarshipHealthState)) return false;

        WarshipHealthState that = (WarshipHealthState) o;

        return warShip.equals(that.warShip);
    }

    @Override
    public int hashCode() {
        return warShip.hashCode();
    }

    /**
     * Checks if the health state has a difference from the untouched state of a fresh warship.
     *
     * @param reference the reference
     * @return <code>true</code> if there is a relevant difference, <code>false</code> otherwise
     */
    public boolean hasChanged(@Nonnull final de.yuga.spacebattle.backend.combat.round.WarshipHealthState reference) {
        Preconditions.checkNotNull(reference, "reference must not be empty");

        if (!this.getWarShip().equals(reference.getWarShip())) {
            throw new NotifyWebUserException("The warship health states can only be checked for the same individual ships.");
        }

        final boolean differState = !(this.getArmorState() == reference.getArmorState()
                && this.getElokaState() == reference.getElokaState()
                && this.getSidewallState() == reference.getSidewallState()
                && this.getHullState() == reference.getHullState()
                && this.getPropulsionState() == reference.getPropulsionState());

        final MissileAmmunitionState referenceMissiles = reference.getMissileAmmunitionState();
        final MissileAmmunitionState toCheckMissiles = this.getMissileAmmunitionState();
        final boolean differMissiles = referenceMissiles.getRemainingShots().entrySet().stream().anyMatch(ref -> {
            final Missile missile = ref.getKey();
            final int refAmount = ref.getValue();
            final int remainingShots = toCheckMissiles.getRemainingShots(missile);
            return refAmount != remainingShots;
        });

        return differState || differMissiles;
    }


    /**
     * Returns the maximum weapon range of the ship class.
     *
     * @return the maximum weapon range
     */
    public Distance getMaximumWeaponRange() {

        final List<AlignedFitting> fittings = getActiveFittings();
        final List<Distance> sortedRanged = fittings.stream()
                .map(fitting -> {
                    Distance damageProjectionRange = Distance.ZERO;
                    final Weapon weapon = fitting.getWeapon();
                    if (weapon != null) {
                        damageProjectionRange = weapon.getDamageProjectionRange();
                    }
                    final Launcher launcher = fitting.getLauncher();
                    if (launcher != null) {
                        final Missile missile = launcher.getAmmunitionModule().getMissile();
                        if (missileAmmunitionState.hasShotsLeft(missile)) {
                            damageProjectionRange = missile.getMaximumMissileRange();
                        }
                    }
                    return damageProjectionRange;
                })
                .sorted(Distance::compareTo).collect(Collectors.toList());
        if (sortedRanged.isEmpty()) {
            return Distance.ZERO;
        }
        return sortedRanged.get(sortedRanged.size() - 1);
    }

    /**
     * Returns the maximum weapon range of the ship class.
     *
     * @param weaponType the weapon type to filter
     * @return the maximum weapon range
     */
    public Distance getMaximumWeaponRangePerType(@Nonnull final EWeaponType weaponType) {
        Preconditions.checkNotNull(weaponType, "weaponType shouldn't be null!");

        final List<AlignedFitting> fittings = getActiveFittings();
        final List<Distance> sortedRanged = fittings.stream()
                .filter(fitting -> weaponType == fitting.getWeaponType())
                .map(fitting -> {
                    Distance damageProjectionRange = Distance.ZERO;
                    final Weapon weapon = fitting.getWeapon();
                    if (weapon != null) {
                        damageProjectionRange = weapon.getDamageProjectionRange();
                    }
                    final Launcher launcher = fitting.getLauncher();
                    if (launcher != null) {
                        final Missile missile = launcher.getAmmunitionModule().getMissile();
                        if (missileAmmunitionState.hasShotsLeft(missile)) {
                            damageProjectionRange = missile.getMaximumMissileRange();
                        }
                    }
                    return damageProjectionRange;
                })
                .sorted(Distance::compareTo).collect(Collectors.toList());
        if (sortedRanged.isEmpty()) {
            return Distance.ZERO;
        }
        return sortedRanged.get(sortedRanged.size() - 1);
    }

    /**
     * Returns the damage which can be applied by this class to the given range in meter.
     *
     * @param boundaries the boundaries
     * @return the damage value
     */
    @Nonnull
    public List<DamagePerRangeAndAlignment> getDamagePerRange(@Nonnull final RangeDefinition boundaries) {
        Preconditions.checkNotNull(boundaries, "boundaries shouldn't be null!");

        final List<AlignedFitting> fittings = getActiveFittings();
        return fittings.stream()
                .filter(FittingUtils.OFFENSIVE_FITTING)
                .filter(f -> {
                    if (f.getLauncher() != null) {
                        final Launcher launcher = f.getLauncher();
                        return missileAmmunitionState.hasShotsLeft(launcher.getAmmunitionModule().getMissile());
                    }
                    return true;
                })
                .map(fitting -> fitting.getDamagePerRange(boundaries))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Returns the damage which can be applied by this class over all ranges.
     *
     * @return the damage value
     */
    public List<DamagePerRangeAndAlignment> getDamagePerRanges() {
        final List<AlignedFitting> fittings = getActiveFittings();
        return fittings.stream()
                .filter(FittingUtils.OFFENSIVE_FITTING)
                .filter(f -> {
                    if (f.getLauncher() != null) {
                        final Launcher launcher = f.getLauncher();
                        return missileAmmunitionState.hasShotsLeft(launcher.getAmmunitionModule().getMissile());
                    }
                    return true;
                })
                .map(AlignedFitting::getDamagePerRange)
                .collect(Collectors.toList());
    }

    public long getMaximumDamage() {
        final List<AlignedFitting> fittings = getActiveFittings();
        return fittings.stream()
                .filter(FittingUtils.OFFENSIVE_FITTING)
                .map(fitting -> {
                    long damageValue = 0;
                    final int amount = fitting.getAmount();
                    final Weapon weapon = fitting.getWeapon();
                    final Launcher launcher = fitting.getLauncher();
                    if (launcher != null) {
                        if (missileAmmunitionState.hasShotsLeft(launcher.getAmmunitionModule().getMissile())) {
                            damageValue = launcher.getAmmunitionModule().getMissile().getWarhead().getDamageValue();
                        }
                    } else if (weapon != null) {
                        damageValue = weapon.getEffectValue();
                    }
                    return damageValue * amount;
                }).mapToLong(Long::longValue).sum();
    }

    @Nonnull
    public List<AlignedFitting> getActiveFittings() {
        return fittings.entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    @Nonnull
    public List<AlignedFitting> getActiveFittingsByWeaponType(@Nonnull final EWeaponType weaponType) {
        Preconditions.checkNotNull(weaponType, "weaponType shouldn't be null!");

        return fittings.entrySet().stream().filter(Map.Entry::getValue).filter(e -> e.getKey().getWeaponType() == weaponType).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    @Override
    public WarshipHealthState clone() {
        try {
            final WarshipHealthState clone = (WarshipHealthState) super.clone();
            //noinspection BoxingBoxedValue
            clone.fittings = fittings.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> Boolean.valueOf(e.getValue())));
            clone.missileAmmunitionState = missileAmmunitionState.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    /**
     * Applies the given damage to the war ship.
     *
     * @param damageValue  the damage to apply
     * @param damageDealer the source of the damage
     */
    public void applyDamage(final long damageValue, @Nonnull final Historizable<? extends Cloneable> damageDealer) {
        Preconditions.checkNotNull(damageDealer, "damageDealer shouldn't be null!");

        final EHitArea attackedPart = EHitArea.getRandomToApplyDamage();
        applyDamage(damageValue, attackedPart, damageDealer);
    }

    /**
     * Applies the given damage to the war ship at the given hit box.
     *
     * @param damageValue  the damage to apply
     * @param attackedPart if null it will be generated, for recursion
     */
    private void applyDamage(final long damageValue, @Nullable EHitArea attackedPart, @Nonnull final Historizable<? extends Cloneable> damageDealer) {
        Preconditions.checkNotNull(damageDealer, "damageDealer shouldn't be null!");

        if (attackedPart == null) {
            // no hit area points to a destroyed hull
            return;
        }
        switch (attackedPart) {
            case FITTING_AND_HULL:
                hullState = applyDamageToHitArea(hullState, damageValue, attackedPart, damageDealer);
                final int chanceForInternalHit = ThreadLocalRandom.current().nextInt(0, 100);
                if (chanceForInternalHit > 0 && chanceForInternalHit <= 7) {
                    final List<AlignedFitting> fittings = new ArrayList<>(this.fittings.keySet());
                    for (AlignedFitting alignedFitting : fittings) {
                        final boolean activityState = this.fittings.get(alignedFitting);
                        if (activityState) {
                            // destroy a fitting and stop
                            this.fittings.put(alignedFitting, false);
                            break;
                        }
                    }
                }
                hitLog.add(new HitLog(damageDealer, this, damageValue, hullState, attackedPart, isAlive(), isFightingCapable()));
                break;
            case ARMOR:
                armorState = applyDamageToHitArea(armorState, damageValue, attackedPart, damageDealer);
                hitLog.add(new HitLog(damageDealer, this, damageValue, armorState, attackedPart, isAlive(), isFightingCapable()));
                break;
            case SIDEWALL:
                sidewallState = applyDamageToHitArea(sidewallState, damageValue, attackedPart, damageDealer);
                hitLog.add(new HitLog(damageDealer, this, damageValue, sidewallState, attackedPart, isAlive(), isFightingCapable()));
                break;
            case PROPULSION:
                propulsionState = applyDamageToHitArea(propulsionState, damageValue, attackedPart, damageDealer);
                hitLog.add(new HitLog(damageDealer, this, damageValue, propulsionState, attackedPart, isAlive(), isFightingCapable()));
                break;
            case ELOKA:
                elokaState = applyDamageToHitArea(elokaState, damageValue, attackedPart, damageDealer);
                hitLog.add(new HitLog(damageDealer, this, damageValue, elokaState, attackedPart, isAlive(), isFightingCapable()));
                break;
        }
    }

    /**
     * Rolls the damage through the parts of the ship.
     *
     * @param state        the state of the matched part
     * @param damageValue  the damage value
     * @param attackedPart the attacked part
     * @return the new states value
     */
    private int applyDamageToHitArea(int state, final long damageValue, @Nullable final EHitArea attackedPart, @Nonnull final Historizable<? extends Cloneable> damageDealer) {
        Preconditions.checkNotNull(damageDealer, "damageDealer shouldn't be null!");

        if (attackedPart == null) {
            hullState = 0;
            return 0;
        }
        if (state <= 0) {
            applyDamage(damageValue, attackedPart.getFallback(), damageDealer);
        } else if (damageValue > state) {
            final long inBetweenDamage = damageValue - state;
            state = 0;
            applyDamage(inBetweenDamage, attackedPart.getFallback(), damageDealer);
        } else {
            state -= damageValue;
        }
        return state;
    }

    @Nonnull
    public Map<AlignedFitting, Boolean> getFittings() {
        return fittings;
    }

    @Nonnull
    public MissileAmmunitionState getMissileAmmunitionState() {
        return missileAmmunitionState;
    }

    public String asString() {
        return warShip.getName() + "{" +
                ", hull: " + hullState +
                ", armor: " + armorState +
                ", sidewall: " + sidewallState +
                ", propulsion: " + propulsionState +
                ", eloka: " + elokaState +
                ", activeFittings: " + fittings.values().stream().filter(aBoolean -> aBoolean).count() +
                ", isAlive: " + isAlive() +
                ", isFightingCapable: " + isFightingCapable() +
                '}';
    }

    public int getHullState() {
        return hullState;
    }

    public int getArmorState() {
        return armorState;
    }

    public int getSidewallState() {
        return sidewallState;
    }

    public int getPropulsionState() {
        return propulsionState;
    }

    public int getElokaState() {
        return elokaState;
    }

    /**
     * Returns the range of the fleets counter missile weaponry.
     *
     * @return the range
     */
    @Nonnull
    public Distance getCounterMissileRange() {
        return getActiveFittings().stream()
                .filter(DEFENSIVE_FITTING)
                .map(AlignedFitting::getRange)
                .max(Comparator.naturalOrder())
                .orElse(Distance.ZERO);
    }

    @Nullable
    @SuppressWarnings("TypeParameterHidesVisibleType")
    public <Module extends BaseModuleWithEffectValue> Module getModule(@Nonnull final Class<Module> module) {
        Preconditions.checkNotNull(module, "module shouldn't be null!");

        //noinspection unchecked
        return (Module) modules.stream().filter(m -> m.getClass().isAssignableFrom(module)).findAny().orElse(null);

    }

    public double getDamagedFraction(@Nonnull final WarshipHealthState reference) {
        Preconditions.checkNotNull(reference, "reference must not be empty");

        final double armorFraction = getFraction(armorState, reference.getArmorState());
        final double elokaFraction = getFraction(elokaState, reference.getElokaState());
        final double hullFraction = getFraction(hullState, reference.getHullState());
        final double propFraction = getFraction(propulsionState, reference.getPropulsionState());
        final double sidewallFraction = getFraction(sidewallState, reference.getSidewallState());

        final long refDamage = reference.getMaximumDamage();
        final long damage = getMaximumDamage();
        final double damageFraction = getFraction(damage, refDamage);

        //noinspection UnnecessaryLocalVariable
        final double overallDamage = (armorFraction + elokaFraction + hullFraction + propFraction + sidewallFraction + damageFraction) / 6;
        return overallDamage;
    }

    private double getFraction(final int state, final int referenceState) {
        return (double) state / (double) referenceState;
    }

    private double getFraction(final long state, final long referenceState) {
        return (double) state / (double) referenceState;
    }
}
