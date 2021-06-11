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
     * These defining the costs of whatever.
     */
    COSTS;

}
