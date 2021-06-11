package de.yuga.spacebattle.backend.entities.orbitals;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Embeddable
public class FleetOrbit {

    @Nonnull
    @ManyToOne
    @JoinColumn(name = "idStarSystem", referencedColumnName = "idStarSystem")
    private StarSystem system;

    @Nullable
    @OneToOne
    @JoinColumn(name = "idPlanet", referencedColumnName = "idPlanet")
    private Planet planet;

    public FleetOrbit() {
    }

    public FleetOrbit(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        this.system = planet.getSystem();
        this.planet = planet;
    }

    /**
     * If the orbit defined a sojourn not in a planetary orbit but in a star system.
     *
     * @param system
     */
    public FleetOrbit(@Nonnull final StarSystem system) {
        Preconditions.checkNotNull(system, "system shouldn't be null!");

        this.system = system;
    }

    @Nonnull
    public StarSystem getSystem() {
        return system;
    }

    public void setSystem(@Nonnull StarSystem system) {
        this.system = system;
    }

    @Nullable
    public Planet getPlanet() {
        return planet;
    }

    public void setPlanet(@Nullable Planet planet) {
        this.planet = planet;
    }

    /**
     * In case of starting a movement but stay in the system, the planet has to be null.
     */
    public void leavePlanet() {
        this.planet = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FleetOrbit)) return false;

        FleetOrbit that = (FleetOrbit) o;

        if (!system.equals(that.system)) return false;
        return planet != null ? planet.equals(that.planet) : that.planet == null;
    }

    @Override
    public int hashCode() {
        int result = system.hashCode();
        result = 31 * result + (planet != null ? planet.hashCode() : 0);
        return result;
    }

}
