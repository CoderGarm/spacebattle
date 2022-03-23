package de.yuga.spacebattle.backend.combat.enums;

/**
 * Compare to @see <a href="kampfsystem.md#movement types">Combat System - Movement Types</a>
 */
public enum EMovementMotivation {

    /**
     * The idea is simple to start a fight.
     */
    INITIATE_COMBAT,
    /**
     * The idea is not to start a fight and not to allow others to do that.
     */
    EVASION_MOVEMENT,
    /**
     * Flee and save our souls.
     */
    ESCAPE_MOVEMENT;

}
