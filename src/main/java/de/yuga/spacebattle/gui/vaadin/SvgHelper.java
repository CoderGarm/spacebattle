package de.yuga.spacebattle.gui.vaadin;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.svg.Svg;
import com.vaadin.flow.component.svg.elements.AbstractPolyElement;
import com.vaadin.flow.component.svg.elements.Line;
import com.vaadin.flow.component.svg.elements.Path;
import org.springframework.data.geo.Point;

import javax.annotation.Nonnull;

public class SvgHelper {

    private SvgHelper() {
    }

    /**
     * Creates a upper-left un-centered coordinate cross.
     */
    public static void createCorneredCoordinateCrossAbsolute(@Nonnull final String color,
                                                             final double smallestX,
                                                             final double biggestX,
                                                             final double smallestY,
                                                             final double biggestY,
                                                             @Nonnull final Svg canvas) {
        Preconditions.checkNotNull(color, "color shouldn't be null!");
        Preconditions.checkNotNull(canvas, "canvas shouldn't be null!");

        final double xCoordinate = biggestX * -1;
        final double yCoordinate = biggestY * -1;

        final Point xFrom = new Point(smallestX, yCoordinate);
        final Point xTo = new Point(biggestX, yCoordinate);
        final Point yFrom = new Point(xCoordinate, smallestY);
        final Point yTo = new Point(xCoordinate, biggestY);

        SvgHelper.createCorneredScaleDivider(xFrom, xTo, yFrom, yTo, color, canvas);

        SvgHelper.createLine("X_LINE_ID", xFrom, xTo, color, canvas);
        SvgHelper.createLine("Y_LINE_ID", yFrom, yTo, color, canvas);
    }

    /**
     * Creates a corresponding scale divider system.
     */
    private static void createCorneredScaleDivider(@Nonnull final Point xFrom,
                                                   @Nonnull final Point xTo,
                                                   @Nonnull final Point yFrom,
                                                   @Nonnull final Point yTo,
                                                   @Nonnull final String color,
                                                   @Nonnull final Svg canvas) {
        Preconditions.checkNotNull(xFrom, "xFrom shouldn't be null!");
        Preconditions.checkNotNull(xTo, "xTo shouldn't be null!");
        Preconditions.checkNotNull(yFrom, "yFrom shouldn't be null!");
        Preconditions.checkNotNull(yTo, "yTo shouldn't be null!");
        Preconditions.checkNotNull(color, "color shouldn't be null!");
        Preconditions.checkNotNull(canvas, "canvas shouldn't be null!");

        final double yCoordinate = xFrom.getY();
        final double xCoordinate = yFrom.getX();

        int countOfMiniLines = getScaleDividerCount(2 * xTo.getX());
        for (; countOfMiniLines > 0; countOfMiniLines--) {
            final double scale = 10 * countOfMiniLines;
            final int scaleDividerLength = getScaleDividerWidth(countOfMiniLines);

            final double yAbsValueFrom = -1 * scaleDividerLength + yCoordinate;
            final double yAbsValueTo = scaleDividerLength + yCoordinate;
            final double xAbsValueFromScale = -1 * scale + xCoordinate;
            final double xAbsValueToScale = scale + xCoordinate;

            SvgHelper.createLine("AXIS_SCALE_X_ID" + countOfMiniLines, new Point(xAbsValueToScale, yAbsValueFrom), new Point(xAbsValueToScale, yAbsValueTo), color, canvas);
            if (countOfMiniLines == 1) {
                SvgHelper.createLine("AXIS_SCALE_X_ID" + (-1 * countOfMiniLines), new Point(xAbsValueFromScale, yAbsValueFrom), new Point(xAbsValueFromScale, yAbsValueTo), color, canvas);
            }
        }

        countOfMiniLines = getScaleDividerCount(2 * yTo.getY());
        for (; countOfMiniLines > 0; countOfMiniLines--) {
            final double scale = 10 * countOfMiniLines;
            final int scaleDividerLength = getScaleDividerWidth(countOfMiniLines);

            final double xAbsValueFrom = -1 * scaleDividerLength + xCoordinate;
            final double xAbsValueTo = scaleDividerLength + xCoordinate;
            final double yAbsValueFromScale = -1 * scale + yCoordinate;
            final double yAbsValueToScale = scale + yCoordinate;
            SvgHelper.createLine("AXIS_SCALE_Y_ID" + countOfMiniLines, new Point(xAbsValueFrom, yAbsValueToScale), new Point(xAbsValueTo, yAbsValueToScale), color, canvas);
            if (countOfMiniLines == 1) {
                SvgHelper.createLine("AXIS_SCALE_Y_ID" + (-1 * countOfMiniLines), new Point(xAbsValueFrom, yAbsValueFromScale), new Point(xAbsValueTo, yAbsValueFromScale), color, canvas);
            }
        }
    }

    /**
     * Creates a line (for the coordinate axis system).
     *
     * @param id    the css selector for this line
     * @param start the point where the line starts
     * @param end   the point where the line ends
     * @param color the color of the line stroke
     */
    private static void createLine(@Nonnull final String id,
                                   @Nonnull final Point start,
                                   @Nonnull final Point end,
                                   @Nonnull final String color,
                                   @Nonnull final Svg canvas) {
        Preconditions.checkNotNull(id, "id shouldn't be null!");
        Preconditions.checkNotNull(start, "start shouldn't be null!");
        Preconditions.checkNotNull(end, "end shouldn't be null!");
        Preconditions.checkNotNull(color, "color shouldn't be null!");
        Preconditions.checkNotNull(canvas, "canvas shouldn't be null!");

        final double startX = start.getX();
        final double startY = start.getY();

        final double endX = end.getX();
        final double endY = end.getY();

        final Line line = new Line(id,
                new AbstractPolyElement.PolyCoordinatePair(startX, startY),
                new AbstractPolyElement.PolyCoordinatePair(endX, endY));
        line.setStroke(color, 1, Path.LINE_CAP.SQUARE, null);
        canvas.add(line);
    }

    /**
     * Creates the coordinate cross for the given parameters.
     *
     * @param center the center
     * @param color  the color of the coordinate cross
     * @param polyX  the x coord which represents the axis length
     * @param polyY  the y coord which represents the axis length
     * @param canvas the canvas to draw at
     */
    public static void createCoordinateCross(@Nonnull final Point center,
                                             @Nonnull final String color,
                                             final double polyX,
                                             final double polyY,
                                             @Nonnull final Svg canvas) {
        Preconditions.checkNotNull(color, "color shouldn't be null!");
        Preconditions.checkNotNull(canvas, "canvas shouldn't be null!");

        final double xCoordinate = center.getX();
        final double yCoordinate = center.getY();

        final double absX = Math.abs(polyX) * 1.1;
        final double absY = Math.abs(polyY) * 1.2;
        SvgHelper.createScaleDivider(xCoordinate, yCoordinate, polyX, polyY, color, canvas);
        final double xAbsValueFrom = -1 * absX + xCoordinate;
        final double xAbsValueTo = absX + xCoordinate;
        final double yAbsValueFrom = -1 * absY + yCoordinate;
        final double yAbsValueTo = absY + yCoordinate;
        SvgHelper.createLine("X_LINE_ID", new Point(xAbsValueFrom, yCoordinate), new Point(xAbsValueTo, yCoordinate), color, canvas);
        SvgHelper.createLine("Y_LINE_ID", new Point(xCoordinate, yAbsValueFrom), new Point(xCoordinate, yAbsValueTo), color, canvas);
    }

    /**
     * Creates the scale dividers for this coordinate cross.
     */
    private static void createScaleDivider(final double xCoordinate,
                                           final double yCoordinate,
                                           final double polyX,
                                           final double polyY,
                                           @Nonnull final String color,
                                           @Nonnull final Svg canvas) {
        Preconditions.checkNotNull(color, "color shouldn't be null!");
        Preconditions.checkNotNull(canvas, "canvas shouldn't be null!");

        final double absX = Math.abs(polyX) * 1.1;
        final double absY = Math.abs(polyY) * 1.2;

        int countOfMiniLines = getScaleDividerCount(absX);
        for (; countOfMiniLines > 0; countOfMiniLines--) {
            final double scale = 10 * countOfMiniLines;
            int scaleDividerLength = getScaleDividerWidth(countOfMiniLines);

            final double yAbsValueFrom = -1 * scaleDividerLength + yCoordinate;
            final double yAbsValueTo = scaleDividerLength + yCoordinate;

            final double xAbsValueFromScale = -1 * scale + xCoordinate;
            final double xAbsValueToScale = scale + xCoordinate;

            SvgHelper.createLine("AXIS_SCALE_X_ID" + countOfMiniLines, new Point(xAbsValueToScale, yAbsValueFrom), new Point(xAbsValueToScale, yAbsValueTo), color, canvas);
            SvgHelper.createLine("AXIS_SCALE_X_ID" + (-1 * countOfMiniLines), new Point(xAbsValueFromScale, yAbsValueFrom), new Point(xAbsValueFromScale, yAbsValueTo), color, canvas);
        }

        countOfMiniLines = getScaleDividerCount(absY);
        for (; countOfMiniLines > 0; countOfMiniLines--) {
            final double scale = 10 * countOfMiniLines;
            int scaleDividerLength = getScaleDividerWidth(countOfMiniLines);

            final double xAbsValueFrom = -1 * scaleDividerLength + xCoordinate;
            final double xAbsValueTo = scaleDividerLength + xCoordinate;
            final double yAbsValueFromScale = -1 * scale + yCoordinate;
            final double yAbsValueToScale = scale + yCoordinate;
            SvgHelper.createLine("AXIS_SCALE_Y_ID" + countOfMiniLines, new Point(xAbsValueFrom, yAbsValueToScale), new Point(xAbsValueTo, yAbsValueToScale), color, canvas);
            SvgHelper.createLine("AXIS_SCALE_Y_ID" + (-1 * countOfMiniLines), new Point(xAbsValueFrom, yAbsValueFromScale), new Point(xAbsValueTo, yAbsValueFromScale), color, canvas);
        }
    }

    /**
     * Calculates the width for the scale dividers position.
     *
     * @param scaleDividerNumber the position
     * @return the width
     */
    private static int getScaleDividerWidth(final int scaleDividerNumber) {
        final int bigDivider = 15;
        final int smallDivider = 5;

        if (scaleDividerNumber == 1) {
            return 5;
        } else if (scaleDividerNumber % bigDivider == 0) {
            return 15;
        } else if (scaleDividerNumber % smallDivider == 0) {
            return 15;
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
    private static int getScaleDividerCount(final double range) {
        return (int) Math.abs(range) / 10;
    }

}
