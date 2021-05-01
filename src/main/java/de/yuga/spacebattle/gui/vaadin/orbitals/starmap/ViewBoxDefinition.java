package de.yuga.spacebattle.gui.vaadin.orbitals.starmap;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.svg.Svg;
import com.vaadin.flow.component.svg.elements.*;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.gui.vaadin.orbitals.details.Canvas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.*;
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
     * The color of the info box' outline.
     */
    public static final String INFO_BOX_STROKE_COLOR = "grey";

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
     * The {@link #viewBoxFactor} is the factor which is calculated if the dimension of the displayed range will be calculated.
     */
    private final double viewBoxFactor;

    /**
     * The radius is the radius which is the outline of the system. It is the base value on which all calculations for the displayed dimension is done.
     */
    private double biggestRadiusOfAllPlanetaryOrbits = Double.MIN_NORMAL;

    /**
     * Creates the view box and coordinate system for the given canvas by the given orbits.
     *
     * @param starSystems all star systems to display in the universe star map
     * @param canvas      the canvas which holds the universe
     */
    public ViewBoxDefinition(@Nonnull final Set<StarSystem> starSystems, @Nonnull final Svg canvas) {
        this(UNIVERSE, starSystems.stream().map(StarSystem::getOrbit).collect(Collectors.toSet()), canvas);
        starSystems.forEach(this::createStarSystemCircle);
    }

    /**
     * Creates the view box and coordinate system for the given canvas by the given star system.
     *
     * @param starSystem all planets to display in the star system map
     * @param canvas     the canvas which holds the star system
     */
    public ViewBoxDefinition(@Nonnull final StarSystem starSystem, @Nonnull final Svg canvas) {
        this(STAR_SYSTEM, starSystem.getPlanets().stream().map(Planet::getOrbit).collect(Collectors.toSet()), canvas);
        final Set<Planet> planets = starSystem.getPlanets();
        planets.forEach(this::createPlanetCircle);
        final Map<Planet, Set<Fleet>> fleetInSystem = starSystem.getFleets().stream()
                .filter(fleet -> fleet.getOrbit() != null)
                .map(e -> new AbstractMap.SimpleEntry<>(e.getOrbit().getPlanet(), e))
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toSet())));
        planets.forEach(planet -> {

            final Set<Fleet> fleetsInOrbit = fleetInSystem.get(planet);
            if (fleetsInOrbit != null) {
                fleetsInOrbit.forEach(this::createFleetPolygon);
            }
        });
        setInfoTerminal(starSystem);
    }


    /**
     * Creates the view box and coordinate system for the given canvas by the given orbits.
     *
     * @param eViewBoxType    for which kind of system the canvas is for
     * @param planetaryOrbits the included orbits
     * @param canvas          the canvas to update
     */
    public ViewBoxDefinition(@Nonnull final EViewBoxType eViewBoxType, @Nonnull final Collection<Orbit> planetaryOrbits, @Nonnull final Svg canvas) {
        Preconditions.checkNotNull(eViewBoxType, "eViewBoxType shouldn't be null!");
        Preconditions.checkNotNull(planetaryOrbits, "orbits shouldn't be null!");
        Preconditions.checkArgument(!planetaryOrbits.isEmpty(), "orbits must not be empty");
        Preconditions.checkNotNull(canvas, "canvas shouldn't be null!");

        this.canvas = canvas;
        this.eViewBoxType = eViewBoxType;
        viewBoxFactor = getViewBoxFactor();
        planetaryOrbits.forEach(this::update);
        this.planetaryOrbits.addAll(planetaryOrbits);
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

        final double radius = getRadius(xCoordinate, yCoordinate);
        if (radius > biggestRadiusOfAllPlanetaryOrbits) {
            biggestRadiusOfAllPlanetaryOrbits = radius;
        }
    }

    private double getRadius(int xCoordinate, int yCoordinate) {
        return Math.sqrt((xCoordinate * xCoordinate) + (yCoordinate * yCoordinate));
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
    private void adjustCanvas() {
        switch (eViewBoxType) {
            case STAR_SYSTEM:
                createVisibleOrbits();
                createCenter();
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

        planetaryOrbits.forEach(orbit -> {
            final int xCoord = Math.abs(orbit.getXCoordinate());
            final int yCoord = Math.abs(orbit.getYCoordinate());

            final double radius = getRadius(xCoord, yCoord);

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
            final double radius = PLANET_RADIUS * 10;

            final String id = "orbit-" + orbit.getOrbitID();
            createCoordinateCross(PLANETARY_SYSTEM_CROSS, id, orbit, radius, PLANETARY_AXIS_COLOR);
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
        createLine("xLine-" + idSuffix, xAbsValueFrom, xAbsValueTo, yCoordinate, yCoordinate, color);
        createLine("yLine-" + idSuffix, xCoordinate, xCoordinate, yAbsValueFrom, yAbsValueTo, color);
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

            final String idX = "axisScaleX" + idSuffix;
            createLine(idX + countOfMiniLines, xAbsValueToScale, xAbsValueToScale, yAbsValueFrom, yAbsValueTo, color);
            createLine(idX + (-1 * countOfMiniLines), xAbsValueFromScale, xAbsValueFromScale, yAbsValueFrom, yAbsValueTo, color);
            final String idY = "axisScaleY" + idSuffix;
            createLine(idY + countOfMiniLines, xAbsValueFrom, xAbsValueTo, yAbsValueToScale, yAbsValueToScale, color);
            createLine(idY + (-1 * countOfMiniLines), xAbsValueFrom, xAbsValueTo, yAbsValueFromScale, yAbsValueFromScale, color);
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

        if (scaleDividerNumber % bigDivider == 0) {
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

        return FLEET_SELECTOR_ID_PREFIX + "-" + fleet.hashCode();
    }

    /**
     * Holds the used slots in an orbit which are occupied by a fleet.
     */
    final Map<Orbit, List<RestrictedFleetArea>> usedFleetSlots = new HashMap<>();

    /**
     * Creates a triangle which represents a fleet.
     * Planet the fleet next to the planet in it's orbit in a random position.
     */
    private void createFleetPolygon(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, FLEET_SELECTOR_ID_PREFIX + " shouldn't be null!");
        Preconditions.checkArgument(fleet.getOrbit() != null, FLEET_SELECTOR_ID_PREFIX + "'s orbit shouldn't be null!");

        final double freeRadius = PLANET_RADIUS * 3;
        final double maxRadius = PLANET_RADIUS * 6;

        final Orbit orbit = fleet.getOrbit().getPlanet().getOrbit();
        final int xCoordinate = orbit.getXCoordinate();
        final int yCoordinate = orbit.getYCoordinate();

        final List<RestrictedFleetArea> restrictedFleetAreas = usedFleetSlots.computeIfAbsent(orbit, k -> new ArrayList<>());

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

        final RestrictedFleetArea restrictedFleetArea = new RestrictedFleetArea(points);
        restrictedFleetAreas.add(restrictedFleetArea);

        Polygon polygon = new Polygon(FLEET_SELECTOR_ID_PREFIX + "-icon" + createFleetID(fleet), points);
        polygon.setFillColor(FLEET_ICON_FILL_COLOR);
        polygon.setStroke(FLEET_STROKE_COLOR, 2, Path.LINE_CAP.SQUARE, Path.LINE_JOIN.ARCS);
        polygon.setDraggable(true);
        canvas.add(polygon);
    }

    /**
     * Generates a random double between the given borders.
     *
     * @param min the lower bound
     * @param max the upper bound
     * @return the random number
     */
    public double getRandomNumber(double min, double max) {
        return ((Math.random() * (max - min)) + min);
    }

    /**
     * Creates a poly line plot for a fleet triangle based on the given position.
     * It must be ensured that the given base coordinates are in free space. Please take notice of some math magic to keep it clear.
     *
     * @param xCoordinate the base x coordinate
     * @param yCoordinate the base y coordinate
     * @return the list of poly points
     */
    @Nonnull
    private List<AbstractPolyElement.PolyCoordinatePair> getPolyCoordinatePairsForFleet(final double xCoordinate, final double yCoordinate) {
        final List<AbstractPolyElement.PolyCoordinatePair> points = new ArrayList<>();

        double xScale = PLANET_RADIUS * 100 / xCoordinate;
        double yScale = PLANET_RADIUS * 100 / yCoordinate;

        final double peakXCoord = xCoordinate + xScale * 5;
        final double peakYCoord = yCoordinate + yScale * 5;

        final double trailEndX = peakXCoord + xScale * 25;
        final double trailEndYTop = peakYCoord + yScale * 7;
        final double trailEndYBottom = peakYCoord - yScale * 4;

        /**
         * The bigger the deeper is the shark fin of the icon
         */
        final double fin_depth_factor = 8;

        points.add(new Polyline.PolyCoordinatePair(peakXCoord, peakYCoord));
        points.add(new Polyline.PolyCoordinatePair(trailEndX, trailEndYTop));
        points.add(new Polyline.PolyCoordinatePair(trailEndX - xScale * fin_depth_factor, trailEndYTop - (trailEndYTop - trailEndYBottom)));
        points.add(new Polyline.PolyCoordinatePair(trailEndX, trailEndYBottom));
        return points;
    }

    /**
     * todo other window system? how to stick that on corner?
     *
     * @param starSystem
     */
    private void setInfoTerminal(@Nonnull final StarSystem starSystem) {
        Preconditions.checkNotNull(starSystem, "starSystem shouldn't be null!");

        final List<AbstractPolyElement.PolyCoordinatePair> points = new ArrayList<>();
        points.add(new Polyline.PolyCoordinatePair(0, 0));
        points.add(new Polyline.PolyCoordinatePair(500, 0));
        points.add(new Polyline.PolyCoordinatePair(500, 500));
        points.add(new Polyline.PolyCoordinatePair(150, 500));
        points.add(new Polyline.PolyCoordinatePair(0, 350));

        Polygon polygon = new Polygon("info-box", points);
        polygon.setFillColor(TRANSPARENT_FILL_COLOR);
        polygon.setStroke(INFO_BOX_STROKE_COLOR, 10, Path.LINE_CAP.SQUARE, Path.LINE_JOIN.ARCS);
        polygon.move(getViewBoxMinX(), getViewBoxMinY() * 2);

        canvas.add(polygon);

        // name, orbit, planetcount
        final Text text = new Text("text", "Sample text.");
        text.setFontFamily("'Roboto', 'Noto', sans-serif");
        text.setFillColor("transparent");
        //text.move(x += space, y);
        canvas.add(text);


    }

    /**
     * This method takes the element and reset it on their old position if it is dragged by the user.
     * It's an ugly hack, but unless there is a working click listener, this must be enough.
     * <p>
     * In an ideal world there will be a click listener and these hack isn't needed.
     *
     * @param element the element which must be re-centered
     * @param orbit   the position where the element must be
     */
    public void dragListenerWorkaround(@Nonnull final SvgElement element, @Nonnull final Orbit orbit) {
        Preconditions.checkNotNull(element, "element shouldn't be null!");
        Preconditions.checkNotNull(orbit, "orbit shouldn't be null!");
        Preconditions.checkArgument(element.getClass().isAssignableFrom(Circle.class), "element must be a Svg Circle!");

        final int xCoordinate = orbit.getXCoordinate();
        final int yCoordinate = orbit.getYCoordinate();
        canvas.remove(element);
        ((Circle) element).center(xCoordinate, yCoordinate);
        canvas.add(element);
    }
}
