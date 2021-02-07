package de.yuga.spacebattle.entities.orbitals;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.entities.AbstractEntityKey;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * The star system which every action founds a place.
 */
@NamedQueries({
        @NamedQuery(name = "Starsystem.getAll", query = "SELECT p FROM Starsystem p")
})
@Entity
@Table(name = "starsystem", uniqueConstraints = @UniqueConstraint(columnNames = {"xCoordinate", "yCoordinate"}))
@AttributeOverride(name = "id", column = @Column(name = "idStarsystem"))
public class Starsystem extends AbstractEntityKey {

    @Nonnull
    @Embedded
    private Orbit orbit;

    /**
     * The planets in this star system.
     */
    @Nonnull
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinTable(name = "systemcomposition",
            joinColumns = @JoinColumn(name = "idPlanet"),
            inverseJoinColumns = @JoinColumn(name = "idStarsystem"))
    @Size(max = 20)
    private final List<Planet> planets = new ArrayList<>();

    public Starsystem() {
    }

    public Starsystem(@Nonnull final Orbit orbit) {
        Preconditions.checkNotNull(orbit, "orbit shouldn't be null!");

        this.orbit = orbit;
    }

    @Nonnull
    public List<Planet> getPlanets() {
        return planets;
    }

    @Nonnull
    public Orbit getOrbit() {
        return orbit;
    }

    public void setOrbit(@Nonnull final Orbit orbit) {
        Preconditions.checkNotNull(orbit, "orbit shouldn't be null!");

        this.orbit = orbit;
    }
}
