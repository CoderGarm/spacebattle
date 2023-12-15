package de.yuga.spacebattle.backend.calculator.distance.dijkstra;

import de.yuga.spacebattle.SpringBootProdProfile;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.services.caclulator.NavigationCalculatorService;
import de.yuga.spacebattle.backend.services.orbitals.StarSystemService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootProdProfile
@Disabled("not needed for unit or integration testing")
class DijkstraAlgorithmLongRunningUnitTest {

    @Autowired
    private NavigationCalculatorService navigationCalculatorService;

    @Autowired
    private StarSystemService starSystemService;

    @Test
    void checkIt() {

        final List<StarSystem> byNames = new ArrayList<>(starSystemService.findByNames(Set.of("Manticore", "Sol")));

        final List<StarSystem> shortestWaypoints = navigationCalculatorService.getShortestWaypoints(byNames.get(0), byNames.get(1));
        final List<String> result = shortestWaypoints.stream().map(StarSystem::getName).collect(Collectors.toList());
        assertEquals("Manticore", result.get(0));
        assertEquals("Sigma Draconis", result.get(1));
        assertEquals("Sol", result.get(2));
    }

    @Test
    void whenSPPSolved_thenCorrect() {

        Node nodeA = new Node("A");
        Node nodeB = new Node("B");
        Node nodeC = new Node("C");
        Node nodeD = new Node("D");
        Node nodeE = new Node("E");
        Node nodeF = new Node("F");

        nodeA.addDestination(nodeB, 10);
        nodeA.addDestination(nodeC, 15);

        nodeB.addDestination(nodeD, 12);
        nodeB.addDestination(nodeF, 15);

        nodeC.addDestination(nodeE, 10);

        nodeD.addDestination(nodeE, 2);
        nodeD.addDestination(nodeF, 1);

        nodeF.addDestination(nodeE, 5);

        Graph graph = new Graph();

        graph.addNode(nodeA);
        graph.addNode(nodeB);
        graph.addNode(nodeC);
        graph.addNode(nodeD);
        graph.addNode(nodeE);
        graph.addNode(nodeF);

        graph = Dijkstra.calculateShortestPathFromSource(graph, nodeA);

        List<Node> shortestPathForNodeB = Arrays.asList(nodeA);
        List<Node> shortestPathForNodeC = Arrays.asList(nodeA);
        List<Node> shortestPathForNodeD = Arrays.asList(nodeA, nodeB);
        List<Node> shortestPathForNodeE = Arrays.asList(nodeA, nodeB, nodeD);
        List<Node> shortestPathForNodeF = Arrays.asList(nodeA, nodeB, nodeD);

        for (Node node : graph.getNodes()) {
            switch (node.getName()) {
                case "B":
                    assertTrue(node
                            .getShortestPath()
                            .equals(shortestPathForNodeB));
                    break;
                case "C":
                    assertTrue(node
                            .getShortestPath()
                            .equals(shortestPathForNodeC));
                    break;
                case "D":
                    assertTrue(node
                            .getShortestPath()
                            .equals(shortestPathForNodeD));
                    break;
                case "E":
                    assertTrue(node
                            .getShortestPath()
                            .equals(shortestPathForNodeE));
                    break;
                case "F":
                    assertTrue(node
                            .getShortestPath()
                            .equals(shortestPathForNodeF));
                    break;
            }
        }
    }
}
