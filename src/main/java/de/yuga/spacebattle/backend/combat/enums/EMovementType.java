package de.yuga.spacebattle.backend.combat.enums;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * Compare to @see <a href="kampfsystem.md#movement types">Combat System - Movement Types</a>
 */
public enum EMovementType {

    /**
     * possibly changes the distance to the opponent<br>
     * normal ability to fire weapon systems<br>
     * normal chance of being hit<br>
     */
    REDUCE_DISTANCE,
    /**
     * possibly changes the distance to the opponent<br>
     * normal ability to fire weapon systems<br>
     * normal chance of being hit<br>
     */
    INCREASE_DISTANCE,
    /**
     * possibly changes the distance to the opponent<br>
     * normal ability to fire weapon systems<br>
     * normal chance of being hit<br>
     */
    HOLD_DISTANCE,
    /**
     * keeps the last course and speed<br>
     * rolls and yaws the ships to put the impeller wedge between the incoming weapons and themselves<br>
     * reduced ability to fire weapon systems nearly to zero<br>
     * reduced chance of being hit nearly to zero<br>
     */
    IMPELLER_WEDGE_PROTECTION,
    /**
     * keeps the last course and speed<br>
     * rolls and yaws the ships to put the most effective weapon systems towards the foe<br>
     * increases ability to fire weapon systems to maximum<br>
     * increases chance of being hit to maximum<br>
     */
    OFFENSIVE_ROLL,
    /**
     * evade to hyper limit and jump
     * possibly changes the distance to the opponent
     * reduced ability to fire weapon systems
     * reduced chance of being hit
     */
    EVASION_MOVEMENT; // todo jump out of system? calculate "could be caught" and break up fight earlier?

    @Nonnull
    public static EMovementType getMovementFromAlignment(@Nonnull final EWeaponAlignment weaponAlignment) {
        Preconditions.checkNotNull(weaponAlignment, "weaponAlignment shouldn't be null!");

        return Arrays.stream(EMovementType.values()).filter(weaponAlignment::isAssignableFromMovementType).findFirst().orElse(HOLD_DISTANCE);
    }
}
