package de.yuga.spacebattle.backend.dto.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Commissioning {

    @Nonnull
    private final Tick today;

    @Nonnull
    private final Planet planet;

    @Nonnull
    private Set<Construction> constructions = new HashSet<>();

    @Nonnull
    private List<WarShip> warships = new ArrayList<>();

    private Commissioning(@Nonnull final Tick today,
                          @Nonnull final Planet planet) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(planet, "planet must not be empty");

        this.today = today;
        this.planet = planet;
    }

    public Commissioning(@Nonnull final Tick today,
                         @Nonnull final Planet planet,
                         @Nonnull final Set<Construction> operationals) {
        this(today, planet);

        this.constructions = Preconditions.checkNotNull(operationals, "operationals must not be empty");
    }

    public Commissioning(@Nonnull final Tick today,
                         @Nonnull final Planet planet,
                         @Nonnull final List<WarShip> warShips) {
        this(today, planet);

        this.warships = Preconditions.checkNotNull(warShips, "warShips must not be empty");
    }

    @Nonnull
    public Tick getToday() {
        return today;
    }

    @Nonnull
    public Planet getPlanet() {
        return planet;
    }

    public void addConstructions(@Nonnull final Set<Construction> operationals) {
        Preconditions.checkNotNull(operationals, "operationals must not be empty");

        operationals.forEach(operational -> {
            final Construction known = this.constructions.stream().filter(c -> c.getOperationalLevel() < operational.getOperationalLevel()).findFirst().orElse(null);
            if (known != null) {
                this.constructions.remove(known);
                this.constructions.add(operational);
            }
        });
    }

    @Nonnull
    public Set<Construction> getConstructions() {
        return constructions;
    }

    public void setWarships(@Nonnull final List<WarShip> warShips) {
        this.warships = Preconditions.checkNotNull(warShips, "warShips must not be empty");
    }

    @Nonnull
    public List<WarShip> getWarships() {
        return warships;
    }

    public boolean isToday(@Nonnull final Tick tick) {
        Preconditions.checkNotNull(tick, "tick must not be empty");

        return today.equals(tick);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final Commissioning that = (Commissioning) o;

        return new EqualsBuilder().append(today, that.today).append(planet, that.planet).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(today).append(planet).toHashCode();
    }
}
