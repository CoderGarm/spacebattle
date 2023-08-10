package de.yuga.spacebattle.rest.dto.combined.spacecrafts;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.yuga.spacebattle.rest.dto.orbitals.FleetOrbit;
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

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The orbit of this split.")
    private FleetOrbit orbit;

    public FleetSplit() {
    }

    @Nonnull
    public Map<String, List<Integer>> getFleetConstellations() {
        return fleetConstellations;
    }

    @Nonnull
    public FleetOrbit getOrbit() {
        return orbit;
    }
}
