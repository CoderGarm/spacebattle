package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;

public enum EBuildingType implements HasIconName {

    BUILDING("Generic Building", "building"),
    ;

    @Nonnull
    final String name;

    @Nonnull
    final String iconName;

    EBuildingType(@Nonnull final String name,
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
