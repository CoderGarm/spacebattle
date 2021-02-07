package de.yuga.spacebattle.entities.spacecrafts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.entities.AbstractEntityKey;
import de.yuga.spacebattle.entities.ResourceDeposit;
import de.yuga.spacebattle.entities.researches.Research;
import de.yuga.spacebattle.enums.EResourceSubType;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@NamedQueries({
        @NamedQuery(name = "Hull.getAll", query = "SELECT a FROM Hull a")
})
@Entity
@Table(name = "hull")
@AttributeOverride(name = "id", column = @Column(name = "idHull"))
public class Hull extends AbstractEntityKey {

    @Nonnull
    @NotNull(message = "name must not be null")
    @Size(min = 1, max = 30)
    private String name;

    private int level;

    private int constructionCapacity;

    @Nonnull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
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
        final StringBuilder sb = new StringBuilder("Hull{");
        sb.append(", id=").append(id);
        sb.append("name='").append(name).append('\'');
        sb.append(", level=").append(level);
        sb.append(", constructionCapacity=").append(constructionCapacity);
        sb.append(", costs=").append(costs);
        sb.append(", description='").append(description).append('\'');
        sb.append(", unlockedThrough=").append(unlockedThrough);
        sb.append('}');
        return sb.toString();
    }
}
