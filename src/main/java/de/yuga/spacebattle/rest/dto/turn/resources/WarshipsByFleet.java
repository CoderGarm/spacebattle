package de.yuga.spacebattle.rest.dto.turn.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class WarshipsByFleet {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The activated warships to their fleet.")
    private String fleet;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The activated warships to their fleet.")
    private List<String> warships = new ArrayList<>();

    public WarshipsByFleet(@Nonnull final String fleet, @Nonnull final List<String> warships) {
        this.fleet = fleet;
        this.warships = warships;
    }
}
