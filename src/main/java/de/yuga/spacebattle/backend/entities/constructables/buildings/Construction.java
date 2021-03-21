package de.yuga.spacebattle.backend.entities.constructables.buildings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.ResourceDeposit;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Job;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@NamedQueries({
        @NamedQuery(name = "Construction.getAll", query = "SELECT a FROM Construction a")
})
@Entity
@Table(name = "construction",
        uniqueConstraints = @UniqueConstraint(name = "CONSTRUCTION_UK", columnNames = {"idPlanet", "idBuilding"}))
@AttributeOverride(name = "id", column = @Column(name = "idConstruction"))
public class Construction extends AbstractEntityKey {

    @JsonIgnore
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

    private int level = 1;

    @Nullable
    @OneToOne(mappedBy = "facility", orphanRemoval = true)
//, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    private Job job;

    public Construction() {
    }

    public Construction(@Nonnull final Planet planet, @Nonnull final Building building, final int level) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");
        Preconditions.checkNotNull(building, "building shouldn't be null!");

        this.planet = planet;
        this.building = building;
        this.level = level;
    }

    /**
     * Calculates the tickly output of this construction.
     *
     * @param resourceFactorByPlanet the planets opportunity to produce
     * @return the tickly production
     */
    public BigDecimal getTickOutput(@Nonnull final BigDecimal resourceFactorByPlanet) {
        Preconditions.checkNotNull(resourceFactorByPlanet, "resourceFactorByPlanet shouldn't be null!");

        BigDecimal increasingFactorPerLevel = building.getIncreasingFactorPerLevel();
        int baseValue = building.getBaseValue();
        BigDecimal result = new BigDecimal(baseValue).add(increasingFactorPerLevel).multiply(new BigDecimal(level));
        return result.multiply(
                resourceFactorByPlanet.divide(BigDecimal.TEN.movePointRight(1), ResourceDeposit.mathContext));
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
            throw new NotifySBUserException("You cannot reduce the level of a construction");
        }
        this.level = level;
    }

    @Nullable
    public Job getJob() {
        return job;
    }

    public void setJob(@Nullable Job job) {
        this.job = job;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Construction)) return false;

        Construction that = (Construction) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id * 33;
    }
}
