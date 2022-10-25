package de.yuga.spacebattle.backend.combat.round;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthState;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MissileAmmunitionState implements Cloneable {

    /**
     * The amount of remaining missile in the arsenal of a warship.
     */
    @Nonnull
    private final Map<Missile, Integer> shotsPerMissile = new HashMap<>();

    public MissileAmmunitionState(@Nonnull final WarshipHealthState healthState) {
        Preconditions.checkNotNull(healthState, "healthState must not be empty");

        shotsPerMissile.putAll(healthState.getRemainingShots());
    }

    /**
     * Returns the remaining amount of missiles of the given type in the arsenal of the warship.
     *
     * @param missile the missile type
     * @return the leftover amount
     */
    public int getRemainingShots(@Nonnull final Missile missile) {
        Preconditions.checkNotNull(missile, "missile shouldn't be null!");

        return shotsPerMissile.getOrDefault(missile, 0);
    }

    @Nonnull
    public Map<Missile, Integer> getRemainingShots() {
        return shotsPerMissile;
    }

    /**
     * Reduces the amount of remaining missiles in the arsenal for the given type.
     *
     * @param missile the missile type
     * @param amount  the amount to reduce about
     */
    public void reduce(@Nonnull final Missile missile, final int amount) {
        Preconditions.checkNotNull(missile, "missile shouldn't be null!");

        final Integer leftOverShots = shotsPerMissile.get(missile);
        if (leftOverShots != null) {
            final int newAmount = leftOverShots - amount;
            if (newAmount < 0) {
                throw new NotifyWebUserException("This should be checked, you can't use more shots then present.");
            }
            shotsPerMissile.put(missile, newAmount);
        }
    }

    /**
     * Returns if there are shots left.
     *
     * @return <code>true</code> if there are missiles remaining, <code>false</code> otherwise
     */
    public boolean hasShotsLeft() {
        return shotsPerMissile.values().stream().mapToInt(Integer::intValue).sum() > 0;
    }

    /**
     * Returns if there are shots left for the missile type.
     *
     * @return <code>true</code> if there are missiles remaining, <code>false</code> otherwise
     */
    public boolean hasShotsLeft(@Nonnull final Missile missile) {
        Preconditions.checkNotNull(missile, "missile shouldn't be null!");

        return shotsPerMissile.getOrDefault(missile, 0) > 0;
    }

    @Override
    public MissileAmmunitionState clone() {
        try {
            final MissileAmmunitionState clone = (MissileAmmunitionState) super.clone();
            //noinspection BoxingBoxedValue
            final Map<Missile, Integer> missileIntegerMap = shotsPerMissile.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> Integer.valueOf(e.getValue())));
            clone.shotsPerMissile.putAll(missileIntegerMap);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
