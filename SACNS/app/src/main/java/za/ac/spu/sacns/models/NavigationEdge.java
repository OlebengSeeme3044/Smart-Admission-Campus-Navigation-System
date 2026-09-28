package za.ac.spu.sacns.models;

import java.io.Serializable;

public class NavigationEdge implements Serializable {

    private String fromNodeId;
    private String toNodeId;
    private double distance;

    public NavigationEdge() {
    }

    public NavigationEdge(String fromNodeId, String toNodeId, double distance) {
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
        this.distance = distance;
    }

    public String getFromNodeId() {
        return fromNodeId;
    }

    public void setFromNodeId(String fromNodeId) {
        this.fromNodeId = fromNodeId;
    }

    public String getToNodeId() {
        return toNodeId;
    }

    public void setToNodeId(String toNodeId) {
        this.toNodeId = toNodeId;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
