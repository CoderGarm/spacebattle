package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;

public enum EModuleType implements HasIconName {

    WEAPON("Attack", "attack"),
    ARMOR("Armor", "armor"),
    SHIELD("Shield", "shield"),
    PROPULSION("Propulsion", "propulsion"),
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
