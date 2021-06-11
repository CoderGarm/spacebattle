package de.yuga.spacebattle.gui.vaadin.constructables.buildings;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.buildings.ProductionType;
import de.yuga.spacebattle.backend.enums.EProductionCategory;
import de.yuga.spacebattle.backend.enums.EResourceType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a distinct selection of parameters to filter constructions.
 */
public class ConstructionFilterDTO {

    @Nullable
    private final EProductionCategory productCategory;

    @Nullable
    private final EResourceType resourceType;

    public ConstructionFilterDTO(@Nullable EProductionCategory productCategory, @Nullable EResourceType resourceType) {
        this.productCategory = productCategory;
        this.resourceType = resourceType;
    }

    @Nullable
    public EProductionCategory getProductCategory() {
        return productCategory;
    }

    @Nullable
    public EResourceType getResourceType() {
        return resourceType;
    }

    /**
     * Checks if this fits to the given parameter.
     *
     * @param productionType the parameter to check
     * @return <code>true</code> if the filter fits, <code>false</code> otherwise
     */
    public boolean fitsFilter(@Nonnull final ProductionType productionType) {
        Preconditions.checkNotNull(productionType, "productionType shouldn't be null!");

        final EResourceType productionTarget = productionType.getProductionTarget();
        final EProductionCategory productionCategory = productionType.getProductionCategory();
        boolean isGood = true;
        if (resourceType != null && resourceType != productionTarget) {
            isGood = false;
        }
        if (this.productCategory != null && this.productCategory != productionCategory) {
            isGood = false;
        }
        return isGood;
    }
}
