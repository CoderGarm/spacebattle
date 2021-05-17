package de.yuga.spacebattle.gui.vaadin.orbitals.starmap;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.svg.elements.AbstractPolyElement;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Hols the area that is used by a fleet shark to make this are occupied at the canvas.
 */
class RestrictedFleetArea {

    private double minX = Double.MAX_VALUE;
    private double maxX = -1 * Double.MAX_VALUE;
    private double minY = Double.MAX_VALUE;
    private double maxY = -1 * Double.MAX_VALUE;
    private boolean isValid = false;

    /**
     * The fleet which occupies the here defined area.
     */
    @Nonnull
    private Fleet fleetInSpace;

    public RestrictedFleetArea(@Nonnull final List<AbstractPolyElement.PolyCoordinatePair> pointList, @Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(pointList, "pointList shouldn't be null!");

        pointList.forEach(polyPair -> {
            double polyX = polyPair.getPolyX();
            if (polyX < minX) {
                minX = polyX;
            }
            if (polyX > maxX) {
                maxX = polyX;
            }

            double polyY = polyPair.getPolyY();
            if (polyY < minY) {
                minY = polyY;
            }
            if (polyY > maxY) {
                maxY = polyY;
            }
            isValid = true;
        });
        fleetInSpace = fleet;
    }

    public boolean isValid() {
        return isValid;
    }

    /**
     * Checks if the elements are out of the restricted range or not.
     *
     * @param pointList the parameter to check against the restricted are
     * @return <code>true</code> if the restricted are is violated, <code>false</code> otherwiese
     */
    public boolean isNotFree(@Nonnull final List<AbstractPolyElement.PolyCoordinatePair> pointList) {
        Preconditions.checkNotNull(pointList, "pointList shouldn't be null!");

        AtomicBoolean isNotFree = new AtomicBoolean(false);
        pointList.forEach(polyPair -> {
            final double xCoordinate = returnWithALittleSpace(polyPair.getPolyX());
            final double yCoordinate = returnWithALittleSpace(polyPair.getPolyY());
            if (xCoordinate > minX && xCoordinate < maxX && yCoordinate > minY && yCoordinate < maxY) {
                isNotFree.set(true);
            }
        });
        return isNotFree.get();
    }

    /**
     * Only to adjust if there is a demand for a bigger restricted are.
     *
     * @param coord the coord to adjust
     * @return the adjusted value
     */
    private double returnWithALittleSpace(final double coord) {
        return coord * 1;
    }

    @Nonnull
    public Fleet getFleetInSpace() {
        return fleetInSpace;
    }

    /**
     * Checks if the given coordinates inside this specific area.
     *
     * @param xCoordinate the x coordinate
     * @param yCoordinate the y coordinate
     * @return <code>true</code> if the coordinates are inside, <code>false</code> otherwise
     */
    public boolean isInside(final double xCoordinate, final double yCoordinate) {
        if (xCoordinate > minX && xCoordinate < maxX && yCoordinate > minY && yCoordinate < maxY) {
            return true;
        }
        return false;
    }

    /**
     * Returns the height of this.
     *
     * @return the height
     */
    public double getHeight() {
        return Math.abs(Math.abs(maxY) - Math.abs(minY));
    }
}
