package de.yuga.spacebattle.rest.dto.combined.spacecrafts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Schema(description = ".")
public class FleetFormationMultiActionResult {

    @Nullable
    @JsonProperty
    @Schema(description = "the merge result")
    private FleetMergeResult mergeResult;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "the split result")
    private List<Fleet> splitResult;


    public FleetFormationMultiActionResult(@Nullable final FleetMergeResult mergeResult,
                                           @Nonnull final Set<de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet> splitResult,
                                           @Nonnull final String preferredLanguage) {
        Preconditions.checkNotNull(splitResult, "splitResult must not be empty");
        Preconditions.checkNotNull(preferredLanguage, "preferredLanguage must not be empty");

        if (mergeResult != null) {
            this.mergeResult = mergeResult;
        }
        this.splitResult = splitResult.stream().map(f -> new Fleet(f, preferredLanguage)).collect(Collectors.toList());
    }

}
