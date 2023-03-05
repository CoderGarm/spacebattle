package de.yuga.spacebattle.backend.entities.spacecrafts;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.entities.i18n.Translation;
import de.yuga.spacebattle.backend.entities.misc.HasCosts;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Weapon;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModuleWithEffectValue;
import de.yuga.spacebattle.backend.enums.ECapacityAreaType;
import de.yuga.spacebattle.backend.enums.EHullType;
import de.yuga.spacebattle.backend.enums.ETechLevel;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;
import de.yuga.spacebattle.backend.services.MasterOfTheUniverseService;
import org.hibernate.annotations.Check;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@NamedQueries({
        @NamedQuery(name = "Hull.getAll", query = "SELECT a FROM Hull a"),
        @NamedQuery(name = "Hull.getAllByResearches", query = "SELECT a FROM Hull a WHERE a.unlockedThrough IN (:researches) OR a.unlockedThrough IS NULL")
})
@Entity
@Table(name = "hull")
@Check(constraints = "overallConstructionCapacity >= constructionCapacity + constructionCapacityBow + constructionCapacityStern + constructionCapacityBroadsides")
@AttributeOverride(name = "id", column = @Column(name = "idHull"))
public class Hull extends HasCosts {

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
                @Nonnull final ETechLevel techLevel,
                @Nonnull final String description,
                @Nonnull final Research unlockedThrough,
                @Nonnull final EHullType hullType,
                @Nonnull final CrewRequirement crewRequirement) {
        super(new Translation(Translation.DEFAULT_LANGUAGE, name), new Translation(Translation.DEFAULT_LANGUAGE, description), techLevel, overallConstructionCapacity, Hull.class);
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(unlockedThrough, "unlockedThrough shouldn't be null!");
        Preconditions.checkNotNull(hullType, "hullType shouldn't be null!");
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");

        this.overallConstructionCapacity = overallConstructionCapacity;
        this.constructionCapacity = constructionCapacity;
        this.constructionCapacityBow = constructionCapacityBow;
        this.constructionCapacityStern = constructionCapacityStern;
        this.constructionCapacityBroadsides = constructionCapacityBroadsides;
        this.unlockedThrough = unlockedThrough;
        this.hullType = hullType;
        this.getCosts().setCrewRequirement(crewRequirement);
    }

    public int getOverallConstructionCapacity() {
        return overallConstructionCapacity;
    }

    public int getTonnage() {
        return overallConstructionCapacity * 1000;
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
    public Research getUnlockedThrough() {
        return unlockedThrough;
    }

    @Nonnull
    public EHullType getHullType() {
        return hullType;
    }

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public void setHullType(@Nonnull final EHullType hullType) {
        this.hullType = hullType;
    }

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public void setOverallConstructionCapacity(final int overallConstructionCapacity) {
        this.overallConstructionCapacity = overallConstructionCapacity;
    }

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public void setConstructionCapacity(final int constructionCapacity) {
        this.constructionCapacity = constructionCapacity;
    }

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public void setConstructionCapacityBow(final int constructionCapacityBow) {
        this.constructionCapacityBow = constructionCapacityBow;
    }

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public void setConstructionCapacityStern(final int constructionCapacityStern) {
        this.constructionCapacityStern = constructionCapacityStern;
    }

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public void setConstructionCapacityBroadsides(final int constructionCapacityBroadsides) {
        this.constructionCapacityBroadsides = constructionCapacityBroadsides;
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

    public int getConstructionCapacity(@Nonnull final ECapacityAreaType capacityAreaType) {
        Preconditions.checkNotNull(capacityAreaType, "capacityAreaType must not be empty");

        switch (capacityAreaType) {
            case BOW:
                return getConstructionCapacityBow();
            case STERN:
                return getConstructionCapacityStern();
            case BROADSIDE:
                return getConstructionCapacityBroadsides();
            case MODULE:
                return getConstructionCapacity();
            default:
            case OVERALL:
                return getOverallConstructionCapacity();
        }
    }
}
