package de.yuga.spacebattle.backend.entities.researches;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.i18n.Translation;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.*;
import de.yuga.spacebattle.backend.entities.turn.resources.HasCosts;
import de.yuga.spacebattle.backend.enums.ETechLevel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@NamedQueries({
        @NamedQuery(name = "Research.getAll",
                query = "SELECT p FROM Research p"),
        @NamedQuery(name = "Research.getTreeAsTuple",
                query = "SELECT new de.yuga.spacebattle.backend.dto.research.ResearchTreeElement(p.id, p.unlockedThrough.id) FROM Research p"),
        @NamedQuery(name = "Research.getResearchesAsDTOById",
                query = "SELECT new de.yuga.spacebattle.rest.dto.researches.Research(p, n.translation, d.translation) FROM Research p " +
                        "LEFT JOIN Translation n ON (n.translatable = p.name AND n.languageCode = :languageCode) " +
                        "LEFT JOIN Translation d ON (d.translatable = p.description AND d.languageCode = :languageCode) " +
                        "WHERE p.id IN (:idResearches)")
})
@Entity
@Table(name = "research")
@AttributeOverride(name = "id", column = @Column(name = "idResearch"))
public class Research extends HasCosts {

    private int levelCap;

    @Nullable
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "unlockedThrough")
    private Research unlockedThrough;

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
    private final Set<Launcher> unlocksLauncher = new HashSet<>();

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
                    @Nonnull final ETechLevel techLevel,
                    @Nullable final Research unlockedThrough) {
        super(new Translation("en", name), new Translation("en", description), techLevel, Research.class);
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");

        this.levelCap = levelCap;
        this.unlockedThrough = unlockedThrough;
    }

    public int getLevelCap() {
        return levelCap;
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
    public Set<Launcher> getUnlocksLauncher() {
        return unlocksLauncher;
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
