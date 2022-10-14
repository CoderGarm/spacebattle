package de.yuga.spacebattle.rest.dto.orbitals;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class Orbit {

    /**
     * Just to position the system.
     */
    @JsonProperty
    @Schema(required = true, description = "The x-coordinate of this orbit.")
    private Distance xCoordinate;

    /**
     * Just to position the system.
     */
    @JsonProperty
    @Schema(required = true, description = "The y-coordinate of this orbit.")
    private Distance yCoordinate;

    public Orbit() {
    }

    public Orbit(@Nonnull final de.yuga.spacebattle.backend.entities.orbitals.Orbit orbit) {
        Preconditions.checkNotNull(orbit, "orbit shouldn't be null!");

        this.xCoordinate = orbit.getXCoordinate();
        this.yCoordinate = orbit.getYCoordinate();
    }

    @JsonIgnore
    public Distance getXCoordinate() {
        return xCoordinate;
    }

    @JsonIgnore
    public Distance getYCoordinate() {
        return yCoordinate;
    }
}
