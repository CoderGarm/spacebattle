package de.yuga.spacebattle.backend.entities.misc;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.resource.ResourceDepositInitializerCalculator;
import de.yuga.spacebattle.backend.converter.MassConverter;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.dto.physics.Mass;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.NamedTechLevel;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.EResourceDemand;
import de.yuga.spacebattle.backend.enums.EShipClassType;
import de.yuga.spacebattle.backend.enums.physics.EMassMetric;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Map;

@MappedSuperclass
public class HasCostsByOwn extends HasNamedTechLevel implements HasEffectValue {

    /**
     * Which is the targeted ship's hull class.
     */
    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private EShipClassType shipClassType;

    @Nonnull
    @NotNull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "idCosts", updatable = false)
    private ResourceDeposit costs;

    /**
     * The capacity represents the capacity in metric tons which will be occupied if build in.<br>
     * This capacity includes all 'opportunity costs' to use a module, from crew quarters up to toilets, from screens and displays up to seats and impact cages.
     */
    @Nonnull
    @NotNull
    @Convert(converter = MassConverter.class)
    private Mass tonnage;

    private int effectValue;

    public HasCostsByOwn() {
    }

    public HasCostsByOwn(@Nonnull final NamedTechLevel baseModule,
                         @Nonnull final String technicalTypeName,
                         final int unlockedThroughLevel,
                         final int effectValue,
                         final int tonnage,
                         @Nonnull final EShipClassType shipClassType,
                         @Nonnull final CrewRequirement crewRequirement) {
        this(baseModule, technicalTypeName, unlockedThroughLevel, effectValue, tonnage, shipClassType, crewRequirement, EResourceDemand.BASE_MODULE);
    }

    public HasCostsByOwn(@Nonnull final NamedTechLevel baseModule,
                         @Nonnull final String technicalTypeName,
                         final int unlockedThroughLevel,
                         final int effectValue,
                         final int tonnage,
                         @Nonnull final EShipClassType shipClassType) {
        this(baseModule, technicalTypeName, unlockedThroughLevel, effectValue, tonnage, shipClassType, new CrewRequirement(Map.of(), EDepositType.COSTS));
    }

    public HasCostsByOwn(@Nonnull final NamedTechLevel baseModule,
                         @Nonnull final String technicalTypeName,
                         final int unlockedThroughLevel,
                         final int effectValue,
                         final int tonnage,
                         @Nonnull final EShipClassType shipClassType,
                         @Nonnull final CrewRequirement crewRequirement,
                         @Nonnull final EResourceDemand eResourceDemand) {
        super(baseModule, unlockedThroughLevel, technicalTypeName);
        Preconditions.checkNotNull(crewRequirement, "crewRequirement must not be empty");
        Preconditions.checkNotNull(eResourceDemand, "eResourceDemand must not be empty");

        this.shipClassType = Preconditions.checkNotNull(shipClassType, "shipClassType must not be empty");
        this.tonnage = new Mass(tonnage, EMassMetric.T);
        this.costs = ResourceDepositInitializerCalculator.initializeCosts(baseModule.getTechLevel(), tonnage, eResourceDemand);
        this.costs.setCrewRequirement(crewRequirement);
        this.effectValue = effectValue;
    }

    @Nonnull
    public EShipClassType getShipClassType() {
        return shipClassType;
    }

    @Override
    public int getEffectValue() {
        return effectValue;
    }

    @Nonnull
    public Mass getTonnage() {
        return tonnage;
    }

    @Nonnull
    public ResourceDeposit getCosts() {
        return costs;
    }
}
