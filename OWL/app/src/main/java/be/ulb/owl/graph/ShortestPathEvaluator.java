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
    Node _src;
    Node _dest;

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
        _src = from;
        _dest = to;
    }

    /**
     *
     * @param a
     * @param b
     * @return The euclidian distance between two nodes
     */
    private double heuristic(Node a, Node b) {
        double deltaX = a.getX() - b.getX();
        double deltaY = a.getY() - b.getY();
        return Math.sqrt(deltaX*deltaX + deltaY*deltaY);
    }

    /**
     *
     * @return The list of nodes to cross to reach the `to` node starting at the `from` node
     */
    public ArrayList<Path> find() {
        boolean found = false;
        while(!_toBeEvaluated.isEmpty()) {
            Node current = getLowestIntermediateScore();
            if (current.isNode(_dest.getName())) {
                found = true;
                break;
            }

            _toBeEvaluated.remove(_toBeEvaluated.indexOf(current));
            _evaluatedNodes.add(current);

            for(Node neighbour: current.getNeighbours()) {
                // TODO Implement core of A*
            }
        }
        // TODO handle whether or not solution has been found
        if(found) {

        } else {

        }
        return new ArrayList<>();
    }

    /**
     *
     * @return The node having the min value as intermediate score
     */
    private Node getLowestIntermediateScore() {
        Node[] keys = (Node[])_intermediateScore.keySet().toArray();
        Node toReturn = keys[0];
        double score = _intermediateScore.get(keys[0]);
        for(Node key: keys) {
            double keyScore = _intermediateScore.get(key);
            if(keyScore < score) {
                score = keyScore;
                toReturn = key;
            }
        }
        return toReturn;
    }
}
