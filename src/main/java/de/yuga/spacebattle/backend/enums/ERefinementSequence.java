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

    EDUCATION_CIVIL_I(0, EEducationType.NONE, EEducationType.SCHOOL),
    EDUCATION_CIVIL_II(1, EEducationType.SCHOOL, EEducationType.COLLEGE),
    EDUCATION_CIVIL_III(1, EEducationType.COLLEGE, EEducationType.UNIVERSITY),

    EDUCATION_MILITARY_I(2, EEducationType.COLLEGE, EEducationType.ENLISTED),
    EDUCATION_MILITARY_II(2, EEducationType.UNIVERSITY, EEducationType.OFFICER),
    ;

    private final int educationPriority;

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

    ERefinementSequence(final int educationPriority, @Nonnull final Enum<?> educt, @Nonnull final Enum<?> product) {
        Preconditions.checkNotNull(educt, "educt shouldn't be null!");
        Preconditions.checkNotNull(product, "product shouldn't be null!");

        this.educationPriority = educationPriority;
        this.educt = educt;
        this.product = product;
    }

    public int getEducationPriority() {
        return educationPriority;
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
