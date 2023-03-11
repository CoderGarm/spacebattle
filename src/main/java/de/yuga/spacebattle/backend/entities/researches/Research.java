package de.yuga.spacebattle.backend.entities.researches;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.i18n.Translation;
import de.yuga.spacebattle.backend.entities.misc.HasCosts;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Launcher;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.NamedTechLevel;
import de.yuga.spacebattle.backend.enums.ETechLevel;
import de.yuga.spacebattle.backend.enums.ETranslationTarget;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@NamedQueries({
        @NamedQuery(name = "Research.getAll",
                query = "SELECT p FROM Research p"),
        @NamedQuery(name = "Research.getTreeAsTuple",
                query = "SELECT new de.yuga.spacebattle.backend.dto.research.ResearchTreeElement(p.id, p.unlockedThrough.id) FROM Research p")
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
    private final Set<Launcher> unlocksLauncher = new HashSet<>();

    @Nonnull
    @OneToMany(mappedBy = "unlockedThrough", fetch = FetchType.EAGER)
    private final Set<Missile> unlocksMissiles = new HashSet<>();

    @Nonnull
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @OneToMany(mappedBy = "unlockedThrough", fetch = FetchType.EAGER)
    private final Set<NamedTechLevel> unlocksNamedTechLevel = new HashSet<>();

    public Research() {
    }

    public Research(@Nonnull final String name,
                    @Nonnull final String description,
                    final int levelCap,
                    @Nonnull final ETechLevel techLevel,
                    @Nullable final Research unlockedThrough) {
        super(new Translation(Translation.DEFAULT_LANGUAGE, name), new Translation(Translation.DEFAULT_LANGUAGE, description), techLevel, null, Research.class);
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
    public Set<Launcher> getUnlocksLauncher() {
        return unlocksLauncher;
    }

    @Nonnull
    public Set<Missile> getUnlocksMissiles() {
        return unlocksMissiles;
    }

    @Nonnull
    public ETranslationTarget getUnlocks() {
        ETranslationTarget eTranslationTarget = unlocksNamedTechLevel.stream().map(NamedTechLevel::getTranslationTarget).findFirst().orElse(null);
        if (eTranslationTarget != null) {
            return eTranslationTarget;
        }
        if (!getUnlocksLauncher().isEmpty()) {
            return ETranslationTarget.LAUNCHER;
        }
        if (!getUnlocksMissiles().isEmpty()) {
            return ETranslationTarget.MISSILE;
        }
        return ETranslationTarget.RESEARCH;
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
