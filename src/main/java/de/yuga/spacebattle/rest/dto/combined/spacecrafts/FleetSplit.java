package de.yuga.spacebattle.rest.dto.combined.spacecrafts;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Schema(description = ".")
public class FleetSplit {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The first are the fleet names. Warship IDs to Fleet name.")
    private final Map<String, List<Integer>> fleetConstellations = new HashMap<>();

    public FleetSplit() {
    }

    @Nonnull
    public Map<String, List<Integer>> getFleetConstellations() {
        return fleetConstellations;
    }
}
