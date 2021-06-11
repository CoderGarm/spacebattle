package de.yuga.spacebattle.gui.vaadin.turn.resource;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.ECollectableType;
import de.yuga.spacebattle.backend.enums.EIconPath;
import de.yuga.spacebattle.backend.enums.EResolution;
import de.yuga.spacebattle.backend.enums.EResourceType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Wraps a {@link EResourceType} and it's amount.
 */
public class ResourceAmountDTO extends ResourceDetailDTO {

    private final long amount;

    @Nullable
    private final Long tickOutput;

    public ResourceAmountDTO(@Nonnull final EResourceType resourceType,
                             final long amount,
                             @Nullable final Long tickOutput) {
        super(resourceType);

        this.amount = amount;
        this.tickOutput = tickOutput;
    }

    /**
     * Returns a String like "500 (50)".
     * <p>
     * There are two possibilities:<br>
     * <br>
     * - without tick output it will return "500"<br>
     * - with tick output it will return "500 (50)"<br>
     * </p>
     *
     * @return the string
     */
    @Override
    public String getAmountAsString() {
        final ECollectableType collectableType = getResourceType().getCollectableType();
        switch (collectableType) {
            case FORFEITABLE:
                return "(" + (tickOutput != null ? tickOutput.toString() : "0") + ")";
            case COLLECTABLE:
                String text = amount + "";
                if (tickOutput != null) {
                    text += " (" + tickOutput.toString() + ")";
                }
                return text;
            case VIABLE:
            default:
                return "";
        }
    }

    @Nonnull
    public Long getAmount() {
        return amount;
    }

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
