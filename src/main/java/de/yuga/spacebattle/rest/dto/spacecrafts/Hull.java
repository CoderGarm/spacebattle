package de.yuga.spacebattle.rest.dto.spacecrafts;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.enums.EHullType;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;

public class Hull {

    @Nonnull
    @ApiModelProperty(required = true, value = "The id of this hull.")
    private int idHull;

    @Nonnull
    @ApiModelProperty(required = true, value = "The name of this hull.")
    private String name;

    @Nonnull
    @ApiModelProperty(required = true, value = "The description of the hull.")
    private String description;

    @Nonnull
    @ApiModelProperty(required = true, value = "The hull type.")
    private EHullType hullType;

    @ApiModelProperty(required = true, value = "The overall construction capacity as displacement like in the wet navies.")
    private int overallConstructionCapacity;

    @ApiModelProperty(required = true, value = "The unaligned construction capacity.")
    private int constructionCapacity;

    @ApiModelProperty(required = true, value = "The bow-aligned construction capacity.")
    private int constructionCapacityBow;

    @ApiModelProperty(required = true, value = "The stern-aligned construction capacity.")
    private int constructionCapacityStern;

    @ApiModelProperty(required = true, value = "The broadside-aligned construction capacity.")
    private int constructionCapacityBroadsides;

    public Hull() {
    }

    public Hull(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.Hull hull) {
        Preconditions.checkNotNull(hull, "hull shouldn't be null!");

        this.idHull = hull.getId();
        this.name = hull.getName();
        this.description = hull.getDescription();
        this.hullType = new EHullType(hull.getHullType());
        this.overallConstructionCapacity = hull.getOverallConstructionCapacity();
        this.constructionCapacity = hull.getConstructionCapacity();
        this.constructionCapacityBow = hull.getConstructionCapacityBow();
        this.constructionCapacityStern = hull.getConstructionCapacityStern();
        this.constructionCapacityBroadsides = hull.getConstructionCapacityBroadsides();
    }

    public int getIdHull() {
        return idHull;
    }

    public void setIdHull(int idHull) {
        this.idHull = idHull;
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

    @Nonnull
    public EHullType getHullType() {
        return hullType;
    }

    public void setHullType(@Nonnull EHullType hullType) {
        this.hullType = hullType;
    }

    public int getOverallConstructionCapacity() {
        return overallConstructionCapacity;
    }

    public void setOverallConstructionCapacity(int overallConstructionCapacity) {
        this.overallConstructionCapacity = overallConstructionCapacity;
    }

    public int getConstructionCapacity() {
        return constructionCapacity;
    }

    public void setConstructionCapacity(int constructionCapacity) {
        this.constructionCapacity = constructionCapacity;
    }

    public int getConstructionCapacityBow() {
        return constructionCapacityBow;
    }

    public void setConstructionCapacityBow(int constructionCapacityBow) {
        this.constructionCapacityBow = constructionCapacityBow;
    }

    public int getConstructionCapacityStern() {
        return constructionCapacityStern;
    }

    public void setConstructionCapacityStern(int constructionCapacityStern) {
        this.constructionCapacityStern = constructionCapacityStern;
    }

    public int getConstructionCapacityBroadsides() {
        return constructionCapacityBroadsides;
    }

    public void setConstructionCapacityBroadsides(int constructionCapacityBroadsides) {
        this.constructionCapacityBroadsides = constructionCapacityBroadsides;
    }
}
