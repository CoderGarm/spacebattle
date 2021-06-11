package de.yuga.spacebattle.gui.vaadin.turn.resource;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EIconPath;
import de.yuga.spacebattle.backend.enums.EResolution;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.gui.vaadin.misc.details.misc.ImageMapper;

import javax.annotation.Nonnull;

/**
 * Wraps a {@link EResourceType} and it's amount.
 */
public abstract class ResourceDetailDTO implements ImageMapper {

    @Nonnull
    private final EResourceType resourceType;

    public ResourceDetailDTO(@Nonnull final EResourceType resourceType) {
        Preconditions.checkNotNull(resourceType, "resourceType shouldn't be null!");

        this.resourceType = resourceType;
    }

    @Nonnull
    public EResourceType getResourceType() {
        return resourceType;
    }

    /**
     * Must create a string representation for the resource's amount.
     *
     * @return the string representation
     */
    public abstract String getAmountAsString();

    @Override
    public String getAlternativeText() {
        return getResourceType().getSingularName();
    }

    @Override
    public String getTitleText() {
        return getResourceType().getSingularName();
    }

    @Override
    public String getPath(@Nonnull final EResolution resolution) {
        Preconditions.checkNotNull(resolution, "resolution shouldn't be null!");

        final EResourceType resourceType = getResourceType();
        final String iconName = resourceType.getIconName();
        return EIconPath.getPath(resourceType, iconName, resolution.getResolution());
    }
}
