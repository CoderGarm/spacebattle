package de.yuga.spacebattle.gui.vaadin.spacecrafts.details;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.spacecrafts.Module;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Wraps a {@link Module} and it's amount.
 */
public class ModuleCountWrapper {

    @Nonnull
    private final Module module;

    @Nonnull
    private Integer count;

    public ModuleCountWrapper(@Nonnull final Module module, @Nonnull final Integer count) {
        Preconditions.checkNotNull(module, "module shouldn't be null!");
        Preconditions.checkNotNull(count, "amount shouldn't be null!");

        this.module = module;
        this.count = count;
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
    public Integer getCountNumeric() {
        return count;
    }

    @Nonnull
    public String getCount() {
        return String.valueOf(count);
    }

    public void setCount(@Nonnull final Integer count) {
        Preconditions.checkNotNull(count, "count shouldn't be null!");

        this.count = count;
    }

    public void setCount(@Nonnull final String count) {
        Preconditions.checkNotNull(count, "count shouldn't be null!");

        this.count = Integer.parseInt(count);
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
                return count;
            }

            @Override
            public Integer setValue(Integer value) {
                count = value;
                return count;
            }
        };
    }
}
