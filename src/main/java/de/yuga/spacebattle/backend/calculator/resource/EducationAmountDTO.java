package de.yuga.spacebattle.backend.calculator.resource;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EEducationType;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

class EducationAmountDTO {

    private long howManyPupils;

    @Nonnull
    private final EEducationType educt;

    @Nonnull
    private final EEducationType product;

    EducationAmountDTO(final long howManyPupils,
                       @Nonnull final EEducationType educt,
                       @Nonnull final EEducationType product) {
        Preconditions.checkNotNull(educt, "educt shouldn't be null!");
        Preconditions.checkNotNull(product, "product shouldn't be null!");

        this.howManyPupils = howManyPupils;
        this.educt = educt;
        this.product = product;
    }

    long getHowManyPupils() {
        return howManyPupils;
    }

    void reduceAmountBy(@Nonnull final BigDecimal modifier) {
        Preconditions.checkNotNull(modifier, "modifier shouldn't be null!");

        final long reduceByAmount = new BigDecimal(howManyPupils).multiply(modifier, ResourceDeposit.MATH_CONTEXT_INTEGER).longValue();
        howManyPupils = howManyPupils - reduceByAmount;
    }

    @Nonnull
    EEducationType getEduct() {
        return educt;
    }

    @Nonnull
    EEducationType getProduct() {
        return product;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EducationAmountDTO)) return false;

        EducationAmountDTO that = (EducationAmountDTO) o;

        if (educt != that.educt) return false;
        return product == that.product;
    }

    @Override
    public int hashCode() {
        int result = educt.hashCode();
        result = 31 * result + product.hashCode();
        return result;
    }
}
