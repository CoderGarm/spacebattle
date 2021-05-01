package de.yuga.spacebattle.gui.vaadin.orbitals.starmap;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.svg.Svg;
import com.vaadin.flow.component.svg.elements.*;
import de.yuga.spacebattle.backend.distance.DistanceCalculator;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.turn.Move;
import de.yuga.spacebattle.gui.vaadin.orbitals.details.Canvas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.geo.Point;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
     * CSS id prefix extension for every course plot for already flown tracks.
     */
    public static final String BEHIND_ID = "behind";

    /**
     * CSS id prefix extension for every course plot for the track ahead.
     */
    public static final String BEFORE_ID = "before";

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
    private static final String PLANETARY_AXIS_COLOR = "yellow";

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
    public static final String FLEET_STROKE_COLOR = "lightred";

    /**
     * If a transparent background is needed, use this.
     */
    public static final String TRANSPARENT_FILL_COLOR = "transparent";

    /**
     * The course plot color for already flown tracks.
     */
    public static final String COURSE_PLOT_COLOR_BEHIND = "lightgray";

    /**
     * The course plot color for the track ahead.
     */
    public static final String COURSE_PLOT_COLOR_BEFORE = "lightgreen";

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
     * Holds the used slots in an orbit which are occupied by a fleet.
     */
    final Map<Orbit, List<RestrictedFleetArea>> usedFleetSlotsByOrbit = new HashMap<>();

    /**
     * Holds fleets to their representing svg shark polygons.
     */
    private final Map<Fleet, Polygon> fleetSharkMap = new HashMap<>();

    /**
     * Holds svg shark polygons to their fleet.
     */
    private final Map<Polygon, Fleet> fleetSharkFleetMap = new HashMap<>();

    /**
     * Holds all course plots for a given fleet.
     */
    private final Map<Fleet, List<Line>> fleetCourseMap = new HashMap<>();

    /**
     * Creates the view box and coordinate system for the given canvas by the given orbits.
     *
     * @param starSystems all star systems to display in the universe star map
     * @param canvas      the canvas which holds the universe
     */
    public ViewBoxDefinition(@Nonnull final Set<StarSystem> starSystems, @Nonnull final Svg canvas) {
        this.canvas = canvas;
        this.eViewBoxType = UNIVERSE;
        viewBoxFactor = getViewBoxFactor();
        final Set<Orbit> orbitCollection = starSystems.stream().map(StarSystem::getOrbit).collect(Collectors.toSet());
        adjustCanvas(orbitCollection);

        starSystems.forEach(this::createStarSystemCircle);

    }

    /**
     * Creates the view box and coordinate system for the given canvas by the given star system.
     *
     * @param starSystem all planets to display in the star system map
     * @param canvas     the canvas which holds the star system
     */
    public ViewBoxDefinition(@Nonnull final StarSystem starSystem, @Nonnull final Svg canvas) {
        this.canvas = canvas;
        this.eViewBoxType = STAR_SYSTEM;
        viewBoxFactor = getViewBoxFactor();
        final Set<Orbit> orbitCollection = starSystem.getPlanets().stream().map(Planet::getOrbit).collect(Collectors.toSet());
        adjustCanvas(orbitCollection);

        final Set<Planet> planets = starSystem.getPlanets();
        planets.forEach(this::createPlanetCircle);
        final Map<Planet, Set<Fleet>> fleetInSystem = starSystem.getFleets().stream()
                .filter(fleet -> fleet.getOrbit() != null && fleet.getMove() == null)
                .map(e -> new AbstractMap.SimpleEntry<>(e.getOrbit().getPlanet(), e))
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toSet())));

        planets.forEach(planet -> {
            final Set<Fleet> fleetsInOrbit = fleetInSystem.get(planet);
            if (fleetsInOrbit != null) {
                fleetsInOrbit.forEach(this::createFleetPolygonInOrbit);
            }
        });

        final Set<Fleet> movingFleets = starSystem.getFleets().stream()
                .filter(fleet -> fleet.getMove() != null)
                .collect(Collectors.toSet());

        movingFleets.forEach(this::createMovingFleet);
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
    private double getOrbitalDistance(@Nonnull final Orbit orbit1, @Nonnull final Orbit orbit2) {
        Preconditions.checkNotNull(orbit1, "orbit1 shouldn't be null!");
        Preconditions.checkNotNull(orbit2, "orbit2 shouldn't be null!");

        final int x1 = orbit1.getXCoordinate();
        final int y1 = orbit1.getYCoordinate();

        final int x2 = orbit2.getXCoordinate();
        final int y2 = orbit2.getYCoordinate();

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

        orbitCollection.forEach(this::update);
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

            final Circle circle = new Circle("ellipsoid-" + orbit.getOrbitID(), radius);
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
            final String id = "orbit-" + orbit.getOrbitID();
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

        final Svg canvas = new Canvas();
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
        final String circleID = orbit.getOrbitID();
        final Circle circle = new Circle(circleID, ViewBoxDefinition.SYSTEM_RADIUS);
        circle.center(orbit.getXCoordinate(), orbit.getYCoordinate());
        circle.setFillColor("red");
        circle.setDraggable(true);
        canvas.add(circle);
    }

    /**
     * Creates the svg circle which represents the given planet.
     *
     * @param planet the planet which must be represented
     */
    private void createPlanetCircle(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, PLANET_SELECTOR_ID_PREFIX + " shouldn't be null!");

        final String circleID = createPlanetID(planet);
        final Orbit orbit = planet.getOrbit();
        final Circle circle = new Circle(circleID, ViewBoxDefinition.PLANET_RADIUS);
        circle.center(orbit.getXCoordinate(), orbit.getYCoordinate());
        circle.setFillColor("green");
        circle.setDraggable(true);
        canvas.add(circle);

        occupiedAreaPlanetMap.put(new OccupiedArea(planet, circle, orbit, RADIUS_PLANETARY_SYSTEM), planet);
    }

    /**
     * Creates an id for planet circles on the canvas.
     *
     * @param planet the planet which the id is for
     * @return the id
     */
    @Nonnull
    public static String createPlanetID(@Nonnull final Planet planet) {
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
    public static String createFleetID(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, FLEET_SELECTOR_ID_PREFIX + " shouldn't be null!");

        return FLEET_SELECTOR_ID_PREFIX + "-" + fleet.getId();
    }

    /**
     * Creates a triangle which represents a fleet.
     * Planet the fleet next to the planet in it's orbit in a random position.
     */
    public void createFleetPolygonInOrbit(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, FLEET_SELECTOR_ID_PREFIX + " shouldn't be null!");
        Preconditions.checkArgument(fleet.getOrbit() != null, FLEET_SELECTOR_ID_PREFIX + "'s orbit shouldn't be null!");

        final double freeRadius = PLANET_RADIUS * 3;
        final double maxRadius = PLANET_RADIUS * 6;

        final Orbit orbit = fleet.getOrbit().getPlanet().getOrbit();
        final int xCoordinate = orbit.getXCoordinate();
        final int yCoordinate = orbit.getYCoordinate();

        final List<RestrictedFleetArea> restrictedFleetAreas = usedFleetSlotsByOrbit.computeIfAbsent(orbit, k -> new ArrayList<>());

        List<AbstractPolyElement.PolyCoordinatePair> points = new ArrayList<>();
        boolean breaker = true;
        int counter = 1;
        while (breaker) {
            double xRandomCoord = xCoordinate + getRandomNumber(freeRadius, maxRadius) * (Math.random() < 0.5 ? -1 : 1);
            double yRandomCoord = yCoordinate + getRandomNumber(freeRadius, maxRadius) * (Math.random() < 0.5 ? -1 : 1);

            points = getPolyCoordinatePairsForFleet(xRandomCoord, yRandomCoord);
            final List<AbstractPolyElement.PolyCoordinatePair> finalPoints = points;
            if (restrictedFleetAreas.stream().filter(restrictedFleetArea -> restrictedFleetArea.isNotFree(finalPoints)).findFirst().orElse(null) == null) {
                breaker = false;
            }
            // try to find a free position maximal 100 times then place it anyways
            counter++;
            if (counter > 100) break;
        }

        final RestrictedFleetArea restrictedFleetArea = new RestrictedFleetArea(points, fleet);
        restrictedFleetAreas.add(restrictedFleetArea);

        final Polygon fleetShark = createFleetShark(fleet, points);

        canvas.add(fleetShark);
        fleetSharkMap.put(fleet, fleetShark);
        fleetSharkFleetMap.put(fleetShark, fleet);
    }

    /**
     * Creates a fleet shark aligned with the vector from the fleets start to it's target
     * based on the amount of way which as already travelled.
     *
     * @param fleet the fleet in movement
     */
    public void createMovingFleet(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, FLEET_SELECTOR_ID_PREFIX + " shouldn't be null!");
        Preconditions.checkArgument(fleet.getMove() != null, FLEET_SELECTOR_ID_PREFIX + "'s move shouldn't be null!");

        final Move move = fleet.getMove();
        final int moveDoneAtZero = move.getMoveDoneAtZero();

        final FleetOrbit startOrbit = move.getStartOrbit();
        final Planet startPlanet = startOrbit.getPlanet();
        final Orbit startPlanetOrbit = startPlanet.getOrbit();

        final FleetOrbit targetOrbit = move.getTargetOrbit();
        final Planet targetPlanet = targetOrbit.getPlanet();
        final Orbit targetPlanetOrbit = targetPlanet.getOrbit();

        final int travelTime = DistanceCalculator.calculateTimeToTravel(fleet, targetPlanet);
        double ticksToTravelAsFraction = ((double) moveDoneAtZero / (double) travelTime);

        if (ticksToTravelAsFraction == 1) {
            // case: 1-tick-run - just to place the fleet not on the orbit coord cross
            ticksToTravelAsFraction = 0.6;
        }

        final Point point = calculatePositionOnTrack(ticksToTravelAsFraction, startPlanetOrbit, targetPlanetOrbit);
        final double x = point.getX();
        final double y = point.getY();

        final Line line1 = printCoursePlot(COURSE_ID + BEHIND_ID + fleet.getId(),
                new Point(startPlanetOrbit.getXCoordinate(), startPlanetOrbit.getYCoordinate()),
                new Point(x, y),
                COURSE_PLOT_COLOR_BEHIND);

        final Line line2 = printCoursePlot(COURSE_ID + BEFORE_ID + fleet.getId(),
                new Point(x, y),
                new Point(targetPlanetOrbit.getXCoordinate(), targetPlanetOrbit.getYCoordinate()),
                COURSE_PLOT_COLOR_BEFORE);

        final List<AbstractPolyElement.PolyCoordinatePair> points = getPolyCoordinatePairsForFleet(x, y);
        final Polygon fleetShark = createFleetShark(fleet, points);

        // todo new RestrictedFleetArea for fleets in free space later

        canvas.add(fleetShark);
        fleetSharkMap.put(fleet, fleetShark);
        fleetSharkFleetMap.put(fleetShark, fleet);

        final List<Line> lines = fleetCourseMap.computeIfAbsent(fleet, k -> new ArrayList<>());
        lines.add(line1);
        lines.add(line2);
    }

    /**
     * Prints the course plot by thd given values.
     *
     * @param id    the css id for this line
     * @param start the starting point
     * @param end   the target point
     * @param color the color
     * @return the plotted line
     */
    private Line printCoursePlot(@Nonnull final String id,
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

        // reduces the length of the course plots about
        final double lengthModifier = 0.1;

        final double startXt = startX + (lengthModifier * (endX - startX));
        final double startYt = startY + (lengthModifier * (endY - startY));

        final double endXt = endX + (lengthModifier * (startX - endX));
        final double endYt = endY + (lengthModifier * (startY - endY));

        final Line line = new Line(id, new AbstractPolyElement.PolyCoordinatePair(startXt, startYt), new AbstractPolyElement.PolyCoordinatePair(endXt, endYt));

        line.setStroke(color, 1, Path.LINE_CAP.ROUND, Path.LINE_JOIN.ROUND);
        canvas.add(line);
        return line;
    }

    /**
     * Calculates a point on a vector from start to target with an already travelled track, represented by ticks.
     *
     * @param percentageFactorOfTrackTravelled the percentage as factor which are already travelled
     * @param startPlanetOrbit                 the starting orbit
     * @param targetPlanetOrbit                the target orbit
     * @return the point on the track
     */
    private Point calculatePositionOnTrack(final double percentageFactorOfTrackTravelled,
                                           @Nonnull final Orbit startPlanetOrbit,
                                           @Nonnull final Orbit targetPlanetOrbit) {
        Preconditions.checkNotNull(startPlanetOrbit, "startPlanetOrbit shouldn't be null!");
        Preconditions.checkNotNull(targetPlanetOrbit, "targetPlanetOrbit shouldn't be null!");

        final int startX = startPlanetOrbit.getXCoordinate();
        final int startY = startPlanetOrbit.getYCoordinate();

        final int targetX = targetPlanetOrbit.getXCoordinate();
        final int targetY = targetPlanetOrbit.getYCoordinate();

        // calculating resulting position by directional vector
        final double resultX = startX + (percentageFactorOfTrackTravelled * (targetX - startX));
        final double resultY = startY + (percentageFactorOfTrackTravelled * (targetY - startY));

        return new Point(resultX, resultY);
    }

    @Nonnull
    private Polygon createFleetShark(@Nonnull Fleet fleet, List<AbstractPolyElement.PolyCoordinatePair> points) {
        Preconditions.checkNotNull(points, "points shouldn't be null!");

        final Polygon fleetShark = new Polygon(createFleetID(fleet), points);
        fleetShark.setFillColor(FLEET_ICON_FILL_COLOR);
        fleetShark.setStroke(FLEET_STROKE_COLOR, 2, Path.LINE_CAP.SQUARE, Path.LINE_JOIN.ARCS); // todo where is stroke?
        fleetShark.setDraggable(true);
        return fleetShark;
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
     * @param xCoordinate the base x coordinate
     * @param yCoordinate the base y coordinate
     * @return the list of poly points which are in good relation to a planet's circle
     */
    @Nonnull
    private List<AbstractPolyElement.PolyCoordinatePair> getPolyCoordinatePairsForFleet(final double xCoordinate,
                                                                                        final double yCoordinate) {
        final List<AbstractPolyElement.PolyCoordinatePair> points = new ArrayList<>();

        double xScale = PLANET_RADIUS * 100 / xCoordinate;
        double yScale = PLANET_RADIUS * 100 / yCoordinate;

        final double peakXCoord = xCoordinate + xScale * 5;
        final double peakYCoord = yCoordinate + yScale * 5;

        final double trailEndX = peakXCoord + xScale * 25;
        final double trailEndYTop = peakYCoord + yScale * 7;
        final double trailEndYBottom = peakYCoord - yScale * 4;

        // The bigger the deeper is the shark fin of the icon.
        final double fin_depth_factor = 8;

        // to place the shark on the middle of the given x position and not at the peak
        final double xShift = (trailEndX - peakXCoord) / 2;
        // to place the shark on the middle of the given y position and not at the top
        final double yShift = (trailEndYBottom - trailEndYTop) / 2.8;

        // the toe x digit of the shark
        final double xPeak = peakXCoord - xShift;
        // the heel x digit of the shark
        final double xTrail = trailEndX - xShift;
        // the toe y digit of the shark
        double peakY = peakYCoord + yShift;
        // the upper y digit of the fin
        double yTrailTop = trailEndYTop + yShift;
        // the bottom y digit of the fin
        double yTrailBottom = trailEndYBottom + yShift;
        // the up front x digit of the fin's edge
        double xFinSlim = xTrail - xScale * fin_depth_factor;

        points.add(new Polyline.PolyCoordinatePair(xPeak, peakY));
        points.add(new Polyline.PolyCoordinatePair(xTrail, yTrailTop));
        points.add(new Polyline.PolyCoordinatePair(xFinSlim, yTrailTop - (trailEndYTop - trailEndYBottom)));
        points.add(new Polyline.PolyCoordinatePair(xTrail, yTrailBottom));
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
    public void resetPositionOfSvgElement(@Nonnull final SvgElement element) {
        Preconditions.checkNotNull(element, "element shouldn't be null!");

        canvas.update(element);
    }

    /**
     * Removes the given fleets from the canvas und all related strorages.
     *
     * @param fleetsToRemove the fleets to remove
     */
    public void removeFleetSharksFromOrbits(@Nonnull final Set<Fleet> fleetsToRemove) {
        Preconditions.checkNotNull(fleetsToRemove, "fleetsToRemove shouldn't be null!");

        final Set<Polygon> fleetSharksToRemove = fleetsToRemove.stream().filter(fleetSharkMap::containsKey).map(fleetSharkMap::get).collect(Collectors.toSet());
        fleetSharksToRemove.forEach(canvas::remove);
        fleetSharksToRemove.forEach(fleetSharkFleetMap::remove);
        fleetsToRemove.forEach(fleetSharkMap::remove);
        fleetsToRemove.forEach(fleet -> {
            final FleetOrbit fleetOrbit = fleet.getOrbit();
            if (fleetOrbit != null) {
                final Orbit orbit = fleetOrbit.getPlanet().getOrbit();
                final List<RestrictedFleetArea> restrictedFleetAreas = usedFleetSlotsByOrbit.get(orbit);
                restrictedFleetAreas.stream()
                        .filter(r -> r.getFleetInSpace().equals(fleet))
                        .findFirst()
                        .ifPresent(restrictedFleetAreas::remove);
            }
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