package de.yuga.spacebattle.rest.dto.spacecrafts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.ETechnologyType;
import de.yuga.spacebattle.rest.dto.enums.EHullType;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class Hull {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The id of this hull.")
    private int idHull;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The name of this hull.")
    private String name;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The description of the hull.")
    private String description;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The hull type.")
    private EHullType hullType;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The tech type of this hull.")
    private ETechnologyType technologyType = ETechnologyType.CIVIL;

    @JsonProperty
    @Schema(required = true, description = "The overall construction capacity as displacement like in the wet navies.")
    private int overallConstructionCapacity;

    @JsonProperty
    @Schema(required = true, description = "The unaligned construction capacity.")
    private int constructionCapacity;

    @JsonProperty
    @Schema(required = true, description = "The bow-aligned construction capacity.")
    private int constructionCapacityBow;

    @JsonProperty
    @Schema(required = true, description = "The stern-aligned construction capacity.")
    private int constructionCapacityStern;

    @JsonProperty
    @Schema(required = true, description = "The broadside-aligned construction capacity.")
    private int constructionCapacityBroadsides;

    public Hull() {
    }

    public Hull(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.Hull hull,
                @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(hull, "hull shouldn't be null!");

        this.idHull = hull.getId();
        this.name = hull.getName(languageCode);
        this.description = hull.getDescription(languageCode);
        this.hullType = new EHullType(hull.getHullType());
        this.technologyType = hull.getHullType().isCivilShip() ? ETechnologyType.CIVIL : ETechnologyType.MILITARY;
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
