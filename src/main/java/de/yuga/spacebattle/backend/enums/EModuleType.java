package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;

import javax.annotation.Nonnull;

public enum EModuleType implements HasIconName {

    WEAPON("Attack", "attack"),
    ARMOR("Armor", "armor"),
    SIDEWALL("Shield", "shield"),

    /**
     * As coordinates are in light seconds it has the native metric of {@link EDistanceMetric#LS}.
     */
    PROPULSION("Propulsion", "propulsion"),

    /**
     * As coordinates are in light seconds it has the native metric of {@link EDistanceMetric#LY}.
     */
    FTLPROPULSION("FTLPropulsion", "ftlpropulsion"),
    ELECTRONIC_WARFARE("Electronic warfare", "scanner");

    @Nonnull
    final String name;

    @Nonnull
    final String iconName;

    EModuleType(@Nonnull final String name,
                @Nonnull final String iconName) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(iconName, "iconName shouldn't be null!");

        this.name = name;
        this.iconName = iconName;
    }

    @Nonnull
    public String getName() {
        return name;
    }


    @Nonnull
    @Override
    public String getIconName() {
        return iconName;
    }
}
