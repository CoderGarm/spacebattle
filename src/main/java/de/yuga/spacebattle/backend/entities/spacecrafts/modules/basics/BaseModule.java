package de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.ResourceDeposit;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.enums.EResourceSubType;

import javax.annotation.Nonnull;
import javax.persistence.CascadeType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@MappedSuperclass
public class BaseModule extends AbstractEntityKey {

    @Nonnull
    @NotNull(message = "name should not be null")
    @Size(min = 3, max = 30, message = "name should not be null")
    private String name;

    @Nonnull
    @NotNull(message = "description should not be null")
    private String description;

    @Nonnull
    @NotNull(message = "everything costs smething")
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "idCosts", updatable = false)
    private final ResourceDeposit costs = new ResourceDeposit(EResourceSubType.COSTS);

    @Nonnull
    @NotNull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "idResearch")
    private Research unlockedThrough;

    @NotNull(message = "useCapacity should not be null")
    private int useCapacity;

    /**
     * The basic value for this module's effect, e.g. it's attack value or shield value.
     */
    private int effectValue;

    /**
     * The tech level is necessary to allow the user to filter.
     */
    private int techLevel;

    protected BaseModule() {
    }

    public BaseModule(@Nonnull final String name,
                      @Nonnull final String description,
                      @Nonnull final Research unlockedThrough,
                      final int useCapacity,
                      final int effectValue,
                      final int techLevel) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(unlockedThrough, "unlockedThrough shouldn't be null!");

        this.name = name;
        this.description = description;
        this.unlockedThrough = unlockedThrough;
        this.useCapacity = useCapacity;
        this.effectValue = effectValue;
        this.techLevel = techLevel;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public String getDescription() {
        return description;
    }

    @Nonnull
    public ResourceDeposit getCosts() {
        return costs;
    }

    @Nonnull
    public Research getUnlockedThrough() {
        return unlockedThrough;
    }

    public int getUseCapacity() {
        return useCapacity;
    }

    public int getEffectValue() {
        return effectValue;
    }

    public int getTechLevel() {
        return techLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseModule)) return false;

        BaseModule module = (BaseModule) o;
        return id == module.id;
    }

    @Override
    public int hashCode() {
        return 31 * id;
    }


}
