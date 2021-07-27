package de.yuga.spacebattle.backend.entities.spacecrafts;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.resource.ResourceDepositInitializerCalculator;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Weapon;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModuleWithEffectValue;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.EHullType;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;
import org.hibernate.annotations.Check;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@NamedQueries({
        @NamedQuery(name = "Hull.getAll", query = "SELECT a FROM Hull a"),
        @NamedQuery(name = "Hull.getAllByResearches", query = "SELECT a FROM Hull a WHERE a.unlockedThrough IN (:researches) OR a.unlockedThrough IS NULL")
})
@Entity
@Table(name = "hull")
@Check(constraints = "overallConstructionCapacity >= constructionCapacity + constructionCapacityBow + constructionCapacityStern + constructionCapacityBroadsides")
@AttributeOverride(name = "id", column = @Column(name = "idHull"))
public class Hull extends AbstractEntityKey {

    @Nonnull
    @NotNull
    @Size(min = 1, max = 30)
    private String name;

    /**
     * The overall CC represents the size of a hull in metric tons.
     */
    private int overallConstructionCapacity;

    /**
     * This is used by all other {@link BaseModuleWithEffectValue}s which has no {@link EWeaponAlignment}.
     */
    private int constructionCapacity;

    /**
     * This is used by {@link Weapon}s which has {@link EWeaponAlignment#BOW}.
     */
    private int constructionCapacityBow;

    /**
     * This is used by {@link Weapon}s which has {@link EWeaponAlignment#STERN}.
     */
    private int constructionCapacityStern;

    /**
     * This is used by {@link Weapon}s which has {@link EWeaponAlignment#BROADSIDE}.
     */
    private int constructionCapacityBroadsides;

    @Nonnull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "idCosts", updatable = false)
    private final ResourceDeposit costs = ResourceDepositInitializerCalculator.initializeResourceDeposit(Hull.class, EDepositType.COSTS);

    @Nonnull
    @NotNull
    private String description;

    @Nonnull
    @NotNull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "idResearch")
    private Research unlockedThrough;


    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private EHullType hullType;

    public Hull() {
    }

    public Hull(@Nonnull final String name,
                final int overallConstructionCapacity,
                final int constructionCapacity,
                final int constructionCapacityBow,
                final int constructionCapacityStern,
                int constructionCapacityBroadsides,
                @Nonnull final String description,
                @Nonnull final Research unlockedThrough,
                @Nonnull final EHullType hullType,
                @Nonnull final CrewRequirement crewRequirement) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(unlockedThrough, "unlockedThrough shouldn't be null!");
        Preconditions.checkNotNull(hullType, "hullType shouldn't be null!");
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");

        this.name = name;
        this.overallConstructionCapacity = overallConstructionCapacity;
        this.constructionCapacity = constructionCapacity;
        this.constructionCapacityBow = constructionCapacityBow;
        this.constructionCapacityStern = constructionCapacityStern;
        this.constructionCapacityBroadsides = constructionCapacityBroadsides;
        this.description = description;
        this.unlockedThrough = unlockedThrough;
        this.hullType = hullType;
        this.costs.setCrewRequirement(crewRequirement);
    }

    @Nonnull
    public String getName() {
        return name;
    }

    public int getOverallConstructionCapacity() {
        return overallConstructionCapacity;
    }

    public int getConstructionCapacity() {
        return constructionCapacity;
    }

    public int getConstructionCapacityBow() {
        return constructionCapacityBow;
    }

    public int getConstructionCapacityStern() {
        return constructionCapacityStern;
    }

    public int getConstructionCapacityBroadsides() {
        return constructionCapacityBroadsides;
    }

    @Nonnull
    public ResourceDeposit getCosts() {
        return costs;
    }

    @Nonnull
    public String getDescription() {
        return description + " hull type: " + hullType.getDescription();
    }

    @Nonnull
    public Research getUnlockedThrough() {
        return unlockedThrough;
    }

    @Nonnull
    public EHullType getHullType() {
        return hullType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hull)) return false;

        Hull hull = (Hull) o;

        return id == hull.getId();
    }

    @Override
    public int hashCode() {
        return id;
    }
}
