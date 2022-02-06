package de.yuga.spacebattle.backend.combat.enums;

/**
 * Compare to @see <a href="kampfsystem.md#movement types">Combat System - Movement Types</a>
 */
public enum EMovementType {

    /**
     * possibly changes the distance to the opponent
     * normal ability to fire weapon systems
     * normal chance of being hit
     * evasion movement
     */
    REDUCE_DISTANCE,
    /**
     * possibly changes the distance to the opponent
     * normal ability to fire weapon systems
     * normal chance of being hit
     * evasion movement
     */
    INCREASE_DISTANCE,
    /**
     * possibly changes the distance to the opponent
     * normal ability to fire weapon systems
     * normal chance of being hit
     * evasion movement
     */
    HOLD_DISTANCE,
    /**
     * possibly changes the distance to the opponent
     * reduced ability to fire weapon systems
     * reduced chance of being hit
     * sidewall protection
     */
    EVASION_MOVEMENT,
    /**
     * keeps the last course and speed
     * rolls and yaws the ships to put the sidewall between the incoming weapons and themselves
     * reduced ability to fire weapon systems nearly to zero
     * reduced chance of being hit nearly to zero
     */
    SIDEWALL_PROTECTION,
    /**
     * keeps the last course and speed
     * rolls and yaws the ships to put the most effective weapon systems towards the foe
     * increases ability to fire weapon systems to maximum
     * increases chance of being hit to maximum
     */
    OFFENSIVE_ROLL;
}
