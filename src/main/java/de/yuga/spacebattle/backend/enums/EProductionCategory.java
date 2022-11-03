package de.yuga.spacebattle.backend.enums;

import de.yuga.spacebattle.backend.entities.buildings.Building;

/**
 * Describes what is the task of a {@link Building}.
 */
public enum EProductionCategory {


    /**
     * Just a normal production job.
     */
    PRODUCE,

    /**
     * Increase the stockpile capacity.
     */
    CAPACITY,

    /**
     * Process whatever into something other, like a factory which melts metalore into metals.
     */
    REFINEMENT,
    ;
}
