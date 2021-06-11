package de.yuga.spacebattle.gui.vaadin.combined.spacecrafts.details;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.enums.EHullType;
import de.yuga.spacebattle.backend.enums.EIconPath;
import de.yuga.spacebattle.backend.enums.EResolution;
import de.yuga.spacebattle.gui.vaadin.misc.details.misc.ImageMapper;

import javax.annotation.Nonnull;

/**
 * Wraps a {@link Hull} and it's value.
 */
public class HullAmountWrapper implements ImageMapper {

    @Nonnull
    private final Hull hull;

    private final int value;

    public HullAmountWrapper(@Nonnull final Hull hull,
                             final int value) {
        Preconditions.checkNotNull(hull, "hull shouldn't be null!");

        this.hull = hull;
        this.value = value;
    }

    public String getValue() {
        return String.valueOf(value);
    }

    @Nonnull
    public Hull getHull() {
        return hull;
    }

    @Override
    public String getAlternativeText() {
        return getHull().getName() + " (" + getHull().getHullType().getType() + ")";
    }

    @Override
    public String getTitleText() {
        return getAlternativeText();
    }

    @Override
    public String getPath(@Nonnull final EResolution resolution) {
        Preconditions.checkNotNull(resolution, "resolution shouldn't be null!");

        final EHullType resourceType = getHull().getHullType();
        final String iconName = resourceType.getIconName();
        return EIconPath.getPath(resourceType, iconName, resolution.getResolution());
    }
}
