package de.yuga.spacebattle.gui.vaadin.spacecrafts.details;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EModuleType;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

/**
 * Wraps a {@link EModuleType} and it's amount.
 */
public class EModuleAmountWrapper {

    @Nonnull
    private final EModuleType eModuleType;

    @Nonnull
    private final BigDecimal amount;

    public EModuleAmountWrapper(@Nonnull final EModuleType eModuleType,
                                @Nonnull final BigDecimal amount) {
        Preconditions.checkNotNull(eModuleType, "eModuleType shouldn't be null!");
        Preconditions.checkNotNull(amount, "amount shouldn't be null!");

        this.eModuleType = eModuleType;
        this.amount = amount;
    }

    public String getAmount() {
        return amount.toString();
    }

    @Nonnull
    public EModuleType getModuleType() {
        return eModuleType;
    }
}
