package de.yuga.spacebattle.rest.dto.turn;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
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
    @Schema(required = true, description = "The origin from which it comes.")
    private String from;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The designation to which it goes.")
    private String to;

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

        this.from = from.getFrom().getName();
        this.to = from.getTo().getName();
        this.resources = from.getResources().entrySet().stream().map(e -> new ResourceAmount(e.getKey(), e.getValue())).collect(Collectors.toList());
        this.humanResources = from.getHumanResources().entrySet().stream().map(e -> new HumanResourceAmount(e.getKey(), e.getValue())).collect(Collectors.toList());
    }

    public TransportJob(@Nonnull final de.yuga.spacebattle.backend.dto.turn.OrbitalTransportJob from) throws NotifyWebUserException {
        Preconditions.checkNotNull(from, "from must not be empty");

        switch (from.getTransportType()) {
            case PLANET_TO_FLEET:
                this.from = from.getPlanet().getName();
                this.to = from.getFleet().getName();
                break;
            case FLEET_TO_PLANET:
                this.from = from.getFleet().getName();
                this.to = from.getPlanet().getName();
                break;
            default:
                throw new NotifyWebUserException("This is sadly not implemented.");
        }
        this.resources = from.getResources().entrySet().stream().map(e -> new ResourceAmount(e.getKey(), e.getValue())).collect(Collectors.toList());
        this.humanResources = from.getHumanResources().entrySet().stream().map(e -> new HumanResourceAmount(e.getKey(), e.getValue())).collect(Collectors.toList());
    }
}
