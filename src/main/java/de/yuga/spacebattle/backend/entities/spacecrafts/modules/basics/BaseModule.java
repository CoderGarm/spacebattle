package de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.resource.ResourceDepositInitializerCalculator;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EDepositType;

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
    @NotNull
    @Size(min = 3, max = 30, message = "name should not be null")
    private String name;

    @Nonnull
    @NotNull
    private String description;

    @Nonnull
    @NotNull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "idCosts", updatable = false)
    private final ResourceDeposit costs = ResourceDepositInitializerCalculator.initializeResourceDeposit(BaseModule.class, EDepositType.COSTS);

    @Nonnull
    @NotNull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "idResearch")
    private Research unlockedThrough;

    /**
     * The capacity represents the capacity in metric tons which will be occupied if build in.<br>
     * This capacity includes all 'opportunity costs' to use a module, from crew quarters up to toilets, from screens and displays up to seats and impact cages.
     */
    @NotNull
    private int useCapacity;

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
                      final int techLevel,
                      @Nonnull final CrewRequirement crewRequirement) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(unlockedThrough, "unlockedThrough shouldn't be null!");
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");

        this.name = name;
        this.description = description;
        this.unlockedThrough = unlockedThrough;
        this.useCapacity = useCapacity;
        this.techLevel = techLevel;
        this.costs.setCrewRequirement(crewRequirement);
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
