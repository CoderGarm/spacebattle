package de.yuga.spacebattle.backend.enums;

import de.yuga.spacebattle.backend.combat.enums.EMovementType;

import javax.annotation.Nullable;

/**
 * Defines the alignment of a weapon or counter weapon.
 */
public enum EWeaponAlignment {

    /**
     * If it is aligned to the front.
     * These aligned weapons are mainly used in a hunt.
     */
    BOW,

    /**
     * If it is aligned to the back.
     * These aligned weapons are mainly used if the ship is fleeing.
     */
    STERN,

    /**
     * If it is aligned to the broad sides.
     * These aligned weapons are mainly used in sidewall battle.
     * <p>
     * Note that this alignment indicates that a such aligned weapon must be doubled by design.
     */
    BROADSIDE;

    /**
     * Checks if a given movement type matches with a weapons alignment.
     *
     * @param movementType the movement type to check
     * @return <code>true</code> if the movement type allows to fire this alignment, <code>false</code> otherwise
     */
    public boolean isAssignableFromMovementType(@Nullable final EMovementType movementType) {
        if (movementType == null) {
            return false;
        }

        switch (movementType) {
            case OFFENSIVE_ROLL:
                return this == BROADSIDE;
            case REDUCE_DISTANCE:
                return this == BOW;
            case INCREASE_DISTANCE:
            case EVASION_MOVEMENT:
                return this == STERN;
            case HOLD_DISTANCE:
                // every weapon is allowed because of free movement
                return true;
            default:
            case SIDEWALL_PROTECTION:
                return false;
        }
    }
}
