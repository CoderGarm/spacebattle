package de.yuga.spacebattle.gui.vaadin.spacecrafts.details;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.spacecrafts.Module;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Wraps a {@link Module} and it's amount.
 */
public class ModuleAmountWrapper {

    @Nonnull
    private final Module module;

    @Nonnull
    private Integer amount;

    public ModuleAmountWrapper(@Nonnull final Module module, @Nonnull final Integer amount) {
        Preconditions.checkNotNull(module, "module shouldn't be null!");
        Preconditions.checkNotNull(amount, "amount shouldn't be null!");

        this.module = module;
        this.amount = amount;
    }

    @Nonnull
    public Module getModule() {
        return module;
    }

    @Nonnull
    public String getModuleName() {
        return module.getName();
    }

    @Nonnull
    public String getModuleDescription() {
        return module.getDescription();
    }

    @Nonnull
    public Integer getAmountNumeric() {
        return amount;
    }

    @Nonnull
    public String getAmount() {
        return String.valueOf(amount);
    }

    public void setAmount(@Nonnull final Integer amount) {
        Preconditions.checkNotNull(amount, "amount shouldn't be null!");

        this.amount = amount;
    }

    public void setAmount(@Nonnull final String amount) {
        Preconditions.checkNotNull(amount, "amount shouldn't be null!");

        this.amount = Integer.parseInt(amount);
    }

    /**
     * Necessary while vaadin data binding uses this entry to compute further.
     *
     * @return the entry which represents this wrapper
     */
    public Map.Entry<Module, Integer> getAsEntry() {
        return new Map.Entry<Module, Integer>() {
            @Override
            public Module getKey() {
                return module;
            }

            @Override
            public Integer getValue() {
                return amount;
            }

            @Override
            public Integer setValue(Integer value) {
                amount = value;
                return amount;
            }
        };
    }
}
