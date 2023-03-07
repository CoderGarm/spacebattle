package de.yuga.spacebattle.backend.calculator.resource;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.enums.ERefinementSequence;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

public class EducationAmountDTO {

    private final long howManyPupils;

    @Nonnull
    private final ERefinementSequence refinementSequence;

    EducationAmountDTO(final long howManyPupils, @Nonnull final ERefinementSequence refinementSequence) {
        Preconditions.checkNotNull(refinementSequence, "refinementSequence must not be empty");

        this.howManyPupils = howManyPupils;
        this.refinementSequence = refinementSequence;
    }


    long getHowManyPupils() {
        return howManyPupils;
    }

    EducationAmountDTO reduceAmountBy(@Nonnull final BigDecimal modifier) {
        Preconditions.checkNotNull(modifier, "modifier shouldn't be null!");

        final long reduceByAmount = new BigDecimal(howManyPupils).multiply(modifier, ResourceDeposit.MATH_CONTEXT_INTEGER).longValue();
        return new EducationAmountDTO(howManyPupils - reduceByAmount, refinementSequence);
    }

    @Nonnull
    EEducationType getEduct() {
        return refinementSequence.getEduct();
    }

    @Nonnull
    EEducationType getProduct() {
        return refinementSequence.getProduct();
    }

    public boolean matches(@Nonnull final ERefinementSequence refinementSequence) {
        Preconditions.checkNotNull(refinementSequence, "refinementSequence must not be empty");

        return this.refinementSequence == refinementSequence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EducationAmountDTO)) return false;

        EducationAmountDTO that = (EducationAmountDTO) o;

        return matches(that.refinementSequence);
    }

    @Override
    public int hashCode() {
        return refinementSequence.hashCode();
    }

    @Override
    public String toString() {
        return "educt: " + getEduct() + ", product: " + getProduct() + ", pupils: " + howManyPupils;
    }
}
