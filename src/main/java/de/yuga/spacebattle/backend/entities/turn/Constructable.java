package de.yuga.spacebattle.backend.entities.turn;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.resource.JobCostsCalculator;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.FleetSnapshot;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.OrbitalModule;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.EJobType;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.services.caclulator.TickOutputCalculator;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Nullable
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "idFleetSnapshot")
    private FleetSnapshot fleetSnapshot;

    @Nonnull
    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "orbitalModuleJobElements", joinColumns = @JoinColumn(name = "idJob"))
    private final Set<OrbitalModuleJobElement> orbitalModuleJobElements = new HashSet<>();

    @Nullable
    @Enumerated(EnumType.STRING)
    private EJobType jobType;

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

    public Constructable(@Nonnull final Research research, @Nonnull final Integer targetLevel) {
        Preconditions.checkNotNull(research, "research shouldn't be null!");
        Preconditions.checkNotNull(targetLevel, "targetLevel shouldn't be null!");
        Preconditions.checkArgument(targetLevel > 0, "targetLevel shouldn't be lower than one!");

        this.research = research;
        this.targetLevel = targetLevel;
        this.resourceType = EResourceType.RESEARCH;
    }

    public Constructable(@Nonnull final Fleet fleet, @Nonnull final EJobType jobType) {
        Preconditions.checkNotNull(fleet, "toRepair must not be empty");
        Preconditions.checkNotNull(jobType, "jobType must not be empty");

        this.fleet = fleet;
        this.resourceType = EResourceType.ORBITAL_CONSTRUCTION;
        this.jobType = jobType;
    }

    public Constructable(@Nonnull final Map<OrbitalModule, Integer> jobLoad, @Nonnull final EJobType jobType) {
        Preconditions.checkNotNull(jobLoad, "jobLoad must not be empty");
        Preconditions.checkNotNull(jobType, "jobType must not be empty");

        this.orbitalModuleJobElements.addAll(jobLoad.entrySet().stream().map(e -> new OrbitalModuleJobElement(e.getKey(), e.getValue())).collect(Collectors.toList()));
        this.resourceType = EResourceType.ORBITAL_CONSTRUCTION;
        this.jobType = jobType;
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

    @Nonnull
    public Set<OrbitalModuleJobElement> getOrbitalModuleJobElements() {
        return orbitalModuleJobElements;
    }

    @Nullable
    public Fleet getFleet() {
        return fleet;
    }

    @Nullable
    public FleetSnapshot getFleetSnapshot() {
        return fleetSnapshot;
    }

    @Nonnull
    public EResourceType getResourceType() {
        return resourceType;
    }

    public boolean isRepairJob() {
        return EJobType.REPAIR == jobType;
    }

    public boolean isUpgradeJob() {
        return EJobType.UPGRADE == jobType;
    }

    public boolean isShipyardJob() {
        return resourceType == EResourceType.ORBITAL_CONSTRUCTION;
    }

    /**
     * Returns the costs for this construction.
     *
     * @return the costs
     */
    @Nonnull
    public ResourceDeposit getJobCosts() {
        if (research != null && targetLevel != null) {
            final long baseCosts = research.getCosts().getResourceAmountByType(EResourceType.RESEARCH);
            final ResourceDeposit resources = new ResourceDeposit(EDepositType.COSTS);
            final BigDecimal researchCosts = TickOutputCalculator.getResearchCosts(baseCosts, targetLevel);
            resources.updateResource(EResourceType.RESEARCH, researchCosts.longValue());
            return resources;
        }

        if (building != null && targetLevel != null) {
            final Integer targetLevel = this.targetLevel;
            final ResourceDeposit costs = building.getCosts();
            return JobCostsCalculator.getCostsForLevel(costs, targetLevel);
        }

        if (fleet != null) {
            return JobCostsCalculator.calculateJobCost(fleet, Objects.requireNonNull(jobType));
        }

        if (!orbitalModuleJobElements.isEmpty()) {
            return JobCostsCalculator.calculateJobCost(orbitalModuleJobElements, Objects.requireNonNull(jobType));
        }

        throw new NotifyWebUserException("You have tried something interesting. May be you should talk to an admin.");
    }

    public boolean isResearchJob() {
        return this.research != null;
    }

    public void snapshotFleet() {
        Preconditions.checkNotNull(fleet, "fleet must not be empty");

        fleetSnapshot = new FleetSnapshot(fleet, fleet.getAliveShips());
        fleet = null;
    }
}
