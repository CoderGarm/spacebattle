package de.yuga.spacebattle.backend.dto.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Move;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;

public class FleetMovement {

    @Nonnull
    private final Tick today;

    @Nonnull
    private final Fleet fleet;

    @Nonnull
    private final Planet origin;

    @Nonnull
    private final Planet destination;

    private final int originalDuration;

    public FleetMovement(@Nonnull final Tick today,
                         @Nonnull final Fleet fleet,
                         @Nonnull final Planet origin,
                         @Nonnull final Planet destination,
                         @Nonnull final Move move) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(fleet, "fleet must not be empty");
        Preconditions.checkNotNull(origin, "origin must not be empty");
        Preconditions.checkNotNull(destination, "destination must not be empty");
        Preconditions.checkNotNull(move, "move must not be empty");

        this.today = today;
        this.fleet = fleet;
        this.origin = origin;
        this.destination = destination;
        this.originalDuration = move.getOriginalDuration();
    }

    @Nonnull
    public Tick getToday() {
        return today;
    }

    @Nonnull
    public Fleet getFleet() {
        return fleet;
    }

    @Nonnull
    public Planet getOrigin() {
        return origin;
    }

    @Nonnull
    public Planet getDestination() {
        return destination;
    }

    public int getOriginalDuration() {
        return originalDuration;
    }

    public boolean isToday(@Nonnull final Tick tick) {
        Preconditions.checkNotNull(tick, "tick must not be empty");

        return today.equals(tick);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final FleetMovement that = (FleetMovement) o;

        return new EqualsBuilder().append(today, that.today).append(fleet, that.fleet).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(today).append(fleet).toHashCode();
    }
}
