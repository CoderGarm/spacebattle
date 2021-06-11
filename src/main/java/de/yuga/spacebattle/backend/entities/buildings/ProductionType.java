package de.yuga.spacebattle.backend.entities.buildings;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EProductionCategory;
import de.yuga.spacebattle.backend.enums.ERefinementSequence;
import de.yuga.spacebattle.backend.enums.EResourceType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;

/**
 * The job description of a building.
 */
@Embeddable
public class ProductionType {

    /**
     * What this building is working on.
     */
    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EResourceType productionTarget;

    /**
     * What is the task of this building.
     */
    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EProductionCategory productionCategory;

    /**
     * In case of an refinement task - here is the workflow.
     */
    @Nullable
    @Enumerated(EnumType.STRING)
    private ERefinementSequence refinementSequence;

    public ProductionType(@Nonnull final EResourceType productionTarget,
                          @Nonnull final EProductionCategory productionCategory,
                          @Nullable final ERefinementSequence refinementSequence) {
        Preconditions.checkNotNull(productionTarget, "productionTarget shouldn't be null!");
        Preconditions.checkNotNull(productionCategory, "productionCategory shouldn't be null!");

        this.productionTarget = productionTarget;
        this.productionCategory = productionCategory;
        this.refinementSequence = refinementSequence;
    }

    public ProductionType() {
    }

    @Nonnull
    public EResourceType getProductionTarget() {
        return productionTarget;
    }

    @Nonnull
    public EProductionCategory getProductionCategory() {
        return productionCategory;
    }

    @Nullable
    public ERefinementSequence getRefinementSequence() {
        return refinementSequence;
    }

    @Nonnull
    public String getRefinementSequenceAsString() {
        if (refinementSequence == null) {
            return "";
        }
        final Enum<?> educt = refinementSequence.getEduct();
        final Enum<?> product = refinementSequence.getProduct();
        return educt.name() + " to " + product.name();
    }
}
