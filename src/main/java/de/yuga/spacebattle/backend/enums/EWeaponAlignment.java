package de.yuga.spacebattle.backend.enums;

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
    BROADSIDE
}
