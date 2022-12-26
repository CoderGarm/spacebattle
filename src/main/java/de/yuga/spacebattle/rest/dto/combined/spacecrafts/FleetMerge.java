package de.yuga.spacebattle.rest.dto.combined.spacecrafts;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Schema(description = ".")
public class FleetMerge {

    @Nonnull
    @Schema(required = true, description = "The first fleet composition. Warship IDs to Fleet ID.")
    private final Map<Integer, List<Integer>> fleetConstellations = new HashMap<>();

    public FleetMerge() {
    }

    @Nonnull
    public Map<Integer, List<Integer>> getFleetConstellations() {
        return fleetConstellations;
    }
}
