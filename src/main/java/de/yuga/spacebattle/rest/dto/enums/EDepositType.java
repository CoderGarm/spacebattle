package de.yuga.spacebattle.rest.dto.enums;

import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

/**
 * This enum dedicates if a {@link ResourceDeposit} must be calculated as deposit for a planet or as costs.
 */
@Schema(description = ".")
public class EDepositType extends HasTypeName {

    public EDepositType() {
        super();
    }

    public EDepositType(@Nonnull final de.yuga.spacebattle.backend.enums.EDepositType depositType) {
        super(depositType);
    }
}
