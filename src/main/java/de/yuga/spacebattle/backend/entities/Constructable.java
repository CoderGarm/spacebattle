package de.yuga.spacebattle.backend.entities;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.backend.enums.EResourceType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the payload of a job.
 *
 * <code>What</code> is at work and <code>how much</code>.
 * The parent {@link Job} contains the information about the <code>where</code> and <code>for whom</code>.
 */
@Embeddable
public class Constructable {

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
    @JoinColumn(name = "idShipClass", referencedColumnName = "idShipClass")
    private ShipClass shipClass;

    @Nullable
    private Integer amountShips;

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

    public Constructable(@Nonnull final ShipClass shipClass, @Nonnull final Integer amountShips) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");
        Preconditions.checkNotNull(amountShips, "amountShips shouldn't be null!");
        Preconditions.checkArgument(amountShips > 0, "amountShips shouldn't be lower than one!");

        this.shipClass = shipClass;
        this.amountShips = amountShips;
        this.resourceType = EResourceType.ORBITALCONSTRUCTION;
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
    public ShipClass getShipClass() {
        return shipClass;
    }

    @Nullable
    public Integer getAmountShips() {
        return amountShips;
    }

    @Nullable
    public Research getResearch() {
        return research;
    }

    @Nonnull
    public EResourceType getResourceType() {
        return resourceType;
    }

    /**
     * Returns the costs for this construction.
     *
     * @return the costs
     */
    public Map<EResourceType, BigDecimal> getJobCosts() {

        if (research != null && targetLevel != null) {
            final BigDecimal researchCosts = research.getCosts().getResourceAmountByType(EResourceType.RESEARCH).multiply(new BigDecimal(targetLevel));
            final Map<EResourceType, BigDecimal> costs = new HashMap<>();
            costs.put(EResourceType.RESEARCH, researchCosts);
            return costs;
        }

        if (shipClass != null) {
            if (shipClass.getHull() == null) {
                throw new NotifySBUserException("You need a hull for your ship, really!");
            }
            return shipClass.getCostsOverall().getResources();
        }

        if (building != null && targetLevel != null) {
            Integer targetLevel = this.targetLevel;
            ResourceDeposit costs = building.getCosts();
            return getCostsForLevel(costs, targetLevel);
        }

        throw new NotifySBUserException("You have tried something interesting. May be you should talk to an admin.");
    }

    /**
     * Calculates the full costs by the given target level.
     *
     * @param costs       the base costs
     * @param targetLevel the target level
     * @return the costs for the target level
     */
    @Nonnull
    private Map<EResourceType, BigDecimal> getCostsForLevel(@Nonnull final ResourceDeposit costs,
                                                            final int targetLevel) {

        final Map<EResourceType, BigDecimal> resources = new HashMap<>(costs.getResources());
        for (EResourceType resourceType : resources.keySet()) {
            final BigDecimal resourceAmountByType = costs.getResourceAmountByType(resourceType);
            costs.updateResource(resourceType, resourceAmountByType.multiply(new BigDecimal(targetLevel)));
        }
        return resources;
    }
}
