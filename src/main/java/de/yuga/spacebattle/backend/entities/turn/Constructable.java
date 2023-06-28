package de.yuga.spacebattle.backend.entities.turn;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.resource.JobCostsCalculator;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Represents the payload of a job.<br>
 * <code>What</code> is at work and <code>how much</code> will be constructed.
 * The parent {@link Job} contains the information about the <code>where</code> and <code>for whom</code>.
 */
@Embeddable
public class Constructable {

    /**
     * The resource type which must be invested to construct <code>this</code>.
     */
    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private EResourceType resourceType;

    @Nullable
    @OneToOne
    @JoinColumn(name = "idBuilding", referencedColumnName = "idBuilding")
    private Building building;

    @Nullable
    @OneToOne
    @JoinColumn(name = "idResearch", referencedColumnName = "idResearch")
    private Research research;

    @Nullable
    private Integer targetLevel;

    @Nullable
    @OneToOne
    @JoinColumn(name = "idFleet", referencedColumnName = "idFleet")
    private Fleet fleet;

    @Column(columnDefinition = "boolean not null default false")
    private boolean isRepairJob = false;

    @Nullable
    @Transient
    private BigDecimal empireWideResearchPoints;

    public Constructable() {
    }

    public Constructable(@Nonnull final Building building, @Nonnull final Integer targetLevel) {
        Preconditions.checkNotNull(building, "building shouldn't be null!");
        Preconditions.checkNotNull(targetLevel, "targetLevel shouldn't be null!");
        Preconditions.checkArgument(targetLevel > 0, "targetLevel shouldn't lower than one!");

        this.building = building;
        this.targetLevel = targetLevel;
        this.resourceType = EResourceType.CONSTRUCTION;
    }

    public Constructable(@Nonnull final Research research, @Nonnull final Integer targetLevel, @Nonnull final BigDecimal empireWideResearchPoints) {
        Preconditions.checkNotNull(research, "research shouldn't be null!");
        Preconditions.checkNotNull(targetLevel, "targetLevel shouldn't be null!");
        Preconditions.checkArgument(targetLevel > 0, "targetLevel shouldn't be lower than one!");

        this.research = research;
        this.targetLevel = targetLevel;
        this.resourceType = EResourceType.RESEARCH;
        this.empireWideResearchPoints = empireWideResearchPoints;
    }

    public Constructable(@Nonnull final Fleet fleet, final boolean isRepairJob) {
        Preconditions.checkNotNull(fleet, "toRepair must not be empty");

        this.fleet = fleet;
        this.resourceType = EResourceType.ORBITAL_CONSTRUCTION;
        this.isRepairJob = isRepairJob;
    }

    @Nullable
    public Building getBuilding() {
        return building;
    }

    @Nullable
    public Integer getTargetLevel() {
        return targetLevel;
    }

    @Nullable
    public Research getResearch() {
        return research;
    }

    @Nullable
    public Fleet getFleet() {
        return fleet;
    }

    @Nonnull
    public EResourceType getResourceType() {
        return resourceType;
    }

    public boolean isRepairJob() {
        return isRepairJob;
    }

    /**
     * Returns the costs for this construction.
     *
     * @return the costs
     */
    @Nonnull
    public ResourceDeposit getJobCosts() {
        if (research != null && targetLevel != null) {
            final long amountByType = research.getCosts().getResourceAmountByType(EResourceType.RESEARCH);
            final BigDecimal researchCosts = new BigDecimal(amountByType)
                    .multiply(new BigDecimal(targetLevel, ResourceDeposit.MATH_CONTEXT_INTEGER));
            final ResourceDeposit resources = new ResourceDeposit();
            resources.setSubType(EDepositType.COSTS);
            resources.updateResource(EResourceType.RESEARCH, researchCosts.longValue());
            return resources;
        }

        if (building != null && targetLevel != null) {
            final Integer targetLevel = this.targetLevel;
            final ResourceDeposit costs = building.getCosts();
            return JobCostsCalculator.getCostsForLevel(costs, targetLevel);
        }

        if (fleet != null) {
            return JobCostsCalculator.calculateJobCost(fleet, isRepairJob);
        }

        throw new NotifyWebUserException("You have tried something interesting. May be you should talk to an admin.");
    }

    @Nullable
    public BigDecimal getEmpireWideResearchPoints() {
        return empireWideResearchPoints;
    }
}
