package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;

import javax.annotation.Nonnull;

/**
 * This enum dedicates if a {@link ResourceDeposit} must be calculated as deposit for a planet or as costs.
 */
public enum EDepositType {


    /**
     * These defining the stockpile of whatever.
     */
    DEPOSITS(ECalculationType.NONE),

    /**
     * These defining the need to supply all constructions with workers and ships with a crew.
     */
    DEMAND(ECalculationType.NONE),

    /**
     * Defines that whatever is described, it is in use.
     */
    UTILIZATION(ECalculationType.NONE),

    /**
     * These defining the costs of whatever.
     */
    COSTS(ECalculationType.SUBTRACT),

    /**
     * The income per tick of whatever.
     */
    INCOME(ECalculationType.ADD),

    /**
     * If the deposit contains the capacity by resource type.
     */
    CAPACITY(ECalculationType.NONE),
    ;

    @Nonnull
    private final ECalculationType calculationType;

    EDepositType(@Nonnull final ECalculationType calculationType) {
        Preconditions.checkNotNull(calculationType, "calculationType shouldn't be null!");

        this.calculationType = calculationType;
    }

    @Nonnull
    public ECalculationType getCalculationType() {
        return calculationType;
    }
}
