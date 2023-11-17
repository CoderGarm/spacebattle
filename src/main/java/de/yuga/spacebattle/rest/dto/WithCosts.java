package de.yuga.spacebattle.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.turn.resources.ResourceDeposit;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

public class WithCosts<BASECLASS> {

    @JsonProperty
    @Schema(description = "The costs.")
    private ResourceDeposit costs;

    public WithCosts<BASECLASS> withCosts(@Nonnull final de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit costs) {
        Preconditions.checkNotNull(costs, "costs must not be empty");

        this.costs = new ResourceDeposit(costs);
        return this;
    }
}
