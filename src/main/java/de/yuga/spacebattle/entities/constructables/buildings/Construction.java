package de.yuga.spacebattle.entities.constructables.buildings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.entities.AbstractEntityKey;
import de.yuga.spacebattle.entities.buildings.Building;
import de.yuga.spacebattle.entities.orbitals.Planet;
import de.yuga.spacebattle.entities.turn.Job;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@NamedQueries({
        @NamedQuery(name = "Construction.getAll", query = "SELECT a FROM Construction a")
})
@Entity
@Table(name = "construction", uniqueConstraints = @UniqueConstraint(columnNames = {"idPlanet", "idBuilding"}))
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
    @OneToOne(mappedBy = "facility")
    //@JoinColumn(name = "idJob")
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
     * @param rescourceFactorByPlanet the planets opportunity to produce
     * @return the tickly production
     */
    public BigDecimal getTickOutput(@Nonnull final BigDecimal rescourceFactorByPlanet) {
        Preconditions.checkNotNull(rescourceFactorByPlanet, "rescourceFactorByPlanet shouldn't be null!");

        BigDecimal increasmentFactorPerLevel = building.getIncreasmentFactorPerLevel();
        int baseValue = building.getBaseValue();
        BigDecimal result = new BigDecimal(baseValue).add(increasmentFactorPerLevel).multiply(new BigDecimal(level));
        return result.multiply(rescourceFactorByPlanet);
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

    /**
     * Returns the costs for this construction.
     *
     * @return the costs
     *//*
    public Map<EResourceType, BigDecimal> getJobCosts() {
        ResourceDeposit costs = building.getCosts();
        Map<EResourceType, BigDecimal> resources = new HashMap<>(costs.getResources());
        for (EResourceType resourceType : resources.keySet()) {
            BigDecimal resourceAmountByType = costs.getResourceAmountByType(resourceType);
            costs.updateResource(resourceType, resourceAmountByType.multiply(new BigDecimal(level)));
        }
        return resources;
    }*/
}
