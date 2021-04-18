package de.yuga.spacebattle.gui.vaadin.misc;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.svg.Svg;
import com.vaadin.flow.component.svg.elements.AbstractPolyElement;
import com.vaadin.flow.component.svg.elements.Circle;
import com.vaadin.flow.component.svg.elements.Line;
import com.vaadin.flow.component.svg.elements.Path;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.gui.vaadin.orbitals.details.Canvas;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This defines the view box for the {@link Svg} canvas.
 */
public class ViewBoxDefinition {

    /**
     * What this canvas is for.
     */
    public enum EViewBoxType {
        /**
         * Is the canvas is for the universe.
         */
        UNIVERSE,
        /**
         * Is the canvas is for a star system.
         */
        STAR_SYSTEM
    }

    public static final int PLANET_RADIUS = 10;
    public static final int SYSTEM_RADIUS = 5;
    public static final int STAR_RADIUS = 30;
    public static final int UNIVERSE_CENTER_RADIUS = 50;

    private static final String AXIS_COLOR = "white";
    private static final String ORBIT_COLOR = "white";

    @Nonnull
    private final Svg canvas;

    @Nonnull
    private final Set<Orbit> orbits = new HashSet<>();

    @Nonnull
    private final EViewBoxType eViewBoxType;

    private final double viewBoxFactor;
    private double biggestRadius = Double.MIN_NORMAL;

    /**
     * Creates the view box and coordinate system for the given canvas by the given orbits.
     *
     * @param eViewBoxType for which kind of system the canvas is for
     * @param orbits       the included orbits
     * @param canvas       the canvas to update
     */
    public ViewBoxDefinition(@Nonnull final EViewBoxType eViewBoxType, @Nonnull final Collection<Orbit> orbits, @Nonnull final Svg canvas) {
        Preconditions.checkNotNull(eViewBoxType, "eViewBoxType shouldn't be null!");
        Preconditions.checkNotNull(orbits, "orbits shouldn't be null!");
        Preconditions.checkArgument(!orbits.isEmpty(), "orbits must not be empty");
        Preconditions.checkNotNull(canvas, "canvas shouldn't be null!");

        orbits.forEach(this::update);
        this.orbits.addAll(orbits);
        this.eViewBoxType = eViewBoxType;
        viewBoxFactor = getViewBoxFactor();
        this.canvas = canvas;
        adjustCanvas();
    }

    /**
     * Checks for some needful parameters.
     *
     * @param orbit the orbit to check.
     */
    private void update(@Nonnull final Orbit orbit) {
        Preconditions.checkNotNull(orbit, "orbit shouldn't be null!");

        final int xCoordinate = orbit.getXCoordinate();
        final int yCoordinate = orbit.getYCoordinate();

        final double radius = Math.sqrt((xCoordinate * xCoordinate) + (yCoordinate * yCoordinate));
        if (radius > biggestRadius) {
            biggestRadius = radius;
        }
    }

    /**
     * Returns the factor which is used to set the size of the view box for the canvas.
     *
     * @return the factor
     */
    private double getViewBoxFactor() {
        switch (eViewBoxType) {
            case STAR_SYSTEM:
                return 2;
            case UNIVERSE:
            default:
                return 1;
        }
    }

    /**
     * Puts all the stuff to the canvas and sets the initial view box.
     */
    public void adjustCanvas() {
        switch (eViewBoxType) {
            case STAR_SYSTEM:
                createVisibleOrbits();
                createCenter();
                break;
            case UNIVERSE:
            default:
                break;
        }
        createAxis();

        double viewBoxMinX = getViewBoxMinX();
        double viewBoxMinY = getViewBoxMinY();
        double viewBoxHeight = getViewBoxHeight();
        double viewBoxWidth = getViewBoxWidth();
        canvas.viewbox(viewBoxMinX, viewBoxMinY, viewBoxWidth, viewBoxHeight);

    }

    /**
     * Returns the biggest included radius as <b>absolute value</b>.
     *
     * @return the biggest radius <b>absolute value</b>
     */
    public double getBiggestRadius() {
        return Math.abs(biggestRadius);
    }

    /**
     * Calculates and returns the initial view box width.
     *
     * @return the view box width to set
     */
    private double getViewBoxWidth() {
        final double biggestRadius = getBiggestRadius();
        return (biggestRadius * 2) * viewBoxFactor;
    }

    /**
     * Calculates and returns the initial view box height.
     *
     * @return the view box height to set
     */
    private double getViewBoxHeight() {
        final double biggestRadius = getBiggestRadius();
        return (biggestRadius) * viewBoxFactor;
    }

    /**
     * Calculates and returns the smallest digit on the y-axis which must be displayed.
     *
     * @return the smallest y digit
     */
    private double getViewBoxMinY() {
        final double biggestRadius = getBiggestRadius();
        return (biggestRadius / -2) * viewBoxFactor;
    }

    /**
     * Calculates and returns the smallest digit on the x-axis which must be displayed.
     *
     * @return the smallest x digit
     */
    private double getViewBoxMinX() {
        final double biggestRadius = getBiggestRadius();
        return (biggestRadius * -1) * viewBoxFactor;
    }

    /**
     * Creates the sun or the universe center.
     */
    private void createCenter() {

        final String id;
        final String color;
        final int radius;

        switch (eViewBoxType) {
            case STAR_SYSTEM:
                id = "sun-" + canvas.hashCode();
                color = "yellow";
                radius = STAR_RADIUS;
                break;
            case UNIVERSE:
            default:
                id = "center-of-the-universe";
                color = "grey";
                radius = UNIVERSE_CENTER_RADIUS;
                break;
        }
        final Circle sun = new Circle(id, radius);
        sun.center(0, 0);
        sun.setDraggable(false);
        sun.setFillColor(color);
        canvas.add(sun);
    }

    /**
     * Creates the visible orbits for the known planets.
     */
    private void createVisibleOrbits() {

        orbits.forEach(orbit -> {
            final int xCoord = Math.abs(orbit.getXCoordinate());
            final int yCoord = Math.abs(orbit.getYCoordinate());

            final double radius = Math.sqrt((xCoord * xCoord) + (yCoord * yCoord));

            final Circle circle = new Circle("ellipsoid-" + orbit.getOrbitID(), radius);
            circle.setFillColor("transparent");
            circle.setStroke(ORBIT_COLOR, 1);
            circle.center(0, 0);
            canvas.add(circle);
        });
    }

    /**
     * Creates the coordinate cross.
     */
    private void createAxis() {
        final double biggestRadius = getBiggestRadius() * 1.2;
        createScaleDivider(biggestRadius, biggestRadius);
        createLine("xLine", -1 * biggestRadius, biggestRadius, 0, 0, AXIS_COLOR);
        createLine("yLine", 0, 0, -1 * biggestRadius, biggestRadius, AXIS_COLOR);
    }

    /**
     * Creates the scale dividers for this coordinate cross.
     *
     * @param width  the width of the coordinate system
     * @param height the height of the coordinate system
     */
    private void createScaleDivider(final double width, final double height) {

        int countOfMiniLinesX = getScaleDividerCount(width);
        for (; countOfMiniLinesX > 0; countOfMiniLinesX--) {
            final double xScale = 10 * countOfMiniLinesX;
            int yRange = getScaleDividerWidth(countOfMiniLinesX);
            final String id = "axisScaleX";
            createLine(id + countOfMiniLinesX, xScale, xScale, -1 * yRange, yRange, AXIS_COLOR);
            createLine(id + (-1 * countOfMiniLinesX), -1 * xScale, -1 * xScale, -1 * yRange, yRange, AXIS_COLOR);
        }

        int countOfMiniLinesY = getScaleDividerCount(height);
        for (; countOfMiniLinesY > 0; countOfMiniLinesY--) {
            final double yScale = 10 * countOfMiniLinesY;
            int xRange = getScaleDividerWidth(countOfMiniLinesY);
            final String id = "axisScaleY";
            createLine(id + countOfMiniLinesY, -1 * xRange, xRange, yScale, yScale, AXIS_COLOR);
            createLine(id + (-1 * countOfMiniLinesY), -1 * xRange, xRange, -1 * yScale, -1 * yScale, AXIS_COLOR);
        }
    }

    /**
     * Calculates the width for the scale dividers position.
     *
     * @param scaleDividerNumber the position
     * @return the width
     */
    private int getScaleDividerWidth(final int scaleDividerNumber) {
        if (scaleDividerNumber % 100 == 0) {
            return 50;
        } else if (scaleDividerNumber % 50 == 0) {
            return 30;
        } else {
            return 10;
        }
    }

    /**
     * Calculates the amount of scale dividers.
     *
     * @param range the axis length to check
     * @return the count
     */
    private int getScaleDividerCount(final double range) {
        return (int) Math.abs(range) / 10;
    }

    /**
     * Creates a line (for the coordinate axis system).
     *
     * @param id            the css selector for this line
     * @param xAbsValueFrom the point on the x axis where the line starts
     * @param xAbsValueTo   the point on the x axis where the line ends
     * @param yAbsValueFrom the point on the y axis where the line starts
     * @param yAbsValueTo   the point on the y axis where the line ends
     * @param color         the color of the line stroke
     */
    private void createLine(@Nonnull final String id,
                            final double xAbsValueFrom,
                            final double xAbsValueTo,
                            final double yAbsValueFrom,
                            final double yAbsValueTo,
                            @Nonnull final String color) {
        Preconditions.checkNotNull(id, "id shouldn't be null!");
        Preconditions.checkNotNull(color, "color shouldn't be null!");

        final Line line = new Line(id,
                new AbstractPolyElement.PolyCoordinatePair(xAbsValueFrom, yAbsValueFrom),
                new AbstractPolyElement.PolyCoordinatePair(xAbsValueTo, yAbsValueTo));
        line.setStroke(color, 1, Path.LINE_CAP.SQUARE, null);
        canvas.add(line);
    }

    /**
     * Creates a zoomable {@link Svg} in full size as a canvas for other svg elements.
     *
     * @param cssID the css identifier
     * @return the canvas
     */
    public static Svg createMapCanvas(@Nonnull final String cssID) {
        final Svg canvas = new Canvas();
        canvas.setHeightFull();
        canvas.setWidthFull();
        canvas.setId(cssID);
        canvas.setZoomEnabled(true);

        return canvas;
    }
}
