package de.yuga.spacebattle.rest.dto.enums;

import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;

/**
 * This enum dedicates if a {@link ResourceDeposit} must be calculated as deposit for a planet or as costs.
 */
@ApiModel(parent = HasTypeName.class)
public class EDepositType extends HasTypeName {

    @Nonnull
    @ApiModelProperty(required = true, value = "The calculation type.")
    private final ECalculationType calculationType;

    public EDepositType() {
        super();
        calculationType = new ECalculationType();
    }

    public EDepositType(@Nonnull final de.yuga.spacebattle.backend.enums.EDepositType depositType) {
        super(depositType);

        this.calculationType = new ECalculationType(depositType.getCalculationType());
    }

    @Nonnull
    public ECalculationType getCalculationType() {
        return calculationType;
    }
}
