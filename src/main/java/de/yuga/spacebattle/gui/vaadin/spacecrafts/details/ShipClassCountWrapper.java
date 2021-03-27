package de.yuga.spacebattle.gui.vaadin.spacecrafts.details;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Wraps a {@link ShipClass} and it's count.
 */
public class ShipClassCountWrapper {

    @Nonnull
    private final ShipClass shipClass;

    @Nonnull
    private Integer count;

    public ShipClassCountWrapper(@Nonnull final ShipClass shipClass, @Nonnull final Integer count) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");
        Preconditions.checkNotNull(count, "count shouldn't be null!");

        this.shipClass = shipClass;
        this.count = count;
    }

    @Nonnull
    public ShipClass getShipClass() {
        return shipClass;
    }

    @Nonnull
    public String getName() {
        return shipClass.getName();
    }

    @Nonnull
    public Integer getAmountNumeric() {
        return count;
    }

    @Nonnull
    public String getCount() {
        return String.valueOf(count);
    }

    public void setCount(@Nonnull final Integer count) {
        Preconditions.checkNotNull(count, "amount shouldn't be null!");

        this.count = count;
    }

    public void setAmount(@Nonnull final String amount) {
        Preconditions.checkNotNull(amount, "amount shouldn't be null!");

        this.count = Integer.parseInt(amount);
    }

    /**
     * Necessary while vaadin data binding uses this entry to compute further.
     *
     * @return the entry which represents this wrapper
     */
    public Map.Entry<ShipClass, Integer> getAsEntry() {
        return new Map.Entry<ShipClass, Integer>() {
            @Override
            public ShipClass getKey() {
                return shipClass;
            }

            @Override
            public Integer getValue() {
                return count;
            }

            @Override
            public Integer setValue(Integer value) {
                count = value;
                return count;
            }
        };
    }
}
