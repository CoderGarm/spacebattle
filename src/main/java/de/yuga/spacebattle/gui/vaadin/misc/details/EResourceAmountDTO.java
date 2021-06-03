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
public class EResourceAmountDTO {

    @Nonnull
    private final EResourceType resourceType;

    @Nonnull
    private final BigDecimal amount;

    @Nullable
    private final BigDecimal tickOutput;

    public EResourceAmountDTO(@Nonnull final EResourceType resourceType,
                              @Nonnull final BigDecimal amount,
                              @Nullable final BigDecimal tickOutput) {
        Preconditions.checkNotNull(resourceType, "resourceType shouldn't be null!");
        Preconditions.checkNotNull(amount, "amount shouldn't be null!");

        this.resourceType = resourceType;
        this.amount = amount.setScale(0, mathContext.getRoundingMode());
        this.tickOutput = tickOutput != null ? tickOutput.setScale(0, mathContext.getRoundingMode()) : null;
    }

    /**
     * Returns a String like "500 (50)".
     * <p>
     * There are two possibilities:<br>
     * <br>
     * - without tick output it will return "500"<br>
     * - with tick output it will return "500 (50)"<br>
     * </p>
     *
     * @return the string
     */
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

    @Nonnull
    public BigDecimal getAmount() {
        return amount;
    }
}
