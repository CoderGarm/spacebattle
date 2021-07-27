package de.yuga.spacebattle.rest.dto.combined.spacecrafts;

import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;

public class FleetMerge {

    @Nonnull
    @ApiModelProperty(required = true, value = "The fleet which must be merged.")
    private Integer idFleetToMerge;

    @Nonnull
    @ApiModelProperty(required = true, value = "The fleet which is the target of the merge.")
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
