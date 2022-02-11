package de.yuga.spacebattle.rest.dto.buildings;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.buildings.ProductionType;
import de.yuga.spacebattle.backend.enums.EProductionCategory;
import de.yuga.spacebattle.rest.dto.enums.ERefinementSequence;
import de.yuga.spacebattle.rest.dto.enums.EResourceType;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Building {

    @Nonnull
    @ApiModelProperty(required = true, value = "The ID.")
    private Integer idBuilding;

    @Nonnull
    @ApiModelProperty(required = true, value = "The name of this building.")
    private String name;

    @Nonnull
    @ApiModelProperty(required = true, value = "The description.")
    private String description;

    /**
     * The basic effect value at level 1.
     */
    @ApiModelProperty(required = true, value = "The production amount at first level.")
    private int baseValue;

    /**
     * The increasement of value for the next level.
     */
    @ApiModelProperty(required = true, value = "The modification factor per level.")
    private double increasingFactorPerLevel;

    /**
     * What this building is working on.
     */
    @Nonnull
    @ApiModelProperty(required = true, value = "The subject of this building.")
    private EResourceType productionTarget;

    /**
     * What is the task of this building.
     */
    @Nonnull
    @ApiModelProperty(required = true, value = "The action this building is for.")
    private EProductionCategory productionCategory;

    /**
     * In case of an refinement task - here is the workflow.
     */
    @Nullable
    @ApiModelProperty("In case of a refinement building - this is the defines sequence.")
    private ERefinementSequence refinementSequence;

    public Building() {
    }

    public Building(@Nonnull final de.yuga.spacebattle.backend.entities.buildings.Building building) {
        Preconditions.checkNotNull(building, "building shouldn't be null!");

        idBuilding = building.getId();
        name = building.getName();
        description = building.getDescription();
        baseValue = building.getBaseValue();
        increasingFactorPerLevel = building.getIncreasingFactorPerLevel().doubleValue();
        final ProductionType productionType = building.getProductionType();
        productionTarget = new EResourceType(productionType.getProductionTarget());
        productionCategory = productionType.getProductionCategory();
        refinementSequence = productionType.getRefinementSequence() != null ? new ERefinementSequence(productionType.getRefinementSequence()) : null;
    }

    @Nonnull
    public Integer getIdBuilding() {
        return idBuilding;
    }

    public void setIdBuilding(@Nonnull Integer idBuilding) {
        this.idBuilding = idBuilding;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    public void setName(@Nonnull String name) {
        this.name = name;
    }

    @Nonnull
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nonnull String description) {
        this.description = description;
    }

    public int getBaseValue() {
        return baseValue;
    }

    public void setBaseValue(int baseValue) {
        this.baseValue = baseValue;
    }

    public double getIncreasingFactorPerLevel() {
        return increasingFactorPerLevel;
    }

    public void setIncreasingFactorPerLevel(double increasingFactorPerLevel) {
        this.increasingFactorPerLevel = increasingFactorPerLevel;
    }

    @Nonnull
    public EResourceType getProductionTarget() {
        return productionTarget;
    }

    public void setProductionTarget(@Nonnull EResourceType productionTarget) {
        this.productionTarget = productionTarget;
    }

    @Nonnull
    public EProductionCategory getProductionCategory() {
        return productionCategory;
    }

    public void setProductionCategory(@Nonnull EProductionCategory productionCategory) {
        this.productionCategory = productionCategory;
    }

    @Nullable
    public ERefinementSequence getRefinementSequence() {
        return refinementSequence;
    }

    public void setRefinementSequence(@Nullable ERefinementSequence refinementSequence) {
        this.refinementSequence = refinementSequence;
    }
}
