package de.yuga.spacebattle.backend.calculator.distance.dijkstra;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Dijkstra {

    public static void calculateShortestPathFromSource(@Nonnull final Node source) {
        Preconditions.checkNotNull(source, "source must not be empty");

        source.setDistance(0);

        final Set<Node> settledNodes = new HashSet<>();
        final Set<Node> unsettledNodes = new HashSet<>();
        unsettledNodes.add(source);

        while (!unsettledNodes.isEmpty()) {
            final Node currentNode = getLowestDistanceNode(unsettledNodes);
            unsettledNodes.remove(currentNode);
            for (Entry<Node, Integer> adjacencyPair : currentNode.getAdjacentNodes().entrySet()) {
                final Node adjacentNode = adjacencyPair.getKey();
                final int edgeWeigh = adjacencyPair.getValue();

                if (!settledNodes.contains(adjacentNode)) {
                    calculateMinimumDistance(adjacentNode, edgeWeigh, currentNode);
                    unsettledNodes.add(adjacentNode);
                }
            }
            settledNodes.add(currentNode);
        }
    }

    private static void calculateMinimumDistance(@Nonnull final Node evaluationNode, int edgeWeigh, @Nonnull final Node sourceNode) {
        Preconditions.checkNotNull(evaluationNode, "evaluationNode must not be empty");
        Preconditions.checkNotNull(sourceNode, "sourceNode must not be empty");

        final Integer sourceDistance = sourceNode.getDistance();
        if (sourceDistance + edgeWeigh < evaluationNode.getDistance()) {
            evaluationNode.setDistance(sourceDistance + edgeWeigh);
            final LinkedList<Node> shortestPath = new LinkedList<>(sourceNode.getShortestPath());
            shortestPath.add(sourceNode);
            evaluationNode.setShortestPath(shortestPath);
        }
    }

    @Nullable
    private static Node getLowestDistanceNode(@Nonnull final Set<Node> unsettledNodes) {
        Preconditions.checkNotNull(unsettledNodes, "unsettledNodes must not be empty");

        final AtomicReference<Node> lowestDistanceNode = new AtomicReference<>();
        final AtomicInteger lowestDistance = new AtomicInteger(Integer.MAX_VALUE);
        unsettledNodes.forEach(node -> {
            int nodeDistance = node.getDistance();
            if (nodeDistance < lowestDistance.get()) {
                lowestDistance.set(nodeDistance);
                lowestDistanceNode.set(node);
            }
        });

        return lowestDistanceNode.get();
    }
}
