package de.yuga.spacebattle.rest.dto.orbitals;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;

public class Orbit {

    /**
     * Just to position the system.
     */
    @ApiModelProperty(required = true, value = "The x-coordinate of this orbit.")
    private Distance xCoordinate;

    /**
     * Just to position the system.
     */
    @ApiModelProperty(required = true, value = "The y-coordinate of this orbit.")
    private Distance yCoordinate;

    public Orbit() {
    }

    public Orbit(@Nonnull final de.yuga.spacebattle.backend.entities.orbitals.Orbit orbit) {
        Preconditions.checkNotNull(orbit, "orbit shouldn't be null!");

        this.xCoordinate = orbit.getXCoordinate();
        this.yCoordinate = orbit.getYCoordinate();
    }

    public Distance getxCoordinate() {
        return xCoordinate;
    }

    public Distance getyCoordinate() {
        return yCoordinate;
    }
}
