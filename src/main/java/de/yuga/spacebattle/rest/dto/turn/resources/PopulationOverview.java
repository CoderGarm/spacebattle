package de.yuga.spacebattle.rest.dto.turn.resources;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = ".")
public class PopulationOverview {

    @JsonProperty
    @Schema(required = true, description = "The capacity amount.")
    private long capacity = 0;

    @JsonProperty
    @Schema(required = true, description = "The present amount.")
    private long present = 0;

    public void addCapacity(final long capacity) {
        this.capacity += capacity;
    }

    public void addPresent(final long present) {
        this.present += present;
    }
}
