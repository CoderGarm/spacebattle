package de.yuga.spacebattle.gui.vaadin.spacecrafts.details;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.enums.EHullType;
import de.yuga.spacebattle.backend.enums.EIconPath;
import de.yuga.spacebattle.backend.enums.EResolution;
import de.yuga.spacebattle.gui.vaadin.misc.details.misc.ImageMapper;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Wraps a {@link ShipClass} and it's count.
 */
public class ShipClassCountDTO implements ImageMapper {

    @Nonnull
    private final ShipClass shipClass;

    @Nonnull
    private Integer count;

    public ShipClassCountDTO(@Nonnull final ShipClass shipClass, @Nonnull final Integer count) {
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
    public String getHullClass() {
        final Hull hull = getShipClass().getHull();
        if (hull == null) {
            return "";
        }
        return hull.getHullType().getType();
    }

    @Nonnull
    public String getHullDescription() {
        final Hull hull = getShipClass().getHull();
        if (hull == null) {
            return "";
        }
        return hull.getHullType().getDescription();
    }

    @Nonnull
    public String getName() {
        return shipClass.getName();
    }

    @Nonnull
    public Integer getCountNumeric() {
        return count;
    }

    @Nonnull
    public String getCount() {
        return String.valueOf(count);
    }

    public void setCountNumeric(@Nonnull final Integer count) {
        Preconditions.checkNotNull(count, "amount shouldn't be null!");

        this.count = count;
    }

    public void setCount(@Nonnull final String amount) {
        Preconditions.checkNotNull(amount, "amount shouldn't be null!");

        this.count = Integer.parseInt(amount);
    }

    public String getMark() {
        return "" + getShipClass().getMark();
    }

    /**
     * Necessary while vaadin data binding uses this entry to compute further.
     *
     * @return the entry which represents this wrapper
     */
    public Map.Entry<ShipClass, Integer> getAsEntry() {
        return new Map.Entry<>() {
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

    @Override
    public String getAlternativeText() {
        return getShipClass().getHull().getHullType().name();
    }

    @Override
    public String getTitleText() {
        return getAlternativeText();
    }

    @Override
    public String getPath(@Nonnull final EResolution resolution) {
        Preconditions.checkNotNull(resolution, "resolution shouldn't be null!");

        final EHullType hullType = getShipClass().getHull().getHullType();
        final String iconName = hullType.getIconName();
        return EIconPath.getPath(hullType, iconName, resolution.getResolution());
    }

    /**
     * The key of this dto is the ship class and this has to be the truth for all cases where the dto is used.
     *
     * @param o the dto to check
     * @return is equals or not
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShipClassCountDTO)) return false;

        ShipClassCountDTO that = (ShipClassCountDTO) o;

        return shipClass.equals(that.shipClass);
    }

    /**
     * The hash of this dto is the hash of it's key (the ship class)
     * and this has to be the truth for all cases where the dto is used.
     */
    @Override
    public int hashCode() {
        return shipClass.hashCode();
    }
}
