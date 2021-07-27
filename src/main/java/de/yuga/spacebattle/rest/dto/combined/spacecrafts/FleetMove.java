package de.yuga.spacebattle.rest.dto.combined.spacecrafts;

import de.yuga.spacebattle.rest.dto.orbitals.Orbit;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FleetMove {

    @Nonnull
    @ApiModelProperty(required = true, value = "The fleet which must be moved.")
    private int idFleetToMove;

    @Nullable
    @ApiModelProperty(value = "The orbit which is the target of the movement.")
    private Orbit destinationOrbit;

    @Nullable
    @ApiModelProperty(value = "The system which is the target of the movement.")
    private Integer idDestinationSystem;

    @Nonnull
    public int getIdFleetToMove() {
        return idFleetToMove;
    }

    /**
     * Returns the destination orbit of this move.
     *
     * @return the orbit
     */
    @Nullable
    public Orbit getDestinationOrbit() {
        return destinationOrbit;
    }

    @Nullable
    public Integer getIdDestinationSystem() {
        return idDestinationSystem;
    }
}
