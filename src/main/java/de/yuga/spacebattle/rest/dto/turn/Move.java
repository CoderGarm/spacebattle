package de.yuga.spacebattle.rest.dto.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.orbitals.FleetOrbit;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;

public class Move {

    @ApiModelProperty(required = true, value = "The fleet which is in motion.")
    private int idFleetInMotion;

    @Nonnull
    @ApiModelProperty(required = true, value = "The departure of this movement.")
    private FleetOrbit startOrbit;

    @Nonnull
    @ApiModelProperty(required = true, value = "The destination of this movement.")
    private FleetOrbit targetOrbit;

    /**
     * Principle: Countdown to zero -> job done.
     */
    @ApiModelProperty(required = true, value = "The current left over duration of this movement.")
    private int moveDoneAtZero;

    /**
     * Principle: Countdown to zero -> job done.
     */
    @ApiModelProperty(required = true, value = "The original duration of this movement.")
    private int originalDuration;

    public Move() {
    }

    public Move(@Nonnull final de.yuga.spacebattle.backend.entities.turn.Move move) {
        Preconditions.checkNotNull(move, "move shouldn't be null!");

        idFleetInMotion = move.getFleet().getId();
        startOrbit = new FleetOrbit(move.getOriginOrbit());
        targetOrbit = new FleetOrbit(move.getDestinationOrbit());
        moveDoneAtZero = move.getMoveDoneAtZero();
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
