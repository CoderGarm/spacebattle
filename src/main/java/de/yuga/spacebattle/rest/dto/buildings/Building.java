package de.yuga.spacebattle.rest.dto.buildings;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.buildings.ProductionType;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EProductionCategory;
import de.yuga.spacebattle.rest.dto.WithCosts;
import de.yuga.spacebattle.rest.dto.enums.ERefinementSequence;
import de.yuga.spacebattle.rest.dto.enums.EResourceType;
import de.yuga.spacebattle.rest.dto.researches.Research;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Schema(description = ".")
public class Building extends WithCosts<de.yuga.spacebattle.backend.entities.buildings.Building> {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The ID.")
    private Integer idBuilding;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The name of this building.")
    private String name;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The description.")
    private String description;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The unlocking research name.")
    private String unlockedThrough;

    @JsonProperty
    @Schema(required = true, description = "The production amount at first level.")
    private int baseValue;

    @JsonProperty
    @Schema(required = true, description = "The modification factor per level.")
    private double increasingFactorPerLevel;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The subject of this building.")
    private EResourceType productionTarget;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The action this building is for.")
    private EProductionCategory productionCategory;

    @Nullable
    @JsonProperty
    @Schema(description = "In case of a refinement building - this is the defines sequence.")
    private ERefinementSequence refinementSequence;

    public Building() {
    }

    public Building(@Nonnull final de.yuga.spacebattle.backend.entities.buildings.Building building,
                    @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(building, "building shouldn't be null!");

        final ResourceDeposit costs = building.getCosts();

        idBuilding = building.getId();
        name = building.getName(languageCode);
        description = building.getDescription(languageCode);
        this.unlockedThrough = new Research(building.getUnlockedThrough(), languageCode).getName();
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
