package de.yuga.spacebattle.backend.combat.round;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.details.AlignedFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.AmmunitionModule;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Launcher;
import de.yuga.spacebattle.backend.enums.EWeaponType;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MissileAmmunitionState implements Cloneable {

    /**
     * The amount of remaining missile in the arsenal of a warship.
     */
    @Nonnull
    private final Map<Missile, Integer> shotsPerMissile = new HashMap<>();

    public MissileAmmunitionState(@Nonnull final WarShip warShip) {
        Preconditions.checkNotNull(warShip, "warShip shouldn't be null!");

        final Set<AlignedFitting> missiles = warShip.getShipClass().getFittingByType(EWeaponType.MISSILE);
        missiles.stream().filter(a -> a.getLauncher() != null).forEach(alignedFitting -> {
            final Launcher launcher = alignedFitting.getLauncher();
            final int amountOfLaunchers = alignedFitting.getAmount();
            final AmmunitionModule ammunitionModule = launcher.getAmmunitionModule();
            final int ammoPerModule = ammunitionModule.getEffectValue();
            final Missile missile = ammunitionModule.getMissile();
            shotsPerMissile.merge(missile, amountOfLaunchers * ammoPerModule, Integer::sum);
        });
    }

    /**
     * Returns the remaining amount of missiles of the given type in the arsenal of the warship.
     *
     * @param missile the missile type
     * @return the leftover amount
     */
    public int getRemainingShots(@Nonnull final Missile missile) {
        Preconditions.checkNotNull(missile, "missile shouldn't be null!");

        final Integer leftOverShots = shotsPerMissile.get(missile);
        return leftOverShots == null ? 0 : leftOverShots;
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
