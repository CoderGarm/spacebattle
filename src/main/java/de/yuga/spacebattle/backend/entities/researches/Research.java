package de.yuga.spacebattle.backend.entities.researches;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.ResourceDeposit;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.entities.spacecrafts.Module;
import de.yuga.spacebattle.backend.enums.EResourceSubType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Set;

@NamedQueries({
        @NamedQuery(name = "Research.getAll", query = "SELECT p FROM Research p")
})
@Entity
@Table(name = "research")
@AttributeOverride(name = "id", column = @Column(name = "idResearch"))
public class Research extends AbstractEntityKey {

    @Nonnull
    @Size(min = 1, max = 30)
    private String name;

    @Nonnull
    private String description;

    private int levelCap;

    @JsonIgnore
    @Nullable
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "unlockedThrough")
    private Research unlockedThrough;

    @Nonnull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "idCosts", updatable = false)
    private final ResourceDeposit costs = new ResourceDeposit(EResourceSubType.COSTS);

    @Nonnull // is nonnull when arg-constructor is removed
    @OneToMany(mappedBy = "unlockedThrough", fetch = FetchType.EAGER)
    private Set<Building> unlocksBuildings;

    @Nonnull // is nonnull when arg-constructor is removed
    @OneToMany(mappedBy = "unlockedThrough", fetch = FetchType.EAGER)
    private Set<Hull> unlocksHulls;

    @Nonnull // is nonnull when arg-constructor is removed
    @OneToMany(mappedBy = "unlockedThrough", fetch = FetchType.EAGER)
    private Set<Module> unlocksModules;

    public Research() {
    }

    public Research(@Nonnull final String name,
                    @Nonnull final String description,
                    final int levelCap,
                    @Nullable final Research unlockedThrough) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");

        this.name = name;
        this.description = description;
        this.levelCap = levelCap;
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

    public int getLevelCap() {
        return levelCap;
    }

    @Nonnull
    public ResourceDeposit getCosts() {
        return costs;
    }

    @Nonnull
    public Set<Building> getUnlocksBuildings() {
        return unlocksBuildings;
    }

    @Nonnull
    public Set<Hull> getUnlocksHulls() {
        return unlocksHulls;
    }

    @Nonnull
    public Set<Module> getUnlocksModules() {
        return unlocksModules;
    }

    @Nonnull
    public Research getUnlockedThrough() {
        return unlockedThrough;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Research)) return false;

        Research research = (Research) o;

        return getId() == research.getId();
    }

    @Override
    public int hashCode() {
        return getId() * 31;
    }
}
