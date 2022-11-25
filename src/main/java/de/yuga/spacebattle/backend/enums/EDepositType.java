package de.yuga.spacebattle.backend.enums;

import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;

/**
 * This enum dedicates if a {@link ResourceDeposit} must be calculated as deposit for a planet or as costs.
 */
public enum EDepositType {


    /**
     * These defining the stockpile of whatever.
     */
    DEPOSITS,

    /**
     * These defining the need to supply all constructions with workers and ships with a crew.
     */
    DEMAND,

    /**
     * Defines that whatever is described, it is in use.
     */
    UTILIZATION,

    /**
     * These defining the costs of whatever.
     */
    COSTS,

    /**
     * The income per tick of whatever.
     */
    INCOME,

    /**
     * If the deposit contains the capacity by resource type.
     */
    CAPACITY,

    /**
     * If the deposit defines the transportation needs.
     */
    TRANSPORTATION_DEMAND,

    /**
     * If the deposit defines the maximum delivery capacity.
     */
    TRANSPORTATION_DELIVERY,
    ;

    EDepositType() {
    }
}
