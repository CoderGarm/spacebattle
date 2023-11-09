package de.yuga.spacebattle.backend.entities.orbitals;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;

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

        if (orbit == null && planet == null) {
            throw new NotifyWebUserException("You should relly select a destination.");
        }

        this.planet = planet;
        if (this.planet == null) {
            this.orbit = orbit != null ? orbit.clone() : null;
        }
        this.system = system;
    }

    public FleetOrbit(@Nullable final Orbit orbit, @Nullable final StarSystem system) {
        Preconditions.checkNotNull(orbit, "orbit must not be empty");
        Preconditions.checkNotNull(system, "system must not be empty");

        this.orbit = orbit.clone();
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FleetOrbit)) return false;

        FleetOrbit that = (FleetOrbit) o;

        if (system != null ? !system.equals(that.system) : that.system != null) return false;
        return orbit != null ? orbit.equals(that.orbit) : that.orbit == null;
    }

    @Override
    public int hashCode() {
        int result = system != null ? system.hashCode() : 0;
        result = 31 * result + (orbit != null ? orbit.hashCode() : 0);
        return result;
    }
}
