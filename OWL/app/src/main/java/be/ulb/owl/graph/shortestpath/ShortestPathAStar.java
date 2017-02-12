package be.ulb.owl.graph.shortestpath;

import java.util.ArrayList;
import java.util.HashMap;

import be.ulb.owl.graph.NoPathException;
import be.ulb.owl.graph.Node;
import be.ulb.owl.graph.Path;

/**
 * Created by robin on 04/02/17.
 */

public class ShortestPathAStar extends ShortestPathEvaluator {
    private ArrayList<Node> _evaluatedNodes;           // contains all the nodes which have already been treated
    private ArrayList<Node> _toBeEvaluated;            // contains all the nodes that have already been discovered
                                                       // by the algorithm but which haven't been treated yet
    private HashMap<Node, Node> _mostEfficientOrigin;  // maps each node to the node it should come from to be shortest
    private HashMap<Node, Double> _reachingScore;      // contains all the nodes that have already been discovered
                                                       // by the algorithm but which haven(t been treated yet
    private HashMap<Node, Double> _intermediateScore;  // maps each node to the score of the path (from -> to)
                                                       // which contains the given node

    public ShortestPathAStar(ArrayList<Node> nodes, Node from, Node to) {
        super(from, to);
        _evaluatedNodes = new ArrayList<>();
        _toBeEvaluated = new ArrayList<>();
        _toBeEvaluated.add(from);
        _mostEfficientOrigin = new HashMap<>();
        _reachingScore = new HashMap<>();
        _intermediateScore = new HashMap<>();
        for(Node node: nodes) {
            if (!node.isNode(from)) {
                _reachingScore.put(node, Double.POSITIVE_INFINITY);
                _intermediateScore.put(node, Double.POSITIVE_INFINITY);
            }
        }
        _reachingScore.put(from, .0);
        _intermediateScore.put(from, heuristic(from, to));
    }

    @Override
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

    @Override
    public ArrayList<Path> find() throws NoPathException {
        super.find();
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
        double newScore = _reachingScore.get(current) + neighbour.getParentPlan().getDistanceBetweenNodes(neighbour, current);
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
    /**
     *
     * @param a
     * @param b
     * @return The euclidian distance between two nodes
     */
    static public double heuristic(Node a, Node b) {
        double deltaX = a.getX() - b.getX();
        double deltaY = a.getY() - b.getY();
        return Math.sqrt(deltaX*deltaX + deltaY*deltaY);
    }

}
