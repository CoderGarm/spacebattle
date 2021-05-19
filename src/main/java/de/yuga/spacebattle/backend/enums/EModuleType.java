package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;

public enum EModuleType {

    WEAPON("Attack", EIconPath.STATS.getPath(), "attack"),
    ARMOR("Armor", EIconPath.STATS.getPath(), "armor"),
    SHIELD("Shield", EIconPath.STATS.getPath(), "shield"),
    PROPULSION("Propulsion", EIconPath.STATS.getPath(), "propulsion"),
    FTLPROPULSION("FTLPropulsion", EIconPath.STATS.getPath(), "ftlpropulsion"),
    ELECTRONIC_WARFARE("Electronic warfare", EIconPath.STATS.getPath(), "scanner");

    @Nonnull
    final String name;

    @Nonnull
    final String directory;

    @Nonnull
    final String iconName;

    EModuleType(@Nonnull final String name,
                @Nonnull final String directory,
                @Nonnull final String iconName) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(directory, "directory shouldn't be null!");
        Preconditions.checkNotNull(iconName, "iconName shouldn't be null!");

        this.name = name;
        this.directory = directory;
        this.iconName = iconName;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public String getDirectory() {
        return directory;
    }

    @Nonnull
    public String getIconName() {
        return iconName;
    }
}
