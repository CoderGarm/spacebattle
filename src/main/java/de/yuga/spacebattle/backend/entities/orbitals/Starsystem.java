package de.yuga.spacebattle.backend.entities.orbitals;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

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
    @NotNull
    @Column(updatable = false)
    private String name;

    @Nonnull
    @Embedded
    private Orbit orbit;

    /**
     * The planets in this star system.
     */
    @Nonnull
    @Size(max = 20)
    @OneToMany(mappedBy = "system", fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    private final Set<Planet> planets = new HashSet<>();

    public Starsystem() {
    }

    public Starsystem(@Nonnull final String name, @Nonnull final Orbit orbit) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(orbit, "orbit shouldn't be null!");

        this.name = name;
        this.orbit = orbit;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public Set<Planet> getPlanets() {
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
