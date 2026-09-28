package za.ac.spu.sacns.navigation.navigation;

import za.ac.spu.sacns.models.NavigationNode;

public class CampusNavigationData {

    /**
     * Builds the SPU Central Campus navigation graph.
     *
     * Node IDs must match CampusNavigationActivity.locationIds
     * and CampusMapView building IDs.
     */
    public static NavigationGraph createCampusGraph() {

        NavigationGraph graph = new NavigationGraph();

        // ==================== NODES ====================

        NavigationNode gate = new NavigationNode(
                "gate", "Main Gate", 100, 1150);

        // --- Top area ---
        NavigationNode wp = new NavigationNode(
                "wp", "William Pescod (WP)", 620, 200);
        NavigationNode c008 = new NavigationNode(
                "c008", "Teaching Practice (C008)", 560, 380);
        NavigationNode c009 = new NavigationNode(
                "c009", "Foundation Phase (C009)", 720, 300);

        // --- Middle campus ---
        NavigationNode c001 = new NavigationNode(
                "c001", "Moroka Residence (C001)", 400, 450);
        NavigationNode c002 = new NavigationNode(
                "c002", "Student Affairs (C002)", 450, 540);
        NavigationNode c003 = new NavigationNode(
                "c003", "Academic Building (C003)", 660, 450);
        NavigationNode c004 = new NavigationNode(
                "c004", "Library (C004)", 600, 560);
        NavigationNode c005 = new NavigationNode(
                "c005", "Applied Sciences (C005)", 600, 640);
        NavigationNode c006 = new NavigationNode(
                "c006", "Data Science Labs (C006)", 510, 700);
        NavigationNode c007 = new NavigationNode(
                "c007", "Science Lab (C007)", 660, 700);

        // --- Lower campus ---
        NavigationNode c010 = new NavigationNode(
                "c010", "Humanities Labs (C010)", 500, 800);
        NavigationNode c011 = new NavigationNode(
                "c011", "Agriculture (C011)", 620, 800);

        // --- Sports precinct ---
        NavigationNode c017 = new NavigationNode(
                "c017", "Sports Pavilion (C017)", 660, 1050);
        NavigationNode c018 = new NavigationNode(
                "c018", "Spectator Seating (C018)", 350, 1050);
        NavigationNode c019 = new NavigationNode(
                "c019", "Sports Entrance (C019)", 400, 950);

        // ==================== ADD NODES ====================

        graph.addNode(gate);

        graph.addNode(wp);
        graph.addNode(c008);
        graph.addNode(c009);

        graph.addNode(c001);
        graph.addNode(c002);
        graph.addNode(c003);
        graph.addNode(c004);
        graph.addNode(c005);
        graph.addNode(c006);
        graph.addNode(c007);

        graph.addNode(c010);
        graph.addNode(c011);

        graph.addNode(c017);
        graph.addNode(c018);
        graph.addNode(c019);

        // ==================== EDGES (walking distances in metres) ====================

        // --- Top zone ---
        graph.addEdge("wp", "c008", 190);
        graph.addEdge("wp", "c009", 150);
        graph.addEdge("c008", "c009", 180);

        // --- Scanlan corridor (top to middle) ---
        graph.addEdge("c008", "c001", 175);
        graph.addEdge("c009", "c003", 165);

        // --- Middle campus connectors ---
        graph.addEdge("c001", "c002", 105);
        graph.addEdge("c001", "c003", 260);
        graph.addEdge("c002", "c003", 225);
        graph.addEdge("c002", "c004", 155);
        graph.addEdge("c003", "c004", 125);
        graph.addEdge("c004", "c005", 80);
        graph.addEdge("c004", "c006", 165);
        graph.addEdge("c005", "c006", 105);
        graph.addEdge("c005", "c007", 85);
        graph.addEdge("c006", "c007", 150);

        // --- Lower campus ---
        graph.addEdge("c006", "c010", 100);
        graph.addEdge("c007", "c011", 105);
        graph.addEdge("c010", "c011", 120);

        // --- Sports precinct ---
        graph.addEdge("c010", "c019", 175);
        graph.addEdge("c011", "c017", 255);
        graph.addEdge("c019", "c017", 275);
        graph.addEdge("c019", "c018", 100);
        graph.addEdge("c018", "c017", 315);

        // --- Main Gate connections ---
        graph.addEdge("gate", "c018", 250);
        graph.addEdge("gate", "c010", 440);
        graph.addEdge("gate", "c002", 620);

        return graph;
    }
}



