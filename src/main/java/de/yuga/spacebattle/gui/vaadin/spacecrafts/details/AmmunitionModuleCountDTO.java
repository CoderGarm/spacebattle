package de.yuga.spacebattle.gui.vaadin.spacecrafts.details;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.AmmunitionModule;

import javax.annotation.Nonnull;

/**
 * Wraps a {@link AmmunitionModule} and it's amount.
 */
public class AmmunitionModuleCountDTO {

    @Nonnull
    private final AmmunitionModule ammunitionModule;

    @Nonnull
    private Integer count;

    public AmmunitionModuleCountDTO(@Nonnull final AmmunitionModule ammunitionModule, @Nonnull final Integer count) {
        Preconditions.checkNotNull(ammunitionModule, "ammunitionModule shouldn't be null!");
        Preconditions.checkNotNull(count, "amount shouldn't be null!");

        this.ammunitionModule = ammunitionModule;
        this.count = count;
    }

    @Nonnull
    public AmmunitionModule getAmmunitionModule() {
        return ammunitionModule;
    }

    @Nonnull
    public String getName() {
        return ammunitionModule.getName();
    }

    @Nonnull
    public String getDescription() {
        return ammunitionModule.getDescription();
    }

    @Nonnull
    public String getSupportsWhatDescription() {
        final String weaponName = ammunitionModule.getWeapon().getName();
        final int effectValue = ammunitionModule.getEffectValue();
        return "Provides : " + effectValue + " salvos for " + weaponName;
    }

    @Nonnull
    public Integer getCount() {
        return count;
    }

    public void setCount(@Nonnull final Integer count) {
        Preconditions.checkNotNull(count, "count shouldn't be null!");

        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AmmunitionModuleCountDTO)) return false;

        AmmunitionModuleCountDTO that = (AmmunitionModuleCountDTO) o;

        return ammunitionModule.equals(that.ammunitionModule);
    }

    @Override
    public int hashCode() {
        return ammunitionModule.hashCode();
    }
}
