package de.yuga.spacebattle.gui.vaadin.misc.details;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EResourceType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;

import static de.yuga.spacebattle.backend.entities.ResourceDeposit.mathContext;

/**
 * Wraps a {@link EResourceType} and it's amount.
 */
public class EResourceAmountWrapper {

    @Nonnull
    private final EResourceType resourceType;

    @Nonnull
    private final BigDecimal amount;

    @Nullable
    private final BigDecimal tickOutput;

    public EResourceAmountWrapper(@Nonnull final EResourceType resourceType,
                                  @Nonnull final BigDecimal amount,
                                  @Nullable final BigDecimal tickOutput) {
        Preconditions.checkNotNull(resourceType, "resourceType shouldn't be null!");
        Preconditions.checkNotNull(amount, "amount shouldn't be null!");

        this.resourceType = resourceType;
        this.amount = amount.setScale(0, mathContext.getRoundingMode());
        this.tickOutput = tickOutput != null ? tickOutput.setScale(0, mathContext.getRoundingMode()) : null;
    }

    public String getAmountWithDiff() {
        String text = amount.toString();
        if (tickOutput != null) {
            text += " (" + tickOutput.toString() + ")";
        }
        return text;
    }

    @Nonnull
    public EResourceType getResourceType() {
        return resourceType;
    }
}
