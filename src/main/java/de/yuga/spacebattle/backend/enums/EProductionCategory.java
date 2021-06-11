package de.yuga.spacebattle.backend.enums;

import de.yuga.spacebattle.backend.entities.buildings.Building;

/**
 * Describes what is the task of a {@link Building}.
 */
public enum EProductionCategory {


    /**
     * Just a normal production job.
     */
    PRODUCE(100),

    /**
     * Increase the stockpile capacity.
     */
    CAPACITY(1),

    /**
     * Process whatever into something other, like a factory which melts metalore into metals.
     */
    REFINEMENT(1),
    ;

    /**
     * Is the divisor in the formula to calculate the tick output.
     */
    private final int divisor;

    EProductionCategory(int divisor) {
        this.divisor = divisor;
    }

    public int getDivisor() {
        return divisor;
    }
}
