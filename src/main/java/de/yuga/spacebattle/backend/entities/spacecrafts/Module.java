package de.yuga.spacebattle.backend.entities.spacecrafts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.ResourceDeposit;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.backend.enums.ERaceType;
import de.yuga.spacebattle.backend.enums.EResourceSubType;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@NamedQueries({
        @NamedQuery(name = "Module.getAll", query = "SELECT a FROM Module a")
})
@Entity
@Table(name = "module")
@AttributeOverride(name = "id", column = @Column(name = "idModule"))
public class Module extends AbstractEntityKey {

    @Nonnull
    @NotNull(message = "name should not be null")
    @Size(min = 3, max = 30, message = "name should not be null")
    private String name;

    @NotNull(message = "useCapacity should not be null")
    private int useCapacity;

    @NotNull(message = "value should not be null")
    private int value;

    @Nonnull
    @NotNull(message = "EModuleType should not be null")
    private EModuleType moduleType;

    @Nonnull
    @NotNull(message = "description should not be null")
    private String description;

    @Nonnull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "idCosts", updatable = false)
    private final ResourceDeposit costs = new ResourceDeposit(EResourceSubType.COSTS);

    private int level;

    @JsonIgnore
    @Nonnull
    @NotNull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "idResearch")
    private Research unlockedThrough;

    public Module() {
    }

    public Module(@Nonnull final String name,
                  @Nonnull final EModuleType moduleType,
                  @Nonnull final String description,
                  final int useCapacity,
                  final int value,
                  final int level,
                  @Nonnull final Research unlockedThrough) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(moduleType, "moduleType shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(unlockedThrough, "unlockedThrough shouldn't be null!");

        this.name = name;
        this.moduleType = moduleType;
        this.description = description;
        this.useCapacity = useCapacity;
        this.value = value;
        this.level = level;
        this.unlockedThrough = unlockedThrough;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    public int getUseCapacity() {
        return useCapacity;
    }

    @Nonnull
    public EModuleType getModuleType() {
        return moduleType;
    }

    public int getValue() {
        return value;
    }

    public int getEffectiveValue(ERaceType raceType) {
        return value * (1 + this.moduleType.getBonus(raceType));
    }

    @Nonnull
    public String getDescription() {
        return description;
    }

    @Nonnull
    public ResourceDeposit getCosts() {
        return costs;
    }

    public int getLevel() {
        return level;
    }

    @Nonnull
    public Research getUnlockedThrough() {
        return unlockedThrough;
    }
}
