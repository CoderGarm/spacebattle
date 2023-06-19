package de.yuga.spacebattle.rest.dto.misc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;

@Schema(description = ".")
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

    /**
     * Reads the cartesian coordinates and flips the y-axis in order to display the coords directly to the screen.
     */
    public Coords(final String[] split) {
        this.name = split[0];
        this.x = Integer.parseInt(split[1].replace("x", "").replaceAll(" ", ""));
        this.y = Integer.parseInt(split[2].replace("y", "").replaceAll(" ", "")) * -1;
    }

    public Coords(@Nonnull final String line) {
        this(line.split(","));
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final Coords coords = (Coords) o;

        return new EqualsBuilder().append(x, coords.x).append(y, coords.y).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(x).append(y).toHashCode();
    }
}
