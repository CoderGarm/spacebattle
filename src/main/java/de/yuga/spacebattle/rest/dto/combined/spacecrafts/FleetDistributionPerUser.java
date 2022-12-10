package de.yuga.spacebattle.rest.dto.combined.spacecrafts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.rest.dto.orbitals.StarSystem;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Schema(description = ".")
public class FleetDistributionPerUser {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The system which includes the fleets.")
    private StarSystem starSystem;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The fleets by owner.")
    private final List<FleetMarker> fleetMarker = new ArrayList<>();

    public FleetDistributionPerUser() {
    }

    public FleetDistributionPerUser(final Map.Entry<de.yuga.spacebattle.backend.entities.orbitals.StarSystem, Set<Fleet>> entry) {
        Preconditions.checkNotNull(entry, "entry shouldn't be null!");

        starSystem = new StarSystem(entry.getKey());
        fleetMarker.addAll(entry.getValue().stream().map(FleetMarker::new).collect(Collectors.toList()));
    }
}
