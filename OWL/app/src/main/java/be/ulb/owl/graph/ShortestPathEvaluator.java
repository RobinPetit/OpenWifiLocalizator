package be.ulb.owl.graph;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by robin on 10/12/16.
 */

public class ShortestPathEvaluator {
    private ArrayList<Node> _allNodes;
    private ArrayList<Node> _evaluatedNodes;           // contains all the nodes which have already been treated
    private ArrayList<Node> _toBeEvaluated;            // contains all the nodes that have already been discovered
                                                       // by the algorithm but which haven't been treated yet
    private HashMap<Node, Node> _mostEfficientOrigin;  // maps each node to the node it should come from to be shortest
    private HashMap<Node, Double> _reachingScore;      // contains all the nodes that have already been discovered
                                                       // by the algorithm but which haven(t been treated yet
    private HashMap<Node, Double> _intermediateScore;  // maps each node to the score of the path (from -> to)
                                                       // which contains the given node
    private Node _src;
    private Node _dest;

    private boolean _found;
    private boolean _executed;

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
        _found = _executed = false;
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
     * @return The list of nodes to cross to reach the `dest` node starting at the `src` node
     */
    public void lookup() {
        while(!_toBeEvaluated.isEmpty()) {
            Node current = getLowestIntermediateScore();
            assert(_toBeEvaluated.contains(current));
            if (current.equals(_dest)) {
                _found = true;
                break;
            }

            _toBeEvaluated.remove(current);
            _evaluatedNodes.add(current);

            for(Node neighbour: current.getNeighbours())
                evaluateNeighbour(current, neighbour);
        }
        _executed = true;
    }

    /**
     *
     * @return A list of path representing where the user needs to o to reach the given points
     * @throws NoPathException
     */
    public ArrayList<Path> find() throws NoPathException {
        if(!_executed)
            lookup();
        if(!_found)
            throw new NoPathException("Unable to find a path between nodes");
        ArrayList<Path> path = new ArrayList<>();
        Node current = _dest;
        while(!current.equals(_src)) {
            Node next = _mostEfficientOrigin.get(current);
            path.add(0, current.pathTo(next));  // add at the beginning of path
            current = next;
        }
        return path;
    }

    /**
     * Performs a step in A* algorithm by updating values according to current neighbour
     * @param current The node which is treated
     * @param neighbour The neighbour of current which is treated
     */
    private void evaluateNeighbour(Node current, Node neighbour) {
        // do not re-treat nodes
        if(_evaluatedNodes.contains(neighbour))
            return;
        double newScore = _reachingScore.get(current) + current.getDistanceFrom(neighbour);
        if(!_toBeEvaluated.contains(neighbour))
            _toBeEvaluated.add(neighbour);
        else if(newScore >= _reachingScore.get(neighbour))  // if there exists a better path, forget it
            return;

        // if no better path is known for now, set this one
        _mostEfficientOrigin.put(neighbour, current);
        _reachingScore.put(neighbour, newScore);
        _intermediateScore.put(neighbour, _reachingScore.get(neighbour) + heuristic(neighbour, _dest));
    }

    /**
     *
     * @return The node having the min value as intermediate score
     */
    private Node getLowestIntermediateScore() {
        Node toReturn = _toBeEvaluated.get(0);
        double score = _intermediateScore.get(toReturn);
        for(Node key: _toBeEvaluated) {
            double keyScore = _intermediateScore.get(key);
            if(keyScore < score) {
                score = keyScore;
                toReturn = key;
            }
        }
        return toReturn;
    }
}
