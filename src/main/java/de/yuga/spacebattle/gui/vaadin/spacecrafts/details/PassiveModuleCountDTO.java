package de.yuga.spacebattle.gui.vaadin.spacecrafts.details;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.PassiveModule;
import de.yuga.spacebattle.backend.enums.ECalculationType;
import de.yuga.spacebattle.backend.enums.ESupportType;

import javax.annotation.Nonnull;

/**
 * Wraps a {@link PassiveModule} and it's amount.
 */
public class PassiveModuleCountDTO {

    @Nonnull
    private final PassiveModule passiveModule;

    @Nonnull
    private Integer count;

    public PassiveModuleCountDTO(@Nonnull final PassiveModule passiveModule, @Nonnull final Integer count) {
        Preconditions.checkNotNull(passiveModule, "ammunitionModule shouldn't be null!");
        Preconditions.checkNotNull(count, "amount shouldn't be null!");

        this.passiveModule = passiveModule;
        this.count = count;
    }

    @Nonnull
    public PassiveModule getPassiveModule() {
        return passiveModule;
    }

    @Nonnull
    public String getName() {
        return passiveModule.getName();
    }

    @Nonnull
    public String getDescription() {
        return passiveModule.getDescription();
    }

    @Nonnull
    public String getSupportsWhatDescription() {
        final ESupportType supportType = passiveModule.getSupportType();
        final ECalculationType calculationType = passiveModule.getCalculationType();
        final int effectValue = passiveModule.getEffectValue();
        return "Support property: " + supportType.name() + " with " + (effectValue * calculationType.getMultiplier() + "%");
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PassiveModuleCountDTO)) return false;

        PassiveModuleCountDTO that = (PassiveModuleCountDTO) o;

        return passiveModule.equals(that.passiveModule);
    }

    @Override
    public int hashCode() {
        return passiveModule.hashCode();
    }
}
