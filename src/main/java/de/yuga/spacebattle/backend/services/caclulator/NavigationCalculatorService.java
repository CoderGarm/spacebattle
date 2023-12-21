package de.yuga.spacebattle.backend.services.caclulator;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator;
import de.yuga.spacebattle.backend.calculator.distance.dijkstra.Dijkstra;
import de.yuga.spacebattle.backend.calculator.distance.dijkstra.Graph;
import de.yuga.spacebattle.backend.calculator.distance.dijkstra.Node;
import de.yuga.spacebattle.backend.combat.enums.EMovementType;
import de.yuga.spacebattle.backend.dto.physics.Acceleration;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.dto.turn.FlightPlanDto;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.turn.navigation.FlightPlan;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.backend.enums.ETechnologyType;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import de.yuga.spacebattle.backend.enums.space.EWormhole;
import de.yuga.spacebattle.backend.services.orbitals.StarSystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NavigationCalculatorService {

    @Nonnull
    private static final Logger LOGGER = LoggerFactory.getLogger(NavigationCalculatorService.class);

    @Nonnull
    private final StarSystemService starSystemService;

    @Nonnull
    private final Set<StarSystem> wormholeSystems = new HashSet<>();

    @Autowired
    public NavigationCalculatorService(@Nonnull final StarSystemService starSystemService) {
        this.starSystemService = Preconditions.checkNotNull(starSystemService, "starSystemService must not be empty");
    }

    @PostConstruct
    public void loadSystems() {
        LOGGER.info("Loading Dijkstra");
        this.wormholeSystems.addAll(starSystemService.findByNames(EWormhole.getWormholeNames()));
        LOGGER.info("Finished loading Dijkstra");
    }

    public int getTimeToTravel(@Nonnull final StarSystem origin,
                               @Nonnull final StarSystem destination) {
        Preconditions.checkNotNull(origin, "origin must not be empty");
        Preconditions.checkNotNull(destination, "destination must not be empty");

        return getTimeToTravel(new FleetOrbit(Orbit.getCenterOrbit(), origin), new FleetOrbit(Orbit.getCenterOrbit(), destination));
    }

    public int getTimeToTravel(@Nonnull final Planet origin,
                               @Nonnull final Planet destination) {
        Preconditions.checkNotNull(origin, "origin must not be empty");
        Preconditions.checkNotNull(destination, "destination must not be empty");

        return getTimeToTravel(new FleetOrbit(origin), new FleetOrbit(destination));
    }

    public int getTimeToTravel(@Nonnull final FleetOrbit origin,
                               @Nonnull final FleetOrbit destination) {
        Preconditions.checkNotNull(origin, "origin must not be empty");
        Preconditions.checkNotNull(destination, "destination must not be empty");

        final StarSystem start = origin.getSystem();
        final StarSystem target = destination.getSystem();
        if (start != null && target != null) {
            final List<StarSystem> shortestWaypoints = getShortestWaypoints(start, target);
            return calculateWaypoints(ETechnologyType.MILITARY, DistanceCalculator.PUBLIC_TRANSPORT_ACCELERATION, origin, shortestWaypoints).getTicksLeft();
        }
        return DistanceCalculator.calculateTimeToTravel(ETechnologyType.MILITARY, DistanceCalculator.PUBLIC_TRANSPORT_ACCELERATION, origin, destination);
    }

    @Nonnull
    public List<StarSystem> getShortestWaypoints(@Nonnull final StarSystem origin, @Nonnull final StarSystem destination) {
        Preconditions.checkNotNull(origin, "origin must not be empty");
        Preconditions.checkNotNull(destination, "destination must not be empty");

        final List<Node> shortestPath = getShortestPath(origin, destination);

        final List<String> toFind = shortestPath.stream().map(Node::getName)
                .filter(name -> !name.equals(origin.getName()))
                .collect(Collectors.toList());
        final Set<StarSystem> navPoints = starSystemService.findByNames(new HashSet<>(toFind));

        final List<StarSystem> resultingPath = new ArrayList<>();
        resultingPath.add(origin);
        for (final String systemName : toFind) {
            final Set<StarSystem> starSystems = navPoints.stream().filter(s -> s.getName().equals(systemName)).collect(Collectors.toSet());
            if (starSystems.size() == 1) {
                resultingPath.addAll(starSystems);
            } else {
                final StarSystem closerToDestination = starSystems.stream().reduce((o1, o2) -> getTimeToTravel(destination, o1) <= getTimeToTravel(destination, o2) ? o1 : o2)
                        .orElseThrow(NullPointerException::new);
                resultingPath.add(closerToDestination);
            }
        }
        resultingPath.add(destination);
        return resultingPath;
    }

    @Nonnull
    private List<Node> getShortestPath(@Nonnull final StarSystem origin, @Nonnull final StarSystem destination) {
        Preconditions.checkNotNull(origin, "origin must not be empty");
        Preconditions.checkNotNull(destination, "destination must not be empty");

        final Map<StarSystem, Node> systemNodes = createNodes(origin, destination);
        final Graph graph = new Graph();
        graph.setNodes(new HashSet<>(systemNodes.values()));
        final Node nodeA = systemNodes.get(origin);
        final Node nodeB = systemNodes.get(destination);

        final Graph shortestPathFromSource = Dijkstra.calculateShortestPathFromSource(graph, nodeA);

        return shortestPathFromSource.getNodes().stream()
                .filter(n -> n == nodeB)
                .findFirst()
                .orElseThrow(NullPointerException::new)
                .getShortestPath();
    }

    @Nonnull
    private Map<StarSystem, Node> createNodes(@Nonnull final StarSystem origin, @Nonnull final StarSystem destination) {
        Preconditions.checkNotNull(origin, "origin must not be empty");
        Preconditions.checkNotNull(destination, "destination must not be empty");

        final Set<StarSystem> systemsOnTrack = new HashSet<>(wormholeSystems);
        systemsOnTrack.add(destination);

        final Map<StarSystem, Node> systemNodes = new HashMap<>(systemsOnTrack.stream()
                .collect(Collectors.toMap(Function.identity(), s -> new Node(s.getName()))));

        systemsOnTrack.forEach(a ->
                systemsOnTrack.stream()
                        .filter(b -> !a.equals(b))
                        .forEach(b -> {
                            final Node nodeA = systemNodes.get(a);
                            final Node nodeB = systemNodes.get(b);
                            final int coordinateInMetric = a.getOrbit().getDistance(b.getOrbit()).getCoordinateInMetric(EDistanceMetric.LY).intValue();
                            final int distance = EWormhole.areSystemsConnected(a, b) ? EWormhole.getConnectionGrade(a, b) : coordinateInMetric;
                            nodeA.addDestination(nodeB, distance);
                        })
        );
        return systemNodes;
    }

    @Nonnull
    public FlightPlanDto calculateWaypoints(@Nonnull final Fleet fleet,
                                            @Nonnull final List<StarSystem> waypoints) {
        Preconditions.checkNotNull(fleet, "fleet must not be empty");
        Preconditions.checkState(fleet.getOrbit() != null, "The fleet must have an orbit currently");
        Preconditions.checkNotNull(waypoints, "waypoints must not be empty");

        final ETechnologyType restrictingTechnologyType = fleet.getRestrictingTechnologyType();
        final Acceleration acceleration = fleet.getAccelerationFor(EModuleType.FTLPROPULSION);
        // way from inner-system to hyper limit will be ignored - it's a too small potion of the time tick-wise
        final FleetOrbit fleetOrbit = fleet.getOrbit();

        return calculateWaypoints(restrictingTechnologyType, acceleration, fleetOrbit, waypoints);
    }

    @Nonnull
    public FlightPlanDto calculateWaypoints(@Nonnull final ETechnologyType restrictingTechnologyType,
                                            @Nonnull final Acceleration acceleration,
                                            @Nonnull final FleetOrbit fleetOrbit,
                                            @Nonnull final List<StarSystem> waypoints) {
        Preconditions.checkNotNull(restrictingTechnologyType, "restrictingTechnologyType must not be empty");
        Preconditions.checkNotNull(acceleration, "acceleration must not be empty");
        Preconditions.checkNotNull(fleetOrbit, "fleetOrbit must not be empty");
        Preconditions.checkNotNull(waypoints, "waypoints must not be empty");

        final List<FlightPlan> flightPlan = new ArrayList<>();
        double travelTime = 0;
        travelTime += DistanceCalculator.getSubLightDurationToHyperLimit(restrictingTechnologyType, acceleration, fleetOrbit);
        for (int i = 1; i < waypoints.size(); i++) {
            final boolean isLastElement = i == waypoints.size() - 1;
            final StarSystem before = waypoints.get(i - 1);
            final StarSystem waypoint = waypoints.get(i);
            final Orbit beforeOrbit = before.getOrbit();
            final Orbit waypointOrbit = waypoint.getOrbit();

            if (i == 1) {
                // set initial flight plan element
                final Distance distance = beforeOrbit.getDistance(waypointOrbit).multiply(0.5);
                final Orbit orbit = fleetOrbit.getGalacticResultingOrbit().clone().move(EMovementType.REDUCE_DISTANCE, distance, waypointOrbit);
                flightPlan.add(new FlightPlan(new FleetOrbit(orbit, null), 0));
            }

            final boolean systemsConnected = EWormhole.areSystemsConnected(before, waypoint);
            final double duration = systemsConnected ? 0.1 : DistanceCalculator.getDuration(EModuleType.FTLPROPULSION, restrictingTechnologyType, acceleration, beforeOrbit, waypointOrbit);

            final BigDecimal durationWithLeftoverDecimals = BigDecimal.valueOf(travelTime)
                    .subtract(BigDecimal.valueOf(BigDecimal.valueOf(travelTime).intValue()))
                    .add(BigDecimal.valueOf(duration));
            final int steps = !isLastElement ? durationWithLeftoverDecimals.intValue() : durationWithLeftoverDecimals.setScale(0, RoundingMode.CEILING).intValue();
            if (steps >= 1) {
                final List<Orbit> navPoints = DistanceCalculator.getWaypointsFromCourse(EModuleType.FTLPROPULSION, restrictingTechnologyType, acceleration, beforeOrbit, waypointOrbit, steps);
                navPoints.forEach(n -> {
                    final int index = flightPlan.size() + navPoints.indexOf(n);
                    flightPlan.add(new FlightPlan(new FleetOrbit(n, null), index));
                });
            }
            travelTime += duration;
        }
        return new FlightPlanDto((int) Math.ceil(travelTime), waypoints, flightPlan);
    }
}
