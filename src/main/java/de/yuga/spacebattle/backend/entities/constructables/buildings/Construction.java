package de.yuga.spacebattle.backend.entities.constructables.buildings;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.misc.Deletable;
import de.yuga.spacebattle.backend.entities.misc.LeveledOperationable;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Job;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "construction",
        uniqueConstraints = @UniqueConstraint(name = "CONSTRUCTION_UK", columnNames = {"idPlanet", "idBuilding"}))
@AttributeOverride(name = "id", column = @Column(name = "idConstruction"))
public class Construction extends LeveledOperationable {

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

    @Nonnull
    public Set<Job> getJobs() {
        return jobs.stream().filter(Deletable::isAlive).collect(Collectors.toSet());
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
