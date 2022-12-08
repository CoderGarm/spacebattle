package de.yuga.spacebattle.backend.dto.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.turn.Move;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FleetMovement {

    @Nonnull
    private final Tick today;

    @Nonnull
    private final Fleet fleet;

    @Nonnull
    private final StarSystem originSystem;

    @Nullable
    private final Planet originPlanet;

    @Nonnull
    private final StarSystem destinationSystem;

    @Nullable
    private final Planet destinationPlanet;

    private final int originalDuration;

    private final boolean isForeignFleet;

    public FleetMovement(@Nonnull final Tick today,
                         @Nonnull final Fleet fleet,
                         @Nonnull final Planet originPlanet,
                         @Nonnull final Planet destinationPlanet,
                         @Nonnull final Move move) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(fleet, "fleet must not be empty");
        Preconditions.checkNotNull(originPlanet, "origin must not be empty");
        Preconditions.checkNotNull(destinationPlanet, "destination must not be empty");
        Preconditions.checkNotNull(move, "move must not be empty");

        this.today = today;
        this.fleet = fleet;
        this.originPlanet = originPlanet;
        this.originSystem = originPlanet.getSystem();
        this.destinationPlanet = destinationPlanet;
        this.destinationSystem = destinationPlanet.getSystem();
        this.originalDuration = move.getOriginalDuration();
        this.isForeignFleet = fleet.getOwner().equals(destinationPlanet.getOwner());
    }

    public FleetMovement(@Nonnull final Tick today,
                         @Nonnull final Fleet fleet,
                         @Nullable final Planet originPlanet,
                         @Nonnull final StarSystem originSystem,
                         @Nonnull final StarSystem destinationSystem,
                         @Nonnull final Move move,
                         final boolean isForeignFleet) {

        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(fleet, "fleet must not be empty");
        Preconditions.checkNotNull(originSystem, "originSystem must not be empty");
        Preconditions.checkNotNull(destinationSystem, "destinationSystem must not be empty");
        Preconditions.checkNotNull(move, "move must not be empty");

        this.today = today;
        this.fleet = fleet;
        this.originPlanet = originPlanet;
        this.originSystem = originSystem;
        this.destinationPlanet = null;
        this.destinationSystem = destinationSystem;
        this.originalDuration = move.getOriginalDuration();
        this.isForeignFleet = isForeignFleet;
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
    public StarSystem getOriginSystem() {
        return originSystem;
    }

    @Nullable
    public Planet getOriginPlanet() {
        return originPlanet;
    }

    @Nonnull
    public StarSystem getDestinationSystem() {
        return destinationSystem;
    }

    @Nullable
    public Planet getDestinationPlanet() {
        return destinationPlanet;
    }

    public int getOriginalDuration() {
        return originalDuration;
    }

    public boolean isForeignFleet() {
        return isForeignFleet;
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
