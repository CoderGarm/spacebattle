package de.yuga.spacebattle.backend.entities.buildings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.ResourceDeposit;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.enums.EResourceSubType;
import de.yuga.spacebattle.backend.enums.EResourceType;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@NamedQueries({
        @NamedQuery(name = "Building.getAll", query = "SELECT p FROM Building p")
})
@Entity
@Table(name = "building")
@AttributeOverride(name = "id", column = @Column(name = "idBuilding"))
public class Building extends AbstractEntityKey {

    @Nonnull
    @Size(min = 1, max = 30)
    private String name;

    @Nonnull
    private String description;

    private int baseValue;

    private final BigDecimal increasingFactorPerLevel = new BigDecimal("0.2");

    /**
     * what is this building producing
     */
    @Nonnull
    @Enumerated(EnumType.STRING)
    private EResourceType resourceType;

    @Nonnull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "idCosts", updatable = false)
    private final ResourceDeposit costs = new ResourceDeposit(EResourceSubType.COSTS);

    @JsonIgnore
    @Nonnull
    @NotNull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "idResearch")
    private Research unlockedThrough;

    public Building() {
    }

    public Building(@Nonnull final String name,
                    @Nonnull final String description,
                    final int baseValue,
                    @Nonnull final EResourceType resourceType,
                    @Nonnull final Research unlockedThrough) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(resourceType, "resourceType shouldn't be null!");
        Preconditions.checkNotNull(unlockedThrough, "unlockedThrough shouldn't be null!");

        this.name = name;
        this.description = description;
        this.baseValue = baseValue;
        this.resourceType = resourceType;
        this.unlockedThrough = unlockedThrough;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public String getDescription() {
        return description;
    }

    @Nonnull
    public int getBaseValue() {
        return baseValue;
    }

    @Nonnull
    public BigDecimal getIncreasingFactorPerLevel() {
        return increasingFactorPerLevel;
    }

    @Nonnull
    public EResourceType getResourceType() {
        return resourceType;
    }

    @Nonnull
    public ResourceDeposit getCosts() {
        return costs;
    }

    @Nonnull
    public Research getUnlockedThrough() {
        return unlockedThrough;
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
        int result = 31 * id;
        return result;
    }
}
