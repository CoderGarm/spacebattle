package de.yuga.spacebattle.backend.dto.physics;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;

import javax.annotation.Nonnull;

public class OrbitalDistanceMarker {

    @Nonnull
    private final Orbit first;

    @Nonnull
    private final Orbit second;

    @Nonnull
    private final Distance distance;

    public OrbitalDistanceMarker(@Nonnull final Orbit first, @Nonnull final Orbit second) {
        this.first = Preconditions.checkNotNull(first, "first must not be empty");
        this.second = Preconditions.checkNotNull(second, "second must not be empty");
        this.distance = first.getDistance(second);
    }

    @Nonnull
    public Orbit getFirst() {
        return first;
    }

    @Nonnull
    public Orbit getSecond() {
        return second;
    }

    @Nonnull
    public Distance getDistance() {
        return distance;
    }
}
