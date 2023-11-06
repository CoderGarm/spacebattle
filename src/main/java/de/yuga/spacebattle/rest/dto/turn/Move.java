package de.yuga.spacebattle.rest.dto.turn;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.orbitals.FleetOrbit;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.Objects;

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

    /**
     * Principle: Countdown to zero -> job done.
     */
    @JsonProperty
    @Schema(required = true, description = "The current left over duration of this movement.")
    private int moveDoneAtZero;

    /**
     * Principle: Countdown to zero -> job done.
     */
    @JsonProperty
    @Schema(required = true, description = "The original duration of this movement.")
    private int originalDuration;

    public Move() {
    }

    public Move(@Nonnull final de.yuga.spacebattle.backend.entities.turn.Move move) {
        Preconditions.checkNotNull(move, "move shouldn't be null!");

        idFleetInMotion = move.isDeleted() ? Objects.requireNonNull(move.getFleetSnapshot()).getFleet().getId() : move.getFleet().getId();
        startOrbit = new FleetOrbit(move.getOriginOrbit());
        targetOrbit = new FleetOrbit(move.getDestinationOrbit());
        moveDoneAtZero = move.getTicksLeft();
        originalDuration = move.getOriginalDuration();
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

    public int getMoveDoneAtZero() {
        return moveDoneAtZero;
    }

    public int getOriginalDuration() {
        return originalDuration;
    }
}
