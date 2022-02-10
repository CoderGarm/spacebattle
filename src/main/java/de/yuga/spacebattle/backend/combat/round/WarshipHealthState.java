package de.yuga.spacebattle.backend.combat.round;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.FittingUtils;
import de.yuga.spacebattle.backend.combat.dto.DamagePerRangeAndAlignment;
import de.yuga.spacebattle.backend.combat.dto.Historizable;
import de.yuga.spacebattle.backend.combat.dto.HitLog;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.details.AlignedFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Launcher;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Weapon;
import de.yuga.spacebattle.backend.enums.EHitArea;
import de.yuga.spacebattle.backend.enums.EWeaponType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

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
    private MissileAmmunitionState missileAmmunitionState;

    public WarshipHealthState(@Nonnull final WarShip warShip) {
        Preconditions.checkNotNull(warShip, "warShip shouldn't be null!");

        this.warShip = warShip;
        final ShipClass shipClass = warShip.getShipClass();
        armorState = shipClass.getArmor() != null ? shipClass.getArmor().getEffectValue() : 0;
        hullState = armorState;
        sidewallState = shipClass.getSidewall() != null ? shipClass.getSidewall().getEffectValue() : 0;
        propulsionState = shipClass.getPropulsion() != null ? shipClass.getPropulsion().getEffectValue() : 0;
        elokaState = shipClass.getElectronicWarfare() != null ? shipClass.getElectronicWarfare().getEffectValue() : 0;
        shipClass.getFittings().forEach(fitting -> fittings.put(fitting, true));
        this.missileAmmunitionState = new MissileAmmunitionState(warShip);
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
     * Returns the maximum weapon range of the ship class.
     *
     * @return the maximum weapon range
     */
    public BigDecimal getMaximumWeaponRange() {

        final List<AlignedFitting> fittings = getActiveFittings();
        final List<BigDecimal> sortedRanged = fittings.stream()
                .map(fitting -> {
                    BigDecimal damageProjectionRange = BigDecimal.ZERO;
                    final Weapon weapon = fitting.getWeapon();
                    if (weapon != null) {
                        damageProjectionRange = weapon.getDamageProjectionRange();
                    }
                    final Launcher launcher = fitting.getLauncher();
                    if (launcher != null) {
                        final Missile missile = launcher.getAmmunitionModule().getMissile();
                        if (missileAmmunitionState.hasShotsLeft(missile)) {
                            damageProjectionRange = missile.getMissileRange();
                        }
                    }
                    return damageProjectionRange;
                })
                .sorted(BigDecimal::compareTo).collect(Collectors.toList());
        if (sortedRanged.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sortedRanged.get(sortedRanged.size() - 1);
    }

    /**
     * Returns the maximum weapon range of the ship class.
     *
     * @param weaponType the weapon type to filter
     * @return the maximum weapon range
     */
    public BigDecimal getMaximumWeaponRangePerType(@Nonnull final EWeaponType weaponType) {
        Preconditions.checkNotNull(weaponType, "weaponType shouldn't be null!");

        final List<AlignedFitting> fittings = getActiveFittings();
        final List<BigDecimal> sortedRanged = fittings.stream()
                .filter(fitting -> weaponType == fitting.getWeaponType())
                .map(fitting -> {
                    BigDecimal damageProjectionRange = BigDecimal.ZERO;
                    final Weapon weapon = fitting.getWeapon();
                    if (weapon != null) {
                        damageProjectionRange = weapon.getDamageProjectionRange();
                    }
                    final Launcher launcher = fitting.getLauncher();
                    if (launcher != null) {
                        final Missile missile = launcher.getAmmunitionModule().getMissile();
                        if (missileAmmunitionState.hasShotsLeft(missile)) {
                            damageProjectionRange = missile.getMissileRange();
                        }
                    }
                    return damageProjectionRange;
                })
                .sorted(BigDecimal::compareTo).collect(Collectors.toList());
        if (sortedRanged.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return sortedRanged.get(sortedRanged.size() - 1);
    }

    /**
     * Returns the damage which can be applied by this class to the given range in meter.
     *
     * @param lowerBound the lower boundary
     * @param upperBound the upper boundary
     * @return the damage value
     */
    public List<DamagePerRangeAndAlignment> getDamagePerRange(@Nonnull final BigDecimal lowerBound, @Nonnull final BigDecimal upperBound) {
        Preconditions.checkNotNull(lowerBound, "lowerBound shouldn't be null!");
        Preconditions.checkNotNull(upperBound, "upperBound shouldn't be null!");

        final List<AlignedFitting> fittings = getActiveFittings();
        return fittings.stream()
                .filter(FittingUtils.OFFENSIVE_FITTING)
                .map(fitting -> fitting.getDamagePerRange(lowerBound, upperBound))
                .filter(Objects::nonNull)
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
                        damageValue = launcher.getAmmunitionModule().getMissile().getWarhead().getDamageValue();
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
}
