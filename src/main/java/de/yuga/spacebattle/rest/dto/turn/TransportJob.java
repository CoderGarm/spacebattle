package de.yuga.spacebattle.rest.dto.turn;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.orbitals.Planet;
import de.yuga.spacebattle.rest.dto.turn.resources.HumanResourceAmount;
import de.yuga.spacebattle.rest.dto.turn.resources.ResourceAmount;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = ".")
public class TransportJob {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The planet from which it comes.")
    private Planet from;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The planet to which it goes.")
    private Planet to;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The transferred resources.")
    private List<ResourceAmount> resources;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The transferred humans.")
    private List<HumanResourceAmount> humanResources;

    public TransportJob(@Nonnull final de.yuga.spacebattle.backend.dto.turn.TransportJob from) {
        Preconditions.checkNotNull(from, "from must not be empty");

        this.from = new Planet(from.getFrom());
        this.to = new Planet(from.getTo());
        this.resources = from.getResources().entrySet().stream().map(e -> new ResourceAmount(e.getKey(), e.getValue())).collect(Collectors.toList());
        this.humanResources = from.getHumanResources().entrySet().stream().map(e -> new HumanResourceAmount(e.getKey(), e.getValue())).collect(Collectors.toList());
    }
}
