package de.yuga.spacebattle.backend.combat.dto;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.BattleCalculator;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.fittings.AlignedFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Launcher;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Weapon;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class CounterMissileWeaponry {

    /**
     * The map represents the ability of this anti missile volley - key is the maneuverability, the value is the amount of shots
     */
    @Nonnull
    final Map<Integer, Integer> antiMissileCapacity = new HashMap<>();

    public CounterMissileWeaponry(@Nonnull final List<AlignedFitting> alignedFittings) {
        Preconditions.checkNotNull(alignedFittings, "alignedFittings shouldn't be null!");

        alignedFittings.stream().map(CounterWeaponry::new)
                .collect(Collectors.groupingBy(CounterWeaponry::getManeuverability,
                        Collectors.mapping(CounterWeaponry::getAmountOfDamageProjectors, Collectors.toList())))
                .forEach((maneuverability, amountOfShotsList) -> {
                    final int summedShots = amountOfShotsList.stream().mapToInt(Integer::intValue).sum();
                    this.antiMissileCapacity.merge(maneuverability, summedShots, Integer::sum);
                });
    }

    /**
     * Calculates the impact of the anti missile volley to the given salvo.<br>
     * <br>
     * There is a grouping by anti missile volley and incoming missile group of the salvo.<br>
     * If the salvo per type if smaller than the amount of shots, the left shots of the volley are gone because of a wanted unpredictability.
     *
     * @param missile                 the missile type
     * @param initialAmountOfMissiles the initial salvo size by the given type
     * @return the amount of destroyed missiles
     */
    public int calculateDestroyedMissiles(@Nonnull final Missile missile, final int initialAmountOfMissiles) {
        Preconditions.checkNotNull(missile, "missile shouldn't be null!");

        int hits = 0;
        if (isAntiMissileCapacityLeft()) {
            final int maneuverabilityResistance = missile.getMissileMotor().getManeuverability();
            final Map<Integer, Integer> usedShots = new HashMap<>();
            int amountOfMissiles = initialAmountOfMissiles;
            for (final Integer maneuverability : antiMissileCapacity.keySet()) {
                final Integer shotOfVolley = antiMissileCapacity.get(maneuverability);
                int shots = shotOfVolley;
                int lostCounter = 0;
                for (int i = 0; i <= shotOfVolley; i++) {
                    if (shots > 0 && amountOfMissiles > 0) {
                        shots--;
                        final boolean isLost = BattleCalculator.calculateAntiMissileImpact(maneuverabilityResistance, maneuverability);
                        if (isLost) {
                            // detect losses
                            amountOfMissiles--;
                            lostCounter++;
                        }
                    }
                }
                usedShots.put(maneuverability, shots);
                hits += lostCounter;
            }
            reducingAntiMissileVolley(usedShots);
        }
        return hits;
    }

    /**
     * Reduces the amount of this volley about the used ones.
     *
     * @param usedShots the fired shots per maneuverability
     */
    private void reducingAntiMissileVolley(final Map<Integer, Integer> usedShots) {
        usedShots.forEach((maneuverability, usedShotsAmount) -> {
            int shotsLeftPerManeuverability = antiMissileCapacity.get(maneuverability);
            if (usedShotsAmount >= shotsLeftPerManeuverability) {
                antiMissileCapacity.remove(maneuverability);
            } else {
                shotsLeftPerManeuverability -= usedShotsAmount;
                antiMissileCapacity.put(maneuverability, shotsLeftPerManeuverability);
            }
        });
    }

    /**
     * Checks if there are any anti missile weapons left.
     *
     * @return <code>true</code> if there are any damage emitters left, <code>false</code> otherwise
     */
    private boolean isAntiMissileCapacityLeft() {
        return !antiMissileCapacity.isEmpty();
    }

    /**
     * An anti-missile weapon destroys the missile by hit and does not need a damage value.
     */
    private static class CounterWeaponry {

        /**
         * The amount of damage emitters.
         */
        private int amountOfDamageProjectors;

        /**
         * Defines the capability of this weapon to penetrate the shield. todo
         * The means the maneuver capability to find a gap in the tank to fire into it, for instance.
         */
        private int maneuverability;

        private CounterWeaponry(@Nonnull final AlignedFitting alignedFitting) {
            Preconditions.checkNotNull(alignedFitting, "alignedFitting shouldn't be null!");

            final int weaponAmount = alignedFitting.getAmount();
            final Weapon weapon = alignedFitting.getWeapon();
            if (weapon != null) {
                final int amountDamageEmitter = weapon.getAmountDamageEmitter();
                this.maneuverability = ThreadLocalRandom.current().nextInt(10, 51); // todo calculate "chance to hit" by ship class' computer
                this.amountOfDamageProjectors = weaponAmount * amountDamageEmitter;
            }
            final Launcher launcher = alignedFitting.getLauncher();
            if (launcher != null) {
                this.maneuverability = launcher.getAmmunitionModule().getMissile().getMissileMotor().getManeuverability();
                this.amountOfDamageProjectors = weaponAmount;
            }
        }

        public int getAmountOfDamageProjectors() {
            return amountOfDamageProjectors;
        }

        public int getManeuverability() {
            return maneuverability;
        }
    }
}
