package de.yuga.spacebattle.gui.vaadin.orbitals.starmap;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.svg.elements.Circle;
import com.vaadin.flow.component.svg.elements.SvgElement;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;

import javax.annotation.Nonnull;

/**
 * Hols the area that is used by a {@link SvgElement} to make this are occupied at the canvas.
 * Simple model based on the half of the edge length here named radius.
 */
class OccupiedArea {

    /**
     * The lower x bound.
     */
    private final double minX;

    /**
     * The upper x bound.
     */
    private final double maxX;

    /**
     * The lower y bound.
     */
    private final double minY;

    /**
     * The upper y bound.
     */
    private final double maxY;

    /**
     * The svg element which is related to this boundaries, e.g. a {@link Circle} for a {@link Planet}.
     */
    @Nonnull
    private final SvgElement areaOwner;

    /**
     * The object which is related to the svg element - e.g. a {@link Planet} to a {@link Circle}.
     */
    @Nonnull
    private final Object relatedObject;

    public OccupiedArea(@Nonnull final Object relatedObject,
                        @Nonnull final SvgElement areaOwner,
                        @Nonnull final Orbit orbit,
                        final double radius) {
        Preconditions.checkNotNull(relatedObject, "relatedObject shouldn't be null!");
        Preconditions.checkNotNull(areaOwner, "areaOwner shouldn't be null!");
        Preconditions.checkNotNull(orbit, "orbit shouldn't be null!");

        this.relatedObject = relatedObject;
        this.areaOwner = areaOwner;

        final int xCoordinate = orbit.getXCoordinate();
        minX = xCoordinate - radius;
        maxX = xCoordinate + radius;

        final int yCoordinate = orbit.getYCoordinate();
        minY = yCoordinate - radius;
        maxY = yCoordinate + radius;
    }

    @Nonnull
    public SvgElement getAreaOwner() {
        return areaOwner;
    }

    @Nonnull
    public Object getRelatedObject() {
        return relatedObject;
    }

    /**
     * Checks if the given coordinates are inside the bound.
     *
     * @param xCoordinate the xCoord
     * @param yCoordinate the yCoord
     * @return <code>true</code> if both coords are inside the boundaries, <code>false</code> otherwise
     */
    public boolean checkIfInside(final double xCoordinate, final double yCoordinate) {
        if (xCoordinate > minX && xCoordinate < maxX && yCoordinate > minY && yCoordinate < maxY) {
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OccupiedArea)) return false;

        OccupiedArea that = (OccupiedArea) o;

        if (Double.compare(that.minX, minX) != 0) return false;
        if (Double.compare(that.maxX, maxX) != 0) return false;
        if (Double.compare(that.minY, minY) != 0) return false;
        if (Double.compare(that.maxY, maxY) != 0) return false;
        if (!areaOwner.equals(that.areaOwner)) return false;
        return relatedObject.equals(that.relatedObject);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(minX);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maxX);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(minY);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maxY);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + areaOwner.hashCode();
        result = 31 * result + relatedObject.hashCode();
        return result;
    }
}
