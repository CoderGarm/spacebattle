package de.yuga.spacebattle.backend.entities.buildings;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.entities.i18n.Translation;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.turn.resources.HasCosts;
import de.yuga.spacebattle.backend.enums.EBuildingType;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.enums.ETechLevel;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@NamedQueries({
        @NamedQuery(name = "Building.getAll", query = "SELECT p FROM Building p"),
        @NamedQuery(name = "Building.getByResourceType", query = "SELECT p FROM Building p WHERE p.productionType.productionTarget = :productionTarget")
})
@Entity
@Table(name = "building")
@AttributeOverride(name = "id", column = @Column(name = "idBuilding"))
// todo check constraint for productionType.productionCategory and refinementSequence
// todo check constraint for productionType.productionCategory == PRODUCE and productionType.productionTarget == POPULATION must have baseValue with single digit only
public class Building extends HasCosts {

    /**
     * The basic effect value at level 1.
     */
    private int baseValue;

    /**
     * The increasement of value for the next level. todo
     */
    private final BigDecimal increasingFactorPerLevel = new BigDecimal("0.2");

    /**
     * Defines the job of this building.
     */
    @Nonnull
    @NotNull
    @Embedded
    private ProductionType productionType;

    @Nonnull
    @NotNull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "idResearch")
    private Research unlockedThrough;

    @Nonnull
    @Transient
    private final EBuildingType buildingType = EBuildingType.BUILDING;

    public Building() {
    }

    public Building(@Nonnull final String name,
                    @Nonnull final String description,
                    final int baseValue,
                    @Nonnull final ETechLevel techLevel,
                    @Nonnull final ProductionType productionType,
                    @Nonnull final CrewRequirement crewRequirement,
                    @Nonnull final Research unlockedThrough) {
        super(new Translation("en", name), new Translation("en", description), techLevel, Building.class);
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(productionType, "productionType shouldn't be null!");
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");
        Preconditions.checkNotNull(unlockedThrough, "unlockedThrough shouldn't be null!");

        this.baseValue = baseValue;
        this.productionType = productionType;
        this.getCosts().setCrewRequirement(crewRequirement);
        this.unlockedThrough = unlockedThrough;
    }

    public int getBaseValue() {
        return baseValue;
    }

    @Nonnull
    public BigDecimal getIncreasingFactorPerLevel() {
        return increasingFactorPerLevel;
    }

    @Nonnull
    public EResourceType getProductionTarget() {
        return productionType.getProductionTarget();
    }

    @Nonnull
    public ProductionType getProductionType() {
        return productionType;
    }

    @Nonnull
    public Research getUnlockedThrough() {
        return unlockedThrough;
    }

    @Nonnull
    public EBuildingType getBuildingType() {
        return buildingType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Building)) return false;

        Building building = (Building) o;

        return getId() == building.getId();
    }

    @Override
    public int hashCode() {
        return 31 * id;
    }
}
