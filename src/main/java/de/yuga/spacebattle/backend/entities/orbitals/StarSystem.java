package de.yuga.spacebattle.backend.entities.orbitals;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.enums.EDistanceMetric;
import de.yuga.spacebattle.backend.enums.EStarClassType;

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
        @NamedQuery(name = "StarSystem.getAll", query = "SELECT s FROM StarSystem s")
})
@Entity
@Table(name = "starSystem",
        uniqueConstraints = @UniqueConstraint(name = "COORDINATE_UK", columnNames = {"xCoordinate", "yCoordinate"}))
@AttributeOverride(name = "id", column = @Column(name = "idStarSystem"))
public class StarSystem extends AbstractEntityKey {

    @Nonnull
    @Transient
    public static final EDistanceMetric STAR_SYSTEM_STANDARD_METRIC = EDistanceMetric.LY;

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

    @Nonnull
    @OneToMany(mappedBy = "orbit.system", fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    private Set<Fleet> fleets = new HashSet<>();

    @Nonnull
    @Transient
    private final EStarClassType starClassType = EStarClassType.CLASS_G3;

    public StarSystem() {
    }

    public StarSystem(@Nonnull final String name, @Nonnull final Orbit orbit) {
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

    @Nonnull
    public Set<Fleet> getFleets() {
        return fleets;
    }

    public void setFleets(@Nonnull final Set<Fleet> fleets) {
        Preconditions.checkNotNull(fleets, "fleets shouldn't be null!");

        this.fleets = fleets;
    }

    public void setOrbit(@Nonnull final Orbit orbit) {
        Preconditions.checkNotNull(orbit, "orbit shouldn't be null!");

        this.orbit = orbit;
    }

    @Nonnull
    public EStarClassType getStarClassType() {
        return starClassType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StarSystem)) return false;

        StarSystem that = (StarSystem) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id * 31;
    }
}
