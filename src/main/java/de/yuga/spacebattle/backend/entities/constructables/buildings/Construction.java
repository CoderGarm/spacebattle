package de.yuga.spacebattle.backend.entities.constructables.buildings;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@NamedQueries({
        @NamedQuery(name = "Construction.getAll", query = "SELECT a FROM Construction a"),
        @NamedQuery(name = "Construction.getAllByPlanet", query = "SELECT a FROM Construction a WHERE a.planet.id = :idPlanet"),
})
@Entity
@Table(name = "construction",
        uniqueConstraints = @UniqueConstraint(name = "CONSTRUCTION_UK", columnNames = {"idPlanet", "idBuilding"}))
@AttributeOverride(name = "id", column = @Column(name = "idConstruction"))
public class Construction extends AbstractEntityKey {

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idPlanet", updatable = false)
    private Planet planet;

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idBuilding", updatable = false)
    private Building building;

    private int level;

    @Nonnull
    @OneToMany(mappedBy = "facility", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<Job> jobs = new HashSet<>();

    public Construction() {
    }

    public Construction(@Nonnull final Planet planet, @Nonnull final Building building, final int level) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");
        Preconditions.checkNotNull(building, "building shouldn't be null!");

        this.planet = planet;
        this.building = building;
        this.level = level;
    }

    @Nonnull
    public Planet getPlanet() {
        return planet;
    }

    @Nonnull
    public Building getBuilding() {
        return building;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(final int level) {
        if (level <= this.level) {
            throw new NotifyWebUserException("You cannot reduce the level of a construction");
        }
        this.level = level;
    }

    @Nonnull
    public Set<Job> getJobs() {
        return jobs;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Construction)) return false;

        Construction that = (Construction) o;

        if (!planet.equals(that.planet)) return false;
        return building.equals(that.building);
    }

    @Override
    public int hashCode() {
        return id * 31;
    }
}
