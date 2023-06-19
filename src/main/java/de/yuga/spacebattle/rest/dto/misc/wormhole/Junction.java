package de.yuga.spacebattle.rest.dto.misc.wormhole;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.misc.Coords;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import java.util.Set;

@Schema(description = ".")
public class Junction {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The position and name of the junction.")
    private Coords position;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "All termini of the junction.")
    private Set<Coords> termini;

    public Junction(@Nonnull final Coords position, @Nonnull final Set<Coords> termini) {
        this.position = Preconditions.checkNotNull(position, "position must not be empty");
        this.termini = Preconditions.checkNotNull(termini, "termini must not be empty");
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final Junction junction = (Junction) o;

        return new EqualsBuilder().append(position, junction.position).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(position).toHashCode();
    }
}
