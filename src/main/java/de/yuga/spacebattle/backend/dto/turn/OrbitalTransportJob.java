package de.yuga.spacebattle.backend.dto.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.enums.ETransportType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class OrbitalTransportJob {

    @Nonnull
    private final Tick today;

    @Nonnull
    private final Planet planet;

    @Nonnull
    private final Fleet fleet;

    @Nonnull
    private final ETransportType transportType;

    @Nonnull
    private final Map<EResourceType, Long> resources = new HashMap<>();

    @Nonnull
    private final Map<EEducationType, Long> humanResources = new HashMap<>();

    public OrbitalTransportJob(@Nonnull final Tick today,
                               @Nonnull final Planet planet,
                               @Nonnull final Fleet fleet,
                               @Nonnull final ETransportType transportType) {
        Preconditions.checkNotNull(today, "today must not be empty");
        Preconditions.checkNotNull(planet, "from must not be empty");
        Preconditions.checkNotNull(fleet, "to must not be empty");
        Preconditions.checkNotNull(transportType, "transportType must not be empty");

        this.today = today;
        this.planet = planet;
        this.fleet = fleet;
        this.transportType = transportType;
    }

    public void add(@Nonnull final EResourceType what,
                    final long amount) {
        Preconditions.checkNotNull(what, "what must not be empty");

        this.resources.put(what, amount);
    }

    public void add(@Nonnull final EEducationType what,
                    final long amount) {
        Preconditions.checkNotNull(what, "what must not be empty");

        this.humanResources.put(what, amount);
    }

    @Nonnull
    public Tick getToday() {
        return today;
    }

    @Nonnull
    public Planet getPlanet() {
        return planet;
    }

    @Nonnull
    public Fleet getFleet() {
        return fleet;
    }

    @Nonnull
    public ETransportType getTransportType() {
        return transportType;
    }

    @Nonnull
    public Map<EResourceType, Long> getResources() {
        return resources;
    }

    @Nonnull
    public Map<EEducationType, Long> getHumanResources() {
        return humanResources;
    }

    public boolean isToday(@Nonnull final Tick tick) {
        Preconditions.checkNotNull(tick, "tick must not be empty");

        return today.equals(tick);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final OrbitalTransportJob that = (OrbitalTransportJob) o;

        return new EqualsBuilder().append(today, that.today).append(planet, that.planet).append(fleet, that.fleet).append(transportType, that.transportType).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(today).append(planet).append(fleet).append(transportType).toHashCode();
    }
}
