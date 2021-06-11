package de.yuga.spacebattle.gui.vaadin.orbitals.starmap;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.vaadin.flow.component.svg.Svg;
import com.vaadin.flow.component.svg.elements.*;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.distance.DistanceCalculator;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.turn.Colonization;
import de.yuga.spacebattle.backend.entities.turn.Move;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.geo.Point;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.gui.vaadin.orbitals.starmap.ViewBoxDefinition.ECoordinateCrossType.PLANETARY_SYSTEM_CROSS;
import static de.yuga.spacebattle.gui.vaadin.orbitals.starmap.ViewBoxDefinition.ECoordinateCrossType.STAR_SYSTEM_CROSS;
import static de.yuga.spacebattle.gui.vaadin.orbitals.starmap.ViewBoxDefinition.EViewBoxType.STAR_SYSTEM;
import static de.yuga.spacebattle.gui.vaadin.orbitals.starmap.ViewBoxDefinition.EViewBoxType.UNIVERSE;

/**
 * This defines the view box for the {@link Svg} canvas.
 * Also every other thing is worked by this class, every orbit, coordinate crosses and these kind of stuff will be calculated here.
 */
public class ViewBoxDefinition {

    private final static Logger LOGGER = LoggerFactory.getLogger(ViewBoxDefinition.class);

    /**
     * The color for the star system "is colonizable" marker.
     */
    public static final String COLONIZABLE_SYSTEM_MARKER_COLOR = "#306f91";

    /**
     * The color for the star system circle which has a user colony.
     */
    public static final String IS_COLONIZED_BY_USER_COLOR = "darkolivegreen";

    /**
     * The color for a star system circle which is not colonized.
     */
    public static final String NOT_COLONIZED_COLOR = "darkgoldenrod";

    /**
     * The color for a not by user colonized system circle.
     */
    public static final String COLONIZED_BY_OTHERS_COLOR = "#6f1585";

    /**
     * The color for the "colonization in progress" marker.
     */
    public static final String COLONIZATION_IN_PROGRESS = "GoldenRod";

    /**
     * The color of the marker which displays the panned to point.
     */
    public static final String PAN_TO_MARKER_COLOR = "darkred";

    /**
     * The factor defines the enlargement of the coordinate cross' axis above their real width.
     * A factor of 1.2 means that an axis from -10 to 10 is displayed as an axis from -12 to 12.
     */
    public static final double AXIS_ENLARGEMENT_FACTOR = 1.2;

    /**
     * CSS id prefix for every x axis.
     */
    public static final String AXIS_SCALE_X_ID = "axisScaleX";

    /**
     * CSS id prefix for every y axis.
     */
    public static final String AXIS_SCALE_Y_ID = "axisScaleY";

    /**
     * CSS id prefix for every x coord cross scale divider on the y axis.
     */
    public static final String X_LINE_ID = "xLine-";

    /**
     * CSS id prefix for every y coord cross scale divider on the x axis.
     */
    public static final String Y_LINE_ID = "yLine-";

    /**
     * CSS id prefix for every course plot.
     */
    public static final String COURSE_ID = "course-";

    /**
     * CSS id prefix for every fleet text.
     */
    public static final String FLEET_TEXT_ID = "fleet-text-";

    /**
     * What this canvas is for, may be the universe itself, may be only for a star system.
     */
    public enum EViewBoxType {
        /**
         * If the canvas is for the universe.
         */
        UNIVERSE,

        /**
         * If the canvas is for a star system.
         */
        STAR_SYSTEM
    }

    /**
     * What this coordinate cross is for, e.g. for a planetary system or a star system.
     */
    enum ECoordinateCrossType {
        /**
         * If the coordinate cross is for the star system.
         */
        STAR_SYSTEM_CROSS,

        /**
         * If the coordinate cross is for a planetary system.
         */
        PLANETARY_SYSTEM_CROSS
    }

    /**
     * The css selector prefix for planet circles. Also used to identify clicked circle.
     */
    public static final String PLANET_SELECTOR_ID_PREFIX = "planet";

    /**
     * The css selector prefix for fleet sharks. Also used to identify clicked shark.
     */
    public static final String FLEET_SELECTOR_ID_PREFIX = "fleet";

    /**
     * The css selector prefix for orbit. Also used to identify clicked orbits.
     */
    public static final String ORBIT_SELECTOR_ID_PREFIX = "orbit";

    /**
     * The radius of a planet in px which is displayed in the canvas for a star system.
     */
    public static final int PLANET_RADIUS = 10;

    /**
     * The radius of a star in px which is displayed in the canvas for a star system.
     */
    public static final int STAR_RADIUS = 30;

    /**
     * The radius of a star system in px which is displayed in the canvas for the universe.
     */
    public static final int SYSTEM_RADIUS = 5;

    /**
     * The radius of a universe center in px which is displayed in the canvas for the universe.
     */
    public static final int UNIVERSE_CENTER_RADIUS = 50;

    /**
     * The radius of a planetary system in px in the canvas for a star system.
     */
    public static final double RADIUS_PLANETARY_SYSTEM = PLANET_RADIUS * 10;

    /**
     * The color of the main coordinate cross' axis of the canvas.
     */
    private static final String MAIN_AXIS_COLOR = "white";

    /**
     * The color of the planetary system's coordinate cross axis.
     */
    private static final String PLANETARY_AXIS_COLOR = "#b8860b";

    /**
     * The color of the displayed orbit for a planet in the star system canvas.
     */
    private static final String ORBIT_COLOR = "white";

    /**
     * The fleet icon fill color.
     */
    public static final String FLEET_ICON_FILL_COLOR = "white";

    /**
     * The fleet outline color.
     */
    public static final String FLEET_STROKE_COLOR = "transparent";

    /**
     * If a transparent background is needed, use this.
     */
    public static final String TRANSPARENT_FILL_COLOR = "transparent";

    /**
     * If a none filled area is needed.
     */
    public static final String FILL_COLOR_NONE = "NONE";

    /**
     * The course plot color for already flown tracks.
     */
    public static final String COURSE_PLOT_COLOR_INBOUND = "red";

    /**
     * The course plot color for the track ahead.
     */
    public static final String COURSE_PLOT_COLOR_OUTBOUND = "green";

    @Nonnull
    private final UserService userService = ViewHelper.getService(UserService.class);

    /**
     * The canvas which is the base for all map related graphics.
     */
    @Nonnull
    private final Svg canvas;

    /**
     * The set full of known planetary orbits.
     */
    @Nonnull
    private final Set<Orbit> planetaryOrbits = new HashSet<>();

    /**
     * The type of map.
     */
    @Nonnull
    private final EViewBoxType eViewBoxType;

    /**
     * The view box factor is the factor which is calculated if the dimension of the displayed range will be calculated.
     */
    private final double viewBoxFactor;

    /**
     * The radius is the radius which is the outline of the system. It is the base value on which all calculations for the displayed dimension is done.
     */
    private double biggestRadiusOfAllPlanetaryOrbits = Double.MIN_NORMAL;

    /**
     * Holds the occupied area for each planet.
     */
    private final Map<OccupiedArea, Planet> occupiedAreaPlanetMap = new HashMap<>();

    /**
     * Holds the svg circle for each planet.
     */
    private final Map<Planet, Circle> planetCircleMap = new HashMap<>();

    /**
     * Holds the svg circle for each star system
     */
    private final Map<StarSystem, Circle> systemCircleMap = new HashMap<>();

    /**
     * Holds the used slots in an orbit which are occupied by a fleet.
     */
    final Map<Orbit, List<RestrictedFleetArea>> usedFleetSlotsByOrbit = new HashMap<>();

    /**
     * Holds the used slots in on a course which are occupied by a fleet.
     */
    final Map<CourseDefinition, List<RestrictedFleetArea>> usedFleetSlotsByCourse = new HashMap<>();

    /**
     * Holds fleets to their representing svg shark polygons.
     */
    private final Map<Fleet, FleetSvgWrapper> fleetSharkMap = new HashMap<>();

    /**
     * Holds the course plot for a route.
     */
    private final Map<CourseDefinition, CoursePlot> courseDefinitions = new HashMap<>();

    /**
     * Creates the view box and coordinate system for the given canvas by the given orbits.
     *
     * @param starSystems all star systems to display in the universe star map
     * @param canvas      the canvas which holds the universe
     */
    public ViewBoxDefinition(@Nonnull final Set<StarSystem> starSystems, @Nonnull final Svg canvas, @Nullable final StarSystem starSystemToPan) {
        Preconditions.checkNotNull(starSystems, "starSystems shouldn't be null!");
        Preconditions.checkNotNull(canvas, "canvas shouldn't be null!");

        this.canvas = canvas;
        this.eViewBoxType = UNIVERSE;
        viewBoxFactor = getViewBoxFactor();
        final Set<Orbit> orbitCollection = starSystems.stream().map(StarSystem::getOrbit).collect(Collectors.toSet());
        adjustCanvas(orbitCollection);

        panToStarSystem(starSystemToPan);
        starSystems.forEach(this::createStarSystemCircle);
    }

    /**
     * Creates the view box and coordinate system for the given canvas by the given star system.
     *
     * @param starSystem all planets to display in the star system map
     * @param canvas     the canvas which holds the star system
     */
    public ViewBoxDefinition(@Nonnull final StarSystem starSystem, @Nonnull final Svg canvas, @Nullable final Planet planetToPan) {
        Preconditions.checkNotNull(starSystem, "starSystem shouldn't be null!");
        Preconditions.checkNotNull(canvas, "canvas shouldn't be null!");

        this.canvas = canvas;
        this.eViewBoxType = STAR_SYSTEM;
        viewBoxFactor = getViewBoxFactor();
        final Set<Orbit> orbitCollection = starSystem.getPlanets().stream().map(Planet::getOrbit).collect(Collectors.toSet());
        adjustCanvas(orbitCollection);

        final Set<Planet> planets = starSystem.getPlanets();
        final Map<Planet, Set<Fleet>> fleetInSystem = starSystem.getFleets().stream()
                .filter(fleet -> fleet.getOrbit() != null && fleet.getMove() == null)
                .map(e -> new AbstractMap.SimpleEntry<>(e.getOrbit().getPlanet(), e))
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toSet())));

        planets.forEach(planet -> {
            final Set<Fleet> fleetsInOrbit = fleetInSystem.get(planet);
            if (fleetsInOrbit != null) {
                fleetsInOrbit.forEach(this::printFleetPolygonInOrbit);
            }
        });

        final Set<Fleet> movingFleets = starSystem.getFleets().stream()
                .filter(fleet -> fleet.getMove() != null)
                .collect(Collectors.toSet());

        movingFleets.forEach(this::printMovingFleet);
        panToPlanet(planetToPan);
        planets.forEach(this::createPlanetCircle);
    }

    /**
     * Rotates the point from a x-y coordinate set around the given angle and center it at the gicen oddset coodinates.
     *
     * @param x       the x coordinate
     * @param xOffset the x center coordinate
     * @param y       the y coordinate
     * @param yOffset the y center coordinate
     * @param angle   the angle
     * @return the resulting point
     */
    @VisibleForTesting
    static AbstractPolyElement.PolyCoordinatePair rotatePointOffset(final double x, final double xOffset, final double y, final double yOffset, final double angle) {
        final double rotatedX = (x * Math.cos(Math.toRadians(angle))) - (y * Math.sin(Math.toRadians(angle)));
        final double rotatedY = (x * Math.sin(Math.toRadians(angle))) + (y * Math.cos(Math.toRadians(angle)));
        return new Polyline.PolyCoordinatePair(xOffset + rotatedX, yOffset + rotatedY);
    }

    /**
     * Creates markers for the panned svg element.
     *
     * @param orbit        the orbit which has to be marked as panned
     * @param eViewBoxType if the marked orbit is inside a star system or the universe
     */
    private void createPanToMarker(@Nonnull final Orbit orbit, EViewBoxType eViewBoxType) {
        Preconditions.checkNotNull(orbit, "orbit shouldn't be null!");

        final int x = orbit.getXCoordinate();
        final int y = orbit.getYCoordinate();
        final double baseRadius = eViewBoxType == UNIVERSE ? SYSTEM_RADIUS * 2 : PLANET_RADIUS * 2;
        final double pinDistanceSummand = baseRadius * 2;
        final double pinWidthSummand = baseRadius * 0.7;
        final double pinLengthSummand = baseRadius * 4;
        final int strokeWidth = 2;

        final List<SvgElement> svgElements = new ArrayList<>();
        double outerAngle = 45;
        final List<AbstractPolyElement.PolyCoordinatePair> points1 = new ArrayList<>();
        points1.add(rotatePointOffset(0, x, pinDistanceSummand, y, outerAngle));
        points1.add(rotatePointOffset(pinWidthSummand, x, pinLengthSummand, y, outerAngle));
        points1.add(rotatePointOffset(-1 * pinWidthSummand, x, pinLengthSummand, y, outerAngle));

        final Polygon panToMarker1 = new Polygon("panToMarker1", points1);
        panToMarker1.setFillColor(TRANSPARENT_FILL_COLOR);
        panToMarker1.setStroke(PAN_TO_MARKER_COLOR, strokeWidth, Path.LINE_CAP.ROUND, Path.LINE_JOIN.ROUND);

        final List<AbstractPolyElement.PolyCoordinatePair> points2 = new ArrayList<>();
        points2.add(rotatePointOffset(0, x, -1 * pinDistanceSummand, y, outerAngle));
        points2.add(rotatePointOffset(pinWidthSummand, x, -1 * pinLengthSummand, y, outerAngle));
        points2.add(rotatePointOffset(-1 * pinWidthSummand, x, -1 * pinLengthSummand, y, outerAngle));

        final Polygon panToMarker2 = new Polygon("panToMarker2", points2);
        panToMarker2.setFillColor(TRANSPARENT_FILL_COLOR);
        panToMarker2.setStroke("darkred", strokeWidth, Path.LINE_CAP.ROUND, Path.LINE_JOIN.ROUND);

        final List<AbstractPolyElement.PolyCoordinatePair> points3 = new ArrayList<>();
        points3.add(rotatePointOffset(-1 * pinDistanceSummand, x, 0, y, outerAngle));
        points3.add(rotatePointOffset(-1 * pinLengthSummand, x, -1 * pinWidthSummand, y, outerAngle));
        points3.add(rotatePointOffset(-1 * pinLengthSummand, x, 1 * pinWidthSummand, y, outerAngle));

        final Polygon panToMarker3 = new Polygon("panToMarker3", points3);
        panToMarker3.setFillColor(TRANSPARENT_FILL_COLOR);
        panToMarker3.setStroke("darkred", strokeWidth, Path.LINE_CAP.ROUND, Path.LINE_JOIN.ROUND);

        final List<AbstractPolyElement.PolyCoordinatePair> points4 = new ArrayList<>();
        points4.add(rotatePointOffset(pinDistanceSummand, x, 0, y, outerAngle));
        points4.add(rotatePointOffset(pinLengthSummand, x, -1 * pinWidthSummand, y, outerAngle));
        points4.add(rotatePointOffset(pinLengthSummand, x, 1 * pinWidthSummand, y, outerAngle));

        final Polygon panToMarker4 = new Polygon("panToMarker4", points4);
        panToMarker4.setFillColor(TRANSPARENT_FILL_COLOR);
        panToMarker4.setStroke("darkred", strokeWidth, Path.LINE_CAP.ROUND, Path.LINE_JOIN.ROUND);

        // creating a inner ring
        final List<Polyline.PolyCoordinatePair> distanceRing = new ArrayList<>();
        final int amountOfCirclePoints = 360;
        for (int i = 0; i < amountOfCirclePoints; ++i) {
            final double angle = Math.toRadians(((double) i / amountOfCirclePoints) * 360d);
            // pinDistanceSummand - 2 is to separate the ring optically from the markers
            distanceRing.add(
                    new Polyline.PolyCoordinatePair(x + Math.cos(angle) * (pinDistanceSummand - 2),
                            y + Math.sin(angle) * (pinDistanceSummand - 2)));
        }
        // fully close the ring
        distanceRing.add(distanceRing.get(0));

        Polyline polyline = new Polyline("panToMarkerRing", distanceRing);
        polyline.setFillColor(TRANSPARENT_FILL_COLOR);
        polyline.setStroke("darkred", 1, Path.LINE_CAP.ROUND, Path.LINE_JOIN.ROUND);

        svgElements.add(polyline);
        svgElements.add(panToMarker1);
        svgElements.add(panToMarker2);
        svgElements.add(panToMarker3);
        svgElements.add(panToMarker4);
        svgElements.forEach(canvas::add);
    }

    /**
     * If a user has to be finger-pointed to a star system.
     */
    private void panToStarSystem(@Nullable final StarSystem system) {
        if (system != null) {
            final Orbit o = system.getOrbit();
            createPanToMarker(o, UNIVERSE);
            centerAndZoomViewBox(o);
        }
    }

    /**
     * Centers the view box to the given orbit.
     *
     * @param orbit the orbit to center
     */
    private void centerAndZoomViewBox(@Nonnull final Orbit orbit) {
        Preconditions.checkNotNull(orbit, "orbit shouldn't be null!");

        final int x = orbit.getXCoordinate();
        final int y = orbit.getYCoordinate();
        final double viewBoxWidth = getViewBoxWidth() / 2;
        final double viewBoxHeight = getViewBoxHeight() / 2;
        final double minX = x - viewBoxWidth / 2;
        final double minY = y - viewBoxHeight / 2;
        canvas.viewbox(minX, minY, viewBoxWidth, viewBoxHeight);
    }

    /**
     * If a user has to be finger-pointed to a planet.
     */
    private void panToPlanet(@Nullable final Planet planet) {
        if (planet != null) {
            final Orbit o = planet.getOrbit();
            createPanToMarker(o, STAR_SYSTEM);
            centerAndZoomViewBox(o);
        }
    }

    /**
     * Checks for some needful parameters.
     *
     * @param orbit the orbit to check.
     */
    private void updateMapRanges(@Nonnull final Orbit orbit) {
        Preconditions.checkNotNull(orbit, "orbit shouldn't be null!");

        final int xCoordinate = orbit.getXCoordinate();
        final int yCoordinate = orbit.getYCoordinate();

        final double radius = getDistance(xCoordinate, yCoordinate);
        if (radius > biggestRadiusOfAllPlanetaryOrbits) {
            biggestRadiusOfAllPlanetaryOrbits = radius;
        }
    }

    /**
     * Returns the distance between the two given orbits.
     *
     * @param orbit1 the first orbit
     * @param orbit2 the second orbit
     * @return the distance
     */
    public static double getOrbitalDistance(@Nonnull final Orbit orbit1, @Nonnull final Orbit orbit2) {
        Preconditions.checkNotNull(orbit1, "orbit1 shouldn't be null!");
        Preconditions.checkNotNull(orbit2, "orbit2 shouldn't be null!");

        final int x1 = orbit1.getXCoordinate();
        final int y1 = orbit1.getYCoordinate();

        final int x2 = orbit2.getXCoordinate();
        final int y2 = orbit2.getYCoordinate();

        return getDistance(x2 - x1, y2 - y1);
    }

    /**
     * Returns the distance between the two given orbits.
     *
     * @param orbit1 the first orbit
     * @param orbit2 the second orbit
     * @return the distance
     */
    private static double getOrbitalDistance(@Nonnull final Point orbit1, @Nonnull final Point orbit2) {
        Preconditions.checkNotNull(orbit1, "orbit1 shouldn't be null!");
        Preconditions.checkNotNull(orbit2, "orbit2 shouldn't be null!");

        final double x1 = orbit1.getX();
        final double y1 = orbit1.getY();

        final double x2 = orbit2.getX();
        final double y2 = orbit2.getY();

        return getDistance(x2 - x1, y2 - y1);
    }

    /**
     * Calculates the distance between thw two given coordinates.
     *
     * @param firstCoord  the first digit
     * @param secondCoord the second digit
     * @return the distance
     */
    private static double getDistance(double firstCoord, double secondCoord) {
        return Math.sqrt(Math.pow(firstCoord, 2) + Math.pow(secondCoord, 2));
    }

    /**
     * Returns the factor which is used to set the size of the view box for the canvas.
     *
     * @return the factor
     */
    private double getViewBoxFactor() {
        switch (eViewBoxType) {
            case STAR_SYSTEM:
            case UNIVERSE:
            default:
                return 1;
        }
    }

    /**
     * Puts all the stuff to the canvas and sets the initial view box.
     */
    private void adjustCanvas(@Nonnull final Set<Orbit> orbitCollection) {
        Preconditions.checkNotNull(orbitCollection, "orbitCollection shouldn't be null!");

        orbitCollection.forEach(this::updateMapRanges);
        this.planetaryOrbits.addAll(orbitCollection);

        switch (eViewBoxType) {
            case STAR_SYSTEM:
                createVisibleOrbits();
                createMapCenterObject();
                createOrbitsCoordinateSystems();
                break;
            case UNIVERSE:
            default:
                break;
        }
        createCoordinateCross(STAR_SYSTEM_CROSS, "center", Orbit.getCenterOrbit(), getBiggestRadiusOfAllPlanetaryOrbits(), MAIN_AXIS_COLOR);

        double viewBoxMinX = getViewBoxMinX();
        double viewBoxMinY = getViewBoxMinY();
        double viewBoxHeight = getViewBoxHeight();
        double viewBoxWidth = getViewBoxWidth();
        canvas.viewbox(viewBoxMinX, viewBoxMinY, viewBoxWidth, viewBoxHeight);
    }

    /**
     * Creates the sun or the universe center.
     */
    private void createMapCenterObject() {

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

        planetaryOrbits.forEach(orbit -> {
            final int xCoord = Math.abs(orbit.getXCoordinate());
            final int yCoord = Math.abs(orbit.getYCoordinate());

            final double radius = getDistance(xCoord, yCoord);

            final Circle circle = new Circle("ellipsoid-" + idCreateOrbitID(orbit), radius);
            circle.setFillColor(TRANSPARENT_FILL_COLOR);
            circle.setStroke(ORBIT_COLOR, 1);
            circle.center(0, 0);
            canvas.add(circle);
        });
    }

    /**
     * Creates the coordinate systems for {@link ViewBoxDefinition#planetaryOrbits}.
     */
    private void createOrbitsCoordinateSystems() {
        planetaryOrbits.forEach(orbit -> {
            final String id = "orbit-" + idCreateOrbitID(orbit);
            createCoordinateCross(PLANETARY_SYSTEM_CROSS, id, orbit, RADIUS_PLANETARY_SYSTEM, PLANETARY_AXIS_COLOR);
        });
    }

    /**
     * Creates the coordinate cross for the given parameters.
     *
     * @param idSuffix a suffix for the css selector
     * @param orbit    the center
     * @param radius   the axis length
     * @param color    the color of the coordinate cross
     */
    private void createCoordinateCross(@Nonnull final ECoordinateCrossType eCoordinateCrossType,
                                       @Nonnull final String idSuffix,
                                       @Nonnull final Orbit orbit,
                                       final double radius,
                                       @Nonnull final String color) {
        Preconditions.checkNotNull(eCoordinateCrossType, "eCoordinateCrossType shouldn't be null!");
        Preconditions.checkNotNull(idSuffix, "idSuffix shouldn't be null!");
        Preconditions.checkNotNull(color, "color shouldn't be null!");

        final int xCoordinate = orbit.getXCoordinate();
        final int yCoordinate = orbit.getYCoordinate();

        final double enlargedRadius = radius * AXIS_ENLARGEMENT_FACTOR;
        createScaleDivider(eCoordinateCrossType, idSuffix, xCoordinate, yCoordinate, radius, color);
        final double xAbsValueFrom = -1 * enlargedRadius + xCoordinate;
        final double xAbsValueTo = enlargedRadius + xCoordinate;
        final double yAbsValueFrom = -1 * enlargedRadius + yCoordinate;
        final double yAbsValueTo = enlargedRadius + yCoordinate;
        createLine(X_LINE_ID + idSuffix, new Point(xAbsValueFrom, yCoordinate), new Point(xAbsValueTo, yCoordinate), color);
        createLine(Y_LINE_ID + idSuffix, new Point(xCoordinate, yAbsValueFrom), new Point(xCoordinate, yAbsValueTo), color);
    }

    /**
     * Creates the scale dividers for this coordinate cross.
     *
     * @param radius the radius of the coordinate system
     */
    private void createScaleDivider(@Nonnull final ECoordinateCrossType eCoordinateCrossType,
                                    @Nonnull final String idSuffix,
                                    final int xCoordinate,
                                    final int yCoordinate,
                                    final double radius,
                                    @Nonnull final String color) {
        Preconditions.checkNotNull(eCoordinateCrossType, "eCoordinateCrossType shouldn't be null!");
        Preconditions.checkNotNull(idSuffix, "idSuffix shouldn't be null!");
        Preconditions.checkNotNull(color, "color shouldn't be null!");

        final double enlargedRadius = radius * AXIS_ENLARGEMENT_FACTOR;

        int countOfMiniLines = getScaleDividerCount(enlargedRadius);
        for (; countOfMiniLines > 0; countOfMiniLines--) {
            final double scale = 10 * countOfMiniLines;
            int scaleDividerLength = getScaleDividerWidth(eCoordinateCrossType, countOfMiniLines);

            final double xAbsValueFrom = -1 * scaleDividerLength + xCoordinate;
            final double xAbsValueTo = scaleDividerLength + xCoordinate;
            final double yAbsValueFrom = -1 * scaleDividerLength + yCoordinate;
            final double yAbsValueTo = scaleDividerLength + yCoordinate;

            final double xAbsValueFromScale = -1 * scale + xCoordinate;
            final double xAbsValueToScale = scale + xCoordinate;
            final double yAbsValueFromScale = -1 * scale + yCoordinate;
            final double yAbsValueToScale = scale + yCoordinate;

            createLine(AXIS_SCALE_X_ID + idSuffix + countOfMiniLines, new Point(xAbsValueToScale, yAbsValueFrom), new Point(xAbsValueToScale, yAbsValueTo), color);
            createLine(AXIS_SCALE_X_ID + idSuffix + (-1 * countOfMiniLines), new Point(xAbsValueFromScale, yAbsValueFrom), new Point(xAbsValueFromScale, yAbsValueTo), color);
            createLine(AXIS_SCALE_Y_ID + idSuffix + countOfMiniLines, new Point(xAbsValueFrom, yAbsValueToScale), new Point(xAbsValueTo, yAbsValueToScale), color);
            createLine(AXIS_SCALE_Y_ID + idSuffix + (-1 * countOfMiniLines), new Point(xAbsValueFrom, yAbsValueFromScale), new Point(xAbsValueTo, yAbsValueFromScale), color);
        }
    }

    /**
     * Calculates the width for the scale dividers position.
     *
     * @param scaleDividerNumber the position
     * @return the width
     */
    private int getScaleDividerWidth(@Nonnull final ECoordinateCrossType eCoordinateCrossType,
                                     final int scaleDividerNumber) {
        Preconditions.checkNotNull(eCoordinateCrossType, "eCoordinateCrossType shouldn't be null!");


        final int bigDivider;
        final int smallDivider;
        switch (eCoordinateCrossType) {
            case STAR_SYSTEM_CROSS:
                bigDivider = 100;
                smallDivider = 50;
                break;
            case PLANETARY_SYSTEM_CROSS:
            default:
                bigDivider = 10;
                smallDivider = 5;
                break;
        }

        if (scaleDividerNumber <= 3) {
            return 3;
        } else if (scaleDividerNumber % bigDivider == 0) {
            return 50;
        } else if (scaleDividerNumber % smallDivider == 0) {
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
     * @param id    the css selector for this line
     * @param start the point where the line starts
     * @param end   the point where the line ends
     * @param color the color of the line stroke
     */
    private void createLine(@Nonnull final String id,
                            @Nonnull final Point start,
                            @Nonnull final Point end,
                            @Nonnull final String color) {
        Preconditions.checkNotNull(id, "id shouldn't be null!");
        Preconditions.checkNotNull(start, "start shouldn't be null!");
        Preconditions.checkNotNull(end, "end shouldn't be null!");
        Preconditions.checkNotNull(color, "color shouldn't be null!");

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
     * Returns the biggest included radius as <b>absolute value</b>.
     *
     * @return the biggest radius <b>absolute value</b>
     */
    private double getBiggestRadiusOfAllPlanetaryOrbits() {
        return Math.abs(biggestRadiusOfAllPlanetaryOrbits);
    }

    /**
     * Calculates and returns the initial view box width.
     *
     * @return the view box width to set
     */
    private double getViewBoxWidth() {
        final double biggestRadius = getBiggestRadiusOfAllPlanetaryOrbits();
        return (biggestRadius * 2) * viewBoxFactor;
    }

    /**
     * Calculates and returns the initial view box height.
     *
     * @return the view box height to set
     */
    private double getViewBoxHeight() {
        final double biggestRadius = getBiggestRadiusOfAllPlanetaryOrbits();
        return (biggestRadius) * viewBoxFactor;
    }

    /**
     * Calculates and returns the smallest digit on the y-axis which must be displayed.
     *
     * @return the smallest y digit
     */
    private double getViewBoxMinY() {
        final double biggestRadius = getBiggestRadiusOfAllPlanetaryOrbits();
        return (biggestRadius / -2) * viewBoxFactor;
    }

    /**
     * Calculates and returns the smallest digit on the x-axis which must be displayed.
     *
     * @return the smallest x digit
     */
    private double getViewBoxMinX() {
        final double biggestRadius = getBiggestRadiusOfAllPlanetaryOrbits();
        return (biggestRadius * -1) * viewBoxFactor;
    }

    /**
     * Creates a zoomable {@link Svg} in full size as a canvas for other svg elements.
     *
     * @param cssID the css identifier
     * @return the canvas
     */
    public static Svg createStarMapCanvas(@Nonnull final String cssID) {
        Preconditions.checkNotNull(cssID, "cssID shouldn't be null!");

        final Svg canvas = new Svg();
        canvas.setHeightFull();
        canvas.setWidthFull();
        canvas.setId(cssID);
        canvas.setZoomEnabled(true);

        return canvas;
    }

    /**
     * Creates the svg circle which represents the given star system.
     *
     * @param starSystem the star system which must be represented
     */
    private void createStarSystemCircle(@Nonnull final StarSystem starSystem) {
        Preconditions.checkNotNull(starSystem, "starSystem shouldn't be null!");

        final Orbit orbit = starSystem.getOrbit();
        // creates colonizable markers
        starSystem.getPlanets().stream().filter(Planet::isColonizable).findFirst().ifPresent(planet -> {
            final double x1 = orbit.getXCoordinate() - 9;
            final double y1 = orbit.getYCoordinate() - 8;
            final double x2 = orbit.getXCoordinate() + 9;
            final double y2 = orbit.getYCoordinate() + 8;
            final Path path = new Path(starSystem.getName() + "isColonizable", "M" + x1 + "," + y1 + " A 1,1,1 1 1 " + x2 + "," + y2);
            path.setFillColor(TRANSPARENT_FILL_COLOR);
            path.setStroke(COLONIZABLE_SYSTEM_MARKER_COLOR, 2, Path.LINE_CAP.SQUARE, Path.LINE_JOIN.ROUND);
            canvas.add(path);
        });
        // define color of system circle
        String systemCircleFillColor = NOT_COLONIZED_COLOR;
        final User loggedInUser = userService.getLoggedInUser();
        final boolean isColonizedByLoggedInUser = starSystem.getPlanets().stream()
                .anyMatch(planet -> loggedInUser.equals(planet.getOwner()));

        final boolean isColonizedByOtherUser = starSystem.getPlanets().stream()
                .filter(planet -> planet.getOwner() != null)
                .anyMatch(planet -> !loggedInUser.equals(planet.getOwner()));

        if (isColonizedByLoggedInUser) {
            systemCircleFillColor = IS_COLONIZED_BY_USER_COLOR;
            // is colonized by other, too
            if (isColonizedByOtherUser) {

                final double x1 = orbit.getXCoordinate() + (SYSTEM_RADIUS + 4);
                final double y1 = orbit.getYCoordinate() + (SYSTEM_RADIUS + 3);
                final double x2 = orbit.getXCoordinate() - (SYSTEM_RADIUS + 4);
                final double y2 = orbit.getYCoordinate() - (SYSTEM_RADIUS + 3);
                final Path path = new Path(starSystem.getName() + "isColonizable", "M" + x1 + "," + y1 + " A 1,1,1 1 1 " + x2 + "," + y2);
                path.setFillColor(TRANSPARENT_FILL_COLOR);
                path.setStroke(COLONIZED_BY_OTHERS_COLOR, 1, Path.LINE_CAP.SQUARE, Path.LINE_JOIN.ROUND);
                canvas.add(path);
            }
        } else if (isColonizedByOtherUser) {
            // is only colonized by other
            systemCircleFillColor = COLONIZED_BY_OTHERS_COLOR;
        }

        // creates the system circle
        final String circleID = idCreateOrbitID(orbit);
        final Circle circle = new Circle(circleID, ViewBoxDefinition.SYSTEM_RADIUS);
        circle.center(orbit.getXCoordinate(), orbit.getYCoordinate());
        circle.setFillColor(systemCircleFillColor);
        circle.setDraggable(true);
        canvas.add(circle);
        systemCircleMap.put(starSystem, circle);
    }

    /**
     * Creates the svg circle which represents the given planet.
     *
     * @param planet the planet which must be represented
     */
    private void createPlanetCircle(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, PLANET_SELECTOR_ID_PREFIX + " shouldn't be null!");

        final User loggedInUser = userService.getLoggedInUser();
        final Orbit orbit = planet.getOrbit();
        final Set<Colonization> colonizations = userService.getColonizations(loggedInUser);
        colonizations.stream()
                .filter(c -> c.getPlanet().equals(planet))
                .findFirst()
                .ifPresent(colonization -> {

                    final double x1 = orbit.getXCoordinate() + (PLANET_RADIUS + 4);
                    final double y1 = orbit.getYCoordinate() + (SYSTEM_RADIUS + 3);
                    final double x2 = orbit.getXCoordinate() - (PLANET_RADIUS + 4);
                    final double y2 = orbit.getYCoordinate() - (SYSTEM_RADIUS + 3);
                    final Path path = new Path(planet.getName() + "colonizationInProgress", "M" + x1 + "," + y1 + " A 1,1,1 1 1 " + x2 + "," + y2);
                    path.setFillColor(TRANSPARENT_FILL_COLOR);
                    path.setStroke(COLONIZATION_IN_PROGRESS, 1, Path.LINE_CAP.SQUARE, Path.LINE_JOIN.ROUND);
                    canvas.add(path);
                });

        String planetsCircleColor = NOT_COLONIZED_COLOR;
        if (!planet.isColonizable()) {
            if (loggedInUser.equals(planet.getOwner())) {
                planetsCircleColor = IS_COLONIZED_BY_USER_COLOR;
            } else {
                planetsCircleColor = COLONIZED_BY_OTHERS_COLOR;
            }
        }

        // create planet circle
        final String circleID = idCreatePlanetID(planet);
        final Circle circle = new Circle(circleID, ViewBoxDefinition.PLANET_RADIUS);
        circle.center(orbit.getXCoordinate(), orbit.getYCoordinate());
        circle.setFillColor(planetsCircleColor);
        circle.setDraggable(true);
        canvas.add(circle);

        planetCircleMap.put(planet, circle);
        occupiedAreaPlanetMap.put(new OccupiedArea(planet, circle, orbit, RADIUS_PLANETARY_SYSTEM), planet);
    }

    /**
     * Creates an id for planet circles on the canvas.
     *
     * @param planet the planet which the id is for
     * @return the id
     */
    @Nonnull
    public static String idCreatePlanetID(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, PLANET_SELECTOR_ID_PREFIX + " shouldn't be null!");

        return PLANET_SELECTOR_ID_PREFIX + "-" + planet.hashCode();
    }

    /**
     * Creates an id for fleet circles on the canvas.
     *
     * @param fleet the fleet which the id is for
     * @return the id
     */
    @Nonnull
    public static String idCreateFleetID(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        return FLEET_SELECTOR_ID_PREFIX + "-" + fleet.getId();
    }

    /**
     * Creates an id for an orbit.
     *
     * @param orbit the orbit
     * @return the id
     */
    @Nonnull
    public static String idCreateOrbitID(@Nonnull final Orbit orbit) {
        Preconditions.checkNotNull(orbit, "orbit shouldn't be null!");

        return ORBIT_SELECTOR_ID_PREFIX + "-" + orbit.getXCoordinate() + "-" + orbit.getYCoordinate();
    }

    /**
     * Creates an id for a fleet's text.
     *
     * @param fleet the fleet
     * @return the fleet's text id
     */
    public static String idCreateFleetTextID(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        return FLEET_TEXT_ID + fleet.getId();
    }

    /**
     * Creates an id for a course.
     *
     * @param startOrbit  the starting orbit
     * @param targetOrbit the target orbit
     * @return the id
     */
    @Nonnull
    private String idCreateCourseID(@Nonnull final Orbit startOrbit, @Nonnull final Orbit targetOrbit) {
        Preconditions.checkNotNull(startOrbit, "startOrbit shouldn't be null!");
        Preconditions.checkNotNull(targetOrbit, "targetOrbit shouldn't be null!");

        return COURSE_ID + idCreateOrbitID(startOrbit) + idCreateOrbitID(targetOrbit);
    }

    /**
     * Creates a fleet shark an a course plot, if needed. Will print them to the canvas, too.
     *
     * @param fleet the fleet to print
     */
    public void createFleetShark(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        if (fleet.getMove() != null) {
            printMovingFleet(fleet);
        } else {
            printFleetPolygonInOrbit(fleet);
        }
    }

    /**
     * Creates a triangle which represents a fleet.
     * Planet the fleet next to the planet in it's orbit in a random position.
     */
    private void printFleetPolygonInOrbit(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkArgument(fleet.getOrbit() != null, "fleet's orbit shouldn't be null!");
        Preconditions.checkArgument(fleet.getOrbit().getPlanet() != null, "fleet's orbit planet shouldn't be null!");

        final double xOffset = PLANET_RADIUS * 5;
        final double yOffset = PLANET_RADIUS * 5;

        final Orbit orbit = fleet.getOrbit().getPlanet().getOrbit();
        final double x = orbit.getXCoordinate() + xOffset;
        final double y = orbit.getYCoordinate() + yOffset;

        final List<RestrictedFleetArea> restrictedFleetAreas = usedFleetSlotsByOrbit.computeIfAbsent(orbit, k -> new ArrayList<>());

        printFleetShark(fleet, x, y, restrictedFleetAreas, false);
    }

    /**
     * Creates a fleet shark aligned with the vector from the fleets start to it's target
     * based on the amount of way which as already travelled.
     *
     * @param fleet the fleet in movement
     */
    private void printMovingFleet(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkArgument(fleet.getMove() != null, "fleet's move shouldn't be null!");

        final Move move = fleet.getMove();
        if (move.getStartOrbit().getPlanet() == null || move.getTargetOrbit().getPlanet() == null) {
            throw new NotifySBUserException("Should not happen while a movement needs a starting and a target point.");
        }
        final Orbit startPlanetOrbit = move.getStartOrbit().getPlanet().getOrbit();

        final FleetOrbit targetOrbit = move.getTargetOrbit();
        final Planet targetPlanet = targetOrbit.getPlanet();
        final Orbit targetPlanetOrbit = targetPlanet.getOrbit();

        final CourseDefinition courseDefinition = new CourseDefinition(startPlanetOrbit, targetPlanetOrbit);

        CoursePlot coursePlot = courseDefinitions.get(courseDefinition);
        if (coursePlot == null) {
            coursePlot = printCoursePlot(courseDefinition);
        }

        final double ticksToTravelAsFraction = getTicksToTravelAsFraction(fleet, move, targetPlanet);

        final ProgressOnCourse byProgress = ProgressOnCourse.getByProgress(ticksToTravelAsFraction);
        final List<Fleet> fleetsOnStage = coursePlot.getFleetsOnStage(byProgress);
        boolean drawAgain = true;
        if (fleetsOnStage.contains(fleet)) {
            final ProgressOnCourse progressByFleet = coursePlot.getProgressByFleet(fleet);
            // draw a new icon for the fleet if there is a difference between 'as is' and 'as should be'
            if (byProgress != progressByFleet && fleetSharkMap.containsKey(fleet)) {
                final FleetShark oldFleetShark = fleetSharkMap.get(fleet).getFleetShark();
                final Text fleetText = fleetSharkMap.get(fleet).getFleetText();
                canvas.remove(oldFleetShark);
                canvas.remove(fleetText);
                fleetSharkMap.remove(fleet);
                fleetsOnStage.remove(fleet);
            } else {
                drawAgain = false;
            }
        }
        if (drawAgain) {
            fleetsOnStage.add(fleet);

            final QuadCurve2D quadCurve2D = coursePlot.getQuadCurve2D();
            final List<Point2D> fullCourseAsPoints2D = getFullCoursePoints(quadCurve2D);

            final int pointOnCourseIndex = (int) (fullCourseAsPoints2D.size() * byProgress.getProgressOntrack());
            final Point2D coursePoint2D = fullCourseAsPoints2D.get(pointOnCourseIndex);
            final double x = coursePoint2D.getX();
            final double y = coursePoint2D.getY();

            final boolean courseOutbound = isCourseOutbound(startPlanetOrbit, targetPlanetOrbit);

            final List<RestrictedFleetArea> restrictedFleetAreas = usedFleetSlotsByCourse.computeIfAbsent(courseDefinition, k -> new ArrayList<>());

            printFleetShark(fleet, x, y, restrictedFleetAreas, courseOutbound);
        }
    }

    /**
     * Checks how many ticks are left on this journey - as a fraction of the already travelled track of the full track.
     *
     * @param fleet        the fleet in motion
     * @param move         the planned move itself
     * @param targetPlanet the destination
     * @return the fraction of progress on the track
     */
    private double getTicksToTravelAsFraction(@Nonnull final Fleet fleet,
                                              @Nonnull final Move move,
                                              @Nonnull final Planet targetPlanet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkNotNull(move, "move shouldn't be null!");
        Preconditions.checkNotNull(targetPlanet, "targetPlanet shouldn't be null!");

        final int travelTime = DistanceCalculator.calculateTimeToTravel(fleet, targetPlanet);
        final int moveDoneAtZero = move.getMoveDoneAtZero();
        double ticksToTravelAsFraction = 1 - ((double) moveDoneAtZero / (double) travelTime);

        if (ticksToTravelAsFraction == 1) {
            // case: 1-tick-run - just to place the fleet not on the orbit coord cross
            ticksToTravelAsFraction = 0.6;
        }
        return ticksToTravelAsFraction;
    }

    /**
     * Checks if the planned course is headed outwards or inwards the system.
     *
     * @param startPlanetOrbit  the origin
     * @param targetPlanetOrbit the destination
     * @return <code>true</code> if the course is headed outwards, <code>false</code> otherwise
     */
    private boolean isCourseOutbound(@Nonnull final Orbit startPlanetOrbit, @Nonnull final Orbit targetPlanetOrbit) {
        Preconditions.checkNotNull(startPlanetOrbit, "startPlanetOrbit shouldn't be null!");
        Preconditions.checkNotNull(targetPlanetOrbit, "targetPlanetOrbit shouldn't be null!");

        final double radiusStartOrbit = getDistance(startPlanetOrbit.getXCoordinate(), startPlanetOrbit.getYCoordinate());
        final double radiusTargetOrbit = getDistance(targetPlanetOrbit.getXCoordinate(), targetPlanetOrbit.getYCoordinate());
        // true if the fleet flies outwards
        return radiusStartOrbit <= radiusTargetOrbit;
    }

    /**
     * Creates an points list which represents the full course.
     *
     * @param quadCurve2D the curve
     * @return the course points list
     */
    @Nonnull
    private List<Point2D> getFullCoursePoints(@Nonnull final QuadCurve2D quadCurve2D) {
        Preconditions.checkNotNull(quadCurve2D, "quadCurve2D shouldn't be null!");

        final PathIterator pi = quadCurve2D.getPathIterator(null, 1);
        final List<Point2D> fullCourseAsPoints2D = new ArrayList<>();

        while (!pi.isDone()) {
            final double[] coords = new double[6];

            switch (pi.currentSegment(coords)) {
                case PathIterator.SEG_MOVETO:
                    fullCourseAsPoints2D.add(new Point2D.Double(coords[0], coords[1]));
                    break;
                case PathIterator.SEG_LINETO:
                    final Point2D p1 = fullCourseAsPoints2D.get(fullCourseAsPoints2D.size() - 1);
                    final Point2D p2 = new Point2D.Double(coords[0], coords[1]);
                    double d = p1.distance(p2);
                    double i = d / 0.5;
                    for (int j = 0; j < i; j++) {
                        final Point2D p3 = new Point2D.Double(p1.getX() + (j / i) * (p2.getX() - p1.getX()), p1.getY() + (j / i) * (p2.getY() - p1.getY()));
                        fullCourseAsPoints2D.add(p3);
                    }
                    break;
            }
            pi.next();
        }
        return fullCourseAsPoints2D;
    }

    /**
     * Prints the course plot by the given values.
     *
     * @param courseDefinition the course definition
     * @return the plotted {@link Path} and the representing {@link QuadCurve2D}
     */
    private CoursePlot printCoursePlot(final CourseDefinition courseDefinition) {
        Preconditions.checkNotNull(courseDefinition, "courseDefinition shouldn't be null!");

        final Orbit startOrbit = courseDefinition.getStartOrbit();
        final Orbit targetOrbit = courseDefinition.getTargetOrbit();

        final double startX = startOrbit.getXCoordinate();
        final double startY = startOrbit.getYCoordinate();

        final double endX = targetOrbit.getXCoordinate();
        final double endY = targetOrbit.getYCoordinate();

        final double relativeTargetX = endX - startX;
        final double relativeTargetY = endY - startY;

        final double baseQx = 30;
        final double baseQy = 50;

        int qXMultiplier = 1;
        int qYMultiplier = 1;
        if (relativeTargetY < 0) {
            // qY is negative if the movement on y-axis is inbound
            qYMultiplier = -1;
        }

        final String color;
        if (getDistance(startX, startY) <= getDistance(endX, endY)) {
            color = COURSE_PLOT_COLOR_OUTBOUND;
            // outbound qX is negative
            qXMultiplier = -1;
        } else {
            color = COURSE_PLOT_COLOR_INBOUND;
        }

        final double cX = qXMultiplier * baseQx;
        final double cY = qYMultiplier * baseQy;
        final String qX = "" + cX;
        final String qY = "" + cY;

        final String id = idCreateCourseID(startOrbit, targetOrbit);
        final Path path = new Path(id, "M" + startX + "," + startY + " q" + qX + "," + qY + " " + relativeTargetX + "," + relativeTargetY);
        path.setFillColor(FILL_COLOR_NONE);
        path.setStroke(color, 1, Path.LINE_CAP.ROUND, Path.LINE_JOIN.ROUND);
        canvas.add(path);

        final QuadCurve2D quadCurve2D = new QuadCurve2D.Double();
        quadCurve2D.setCurve(startX, startY, startX + cX, startY + cY, endX, endY);

        final CoursePlot coursePlot = new CoursePlot(path, quadCurve2D);
        courseDefinitions.put(courseDefinition, coursePlot);

        return coursePlot;
    }

    private void printFleetShark(@Nonnull final Fleet fleet, double x, double y, @Nonnull final List<RestrictedFleetArea> restrictedFleetAreas, final boolean courseOutbound) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");
        Preconditions.checkNotNull(restrictedFleetAreas, "restrictedFleetAreas shouldn't be null!");

        final double heightModifier;
        final int spacerBetweenFleetSharks = 10;
        if (!restrictedFleetAreas.isEmpty()) {
            // if there are some fleets at the same spot get the height of one and multiply it by the amount
            heightModifier = spacerBetweenFleetSharks + (restrictedFleetAreas.get(0).getHeight() + spacerBetweenFleetSharks) * restrictedFleetAreas.size();
        } else {
            heightModifier = spacerBetweenFleetSharks;
        }
        final double yModifiedCoord = y + heightModifier;
        final List<AbstractPolyElement.PolyCoordinatePair> points = getPolyCoordinatePairsForFleet(x, yModifiedCoord, courseOutbound);

        final RestrictedFleetArea restrictedFleetArea = new RestrictedFleetArea(points, fleet);
        restrictedFleetAreas.add(restrictedFleetArea);

        final FleetShark fleetShark = new FleetShark(fleet.getId(), idCreateFleetID(fleet), points);
        fleetShark.setFillColor(FLEET_ICON_FILL_COLOR);
        fleetShark.setStroke(FLEET_STROKE_COLOR, 1, Path.LINE_CAP.SQUARE, Path.LINE_JOIN.ARCS);
        fleetShark.setDraggable(true);

        final Point biggestPoint = getBiggestPoint(points);
        final Text fleetText = new Text(idCreateFleetTextID(fleet), fleet.getName());
        fleetText.setFillColor(FLEET_ICON_FILL_COLOR);
        fleetText.move(biggestPoint.getX(), biggestPoint.getY());
        fleetText.setDraggable(true);

        fleetSharkMap.put(fleet, new FleetSvgWrapper(fleetShark, fleetText));
        canvas.add(fleetShark);
        canvas.add(fleetText); // todo wtf wie dragge ich das zusammen?
    }

    /**
     * Returns the upper-right point of the points list.
     *
     * @param points the points list
     * @return the upper-right point
     */
    private Point getBiggestPoint(@Nonnull final List<AbstractPolyElement.PolyCoordinatePair> points) {
        Preconditions.checkNotNull(points, "points shouldn't be null!");
        Preconditions.checkState(!points.isEmpty(), "points shouldn't be empty!");

        final List<Double> xValues = points.stream().map(AbstractPolyElement.PolyCoordinatePair::getPolyX).sorted().collect(Collectors.toList());
        final List<Double> yValues = points.stream().map(AbstractPolyElement.PolyCoordinatePair::getPolyY).sorted().collect(Collectors.toList());
        return new Point(xValues.get(xValues.size() - 1), yValues.get(0));
    }


    /**
     * Generates a random double between the given borders.
     *
     * @param min the lower bound
     * @param max the upper bound
     * @return the random number
     */
    private double getRandomNumber(final double min, final double max) {
        return ((Math.random() * (max - min)) + min);
    }

    /**
     * Creates a poly line plot for a fleet triangle based on the given position.
     * It must be ensured that the given base coordinates are in free space.
     * Please take notice of some math magic to keep it clear.
     *
     * @param xCoordinate    the base x coordinate
     * @param yCoordinate    the base y coordinate
     * @param courseOutbound if the course is outbound or not
     * @return the list of poly points which are in good relation to a planet's circle
     */
    @Nonnull
    private List<AbstractPolyElement.PolyCoordinatePair> getPolyCoordinatePairsForFleet(final double xCoordinate,
                                                                                        final double yCoordinate,
                                                                                        final boolean courseOutbound) {
        final List<AbstractPolyElement.PolyCoordinatePair> points = new ArrayList<>();

        // sets the direction of the peak of the fleet shark to the outer rim
        final int direction = courseOutbound && xCoordinate > 0 ? -1 : 1;

        double xScale = PLANET_RADIUS * 100 / xCoordinate * direction;
        double yScale = PLANET_RADIUS * 100 / yCoordinate * direction;

        // the toe x digit of the shark
        final double peakXCoord = xCoordinate + xScale * 5;
        // the toe y digit of the shark
        final double peakYCoord = yCoordinate + yScale * 5;

        // the heel x digit of the shark
        final double trailEndX = peakXCoord + xScale * 25;
        // the top heel x digit of the shark
        final double trailEndXTop = peakXCoord + xScale * 35;
        // the upper y digit of the fin
        final double trailEndYTop = peakYCoord + yScale * 7;
        // the bottom y digit of the fin
        final double trailEndYBottom = peakYCoord - yScale * 4;

        // The bigger the deeper is the shark fin of the icon.
        final double fin_depth_factor = 8;

        // to place the shark on the middle of the given y position and not at the top
        final double yShift = (trailEndYBottom - trailEndYTop) / 2.8;

        // the up front x digit of the fin's edge
        double xFinSlim = trailEndX - xScale * fin_depth_factor;
        // the y digit of the fin's edge
        double polyY = trailEndYBottom - yShift;

        points.add(new Polyline.PolyCoordinatePair(peakXCoord, peakYCoord));
        points.add(new Polyline.PolyCoordinatePair(trailEndXTop, trailEndYTop));
        points.add(new Polyline.PolyCoordinatePair(xFinSlim, polyY));
        points.add(new Polyline.PolyCoordinatePair(trailEndX, trailEndYBottom));

        return points;
    }

    /**
     * This method takes the element and reset it on their old position if it is dragged by the user.
     * It's an ugly hack, but unless there is a working click listener, this must be enough.
     * <p>
     * In an ideal world there will be a click listener and these hack isn't needed.
     * Main part of the drag listener workaround.
     *
     * @param element the element which must be re-centered
     */
    public void updateSvgElement(@Nonnull final SvgElement element) {
        Preconditions.checkNotNull(element, "element shouldn't be null!");

        canvas.update(element);
    }

    /**
     * Removes the given fleets from the canvas und all related storages.
     *
     * @param fleetsToRemove the fleets to remove
     */
    public void removeFleetSharks(@Nonnull final Set<Fleet> fleetsToRemove) {
        Preconditions.checkNotNull(fleetsToRemove, "fleetsToRemove shouldn't be null!");

        final Set<FleetSvgWrapper> fleetSvgWrappers = fleetsToRemove.stream().filter(fleetSharkMap::containsKey).map(fleetSharkMap::get).collect(Collectors.toSet());
        fleetSvgWrappers.forEach(w -> {
            canvas.remove(w.getFleetShark());
            canvas.remove(w.getFleetText());
        });
        fleetsToRemove.forEach(fleetSharkMap::remove);
        final List<RestrictedFleetArea> restrictedFleetAreas = new ArrayList<>();
        fleetsToRemove.forEach(fleet -> {
            // detect stored fleets in map and delete them later
            if (fleet.isInPlanetaryOrbit()) {
                final FleetOrbit fleetOrbit = fleet.getOrbit();
                if (fleetOrbit == null || fleetOrbit.getPlanet() == null) {
                    throw new NotifySBUserException("Another easter egg because this should be prevented three lines before.");
                }
                final Orbit orbit = fleetOrbit.getPlanet().getOrbit();
                restrictedFleetAreas.addAll(usedFleetSlotsByOrbit.computeIfAbsent(orbit, k -> new ArrayList<>()));
            } else {
                // if not in orbit, fleet must be in motion
                final Move move = fleet.getMove();
                if (move == null || move.getStartOrbit().getPlanet() == null || move.getTargetOrbit().getPlanet() == null) {
                    throw new NotifySBUserException("See above, this should not happen while a move is defined by a origin and a destination.");
                }
                final CourseDefinition courseDefinition = courseDefinitions.entrySet().stream()
                        .filter(e -> e.getValue().getProgressByFleet(fleet) != null)
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse(null);

                if (courseDefinition != null) {
                    final CoursePlot coursePlot = courseDefinitions.get(courseDefinition);
                    coursePlot.removeFleet(fleet);
                }

                restrictedFleetAreas.addAll(usedFleetSlotsByCourse.computeIfAbsent(courseDefinition, k -> new ArrayList<>()));
                // detect course plots without fleets on it and remove them
                final Set<CourseDefinition> withoutCourses = courseDefinitions.entrySet().stream()
                        .filter(e -> e.getValue().isEmpty())
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toSet());

                withoutCourses.stream().map(courseDefinitions::get).forEach(coursePlot -> {
                    final Path course = coursePlot.getCourse();
                    canvas.remove(course);
                });
                courseDefinitions.keySet().removeAll(withoutCourses);
            }
            // remove deleted fleets
            restrictedFleetAreas.stream()
                    .filter(r -> r.getFleetInSpace().equals(fleet))
                    .findFirst()
                    .ifPresent(restrictedFleetAreas::remove);

        });
    }

    /**
     * Returns the planet which occupies an are the coordinated may be inside.
     *
     * @param xCoordinate the xCoord
     * @param yCoordinate the yCoord
     * @return a planet which owns the area the coordinated are in or null
     */
    @Nullable
    public Planet getOccupyingPlanet(final double xCoordinate, final double yCoordinate) {

        final OccupiedArea area = occupiedAreaPlanetMap.keySet().stream()
                .filter(occupiedArea -> occupiedArea.checkIfInside(xCoordinate, yCoordinate))
                .findFirst().orElse(null);

        if (area == null) {
            return null;
        }
        return (Planet) area.getRelatedObject();
    }

    /**
     * Returns the fleet which is inside the given planet's orbit and whom occupied area holds the given coordinates.
     *
     * @param planet      the planet which orbit should be searched
     * @param xCoordinate the xCoordinate to search for
     * @param yCoordinate the yCoordinate to search for
     * @return the found (or not found) fleet to these coordinates in the planet's orbit
     */
    @Nullable
    public Fleet getFleetByArea(@Nonnull final Planet planet, final double xCoordinate, final double yCoordinate) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        final List<RestrictedFleetArea> restrictedFleetAreas = usedFleetSlotsByOrbit.get(planet.getOrbit());
        final AtomicReference<RestrictedFleetArea> areaAtomicReference = new AtomicReference<>();
        restrictedFleetAreas.stream().filter(r -> r.isInside(xCoordinate, yCoordinate)).findFirst().ifPresent(areaAtomicReference::set);
        final RestrictedFleetArea restrictedFleetArea = areaAtomicReference.get();
        if (restrictedFleetArea == null) {
            return null;
        }
        return restrictedFleetArea.getFleetInSpace();
    }

    @Nonnull
    public Set<Fleet> getAllFleetsInOrbit(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        return usedFleetSlotsByOrbit.computeIfAbsent(planet.getOrbit(), k -> new ArrayList<>()).stream().map(RestrictedFleetArea::getFleetInSpace).collect(Collectors.toSet());
    }
}