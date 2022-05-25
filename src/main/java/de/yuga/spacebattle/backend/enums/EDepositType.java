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
     * These defining the costs of whatever.
     */
    COSTS(ECalculationType.SUBTRACT),

    /**
     * The income per tick of whatever.
     */
    INCOME(ECalculationType.ADD);;

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
