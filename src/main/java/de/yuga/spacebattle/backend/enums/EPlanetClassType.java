package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;

public enum EPlanetClassType implements HasIconName {

    PLANET("Generic planet", "M", "planet"),
    ;

    @Nonnull
    final String name;

    @Nonnull
    final String planetClass;

    @Nonnull
    final String iconName;

    EPlanetClassType(@Nonnull final String name,
                     @Nonnull final String planetClass,
                     @Nonnull final String iconName) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(planetClass, "planetClass shouldn't be null!");
        Preconditions.checkNotNull(iconName, "iconName shouldn't be null!");

        this.name = name;
        this.planetClass = planetClass;
        this.iconName = iconName;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public String getPlanetClass() {
        return planetClass;
    }

    @Nonnull
    @Override
    public String getIconName() {
        return iconName;
    }
}
