package be.ulb.owl.graph;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by robin on 10/12/16.
 */

public class ShortestPathEvaluator {
    private ArrayList<Node> _allNodes;
    ArrayList<Node> _evaluatedNodes;           // contains all the nodes which have already been treated
    ArrayList<Node> _toBeEvaluated;            // contains all the nodes that have already been discovered
                                               // by the algorithm but which haven't been treated yet
    HashMap<Node, Node> _mostEfficientOrigin;  // maps each node to the node it should come from to be shortest
    HashMap<Node, Double> _reachingScore;      // contains all the nodes that have already been discovered
                                               // by the algorithm but which haven(t been treated yet
    HashMap<Node, Double> _intermediateScore;  // maps each node to the score of the path (from -> to)
                                               // which contains the given node

    public ShortestPathEvaluator(ArrayList<Node> nodes, Node from, Node to) {
        _allNodes = nodes;
        _evaluatedNodes = new ArrayList<>();

        _toBeEvaluated = new ArrayList<>();
        _toBeEvaluated.add(from);
        _mostEfficientOrigin = new HashMap<>();
        _reachingScore = new HashMap<>();
        _intermediateScore = new HashMap<>();
        for(Node node: _allNodes) {
            if (!node.isNode(from.getName())) {
                _reachingScore.put(node, Double.POSITIVE_INFINITY);
                _intermediateScore.put(node, Double.POSITIVE_INFINITY);
            }
        }
        _reachingScore.put(from, .0);
        _intermediateScore.put(from, heuristic(from, to));
    }

    /**
     *
     * @param a
     * @param b
     * @return The euclidian distance between two nodes
     */
    private double heuristic(Node a, Node b) {
        // TODO implement euclidian distance
        return .5;
    }

    /**
     *
     * @return The list of nodes to cross to reach the `to` node starting at the `from` node
     */
    public ArrayList<Path> find() {
        return new ArrayList<>();
    }
}
