package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;

import javax.annotation.Nonnull;
import java.util.Arrays;

public enum EFleetSizeType implements HasIconName {

    SMALL_FLEET(10, "small_fleet"),
    ;

    final int fleetSize;

    @Nonnull
    final String iconName;

    EFleetSizeType(final int fleetSize,
                   @Nonnull final String iconName) {
        Preconditions.checkNotNull(iconName, "iconName shouldn't be null!");

        this.fleetSize = fleetSize;
        this.iconName = iconName;
    }

    public int getFleetSize() {
        return fleetSize;
    }

    @Nonnull
    @Override
    public String getIconName() {
        return iconName;
    }

    public static EFleetSizeType getByFleet(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        final int size = fleet.getAliveShips().size();
        return Arrays.stream(EFleetSizeType.values()).filter(e -> e.getFleetSize() >= size)
                .findFirst()
                .orElse(EFleetSizeType.values()[EFleetSizeType.values().length - 1]);
    }
}
