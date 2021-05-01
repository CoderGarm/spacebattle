package de.yuga.spacebattle.gui.vaadin.combined.spacecrafts.details.fleetsplit;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;

import javax.annotation.Nonnull;

/**
 * DTO to split the amount of a single ship class as part of a fleet.
 */
public class ShipClassCountSplitDTO implements Cloneable {

    /**
     * The ship class which detachment will be divided.
     */
    @Nonnull
    private final ShipClass shipClass;

    /**
     * The reference amount from the original fleet. More than that couldn't be distributed.
     */
    private final int referenceAmount;

    /**
     * The payload of this dto.
     * The key represents the fleet-in-creation in the display as # and
     * the value is the detached amount of this ship class.
     */
    private int splitCount = 0;

    public ShipClassCountSplitDTO(@Nonnull final ShipClass shipClass,
                                  final int referenceAmount) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");

        this.shipClass = shipClass;
        this.referenceAmount = referenceAmount;
    }

    private ShipClassCountSplitDTO(@Nonnull final ShipClassCountSplitDTO dto) {
        Preconditions.checkNotNull(dto, "dto shouldn't be null!");

        this.shipClass = dto.getShipClass();
        this.referenceAmount = dto.getReferenceAmountNumeric();
        this.splitCount = dto.getSplitCount();
    }

    @Nonnull
    public String getName() {
        return shipClass.getName();
    }

    @Nonnull
    public ShipClass getShipClass() {
        return shipClass;
    }

    public int getReferenceAmountNumeric() {
        return referenceAmount;
    }

    public String getReferenceAmount() {
        return String.valueOf(getReferenceAmountNumeric());
    }

    public int getCalculatedReferenceAmountNumeric() {
        return referenceAmount - splitCount;
    }

    public String getCalculatedReferenceAmount() {
        return String.valueOf(getCalculatedReferenceAmountNumeric());
    }

    public int getSplitCount() {
        return splitCount;
    }

    public void setSplitCount(final int splitCount) {
        this.splitCount = splitCount;
    }

    public String getSplitCountString() {
        return String.valueOf(splitCount);
    }

    public void setSplitCountString(final String splitCount) {
        this.splitCount = Integer.parseInt(splitCount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShipClassCountSplitDTO)) return false;

        ShipClassCountSplitDTO that = (ShipClassCountSplitDTO) o;

        return shipClass.equals(that.shipClass);
    }

    @Override
    public int hashCode() {
        return shipClass.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ShipClassCountSplitDTO{");
        sb.append(", name='").append(getName()).append('\'');
        sb.append(", splitCount=").append(splitCount);
        sb.append(", referenceAmountNumeric=").append(getReferenceAmountNumeric());
        sb.append(", calculatedReferenceAmountNumeric=").append(getCalculatedReferenceAmountNumeric());
        sb.append(", calculatedReferenceAmount='").append(getCalculatedReferenceAmount()).append('\'');
        sb.append(", referenceAmount=").append(referenceAmount);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public ShipClassCountSplitDTO clone() {
        return new ShipClassCountSplitDTO(this);
    }
}
