package de.yuga.spacebattle.backend.enums;

/**
 * Defines the alignment of a weapon or counter weapon.
 */
public enum EAlignmentType {

    /**
     * If it is aligned to the bow or the stern.
     * These aligned weapons are mainly used in a hunt.
     */
    HUNTING_ALIGNMENT,

    /**
     * If it is aligned to the broad sides.
     * These aligned weapons are mainly used in sidewall battle.
     * <p>
     * Note that this alignment indicates that a such aligned weapon must be doubled by design.
     */
    BATTLE_ALIGNMENT
}
