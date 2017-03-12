package be.ulb.owl.graph.shortestpath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import be.ulb.owl.graph.NoPathException;
import be.ulb.owl.graph.Node;
import be.ulb.owl.graph.Path;

/**
 * Created by robin on 04/02/17.
 */

public class ShortestPathDijkstra extends ShortestPathEvaluator {
    private HashSet<Node> _nodesSet = new HashSet<>();
    private HashMap<Node, Node> _predecessor = new HashMap<>();
    private HashMap<Node, Float> _distance = new HashMap<>();

    public ShortestPathDijkstra(ArrayList<Node> nodes, Node from, Node to) {
        super(from, to);
        _nodesSet.addAll(nodes);
        for(final Node node : nodes) {
            _predecessor.put(node, null);
            _distance.put(node, node.isNode(_src) ? 0 : Float.POSITIVE_INFINITY);
        }
    }

    @Override
    protected void lookup() {
        while(!_nodesSet.isEmpty()) {
            Node currentNode = getLowestDistance();
            assert(_nodesSet.contains(currentNode));
            _nodesSet.remove(currentNode);
            if(currentNode.isNode(_dest)) {
                _found = true;
                break;
            }

            for(final Node neighbour : currentNode.getNeighbours()) {
                Float newDistance = _distance.get(currentNode) + (float)(neighbour.getDistanceFrom(currentNode));
                if(newDistance < _distance.get(neighbour)) {
                    _distance.put(neighbour, newDistance);
                    _predecessor.put(neighbour, currentNode);
                }
            }
        }

        _executed = true;
    }

    @Override
    public ArrayList<Path> find() throws NoPathException {
        super.find();
        if(!_found) {
            throw new NoPathException("No path has been found between nodes " + _src + " and " + _dest);
        }
        ArrayList<Path> path = new ArrayList<>();
        Node current = _dest;
        while(!current.isNode(_src)) {
            Node predecessor = _predecessor.get(current);
            path.add(0, current.pathTo(predecessor));
            current = predecessor;
        }
        return path;
    }

    private Node getLowestDistance() {
        Float minDistance = Float.POSITIVE_INFINITY;
        Node toReturn = null;
        for(final Node node : _nodesSet) {
            if(_distance.get(node) <= minDistance) {
                minDistance = _distance.get(node);
                toReturn = node;
            }
        }
        return toReturn;
    }
}
