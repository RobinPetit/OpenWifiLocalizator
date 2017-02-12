package be.ulb.owl.graph.shortestpath;

import java.util.ArrayList;

import be.ulb.owl.graph.Node;

/**
 * Created by robin on 04/02/17.
 */

public class ShortestPathDijkstra extends ShortestPathEvaluator {
    public ShortestPathDijkstra(ArrayList<Node> nodes, Node from, Node to) {
        super(from, to);
    }

    @Override
    protected void lookup() {

    }
}
