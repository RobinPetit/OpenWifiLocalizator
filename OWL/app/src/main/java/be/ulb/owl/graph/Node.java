/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ulb.owl.graph;

import java.util.ArrayList;

import be.ulb.owl.Wifi;

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
     * @param newPath the Path betwee the two node
     */
    protected void addPath(Path newPath) {
        if(!newPath.containsNode(this)) {
            throw new IllegalArgumentException("Path have no link with this node");
        } else {
            _listPath.add(newPath);
        }
    }
    
    /**
     * Check if the current Node have this name
     * 
     * @param name the name who must be tested
     * @return True if this node have this name
     * @see #getName() 
     */
    public boolean isNode(String name) {
        return this._name.equals(name);
    }
    
    
    /**
     * Check if this node have this alias
     * 
     * @param alias the alias which must be tested
     * @return True if the node match
     * @see #getAlias() 
     */
    public boolean haveAlias(String alias) {
        return this._listAlias.contains(alias);
    }
    
    
    /**
     * Get the parent plan
     * 
     * @return the plan object
     */
    public Plan getParentPlan() {
        return _parentPlan;
    }
    
    /**
     * Get X coordinate
     * 
     * @return the X coordinate
     */
    public float getX() {
        return _x;
    }
    
    /**
     * Get Y coordinate
     * 
     * @return the Y coordinate
     */
    public float getY() {
        return _y;
    }
    
    /**
     * Get the name of the node
     * 
     * @return The name of the node
     * @see #isNode(java.lang.String)
     */
    public String getName() {
        return _name;
    }
    
    /**
     * Get the list of the alias<br/>
     * <b>/!\</b> Java use reference... Clone befor modification
     * 
     * @return An ArrayList of string that contains all alias
     * @see #isNode(java.lang.String) 
     */
    public ArrayList<String> getAlias() {
        return _listAlias;
    }

    public ArrayList<Wifi> getWifi() {
        return _listWifi;
    }

    public ArrayList<String> getListWifiBSS() {
        ArrayList<String> tmp = new ArrayList<String>();
        for (Wifi wifi : _listWifi) {
            tmp.add(wifi.getBSS());
        }
        return tmp;
    }

    public ArrayList<Node> getNeighbours() {
        ArrayList<Node> neighbours = new ArrayList<>();
        for(Path path: _listPath) {
            neighbours.add(path.getComplementOf(this));
        }
        return neighbours;
    }

    public double getDistanceFrom(Node neighbour) {
        assert(getNeighbours().contains(neighbour));
        double ret = 0.;
        for(Path path: _listPath) {
            if(path.containsNode(neighbour)) {
                ret = path.getDistance();
                break;
            }
        }
        return ret;
    }

    public Path pathTo(Node dest) throws NoPathException {
        for(Path path : _listPath) {
            if(path.getComplementOf(this).equals(dest))
                return path;
        }
        throw new NoPathException("No path between " + getName() + " and " + dest.getName());
    }
}
