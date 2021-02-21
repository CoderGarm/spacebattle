package de.yuga.spacebattle.gui.vaadin.misc.details;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;

/**
 * Value wrapper for vaadin data binding experiment.
 */
public class ResourceAmountWrapper {

    @Nonnull
    private final BigDecimal amount;

    @Nullable
    private final BigDecimal tickOutput;

    public ResourceAmountWrapper(@Nonnull final BigDecimal amount, @Nullable final BigDecimal tickOutput) {
        Preconditions.checkNotNull(amount, "amount shouldn't be null!");

        this.amount = amount;
        this.tickOutput = tickOutput;
    }

    public String getAmountWithDiff() {
        String text = amount.toString();
        if (tickOutput != null) {
            text += " (" + tickOutput.toString() + ")";
        }
        return text;
    }
}
