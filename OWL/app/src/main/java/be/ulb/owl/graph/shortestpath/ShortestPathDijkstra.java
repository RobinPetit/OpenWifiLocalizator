package be.ulb.owl.graph.shortestpath;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import be.ulb.owl.graph.NoPathException;
import be.ulb.owl.graph.Node;
import be.ulb.owl.graph.Path;

/**
 * Created by robin on 04/02/17.
 */

public class ShortestPathDijkstra extends ShortestPathEvaluator {
    private HashSet<Node> _nodesSet = new HashSet<>();
    private Node[] _predecessor;
    private float[] _distance;

    private int[] _nodesMapping;

    /**
     * Create a shortest path finder using Dijkstra's algorithm
     * @param nodes a list of all nodes of the graph
     * @param from the node to start looking from
     * @param to the node to find a path to
     */
    public ShortestPathDijkstra(ArrayList<Node> nodes, Node from, Node to) {
        super(from, to);
        initNodesMapping(nodes);
        _predecessor = new Node[nodes.size()];
        _distance = new float[nodes.size()];
        _nodesSet.addAll(nodes);
        for(final Node node : nodes) {
            setPredecessorOf(node, null);
            setDistanceTo(node, node.isNode(_src) ? 0 : Float.POSITIVE_INFINITY);
        }
    }

    /**
     * Create the mapped ids of all the nodes
     * @param nodes the list of all nodes of the graph
     */
    private void initNodesMapping(ArrayList<Node> nodes) {
        int maxId = Collections.max(nodes).getID();  // get the highest id
        _nodesMapping = new int[maxId];  // make an array of that given size
        int mappedId = 0;
        for(Node node : nodes) {
            _nodesMapping[node.getID()-1] = mappedId;  // map each node to an id being in [0, maxId)
            mappedId++;
        }
    }

    @Override
    protected void lookup() {
        while(!_nodesSet.isEmpty()) {
            Node currentNode = getNodeHavingLowestDistance();
            assert(_nodesSet.contains(currentNode));
            _nodesSet.remove(currentNode);
            if(getDistanceTo(currentNode) == Float.POSITIVE_INFINITY)
                throw new AssertionError("lowest distance node cannot be at infinite distance!");
            if(currentNode.isNode(_dest)) {
                _found = true;
                break;
            }
            exploreNeighbours(currentNode);
        }

        if(getPredecessorOf(_dest) == null)
            throw new AssertionError("Destination node should have been visited at least once");
        _executed = true;
    }

    /**
     * Get the predecessor of a given node
     * @param node the node to get the predecessor of
     * @return the predecessor of the node (and null if node has no predecessor yet)
     */
    private Node getPredecessorOf(Node node) {
        int nodeMappedId = getMappedIdOf(node);
        return _predecessor[nodeMappedId];
    }

    /**
     * Get the distance from src to a given node
     * @param node the node to get the distancne to
     * @return the distance between src and node
     */
    private float getDistanceTo(Node node) {
        int nodeMappedId = getMappedIdOf(node);
        return _distance[nodeMappedId];
    }

    /**
     * explore all non-explored yet neighbours of current node
     * @param current the node to look at the neighbours of
     */
    private void exploreNeighbours(Node current) {
        // for each neighbour of current node
        for(final Node neighbour : current.getNeighbours()) {
            Float newDistance = getDistanceTo(current) + (float)(neighbour.getDistanceFrom(current));
            // check if current is a better predecessor to neighbour
            if(newDistance < getDistanceTo(neighbour)) {
                // if so, change predecessor and distance
                setDistanceTo(neighbour, newDistance);
                setPredecessorOf(neighbour, current);
            }
        }
    }

    @Override
    public ArrayList<Path> find() throws NoPathException {
        super.find();
        ArrayList<Path> path = new ArrayList<>();
        Node current = _dest;
        while(!current.isNode(_src)) {
            Node predecessor = getPredecessorOf(current);
            path.add(0, current.pathTo(predecessor));
            current = predecessor;
        }
        return path;
    }

    /**
     * Set the predecessor of a given node
     * @param src the node to set a predecessor to
     * @param predecessor the predecessor
     */
    private void setPredecessorOf(Node src, Node predecessor) {
        _predecessor[getMappedIdOf(src)] = predecessor;
    }

    /**
     * Set the distance to a given node
     * @param src the node to set a distance to
     * @param distance the distance
     */
    private void setDistanceTo(Node src, float distance) {
        assert(distance > 0);
        _distance[getMappedIdOf(src)] = distance;
    }

    /**
     * Get the mapped id of a given node
     * @param node the node to get the mapped id of
     * @return the mapped id of the node
     */
    private int getMappedIdOf(Node node) {
        return _nodesMapping[node.getID()-1];
    }

    /**
     * Get the node being the closest to src
     * @return the node minimizing cumulated distance from src
     */
    private Node getNodeHavingLowestDistance() {
        float minDistance = Float.POSITIVE_INFINITY;
        Node toReturn = null;
        for(final Node node : _nodesSet) {
            if(getDistanceTo(node) <= minDistance) {
                minDistance = getDistanceTo(node);
                toReturn = node;
            }
        }
        // showDebugInformation(minDistance, toReturn);
        return toReturn;
    }

    private void showDebugInformation(float minDistance, Node toReturn) {
        Log.d(getClass().getName(), "distance info: " + getDebugDistancesString());
        Log.d(getClass().getName(), "Lowest distance: " + minDistance + " implies node: " + toReturn);
    }

    private String getDebugDistancesString() {
        String ret =  "";
        for(Node node : _nodesSet) {
            if(getDistanceTo(node) != Float.POSITIVE_INFINITY)
                ret += "  " + node + ": " + getDistanceTo(node);
        }
        return ret;
    }
}
