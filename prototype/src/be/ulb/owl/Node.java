/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ulb.owl;

import java.util.ArrayList;

/**
 * Point on a specific plan
 * 
 * @author Detobel36
 */
public class Node {
    
    private ArrayList<Path> _listPath;
    private ArrayList<String> _listAlias;
    private ArrayList<Wifi> _listWifi;
    
    private final Plan _parentPlan;
    private final float _x;
    private final float _y;
    private final String _name;
    
    
    /**
     * Init a node
     * 
     * @param parentPlan plan where is this node
     * @param x x position of th node
     * @param y y position of the node
     * @param name of the node
     * @param listWifi list of all wifi capted on this point
     */
    public Node(Plan parentPlan, float x, float y, String name, 
            ArrayList<Wifi> listWifi) {
        
        this._listPath = new ArrayList<Path>();
        this._listAlias = new ArrayList<String>();
        this._listWifi = listWifi;
        
        this._x = x;
        this._y = y;
        this._name = name;
        this._parentPlan = parentPlan;
    }
    
    /**
     * Add an alias to this node
     * 
     * @param alias the alias who must be add
     */
    public void addAlias(String alias) {
        this._listAlias.add(alias);
    }
    
    /**
     * Add a path to an other Node
     * 
     * @param toNode the distination node
     * @param distance distance between this node and the other
     */
    public void addPath(Node toNode, int distance) {
        Path newPath = new Path(this, toNode, distance);
        _listPath.add(newPath);
    }
    
    
    /**
     * Check if the current Node have this name
     * 
     * @param name the name who must be tested
     * @return True if this node have this name
     */
    public boolean isNode(String name) {
        return this._name.equals(name) || this._listAlias.contains(name);
    }
    
    /**
     * Get the parent plan
     * 
     * @return the plan object
     */
    public Plan getParentPlan() {
        return _parentPlan;
    }
    
    public float getX() {
        return _x;
    }
    
    public float getY() {
        return _y;
    }
    
}
