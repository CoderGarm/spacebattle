package de.yuga.spacebattle.rest.dto.combined.spacecrafts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.rest.dto.AbstractId;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Schema(description = ".")
public class FleetMergeResult {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The removed fleet IDs.")
    private final List<AbstractId> deleted = new ArrayList<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The changed fleets.")
    private final List<FleetMarker> changed = new ArrayList<>();

    public FleetMergeResult(@Nonnull final Set<Fleet> changed, @Nonnull final Set<Fleet> deleted) {
        Preconditions.checkNotNull(changed, "changed must not be empty");
        Preconditions.checkNotNull(deleted, "deleted must not be empty");

        this.deleted.addAll(deleted.stream().map(AbstractId::new).collect(Collectors.toSet()));
        this.changed.addAll(changed.stream().map(FleetMarker::new).collect(Collectors.toSet()));
    }
}
