/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ulb.owl;

/**
 * Link between two node
 * 
 * @author Detobel36
 */
public class Path {
    
    private final float _distance;
    private final Node _nodeOne;
    private final Node _nodeTwo;
    
    /**
     * Create a path between two node
     * 
     * @param nodeOne the first node
     * @param nodeTwo the second node
     * @param distance between the two node
     */
    public Path(Node nodeOne, Node nodeTwo, float distance) {
        this._nodeOne = nodeOne;
        this._nodeTwo = nodeTwo;
        this._distance = distance;
    }
    
    /**
     * Check if this path contain a specific node
     * 
     * @param node the node who must be tested
     * @return True if the node is on the path
     */
    public boolean containsNode(Node node) {
        return _nodeOne.equals(node) || _nodeTwo.equals(node);
    }
    
    /**
     * Get the node on the opostif of the current node
     * 
     * @param node the start node
     * @return the oposit node
     */
    public Node getOpositNode(Node node) {
        // return _nodeOne.equals(node) ? _nodeTwo : _nodeOne;
        if(_nodeOne.equals(node)) {
            return _nodeTwo;
        }
        return _nodeOne;
    }
    
    
}
