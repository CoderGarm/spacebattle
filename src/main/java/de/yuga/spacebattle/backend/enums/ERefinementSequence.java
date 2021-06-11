package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.buildings.Building;

import javax.annotation.Nonnull;

/**
 * Provides the mapping between two enums in case of an {@link EProductionCategory#REFINEMENT} task
 * for a {@link Building}.<br>
 * <b>Attention:</b> It is currently not planned to do something else than education.<br>
 * Don't wonder is this is the only stuff here.
 */
public enum ERefinementSequence {

    EDUCATION_CIVIL_I(EEducationType.NONE, EEducationType.CIVIL_MK_I),
    EDUCATION_CIVIL_II(EEducationType.CIVIL_MK_I, EEducationType.CIVIL_MK_II),
    EDUCATION_CIVIL_III(EEducationType.CIVIL_MK_II, EEducationType.CIVIL_MK_III),

    EDUCATION_MILITARY_I(EEducationType.CIVIL_MK_II, EEducationType.MILITARY_MK_I),
    EDUCATION_MILITARY_II(EEducationType.CIVIL_MK_III, EEducationType.MILITARY_MK_II),
    ;

    /**
     * What goes in.
     */
    @Nonnull
    private final Enum<?> educt;

    /**
     * What goes out.
     */
    @Nonnull
    private final Enum<?> product;

    ERefinementSequence(@Nonnull final Enum<?> educt, @Nonnull final Enum<?> product) {
        Preconditions.checkNotNull(educt, "educt shouldn't be null!");
        Preconditions.checkNotNull(product, "product shouldn't be null!");

        this.educt = educt;
        this.product = product;
    }

    /**
     * As no other refinement sequence is planned this will be the way for now.
     *
     * @return the educt
     */
    @Nonnull
    public EEducationType getEduct() {
        return (EEducationType) educt;
    }

    /**
     * As no other refinement sequence is planned this will be the way for now.
     *
     * @return the product
     */
    @Nonnull
    public EEducationType getProduct() {
        return (EEducationType) product;
    }
}
