package de.yuga.spacebattle.backend.entities.researches;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.resource.ResourceDepositInitializerCalculator;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.*;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EDepositType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.HashSet;
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

    @Nullable
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "unlockedThrough")
    private Research unlockedThrough;

    @Nonnull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "idCosts", updatable = false)
    private final ResourceDeposit costs = ResourceDepositInitializerCalculator.initializeResourceDeposit(Research.class, EDepositType.COSTS);

    @Nonnull
    @OneToMany(mappedBy = "unlockedThrough", fetch = FetchType.EAGER)
    private final Set<Building> unlocksBuildings = new HashSet<>();

    @Nonnull
    @OneToMany(mappedBy = "unlockedThrough", fetch = FetchType.EAGER)
    private final Set<Hull> unlocksHulls = new HashSet<>();

    @Nonnull
    @OneToMany(mappedBy = "unlockedThrough", fetch = FetchType.EAGER)
    private final Set<Weapon> unlocksWeapons = new HashSet<>();

    @Nonnull
    @OneToMany(mappedBy = "unlockedThrough", fetch = FetchType.EAGER)
    private final Set<Armor> unlocksArmor = new HashSet<>();

    @Nonnull
    @OneToMany(mappedBy = "unlockedThrough", fetch = FetchType.EAGER)
    private final Set<Propulsion> unlocksPropulsion = new HashSet<>();

    @Nonnull
    @OneToMany(mappedBy = "unlockedThrough", fetch = FetchType.EAGER)
    private final Set<Sidewall> unlocksSidewall = new HashSet<>();

    @Nonnull
    @OneToMany(mappedBy = "unlockedThrough", fetch = FetchType.EAGER)
    private final Set<ElectronicWarfare> unlocksElectronicWarfare = new HashSet<>();

    @Nonnull
    @OneToMany(mappedBy = "unlockedThrough", fetch = FetchType.EAGER)
    private final Set<Missile> unlocksMissiles = new HashSet<>();

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
    public Set<Weapon> getUnlocksWeapons() {
        return unlocksWeapons;
    }

    @Nonnull
    public Set<Armor> getUnlocksArmor() {
        return unlocksArmor;
    }

    @Nonnull
    public Set<Propulsion> getUnlocksPropulsion() {
        return unlocksPropulsion;
    }

    @Nonnull
    public Set<Sidewall> getUnlocksSidewall() {
        return unlocksSidewall;
    }

    @Nonnull
    public Set<ElectronicWarfare> getUnlocksElectronicWarfare() {
        return unlocksElectronicWarfare;
    }

    @Nonnull
    public Set<Missile> getUnlocksMissiles() {
        return unlocksMissiles;
    }

    @Nullable
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
