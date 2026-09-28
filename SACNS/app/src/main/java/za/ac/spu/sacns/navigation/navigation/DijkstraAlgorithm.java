package za.ac.spu.sacns.navigation.navigation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import za.ac.spu.sacns.models.NavigationEdge;
import za.ac.spu.sacns.models.NavigationNode;

public class DijkstraAlgorithm {

    public static class RouteResult {

        private final List<NavigationNode> path;
        private final double distance;

        public RouteResult(List<NavigationNode> path, double distance) {
            this.path = path;
            this.distance = distance;
        }

        public List<NavigationNode> getPath() {
            return path;
        }

        public double getDistance() {
            return distance;
        }
    }

    private static class NodeDistance {

        String nodeId;
        double distance;

        NodeDistance(String nodeId, double distance) {
            this.nodeId = nodeId;
            this.distance = distance;
        }
    }

    public static RouteResult findShortestPath(
            NavigationGraph graph,
            String startId,
            String destinationId
    ) {

        Map<String, Double> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();

        for (String nodeId : graph.getNodes().keySet()) {
            distances.put(nodeId, Double.POSITIVE_INFINITY);
        }

        distances.put(startId, 0.0);

        PriorityQueue<NodeDistance> queue =
                new PriorityQueue<>(
                        Comparator.comparingDouble(node -> node.distance)
                );

        queue.add(new NodeDistance(startId, 0.0));

        while (!queue.isEmpty()) {

            NodeDistance current = queue.poll();

            if (current.distance > distances.get(current.nodeId)) {
                continue;
            }

            if (current.nodeId.equals(destinationId)) {
                break;
            }

            for (NavigationEdge edge : graph.getEdges(current.nodeId)) {

                double newDistance =
                        current.distance + edge.getDistance();

                String neighbourId = edge.getToNodeId();

                if (newDistance < distances.get(neighbourId)) {

                    distances.put(neighbourId, newDistance);
                    previous.put(neighbourId, current.nodeId);

                    queue.add(
                            new NodeDistance(
                                    neighbourId,
                                    newDistance
                            )
                    );
                }
            }
        }

        if (!distances.containsKey(destinationId)
                || distances.get(destinationId) == Double.POSITIVE_INFINITY) {

            return new RouteResult(
                    new ArrayList<>(),
                    Double.POSITIVE_INFINITY
            );
        }

        List<NavigationNode> path = new ArrayList<>();

        String currentId = destinationId;

        while (currentId != null) {

            NavigationNode node = graph.getNode(currentId);

            if (node == null) {
                return new RouteResult(
                        new ArrayList<>(),
                        Double.POSITIVE_INFINITY
                );
            }

            path.add(node);
            currentId = previous.get(currentId);
        }

        Collections.reverse(path);

        return new RouteResult(
                path,
                distances.get(destinationId)
        );
    }
}