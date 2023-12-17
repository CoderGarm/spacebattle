package de.yuga.spacebattle.backend.entities.orbitals;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class FleetOrbit {

    /**
     * If the system is null the orbit is placed in the hyper space.<br>
     * Otherwise, the inertial system of the orbit is the defined system.
     */
    @Nullable
    @ManyToOne
    @JoinColumn(name = "idStarSystem", referencedColumnName = "idStarSystem")
    private StarSystem system;

    @Nullable
    @ManyToOne
    @JoinColumn(name = "idPlanet", referencedColumnName = "idPlanet")
    private Planet planet;

    /**
     * The orbit could be null if the fleet is currently on a local movement.
     */
    @Nullable
    @Embedded
    private Orbit orbit;

    public FleetOrbit() {
    }

    public FleetOrbit(@Nullable final Orbit orbit, @Nullable final Planet planet, @Nullable final StarSystem system) {

        this.planet = planet;
        if (this.planet == null) {
            this.orbit = orbit != null ? orbit.clone() : null;
        }
        this.system = system;
    }

    public FleetOrbit(@Nullable final Orbit orbit, @Nullable final StarSystem system) {

        this.orbit = orbit != null ? orbit.clone() : null;
        this.system = system;
    }

    public FleetOrbit(@Nonnull final FleetOrbit orbit) {
        Preconditions.checkNotNull(orbit, "orbit shouldn't be null!");

        this.planet = orbit.getPlanet();
        if (this.planet == null) {
            this.orbit = orbit.getOrbit() != null ? orbit.getOrbit().clone() : null;
        }
        this.system = orbit.getSystem();
    }

    public FleetOrbit(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet must not be empty");

        this.planet = planet;
        this.system = planet.getSystem();
    }

    @Nullable
    public StarSystem getSystem() {
        return system;
    }

    @Nullable
    public Planet getPlanet() {
        return planet;
    }

    @Nullable
    public Orbit getOrbit() {
        return orbit;
    }

    @Nullable
    public Orbit getResultingOrbit() {
        if (planet != null) {
            return planet.getOrbit();
        }
        return orbit;
    }

    /**
     * In case of starting a movement but stay in the system, the planet has to be null.
     */
    public void leavePlanet() {
        planet = null;
        orbit = null;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final FleetOrbit that = (FleetOrbit) o;

        final Orbit o1 = getExplicitOrbit(this);
        final Orbit o2 = getExplicitOrbit(that);

        return new EqualsBuilder().append(system, that.system).append(o1, o2).isEquals();
    }

    @Nullable
    private static Orbit getExplicitOrbit(@Nonnull final FleetOrbit fleetOrbit) {
        Preconditions.checkNotNull(fleetOrbit, "fleetOrbit must not be empty");

        if (fleetOrbit.planet != null && fleetOrbit.orbit == null) {
            return fleetOrbit.planet.getOrbit();
        } else {
            return fleetOrbit.orbit;
        }
    }

    @Override
    public int hashCode() {
        final Orbit o1 = getExplicitOrbit(this);
        return new HashCodeBuilder(17, 37).append(system).append(o1).toHashCode();
    }
}
