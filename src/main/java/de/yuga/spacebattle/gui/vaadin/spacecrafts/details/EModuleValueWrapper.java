package de.yuga.spacebattle.gui.vaadin.spacecrafts.details;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EModuleType;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

/**
 * Wraps a {@link EModuleType} and it's value.
 */
public class EModuleValueWrapper {

    @Nonnull
    private final EModuleType eModuleType;

    @Nonnull
    private final BigDecimal value;

    public EModuleValueWrapper(@Nonnull final EModuleType eModuleType,
                               @Nonnull final BigDecimal value) {
        Preconditions.checkNotNull(eModuleType, "eModuleType shouldn't be null!");
        Preconditions.checkNotNull(value, "value shouldn't be null!");

        this.eModuleType = eModuleType;
        this.value = value;
    }

    public String getValue() {
        return value.toString();
    }

    @Nonnull
    public EModuleType getModuleType() {
        return eModuleType;
    }
}
