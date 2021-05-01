package de.yuga.spacebattle.backend.entities.spacecrafts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.ResourceDeposit;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.enums.EResourceSubType;

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
@AttributeOverride(name = "id", column = @Column(name = "idHull"))
public class Hull extends AbstractEntityKey {

    @Nonnull
    @NotNull(message = "name must not be null")
    @Size(min = 1, max = 30)
    private String name;

    /**
     * The level represents the size of a ship, bigger levels represents bigger hulls.
     */
    private int level;

    private int constructionCapacity;

    @Nonnull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "idCosts", updatable = false)
    private final ResourceDeposit costs = new ResourceDeposit(EResourceSubType.COSTS);

    @Nonnull
    @NotNull(message = "description must not be null")
    private String description;

    @JsonIgnore
    @Nonnull
    @NotNull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "idResearch")
    private Research unlockedThrough;

    public Hull() {
    }

    public Hull(@Nonnull final String name,
                final int level,
                final int constructionCapacity,
                @Nonnull final String description,
                @Nonnull final Research unlockedThrough) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(unlockedThrough, "unlockedThrough shouldn't be null!");

        this.name = name;
        this.level = level;
        this.constructionCapacity = constructionCapacity;
        this.description = description;
        this.unlockedThrough = unlockedThrough;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public int getConstructionCapacity() {
        return constructionCapacity;
    }

    @Nonnull
    public ResourceDeposit getCosts() {
        return costs;
    }

    @Nonnull
    public String getDescription() {
        return description;
    }

    @Nonnull
    public Research getUnlockedThrough() {
        return unlockedThrough;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(name).append(", ").append(description);
        sb.append(", Level ").append(level);
        sb.append(", Capacity ").append(constructionCapacity);
        return sb.toString();
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
