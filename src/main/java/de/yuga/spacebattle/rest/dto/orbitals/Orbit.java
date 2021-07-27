package de.yuga.spacebattle.rest.dto.orbitals;

import com.google.common.base.Preconditions;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;
import java.math.BigInteger;

public class Orbit {

    /**
     * Just to position the system.
     */
    @ApiModelProperty(required = true, value = "The x-coordinate of this orbit.")
    private BigInteger xCoordinate;

    /**
     * Just to position the system.
     */
    @ApiModelProperty(required = true, value = "The y-coordinate of this orbit.")
    private BigInteger yCoordinate;

    public Orbit() {
    }

    public Orbit(@Nonnull final de.yuga.spacebattle.backend.entities.orbitals.Orbit orbit) {
        Preconditions.checkNotNull(orbit, "orbit shouldn't be null!");

        this.xCoordinate = orbit.getXCoordinate();
        this.yCoordinate = orbit.getYCoordinate();
    }

    public BigInteger getxCoordinate() {
        return xCoordinate;
    }

    public BigInteger getyCoordinate() {
        return yCoordinate;
    }
}
