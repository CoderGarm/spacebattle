package de.yuga.spacebattle.rest.dto.spacecrafts.modules.basics;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class HasCostsByParent {

    @JsonProperty
    @Schema(required = true, description = "The percentage of the parent's module cost which represents the costs of 'this'.")
    private int costsPercentage;

    public HasCostsByParent() {
    }

    public HasCostsByParent(@Nonnull final de.yuga.spacebattle.backend.entities.misc.HasCostsByParent hasCostsByParent) {
        Preconditions.checkNotNull(hasCostsByParent, "hasCostsByParent must not be empty");

        this.costsPercentage = hasCostsByParent.getCostsPercentage();
    }
}
