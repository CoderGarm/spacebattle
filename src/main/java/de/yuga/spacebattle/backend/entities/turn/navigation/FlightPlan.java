package de.yuga.spacebattle.backend.entities.turn.navigation;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.turn.Move;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.persistence.*;

@Embeddable
public class FlightPlan {

    @Nonnull
    @ManyToOne
    @JoinColumn(name = "idMove")
    private Move move;

    @OrderColumn
    @Column(columnDefinition = "decimal(3, 0)")
    private int timeAfterStart;

    @Nonnull
    @Embedded // fixme Could not determine type for: de.yuga.spacebattle.backend.dto.physics.Distance, at table:
    private FleetOrbit location;

    public FlightPlan() {
    }

    public FlightPlan(@Nonnull final Move move, @Nonnull final FleetOrbit location, final int timeAfterStart) {
        this.move = Preconditions.checkNotNull(move, "move must not be empty");
        this.location = Preconditions.checkNotNull(location, "location must not be empty");
        this.timeAfterStart = timeAfterStart;
    }

    @Nonnull
    public Move getMove() {
        return move;
    }

    public int getTimeAfterStart() {
        return timeAfterStart;
    }

    @Nonnull
    public FleetOrbit getLocation() {
        return location;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final FlightPlan that = (FlightPlan) o;

        return new EqualsBuilder().append(move, that.move).append(timeAfterStart, that.timeAfterStart).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(move).append(timeAfterStart).toHashCode();
    }
}
