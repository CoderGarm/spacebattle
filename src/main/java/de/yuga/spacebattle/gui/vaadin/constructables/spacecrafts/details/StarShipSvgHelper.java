package de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.svg.Svg;
import com.vaadin.flow.component.svg.elements.*;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;
import de.yuga.spacebattle.gui.vaadin.SvgHelper;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.geo.Point;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details.HullSvgDisplay.HULL_OUTLINE_COLOR;

public class StarShipSvgHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(StarShipSvgHelper.class);

    @Nullable
    private Svg canvas;

    public static final String WEAPON_SLOT_OUTLINE = "black";
    public static final String AXIS_COLOR = "darkgoldenrod";
    public static final String BROADSIDES_COLOR = "navy";
    public static final String BOW_COLOR = "indigo";
    public static final String STERN_COLOR = "darkmagenta";


    public static final String BROADSIDE = "broadside";
    public static final String BOW = "bow";
    public static final String STERN = "stern";
    public static final String STARSHIP_DISPLAY = "starship-display";

    public static final int Y_BASE_COORDINATE = 40;
    public static final int Y_BASE_OFFSET = 5;
    public static final int HULL_STROKE_WIDTH = 2;

    @Nullable
    private List<AbstractPolyElement.PolyCoordinatePair> broadsidesPoints;

    @Nullable
    private List<AbstractPolyElement.PolyCoordinatePair> broadsides2Points;

    @Nonnull
    private final Text broadsideText = new Text("broadside-text", "Broadsides");

    @Nullable
    private Polygon broadsidePolygon;

    @Nullable
    private Polygon broadside2Polygon;

    @Nonnull
    private final Map<Point, Circle> usedSlotsBroadside = new HashMap<>();

    @Nullable
    private List<AbstractPolyElement.PolyCoordinatePair> bowPoints;

    @Nullable
    private List<AbstractPolyElement.PolyCoordinatePair> bow2Points;

    @Nonnull
    private final Text bowText = new Text("bow-text", "Bow");

    @Nullable
    private Polygon bowPolygon;

    @Nullable
    private Polygon bow2Polygon;

    @Nonnull
    private final Map<Point, Circle> usedSlotsBow = new HashMap<>();

    @Nullable
    private List<AbstractPolyElement.PolyCoordinatePair> sternPoints;

    @Nullable
    private List<AbstractPolyElement.PolyCoordinatePair> stern2Points;

    @Nonnull
    private final Text sternText = new Text("stern-text", "Stern");

    @Nullable
    private Polygon sternPolygon;

    @Nullable
    private Polygon stern2Polygon;

    @Nonnull
    private final Map<Point, Circle> usedSlotsStern = new HashMap<>();

    // just to hold the circle amount for refreshing the canvas
    private int bowCircleRadius = 3;
    public int bowAmount = 0;
    private int broadsideCircleRadius = 4;
    public int broadsideAmount = 0;
    private int sternCircleRadius = 3;
    public int sternAmount = 0;

    public static Svg createShipCanvas() {
        final Svg canvas = new Svg();
        canvas.setId(STARSHIP_DISPLAY);
        canvas.setZoomEnabled(true);
        canvas.setHeightFull();
        canvas.setWidthFull();
        return canvas;
    }

    public StarShipSvgHelper() {
        createStarShipHull();
    }

    /**
     * Creates a zoomable {@link Svg} in full size as a canvas for other svg elements.
     */
    public void createStarShipHull() {
        this.canvas = StarShipSvgHelper.createShipCanvas();
        final List<AbstractPolyElement.PolyCoordinatePair> hullSilhouettePoints = getPolyCoordinatePairsForShipDisplay();
        this.canvas.add(new HullSvgDisplay("hull", hullSilhouettePoints));

        final List<Double> xSorted = hullSilhouettePoints.stream().map(AbstractPolyElement.PolyCoordinatePair::getPolyX).sorted().collect(Collectors.toList());
        final List<Double> ySorted = hullSilhouettePoints.stream().map(AbstractPolyElement.PolyCoordinatePair::getPolyY).sorted().collect(Collectors.toList());

        final double biggestX = xSorted.get(hullSilhouettePoints.size() - 1) * 1.1;
        final double smallestX = xSorted.get(0) * 1.1;
        final double biggestY = ySorted.get(hullSilhouettePoints.size() - 1) * 1.4;
        final double smallestY = ySorted.get(0) * 1.4;
        SvgHelper.createCorneredCoordinateCrossAbsolute(AXIS_COLOR, smallestX, biggestX, smallestY, biggestY, this.canvas);

        double minx = biggestX * -1.4;
        double miny = biggestY * -1.4;
        double width = Math.abs(minx) * 2;
        double height = Math.abs(miny) * 2;
        this.canvas.viewbox(minx, miny, width, height);

        addBroadsidePolygon();
        addBowPolygon();
        addSternPolygon();

        calculateBroadsideSlots(broadsideCircleRadius, broadsideAmount);
        calculateBowSlots(bowCircleRadius, bowAmount);
        calculateSternSlots(sternCircleRadius, sternAmount);
    }

    public void calculateSternSlots(int circleRadius, int amountOfSlots) {
        sternCircleRadius = circleRadius;
        sternAmount = amountOfSlots;
    }

    /**
     * Creates an amount of weapon slots for the bow.
     * <p>
     * <b>Attention: </b>
     * Even if there are two lists of points which are representing the hull segment silhouette,
     * they are not the same and the order of points is not the same, too.
     * The first list will be created and the second one is mirrored around the x- and y-axis afterwards.
     * </p>
     *
     * @param circleRadius  the weapon slot radius
     * @param amountOfSlots the amount of weapon slots to create
     */
    public void calculateBowSlots(int circleRadius, int amountOfSlots) {
        final int initialAmount = amountOfSlots;
        bowCircleRadius = circleRadius;
        bowAmount = amountOfSlots;

        usedSlotsBow.values().forEach(canvas::remove);
        usedSlotsBow.clear();

        final List<Double> sortedX = bowPoints.stream().map(AbstractPolyElement.PolyCoordinatePair::getPolyX).sorted().collect(Collectors.toList());
        final List<Double> sortedY = bowPoints.stream().map(AbstractPolyElement.PolyCoordinatePair::getPolyY).sorted().collect(Collectors.toList());

        final int spacer = 1;
        final double smallestX = sortedX.get(0);
        final double biggestX = sortedX.get(sortedX.size() - 1);
        final double spaceNeeded = (circleRadius + spacer) * 2;

        final double biggestY = sortedY.get(sortedY.size() - 1);

        final AbstractPolyElement.PolyCoordinatePair upperBowReferenceP1 = bowPoints.get(0);
        final AbstractPolyElement.PolyCoordinatePair upperBowReferenceP2 = bowPoints.get(1);
        final AbstractPolyElement.PolyCoordinatePair upperBowReferenceP3 = bowPoints.get(2);

        final double bowLineAtFrontHeightY = Math.abs(upperBowReferenceP3.getPolyY() - upperBowReferenceP2.getPolyY());

        final List<Double> sorted2X = bow2Points.stream().map(AbstractPolyElement.PolyCoordinatePair::getPolyX).sorted().collect(Collectors.toList());
        final List<Double> sorted2Y = bow2Points.stream().map(AbstractPolyElement.PolyCoordinatePair::getPolyY).sorted().collect(Collectors.toList());

        final double biggest2X = sorted2X.get(sorted2X.size() - 1);
        final double smallest2Y = sorted2Y.get(0);

        final double borderMargin = circleRadius * 2;

        /*
         * creating the next slots in a diagonal row from lower right to the upper left point alternating on both sides
         */
        final SimpleRegression regression = new SimpleRegression();

        final double diagonalDistance = getDistance(upperBowReferenceP1, upperBowReferenceP2);

        regression.addData(upperBowReferenceP1.getPolyX(), upperBowReferenceP1.getPolyY());
        regression.addData(upperBowReferenceP2.getPolyX(), upperBowReferenceP2.getPolyY());
        final double slope = regression.getSlope();

        int maxPerDiagonalRow = (int) Math.abs((diagonalDistance - 2 * HULL_STROKE_WIDTH) / spaceNeeded);

        // calculate upper coord set
        double xCoordUpper = biggestX - borderMargin;
        double yCoordUpper = upperBowReferenceP2.getPolyY() + spaceNeeded / 2;
        // calculate lower coord set
        final AbstractPolyElement.PolyCoordinatePair lowerBowReferenceP3 = bow2Points.get(2);

        double xCoordLower = biggest2X - borderMargin;
        double yCoordLower = lowerBowReferenceP3.getPolyY() - spaceNeeded / 2;
        // creating the slots in the diagonal rows
        while (amountOfSlots > 0) {
            if (maxPerDiagonalRow <= 0) {
                break;
            }
            createWeaponSlot(EWeaponAlignment.BOW, circleRadius, xCoordUpper, yCoordUpper, "u", usedSlotsBow);
            amountOfSlots--;
            maxPerDiagonalRow--;
            if (amountOfSlots > 0) {
                createWeaponSlot(EWeaponAlignment.BOW, circleRadius, xCoordLower, yCoordLower, "l", usedSlotsBow);
                amountOfSlots--;
            }
            // modify upper coord set
            xCoordUpper = xCoordUpper - spaceNeeded;
            yCoordUpper = yCoordUpper - (spaceNeeded * slope);
            // modify lower coord set
            xCoordLower = xCoordLower - spaceNeeded;
            yCoordLower = yCoordLower + (spaceNeeded * slope);
        }

        /*
         * creating the next slots in a row from lower right to the lower left point alternating on both sides
         */
        // the coordinates of the first two weapon slots in the bow
        double firstXUpper = biggestX - borderMargin;
        double yUpper1stRow = biggestY - borderMargin;
        double firstXLower = biggest2X - borderMargin;
        double yLower1stRow = smallest2Y + borderMargin;

        final double distance = getDistance(new Point(biggestX, yUpper1stRow), new Point(smallestX, yUpper1stRow));
        final int maxPerRow = (int) Math.abs((distance - 2 * HULL_STROKE_WIDTH) / spaceNeeded);

        final int maxAmountYRows = (int) Math.abs((bowLineAtFrontHeightY - 2 * HULL_STROKE_WIDTH) / spaceNeeded);

        xCoordUpper = firstXUpper;
        double yUpper2ndRow = yUpper1stRow - spaceNeeded;
        xCoordLower = firstXLower;
        double yLower2ndRow = yLower1stRow + spaceNeeded;

        int maxPerRowDeputy = maxPerRow;
        while (amountOfSlots > 0) {
            if (maxPerRowDeputy <= 0) {
                break;
            }
            // creating row at the outline
            createWeaponSlot(EWeaponAlignment.BOW, circleRadius, xCoordUpper, yUpper1stRow, "u1", usedSlotsBow);
            amountOfSlots--;
            maxPerRowDeputy--;
            if (amountOfSlots > 0) {
                createWeaponSlot(EWeaponAlignment.BOW, circleRadius, xCoordLower, yLower1stRow, "l1", usedSlotsBow);
                amountOfSlots--;
            }
            // creating most inner row
            if (maxAmountYRows > 2) {
                createWeaponSlot(EWeaponAlignment.BOW, circleRadius, xCoordUpper, yUpper2ndRow, "u2", usedSlotsBow);
                amountOfSlots--;
                if (amountOfSlots > 0) {
                    createWeaponSlot(EWeaponAlignment.BOW, circleRadius, xCoordLower, yLower2ndRow, "l2", usedSlotsBow);
                    amountOfSlots--;
                }
            }

            // modify upper coord set
            xCoordUpper = xCoordUpper - spaceNeeded;
            // modify lower coord set
            xCoordLower = xCoordLower - spaceNeeded;
        }

        // creating a complete new set if the space is to small
        if (amountOfSlots > 0) {
            usedSlotsBow.values().forEach(canvas::remove);
            usedSlotsBow.clear();
            calculateBowSlots(circleRadius - 1, initialAmount);
        }
    }

    /**
     * Returns the distance between the two given orbits.
     *
     * @param point1 the first orbit
     * @param point2 the second orbit
     * @return the distance
     */
    private double getDistance(@Nonnull final Point point1, @Nonnull final Point point2) {
        Preconditions.checkNotNull(point1, "point1 shouldn't be null!");
        Preconditions.checkNotNull(point2, "point2 shouldn't be null!");

        final double x1 = point1.getX();
        final double y1 = point1.getY();

        final double x2 = point2.getX();
        final double y2 = point2.getY();

        return getDistance(x2 - x1, y2 - y1);
    }

    /**
     * Returns the distance between the two given orbits.
     *
     * @param point1 the first orbit
     * @param point2 the second orbit
     * @return the distance
     */
    private double getDistance(@Nonnull final AbstractPolyElement.PolyCoordinatePair point1, @Nonnull final AbstractPolyElement.PolyCoordinatePair point2) {
        Preconditions.checkNotNull(point1, "point1 shouldn't be null!");
        Preconditions.checkNotNull(point2, "point2 shouldn't be null!");

        final double x1 = point1.getPolyX();
        final double y1 = point1.getPolyY();

        final double x2 = point2.getPolyX();
        final double y2 = point2.getPolyY();

        return getDistance(x2 - x1, y2 - y1);
    }

    /**
     * Calculates the distance between thw two given coordinates.
     *
     * @param firstCoord  the first digit
     * @param secondCoord the second digit
     * @return the distance
     */
    private double getDistance(double firstCoord, double secondCoord) {
        return Math.sqrt(Math.pow(firstCoord, 2) + Math.pow(secondCoord, 2));
    }

    /**
     * Creates a bow or stern weapon slot at the given coordinates for the given type.
     *
     * @param weaponAlignment the alignment type
     * @param circleRadius    the circle radius
     * @param xCoord          the x coordinate
     * @param yCoord          the y coordinate
     * @param idPrefix        the prefix for the css selector
     * @param usedSlots       the map which holds the weapon's circles
     */
    private void createWeaponSlot(@Nonnull final EWeaponAlignment weaponAlignment,
                                  final int circleRadius,
                                  final double xCoord,
                                  final double yCoord,
                                  @Nonnull final String idPrefix,
                                  @Nonnull final Map<Point, Circle> usedSlots) {
        Preconditions.checkNotNull(idPrefix, "idPrefix shouldn't be null!");

        final Circle upperCircle = new Circle(idPrefix + "-" + weaponAlignment.name() + "-" + usedSlots.size(), circleRadius);
        upperCircle.setFillColor(HULL_OUTLINE_COLOR);
        upperCircle.setStroke(WEAPON_SLOT_OUTLINE, 1);
        upperCircle.center(xCoord, yCoord);
        usedSlots.put(new Point(xCoord, yCoord), upperCircle);
        canvas.add(upperCircle);
    }

    /**
     * Creates a broadside weapon slot at the given coordinates for the given type.
     * The given coordinates will be added to the first in the points base list.
     *
     * @param circleRadius the circle radius
     * @param xCoord       the x coordinate
     * @param yCoord       the y coordinate
     * @param idPrefix     the prefix for the css selector
     */
    private void createBroadsideWeaponSlot(final int circleRadius,
                                           final double xCoord,
                                           final double yCoord,
                                           @Nonnull final String idPrefix) {
        Preconditions.checkNotNull(idPrefix, "idPrefix shouldn't be null!");

        final AbstractPolyElement.PolyCoordinatePair firstBroadsidePoint = broadsidesPoints.get(0);
        final Circle upperCircle = new Circle(idPrefix + "-" + EWeaponAlignment.BROADSIDE.name() + "-" + usedSlotsBroadside.size(), circleRadius);
        upperCircle.setFillColor(HULL_OUTLINE_COLOR);
        upperCircle.setStroke(WEAPON_SLOT_OUTLINE, 1);
        upperCircle.center(xCoord, firstBroadsidePoint.getPolyY() + yCoord);
        usedSlotsBroadside.put(new Point(xCoord, yCoord), upperCircle);
        canvas.add(upperCircle);
    }

    /**
     * Creates an amount of weapon slots for the broadsides.
     * <p>
     * <b>Attention:</b>
     * The amount of slots will be doubled per definition. A broadside weapon will project damage in two directions per design.
     * </p>
     *
     * @param circleRadius  the weapon slot radius
     * @param amountOfSlots the amount of weapon slots to create
     */
    public void calculateBroadsideSlots(final int circleRadius, final int amountOfSlots) {
        broadsideCircleRadius = circleRadius;
        broadsideAmount = amountOfSlots;

        usedSlotsBroadside.values().forEach(canvas::remove);
        usedSlotsBroadside.clear();

        final List<Double> sortedX = broadsidesPoints.stream().map(AbstractPolyElement.PolyCoordinatePair::getPolyX).sorted().collect(Collectors.toList());
        final List<Double> sortedY = broadsidesPoints.stream().map(AbstractPolyElement.PolyCoordinatePair::getPolyY).sorted().collect(Collectors.toList());

        final int spacerX = 3;
        final int spacerY = 1;
        final double smallestX = sortedX.get(0);
        final double biggestX = sortedX.get(sortedX.size() - 1);
        final double rangeX = biggestX - smallestX;
        double spaceNeededX = (circleRadius + spacerX) * 2;
        final int maximumSlotsX = ((int) rangeX / (int) spaceNeededX) - 1;

        final double smallestY = sortedY.get(0);
        final double biggestY = sortedY.get(sortedY.size() - 1);
        final double rangeY = biggestY - smallestY;

        final double spaceNeededY = (circleRadius + spacerY) * 2;
        final int maximumSlotsY = (int) rangeY / (int) spaceNeededY;

        final double yCoordBaseRow = (rangeY / 2) - (spacerY * 2);
        final double xCoord = amountOfSlots == 1 ? 0 : spaceNeededX;

        // holds the y coord of the rows where the stuff can be placed - or nothing it must be calculated again
        double[] yPlacementArray = null;
        if (amountOfSlots <= maximumSlotsX) {
            // place in middle row
            yPlacementArray = new double[]{yCoordBaseRow};

        } else if (amountOfSlots <= (maximumSlotsY * maximumSlotsX)) {
            if (amountOfSlots <= (2 * maximumSlotsX)) {
                // place in double row
                final double yCoordDown = yCoordBaseRow + (yCoordBaseRow / 2);
                final double yCoordUp = yCoordBaseRow - (yCoordBaseRow / 2);
                yPlacementArray = new double[]{yCoordDown, yCoordUp};

            } else if (amountOfSlots <= (3 * maximumSlotsX)) {
                // place in thirdly row
                final double yCoordDown = yCoordBaseRow + (yCoordBaseRow / 2);
                final double yCoordUp = yCoordBaseRow - (yCoordBaseRow / 2);
                yPlacementArray = new double[]{yCoordDown, yCoordBaseRow, yCoordUp};
            }
        }

        if (yPlacementArray == null) {
            // reduce radius and recursive
            calculateBroadsideSlots(circleRadius - 1, amountOfSlots);
            return;
        }
        final int rowAmount = yPlacementArray.length;

        final int amountPerRow = amountOfSlots / rowAmount;
        spaceNeededX = ((rangeX - 4 * spaceNeededX) / amountPerRow);

        final int overlap = amountOfSlots - (amountPerRow * rowAmount);
        // todo place the "odd" amounts somewhere better than at the end

        for (int yRow = 0; yRow < rowAmount; yRow++) {
            final double rowCoordY = yPlacementArray[yRow];
            double flowingX = xCoord;
            final int drawPerRow = amountPerRow + (yRow == 0 ? overlap : 0);
            for (int i = 1; i <= drawPerRow; i++) {
                createBroadsideWeaponSlot(circleRadius, flowingX, rowCoordY, "u-");
                // every even must be placed one spacer away
                flowingX = -1 * flowingX + (i % 2 == 0 ? spaceNeededX : 0);
            }
        }

        // todo do not double the slots that ugly
        final Set<Point> slots = new HashSet<>(usedSlotsBroadside.keySet());
        slots.forEach(point -> {
            final double x = point.getX();
            final double y = point.getY();
            createBroadsideWeaponSlot(circleRadius, x, Y_BASE_COORDINATE + Y_BASE_OFFSET + y, "l-");
        });
    }

    /**
     * Creates a poly line plot for a star ship hull silhouette and for the bow, stern and the broadside polygon.
     * <p>
     * The single segments will be created by creating the first part. The second part is mirrored around the x- and y-axis afterwards.
     * So the two parts of a segment are neither equal nor same nor ordered in the same way or something else.
     */
    private List<AbstractPolyElement.PolyCoordinatePair> getPolyCoordinatePairsForShipDisplay() {

        // create upper broadside polygon
        final List<AbstractPolyElement.PolyCoordinatePair> hullSilhouette = new ArrayList<>();
        // draw a ship hull
        absolutePoint(0, -Y_BASE_COORDINATE, hullSilhouette, false);
        right(150, hullSilhouette, false);
        down(40, hullSilhouette, false);

        broadsidesPoints = new ArrayList<>(hullSilhouette);
        up(Y_BASE_OFFSET, broadsidesPoints, false);
        mirrorThePointsOnYAxis(broadsidesPoints);

        broadsidePolygon = new Polygon(BROADSIDE, broadsidesPoints);
        broadsidePolygon.setFillColor(BROADSIDES_COLOR);
        setStroke(broadsidePolygon);

        broadsides2Points = new ArrayList<>(broadsidesPoints);
        mirrorThePointsOnXAxis(broadsides2Points);
        broadsides2Points.removeAll(broadsidesPoints);
        broadside2Polygon = new Polygon(BROADSIDE, broadsides2Points);
        broadside2Polygon.setFillColor(BROADSIDES_COLOR);
        setStroke(broadside2Polygon);

        final AbstractPolyElement.PolyCoordinatePair lastBroadside = broadsides2Points.get(broadsides2Points.size() - 1);
        broadsideText.move(lastBroadside.getPolyX(), lastBroadside.getPolyY() * 1.2);
        broadsideText.setFillColor(HULL_OUTLINE_COLOR);
        canvas.add(broadsideText);

        up(40, hullSilhouette, false);
        relativePoint(50, -10, hullSilhouette, false);
        up(15, hullSilhouette, false);
        right(40, hullSilhouette, false);

        // create upper bow polygon
        bowPoints = new ArrayList<>();
        bowPoints.add(hullSilhouette.get(hullSilhouette.size() - 1));

        relativePoint(60, -20, hullSilhouette, false);
        down(20, hullSilhouette, false);

        bowPoints.add(hullSilhouette.get(hullSilhouette.size() - 2));
        bowPoints.add(hullSilhouette.get(hullSilhouette.size() - 1));
        final AbstractPolyElement.PolyCoordinatePair fP = bowPoints.get(0);
        final AbstractPolyElement.PolyCoordinatePair lP = bowPoints.get(bowPoints.size() - 1);
        left(lP.getPolyX() - fP.getPolyX(), bowPoints, false);

        bowPolygon = new Polygon(BOW, bowPoints);
        bowPolygon.setFillColor(BOW_COLOR);
        setStroke(bowPolygon);

        // create lower bow polygon
        bow2Points = new ArrayList<>(bowPoints);
        mirrorThePointsOnXAxis(bow2Points);
        bow2Points.removeAll(bowPoints);
        bow2Polygon = new Polygon(BOW, bow2Points);
        bow2Polygon.setFillColor(BOW_COLOR);
        setStroke(bow2Polygon);

        final AbstractPolyElement.PolyCoordinatePair lastBow = bow2Points.get(bow2Points.size() - 1);
        bowText.move(lastBow.getPolyX(), lastBow.getPolyY() * 1.2);
        bowText.setFillColor(HULL_OUTLINE_COLOR);
        canvas.add(bowText);

        // create upper stern polygon
        sternPoints = new ArrayList<>(bowPoints);
        mirrorThePointsOnYAxis(sternPoints);
        sternPoints.removeAll(bowPoints);

        sternPolygon = new Polygon(STERN, sternPoints);
        sternPolygon.setFillColor(STERN_COLOR);
        setStroke(sternPolygon);

        // create lower stern polygon
        stern2Points = new ArrayList<>(bow2Points);
        mirrorThePointsOnYAxis(stern2Points);
        stern2Points.removeAll(bow2Points);

        stern2Polygon = new Polygon(STERN, stern2Points);
        stern2Polygon.setFillColor(STERN_COLOR);
        setStroke(stern2Polygon);

        final AbstractPolyElement.PolyCoordinatePair lastStern = stern2Points.get(0);
        sternText.move(lastStern.getPolyX(), lastStern.getPolyY() * 1.2);
        sternText.setFillColor(HULL_OUTLINE_COLOR);
        canvas.add(sternText);

        mirrorThePointsOnYAxis(hullSilhouette);
        mirrorThePointsOnXAxis(hullSilhouette);

        return hullSilhouette;
    }

    /**
     * Adds all broadside related stuff to the canvas.
     */
    public void addBroadsidePolygon() {
        canvas.add(broadsidePolygon);
        canvas.add(broadside2Polygon);
        usedSlotsBroadside.values().forEach(canvas::add);
    }

    /**
     * Adds all bow related stuff to the canvas.
     */
    public void addBowPolygon() {
        canvas.add(bowPolygon);
        canvas.add(bow2Polygon);
        usedSlotsBow.values().forEach(canvas::add);
    }

    /**
     * Adds all stern related stuff to the canvas.
     */
    public void addSternPolygon() {
        canvas.add(sternPolygon);
        canvas.add(stern2Polygon);
        usedSlotsStern.values().forEach(canvas::add);
    }

    /**
     * Removes all broadside related stuff to the canvas.
     */
    public void removeBroadsidePolygon() {
        canvas.remove(broadsidePolygon);
        canvas.remove(broadside2Polygon);
        usedSlotsBroadside.values().forEach(canvas::remove);
    }

    /**
     * Removes all bow related stuff to the canvas.
     */
    public void removeBowPolygon() {
        canvas.remove(bowPolygon);
        canvas.remove(bow2Polygon);
        usedSlotsBow.values().forEach(canvas::remove);
    }

    /**
     * Removes all stern related stuff to the canvas.
     */
    public void removeSternPolygon() {
        canvas.remove(sternPolygon);
        canvas.remove(stern2Polygon);
        usedSlotsStern.values().forEach(canvas::remove);
    }

    /**
     * Sets a stroke to the given polygon.
     *
     * @param polygon the polygon to stroke
     */
    private void setStroke(@Nonnull final Polygon polygon) {
        Preconditions.checkNotNull(polygon, "polygon shouldn't be null!");

        polygon.setStroke(HULL_OUTLINE_COLOR, HULL_STROKE_WIDTH, Path.LINE_CAP.SQUARE, Path.LINE_JOIN.ARCS);
    }

    /**
     * Mirrors the coordinate points at the x axis and sets the mirrored points into the given list.
     *
     * @param points the list of points to mirror them
     */
    private void mirrorThePointsOnXAxis(final List<AbstractPolyElement.PolyCoordinatePair> points) {
        for (int i = points.size() - 1; i >= 0; i--) {
            final AbstractPolyElement.PolyCoordinatePair p = points.get(i);
            points.add(new AbstractPolyElement.PolyCoordinatePair(p.getPolyX(), p.getPolyY() * -1));
        }
    }

    /**
     * Mirrors the coordinate points at the y axis and sets the mirrored points into the given list.
     *
     * @param points the list of points to mirror them
     */
    private void mirrorThePointsOnYAxis(final List<AbstractPolyElement.PolyCoordinatePair> points) {
        for (int i = points.size() - 1; i >= 0; i--) {
            final AbstractPolyElement.PolyCoordinatePair p = points.get(i);
            points.add(new AbstractPolyElement.PolyCoordinatePair(p.getPolyX() * -1, p.getPolyY()));
        }
    }

    /**
     * Sets a point to absolute coordinates for the given list of points.
     *
     * @param x          the x coordinate
     * @param y          the y coordinate
     * @param points     the list of points
     * @param drawCircle if a circle should be drawn or not
     */
    private void absolutePoint(final double x, final double y, @Nonnull final List<AbstractPolyElement.PolyCoordinatePair> points, final boolean drawCircle) {
        Preconditions.checkNotNull(points, "points shouldn't be null!");

        points.add(new AbstractPolyElement.PolyCoordinatePair(x, y));
        setCircle(points, drawCircle);
    }

    /**
     * Sets a relative point for the given list of points.
     *
     * @param x          the x coordinate
     * @param y          the y coordinate
     * @param points     the list of points
     * @param drawCircle if a circle should be drawn or not
     */
    private void relativePoint(final double x, final double y, @Nonnull final List<AbstractPolyElement.PolyCoordinatePair> points, final boolean drawCircle) {
        Preconditions.checkNotNull(points, "points shouldn't be null!");

        final AbstractPolyElement.PolyCoordinatePair point = points.get(points.size() - 1);
        points.add(new AbstractPolyElement.PolyCoordinatePair(point.getPolyX() + x, point.getPolyY() - y));
        setCircle(points, drawCircle);
    }

    /**
     * Sets a relative point to the right for the given list of points.
     *
     * @param x          the x coordinate
     * @param points     the list of points
     * @param drawCircle if a circle should be drawn or not
     */
    private void right(final double x, @Nonnull final List<AbstractPolyElement.PolyCoordinatePair> points, final boolean drawCircle) {
        Preconditions.checkNotNull(points, "points shouldn't be null!");

        final AbstractPolyElement.PolyCoordinatePair point = points.get(points.size() - 1);
        points.add(new AbstractPolyElement.PolyCoordinatePair(point.getPolyX() + x, point.getPolyY()));
        setCircle(points, drawCircle);
    }

    /**
     * Sets a relative point to the left for the given list of points.
     *
     * @param x          the x coordinate
     * @param points     the list of points
     * @param drawCircle if a circle should be drawn or not
     */
    private void left(final double x, @Nonnull final List<AbstractPolyElement.PolyCoordinatePair> points, final boolean drawCircle) {
        Preconditions.checkNotNull(points, "points shouldn't be null!");

        final AbstractPolyElement.PolyCoordinatePair point = points.get(points.size() - 1);
        points.add(new AbstractPolyElement.PolyCoordinatePair(point.getPolyX() - x, point.getPolyY()));
        setCircle(points, drawCircle);
    }

    /**
     * Sets a relative point upwards for the given list of points.
     *
     * @param y          the y coordinate
     * @param points     the list of points
     * @param drawCircle if a circle should be drawn or not
     */
    private void up(final double y, @Nonnull final List<AbstractPolyElement.PolyCoordinatePair> points, final boolean drawCircle) {
        Preconditions.checkNotNull(points, "points shouldn't be null!");

        final AbstractPolyElement.PolyCoordinatePair point = points.get(points.size() - 1);
        points.add(new AbstractPolyElement.PolyCoordinatePair(point.getPolyX(), point.getPolyY() - y));
        setCircle(points, drawCircle);
    }

    /**
     * Sets a relative point downwards for the given list of points.
     *
     * @param y          the y coordinate
     * @param points     the list of points
     * @param drawCircle if a circle should be drawn or not
     */
    private void down(final double y, @Nonnull final List<AbstractPolyElement.PolyCoordinatePair> points, final boolean drawCircle) {
        Preconditions.checkNotNull(points, "points shouldn't be null!");

        final AbstractPolyElement.PolyCoordinatePair point = points.get(points.size() - 1);
        points.add(new AbstractPolyElement.PolyCoordinatePair(point.getPolyX(), point.getPolyY() + y));
        setCircle(points, drawCircle);
    }

    /**
     * Creates a circle for the last point of the given list of points.
     *
     * @param points     the list of points
     * @param drawCircle if a circle should be drawn or not
     */
    private void setCircle(@Nonnull final List<AbstractPolyElement.PolyCoordinatePair> points, final boolean drawCircle) {
        Preconditions.checkNotNull(points, "points shouldn't be null!");

        if (drawCircle) {
            final AbstractPolyElement.PolyCoordinatePair point2 = points.get(points.size() - 1);
            Circle c = new Circle("c-" + point2.hashCode(), 5);
            c.setFillColor("green");
            c.center(point2.getPolyX(), point2.getPolyY());
            canvas.add(c);
        }
    }

    @Nonnull
    public Svg getCanvas() {
        return canvas;
    }
}
