package de.yuga.spacebattle.backend.services.caclulator;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator;
import de.yuga.spacebattle.backend.calculator.distance.dijkstra.Dijkstra;
import de.yuga.spacebattle.backend.calculator.distance.dijkstra.Graph;
import de.yuga.spacebattle.backend.calculator.distance.dijkstra.Node;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.enums.space.EWormhole;
import de.yuga.spacebattle.backend.services.orbitals.StarSystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
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
                final StarSystem closerToDestination = starSystems.stream().reduce((o1, o2) -> DistanceCalculator.getTimeToTravel(destination, o1) <= DistanceCalculator.getTimeToTravel(destination, o2) ? o1 : o2)
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
        systemsOnTrack.add(origin);
        systemsOnTrack.add(destination);

        final Map<StarSystem, Node> systemNodes = new HashMap<>(systemsOnTrack.stream()
                .collect(Collectors.toMap(Function.identity(), s -> new Node(s.getName()))));

        // todo I can be build only for the origins node - nothing else is necessary
        systemsOnTrack.forEach(a ->
                systemsOnTrack.stream()
                        .filter(b -> !a.equals(b))
                        .forEach(b -> {
                            final Node nodeA = systemNodes.get(a);
                            final Node nodeB = systemNodes.get(b);
                            final int distance = EWormhole.areSystemsConnected(a, b) ? 0 : DistanceCalculator.getTimeToTravel(a, b);
                            nodeA.addDestination(nodeB, distance);
                        })
        );
        return systemNodes;
    }

}
