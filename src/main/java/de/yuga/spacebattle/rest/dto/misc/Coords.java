package de.yuga.spacebattle.rest.dto.misc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

public class Coords {

    @JsonProperty
    @Schema(required = true)
    private final int x;

    @JsonProperty
    @Schema(required = true)
    private final int y;

    @JsonProperty
    @Schema(required = true)
    private final String name;

    /*
    @Nonnull
    @Schema(required = true, description = "The stellar class of the star of this system.")
    private final de.yuga.spacebattle.rest.dto.enums.EStarClassType starClassType= new de.yuga.spacebattle.rest.dto.enums.EStarClassType(EStarClassType.CLASS_G3);
    */

    /**
     * Reads the cartesian coordinates and flips the y-axis in order to display the coords directly to the screen.
     */
    public Coords(final String[] split) {
        this.name = split[0];
        this.x = Integer.parseInt(split[1].replace("x", "").replaceAll(" ", ""));
        this.y = Integer.parseInt(split[2].replace("y", "").replaceAll(" ", "")) * -1;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Nonnull
    @JsonIgnore
    public String getName() {
        return name;
    }

    @Nonnull
    @JsonIgnore
    public Position getPosition() {
        return new Position(x, y);
    }
}
