package de.yuga.spacebattle.rest.dto.combined.spacecrafts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.yuga.spacebattle.rest.dto.orbitals.Orbit;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Schema(description = ".")
public class FleetMove {

    @Nonnull
    @Schema(required = true, description = "The fleet which must be moved.")
    private int idFleetToMove;

    @Nullable
    @Schema(description = "The orbit which is the target of the movement.")
    private Orbit destinationOrbit;

    @Nullable
    @Schema(description = "The system which is the target of the movement.")
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

    @Override
    @JsonIgnore
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final FleetMove fleetMove = (FleetMove) o;

        return new EqualsBuilder().append(idFleetToMove, fleetMove.idFleetToMove).isEquals();
    }

    @Override
    @JsonIgnore
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(idFleetToMove).toHashCode();
    }
}
