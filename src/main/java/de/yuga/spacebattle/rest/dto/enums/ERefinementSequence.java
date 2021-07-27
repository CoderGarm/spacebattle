package de.yuga.spacebattle.rest.dto.enums;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.enums.EProductionCategory;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;

/**
 * Provides the mapping between two enums in case of an {@link EProductionCategory#REFINEMENT} task
 * for a {@link Building}.<br>
 * <b>Attention:</b> It is currently not planned to do something else than education.<br>
 * Don't wonder is this is the only stuff here.
 */
public class ERefinementSequence {

    @Nonnull
    @ApiModelProperty(required = true, value = "The refinement sequence type name.")
    private final String typeName;

    /**
     * What goes in.
     */
    @Nonnull
    @ApiModelProperty(required = true, value = "The educt or start of the sequence.")
    private final EEducationType educt;

    /**
     * What goes out.
     */
    @Nonnull
    @ApiModelProperty(required = true, value = "The product or result of the sequence.")
    private final EEducationType product;

    public ERefinementSequence() {
        this.typeName = "";
        this.educt = new EEducationType();
        this.product = new EEducationType();
    }

    public ERefinementSequence(@Nonnull final de.yuga.spacebattle.backend.enums.ERefinementSequence refinementSequence) {
        Preconditions.checkNotNull(refinementSequence, "refinementSequence shouldn't be null!");

        typeName = refinementSequence.name();
        educt = new EEducationType(refinementSequence.getEduct());
        product = new EEducationType(refinementSequence.getProduct());
    }

    @Nonnull
    public String getTypeName() {
        return typeName;
    }

    /**
     * As no other refinement sequence is planned this will be the way for now.
     *
     * @return the educt
     */
    @Nonnull
    public EEducationType getEduct() {
        return educt;
    }

    /**
     * As no other refinement sequence is planned this will be the way for now.
     *
     * @return the product
     */
    @Nonnull
    public EEducationType getProduct() {
        return product;
    }
}
