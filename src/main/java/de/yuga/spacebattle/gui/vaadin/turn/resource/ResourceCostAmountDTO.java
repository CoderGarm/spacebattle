package de.yuga.spacebattle.gui.vaadin.turn.resource;

import de.yuga.spacebattle.backend.enums.EResourceType;

import javax.annotation.Nonnull;

/**
 * Wraps a {@link EResourceType} and it's amount to display it as cost.
 */
public class ResourceCostAmountDTO extends ResourceDetailDTO {

    private final long amount;

    public ResourceCostAmountDTO(@Nonnull final EResourceType resourceType, final long amount) {
        super(resourceType);

        this.amount = amount;
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
        return amount + "";
    }

    @Nonnull
    public Long getAmount() {
        return amount;
    }
}
