package de.yuga.spacebattle.entities.researches;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.entities.AbstractEntityKey;
import de.yuga.spacebattle.entities.ResourceDeposit;
import de.yuga.spacebattle.entities.buildings.Building;
import de.yuga.spacebattle.entities.spacecrafts.Hull;
import de.yuga.spacebattle.entities.spacecrafts.Module;
import de.yuga.spacebattle.enums.EResourceSubType;

import javax.annotation.Nonnull;
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

    @Nonnull
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "idCosts")
    private final ResourceDeposit costs = new ResourceDeposit(EResourceSubType.COSTS);

    @Nonnull // is nonnull when arg-constructor is removed
    @OneToMany(mappedBy = "unlockedThrough")
    private Set<Building> unlocksBuildings;

    @Nonnull // is nonnull when arg-constructor is removed
    @OneToMany(mappedBy = "unlockedThrough")
    private Set<Hull> unlocksHulls;

    @Nonnull // is nonnull when arg-constructor is removed
    @OneToMany(mappedBy = "unlockedThrough")
    private Set<Module> unlocksModules;

    public Research() {
    }

    public Research(@Nonnull final String name, @Nonnull final String description, final int levelCap) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");

        this.name = name;
        this.description = description;
        this.levelCap = levelCap;
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
}
