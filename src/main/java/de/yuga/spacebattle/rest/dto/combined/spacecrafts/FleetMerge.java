package de.yuga.spacebattle.rest.dto.combined.spacecrafts;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class FleetMerge {

    @Nonnull
    @Schema(required = true, description = "The fleet which must be merged.")
    private Integer idFleetToMerge;

    @Nonnull
    @Schema(required = true, description = "The fleet which is the target of the merge.")
    private Integer idFleetMergeTarget;

    @Nonnull
    public Integer getIdFleetToMerge() {
        return idFleetToMerge;
    }

    @Nonnull
    public Integer getIdFleetMergeTarget() {
        return idFleetMergeTarget;
    }
}
