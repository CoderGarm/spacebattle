package de.yuga.spacebattle.backend.entities.orbitals;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.enums.EStarClassType;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

/**
 * The star system which every action founds a place.
 */
@NamedQueries({
        @NamedQuery(name = "StarSystem.getAll", query = "SELECT s FROM StarSystem s"),
        @NamedQuery(name = "StarSystem.getAllColonizable", query = "SELECT DISTINCT s FROM StarSystem s LEFT JOIN s.planets p WHERE p.owner IS NULL"),
        @NamedQuery(name = "StarSystem.getAllColonized", query = "SELECT DISTINCT s FROM StarSystem s LEFT JOIN s.planets p WHERE p.owner IS NOT NULL"),
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
    @Column
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

    public void setName(@Nonnull final String name) {
        Preconditions.checkNotNull(name, "name must not be empty");

        this.name = name;
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
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .toString();
    }

    public static boolean equalsAtMap(@Nullable final StarSystem o1, @Nullable final StarSystem o2) {

        if (o1 != null && o2 == null) {
            return false;
        }
        if (o1 == null && o2 != null) {
            return false;
        }
        //noinspection ConstantValue
        if (o1 == null && o2 == null) {
            return false;
        }

        return o1.equals(o2);
    }

    @Override
    public int hashCode() {
        return id * 31;
    }
}
