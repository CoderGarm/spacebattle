package de.yuga.spacebattle.backend.entities;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.Module;
import de.yuga.spacebattle.backend.enums.EResourceType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Embeddable
/**
 * todo constraint building and targetlevel XOR shipclass and amount
 */
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
    @JoinColumn(name = "idShipclass", referencedColumnName = "idShipclass")
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

    public void setBuilding(@Nullable final Building building) {
        this.building = building;
    }

    @Nullable
    public Integer getTargetLevel() {
        return targetLevel;
    }

    public void setTargetLevel(@Nullable final Integer targetLevelBuilding) {
        this.targetLevel = targetLevelBuilding;
    }

    @Nullable
    public ShipClass getShipClass() {
        return shipClass;
    }

    public void setShipClass(@Nullable final ShipClass shipClass) {
        this.shipClass = shipClass;
    }

    @Nullable
    public Integer getAmountShips() {
        return amountShips;
    }

    public void setAmountShips(@Nullable final Integer amountShips) {
        this.amountShips = amountShips;
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

        if (building == null || targetLevel == null) {
            ResourceDeposit costsHull = shipClass.getHull().getCosts();
            ResourceDeposit clone = new ResourceDeposit(costsHull);
            Map<Module, Integer> modules = shipClass.getModules();
            for (Module module : modules.keySet()) {
                ResourceDeposit costs = module.getCosts();
                Map<EResourceType, BigDecimal> resources = getCostsByMultiplyer(costs, modules.get(module));
                for (EResourceType resourceType : resources.keySet()) {
                    clone.updateResource(resourceType, resources.get(resourceType));
                }
            }
            return clone.getResources();
        }

        Integer targetLevel = this.targetLevel;
        ResourceDeposit costs = building.getCosts();
        Map<EResourceType, BigDecimal> resources = getCostsByMultiplyer(costs, targetLevel);
        return resources;
    }

    @Nonnull
    private Map<EResourceType, BigDecimal> getCostsByMultiplyer(@Nonnull final ResourceDeposit costs,
                                                                final int targetLevel) {

        Map<EResourceType, BigDecimal> resources = new HashMap<>(costs.getResources());
        for (EResourceType resourceType : resources.keySet()) {
            BigDecimal resourceAmountByType = costs.getResourceAmountByType(resourceType);
            costs.updateResource(resourceType, resourceAmountByType.multiply(new BigDecimal(targetLevel)));
        }
        return resources;
    }
}
