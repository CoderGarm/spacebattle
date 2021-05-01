package de.yuga.spacebattle.backend.entities.orbitals;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Embeddable
public class FleetOrbit {

    @Nonnull
    @ManyToOne
    @JoinColumn(name = "idStarsystem", referencedColumnName = "idStarsystem")
    private StarSystem system;

    @Nonnull
    @OneToOne
    @JoinColumn(name = "idPlanet", referencedColumnName = "idPlanet")
    private Planet planet;

    public FleetOrbit() {
    }

    public FleetOrbit(@Nonnull final StarSystem system, @Nonnull final Planet planet) {
        Preconditions.checkNotNull(system, "system shouldn't be null!");
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        this.system = system;
        this.planet = planet;
    }

    @Nonnull
    public StarSystem getSystem() {
        return system;
    }

    public void setSystem(@Nonnull StarSystem system) {
        this.system = system;
    }

    @Nonnull
    public Planet getPlanet() {
        return planet;
    }

    public void setPlanet(@Nonnull Planet planet) {
        this.planet = planet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FleetOrbit)) return false;

        FleetOrbit that = (FleetOrbit) o;

        if (!system.equals(that.system)) return false;
        return planet.equals(that.planet);
    }

    @Override
    public int hashCode() {
        int result = system.hashCode();
        result = 31 * result + planet.hashCode();
        return result;
    }
}
