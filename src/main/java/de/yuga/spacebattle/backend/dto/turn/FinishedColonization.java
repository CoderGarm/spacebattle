package de.yuga.spacebattle.backend.dto.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;

public class FinishedColonization {

    @Nonnull
    private final Tick today;

    @Nonnull
    private final Planet planet;

    public FinishedColonization(@Nonnull final Tick today,
                                @Nonnull final Planet planet) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");

        this.today = today;
        this.planet = planet;
    }

    @Nonnull
    public Tick getToday() {
        return today;
    }

    @Nonnull
    public Planet getPlanet() {
        return planet;
    }

    public boolean isToday(@Nonnull final Tick tick) {
        Preconditions.checkNotNull(tick, "tick must not be empty");

        return today.equals(tick);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final FinishedColonization that = (FinishedColonization) o;

        return new EqualsBuilder().append(today, that.today).append(planet, that.planet).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(today).append(planet).toHashCode();
    }
}
