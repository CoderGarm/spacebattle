package de.yuga.spacebattle.rest.dto.turn;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.orbitals.FleetOrbit;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Schema(description = ".")
public class Move {

    @JsonProperty
    @Schema(required = true, description = "The fleet which is in motion.")
    private int idFleetInMotion;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The departure of this movement.")
    private FleetOrbit startOrbit;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The destination of this movement.")
    private FleetOrbit targetOrbit;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "When the mission was started.")
    private de.yuga.spacebattle.rest.dto.turn.Tick started;

    /**
     * Principle: Countdown to zero -> job done.
     */
    @JsonProperty
    @Schema(required = true, description = "The current left over duration of this movement.")
    private int ticksLeft;

    @JsonProperty
    @Schema(required = true, description = "The original duration of this movement.")
    private int originalDuration;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The waypoints of this move.")
    private List<FleetOrbit> waypoints = new ArrayList<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The real flight plan of this move.")
    private List<de.yuga.spacebattle.rest.dto.turn.FlightPlan> flightPlan = new ArrayList<>();

    public Move() {
    }

    public Move(@Nonnull final de.yuga.spacebattle.backend.entities.turn.Move move) {
        Preconditions.checkNotNull(move, "move shouldn't be null!");

        idFleetInMotion = move.isDeleted() ? Objects.requireNonNull(move.getFleetSnapshot()).getFleet().getId() : move.getFleet().getId();
        startOrbit = new FleetOrbit(move.getOriginOrbit());
        targetOrbit = new FleetOrbit(move.getDestinationOrbit());
        ticksLeft = move.getTicksLeft();
        originalDuration = move.getOriginalDuration();
        started = new Tick(move.getStarted());
        waypoints.addAll(move.getWaypoints().stream().map(s -> new FleetOrbit(s)).collect(Collectors.toList()));
        flightPlan.addAll(move.getFlightPlan().stream().map(FlightPlan::new).collect(Collectors.toList()));
    }

    public int getIdFleetInMotion() {
        return idFleetInMotion;
    }

    @Nonnull
    public FleetOrbit getStartOrbit() {
        return startOrbit;
    }

    @Nonnull
    public FleetOrbit getTargetOrbit() {
        return targetOrbit;
    }

    public int getTicksLeft() {
        return ticksLeft;
    }

    public int getOriginalDuration() {
        return originalDuration;
    }
}
