package de.yuga.spacebattle.rest.dto.turn;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.AbstractId;
import de.yuga.spacebattle.rest.dto.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.rest.dto.turn.resources.HumanResourceAmount;
import de.yuga.spacebattle.rest.dto.turn.resources.ResourceAmount;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Schema(description = ".")
public class TransportJob {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The origin from which it comes.")
    private AbstractId from;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The designation to which it goes.")
    private AbstractId to;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The transferred resources.")
    private List<ResourceAmount> resources = new ArrayList<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The transferred humans.")
    private List<HumanResourceAmount> humanResources = new ArrayList<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "If ships were moved.")
    private Set<WarShip> ships = new HashSet<>();

    @JsonProperty
    @Schema(required = true, description = "If the job can be edited")
    private boolean canBeEdited = false;

    @Nullable
    @JsonProperty
    @Schema(description = "When the job was started.")
    private Tick started;

    @Nullable
    @JsonProperty
    @Schema(description = "The left duration of this job.")
    private Integer ticksLeft;

    public TransportJob(@Nonnull final de.yuga.spacebattle.backend.dto.turn.TransportJob from) {
        Preconditions.checkNotNull(from, "from must not be empty");

        this.from = new AbstractId(from.getFrom(), from.getFrom().getName());
        this.to = new AbstractId(from.getTo(), from.getTo().getName());
        this.resources = from.getResources().entrySet().stream().map(e -> new ResourceAmount(e.getKey(), e.getValue())).collect(Collectors.toList());
        this.humanResources = from.getHumanResources().entrySet().stream().map(e -> new HumanResourceAmount(e.getKey(), e.getValue())).collect(Collectors.toList());
    }

    public TransportJob(@Nonnull final de.yuga.spacebattle.backend.dto.turn.OrbitalTransportJob from) {
        Preconditions.checkNotNull(from, "from must not be empty");

        switch (from.getTransportType()) {
            case PLANET_TO_FLEET:
                this.from = new AbstractId(from.getPlanet(), from.getPlanet().getName());
                this.to = new AbstractId(from.getFleet(), from.getFleet().getName());
                break;
            case FLEET_TO_PLANET:
                this.from = new AbstractId(from.getFleet(), from.getFleet().getName());
                this.to = new AbstractId(from.getPlanet(), from.getPlanet().getName());
                break;
            default:
                throw new NotifyWebUserException("This is sadly not implemented.");
        }
        this.resources = from.getResources().entrySet().stream().map(e -> new ResourceAmount(e.getKey(), e.getValue())).collect(Collectors.toList());
        this.humanResources = from.getHumanResources().entrySet().stream().map(e -> new HumanResourceAmount(e.getKey(), e.getValue())).collect(Collectors.toList());
    }

    public TransportJob(@Nullable final de.yuga.spacebattle.backend.entities.turn.Tick today,
                        @Nonnull final de.yuga.spacebattle.backend.entities.turn.TransportJob transportJob,
                        @Nonnull final Set<de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip> ships,
                        @Nonnull final String preferredLanguage) {
        Preconditions.checkNotNull(transportJob, "transportJob must not be empty");
        Preconditions.checkNotNull(preferredLanguage, "preferredLanguage must not be empty");

        this.started = new Tick(transportJob.getTick());
        this.ticksLeft = transportJob.getTicksLeft();
        this.from = new AbstractId(transportJob.getOrigin(), transportJob.getOrigin().getName());
        this.to = new AbstractId(transportJob.getDestination(), transportJob.getDestination().getName());
        this.ships.addAll(ships.stream().map(warShip -> new WarShip(warShip, warShip.getWarshipHealthState(), preferredLanguage)).collect(Collectors.toSet()));
        if (today != null) {
            this.canBeEdited = transportJob.getTick().equals(today);
        }
    }

    public void reCalcEditableState(@Nonnull final de.yuga.spacebattle.backend.entities.turn.Tick today) {
        Preconditions.checkNotNull(today, "today must not be empty");

        this.canBeEdited = started != null && started.getTickNo() == today.getNo();
    }
}
