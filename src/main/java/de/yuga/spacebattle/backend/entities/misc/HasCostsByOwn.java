package de.yuga.spacebattle.backend.entities.misc;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.resource.ResourceDepositInitializerCalculator;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.NamedTechLevel;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EResourceDemand;

import javax.annotation.Nonnull;
import javax.persistence.CascadeType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

@MappedSuperclass
public class HasCostsByOwn extends HasNamedTechLevel implements HasEffectValue {

    @Nonnull
    @NotNull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "idCosts", updatable = false)
    private ResourceDeposit costs;

    /**
     * The capacity represents the capacity in metric tons which will be occupied if build in.<br>
     * This capacity includes all 'opportunity costs' to use a module, from crew quarters up to toilets, from screens and displays up to seats and impact cages.
     */
    @NotNull
    private int useCapacity;

    private int effectValue;

    public HasCostsByOwn() {
    }

    public HasCostsByOwn(@Nonnull final NamedTechLevel baseModule,
                         @Nonnull final String technicalTypeName,
                         final int unlockedThroughLevel,
                         final int effectValue,
                         final int useCapacity,
                         @Nonnull final CrewRequirement crewRequirement) {
        this(baseModule, technicalTypeName, unlockedThroughLevel, effectValue, useCapacity, crewRequirement, EResourceDemand.BASE_MODULE);
    }

    public HasCostsByOwn(@Nonnull final NamedTechLevel baseModule,
                         @Nonnull final String technicalTypeName,
                         final int unlockedThroughLevel,
                         final int effectValue,
                         final int useCapacity,
                         @Nonnull final CrewRequirement crewRequirement,
                         @Nonnull final EResourceDemand eResourceDemand) {
        super(baseModule, unlockedThroughLevel, technicalTypeName);
        Preconditions.checkNotNull(crewRequirement, "crewRequirement must not be empty");
        Preconditions.checkNotNull(eResourceDemand, "eResourceDemand must not be empty");

        this.effectValue = effectValue;
        this.useCapacity = useCapacity;
        this.costs = ResourceDepositInitializerCalculator.initializeCosts(baseModule.getTechLevel(), useCapacity, eResourceDemand);
        this.costs.setCrewRequirement(crewRequirement);
    }

    @Override
    public int getEffectValue() {
        return effectValue;
    }

    public int getUseCapacity() {
        return useCapacity;
    }

    @Nonnull
    public ResourceDeposit getCosts() {
        return costs;
    }
}
