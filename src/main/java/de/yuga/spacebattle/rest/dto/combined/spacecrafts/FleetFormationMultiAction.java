package de.yuga.spacebattle.rest.dto.combined.spacecrafts;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Schema(description = ".")
public class FleetFormationMultiAction {

    @Nullable
    @JsonProperty
    @Schema(description = "the merge")
    private FleetMerge fleetMerge;

    @Nullable
    @JsonProperty
    @Schema(description = "the split")
    private FleetSplit fleetSplit;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "the id's of the warships which will be transferred to the reserve")
    private List<Integer> shipsToPool = new ArrayList<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "the id's of the warships which must be operational")
    private List<Integer> orderOperational = new ArrayList<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "the id's of the warships which must be not operational")
    private List<Integer> orderInoperational = new ArrayList<>();

    public FleetFormationMultiAction() {
    }

    @Nullable
    public FleetMerge getFleetMerge() {
        return fleetMerge;
    }

    @Nullable
    public FleetSplit getFleetSplit() {
        return fleetSplit;
    }

    @Nonnull
    public List<Integer> getShipsToPool() {
        return shipsToPool;
    }

    @Nonnull
    public List<Integer> getOrderOperational() {
        return orderOperational;
    }

    @Nonnull
    public List<Integer> getOrderInoperational() {
        return orderInoperational;
    }
}
