package za.ac.spu.sacns.navigation.navigation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.ac.spu.sacns.models.NavigationEdge;
import za.ac.spu.sacns.models.NavigationNode;

public class NavigationGraph {

    private final Map<String, NavigationNode> nodes = new HashMap<>();
    private final Map<String, List<NavigationEdge>> edges = new HashMap<>();

    public void addNode(NavigationNode node) {
        nodes.put(node.getId(), node);
        edges.put(node.getId(), new ArrayList<>());
    }

    public void addEdge(String fromId, String toId, double distance) {
        if (!nodes.containsKey(fromId) || !nodes.containsKey(toId)) {
            return;
        }

        edges.get(fromId).add(
                new NavigationEdge(fromId, toId, distance)
        );

        edges.get(toId).add(
                new NavigationEdge(toId, fromId, distance)
        );
    }

    public NavigationNode getNode(String id) {
        return nodes.get(id);
    }

    public List<NavigationEdge> getEdges(String nodeId) {
        return edges.getOrDefault(nodeId, new ArrayList<>());
    }

    public Map<String, NavigationNode> getNodes() {
        return nodes;
    }
}