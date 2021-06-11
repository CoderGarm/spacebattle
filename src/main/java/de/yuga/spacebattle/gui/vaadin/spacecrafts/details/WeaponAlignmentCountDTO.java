package de.yuga.spacebattle.gui.vaadin.spacecrafts.details;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Weapon;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * Wraps a {@link Weapon} and it's amount.
 */
public class WeaponAlignmentCountDTO {

    @Nonnull
    private final Weapon weapon;

    @Nonnull
    private Integer count;

    @Nullable
    private EWeaponAlignment selectedWeaponAlignment;

    @Nonnull
    private final Set<EWeaponAlignment> allowedWeaponAlignments;

    public WeaponAlignmentCountDTO(@Nonnull final Weapon weapon, @Nonnull final Integer count) {
        Preconditions.checkNotNull(weapon, "module shouldn't be null!");
        Preconditions.checkNotNull(count, "amount shouldn't be null!");

        this.weapon = weapon;
        this.count = count;
        this.allowedWeaponAlignments = weapon.getAllowedWeaponAlignments();
    }

    @Nonnull
    public Weapon getWeapon() {
        return weapon;
    }

    @Nonnull
    public String getWeaponName() {
        return weapon.getName();
    }

    @Nonnull
    public String getWeaponDescription() {
        return weapon.getDescription();
    }

    @Nonnull
    public Integer getCount() {
        return count;
    }

    @Nullable
    public EWeaponAlignment getSelectedWeaponAlignment() {
        return selectedWeaponAlignment;
    }

    @Nonnull
    public Set<EWeaponAlignment> getAllowedWeaponAlignments() {
        return allowedWeaponAlignments;
    }

    public void setSelectedWeaponAlignment(@Nullable EWeaponAlignment selectedWeaponAlignment) {
        this.selectedWeaponAlignment = selectedWeaponAlignment;
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
        if (!(o instanceof WeaponAlignmentCountDTO)) return false;

        WeaponAlignmentCountDTO that = (WeaponAlignmentCountDTO) o;

        if (!weapon.equals(that.weapon)) return false;
        return selectedWeaponAlignment == that.selectedWeaponAlignment;
    }

    @Override
    public int hashCode() {
        int result = weapon.hashCode();
        result = 31 * result + (selectedWeaponAlignment != null ? selectedWeaponAlignment.hashCode() : 0);
        return result;
    }
}
