/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ulb.owl.graph;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;

import be.ulb.owl.scanner.Wifi;
import be.ulb.owl.utils.SQLUtils;

/**
 * Point on a specific plan
 * 
 * @author Detobel36
 */
public class Node implements Comparable<Node> {
    
    private final Plan _parentPlan;
    private final float _x;
    private final float _y;
    private final int _id;
    private ArrayList<Path> _listPath;
    private ArrayList<String> _listAlias;
    private ArrayList<Wifi> _listWifi;
    

    public Node(Plan parentPlan, float x, float y, int id) {
        this._x = x-parentPlan.getBgCoordX();
        this._y = y-parentPlan.getBgCoordY();
        this._id = id;
        this._parentPlan = parentPlan;

        this._listAlias = SQLUtils.loadAlias(id);
        this._listWifi = new ArrayList<Wifi>();
        this._listPath = new ArrayList<Path>();
    }


    
    /**
     * Add an alias to this node
     * 
     * @param alias the alias who must be add
     */
    private void addAlias(String alias) {
        this._listAlias.add(alias);
    }
    
    
    /**
     * Add a path to another Node
     * 
     * @param newPath the Path between the two node
     */
    protected void addPath(Path newPath) {
        if(!newPath.containsNode(this)) {
            throw new IllegalArgumentException("Path has no link with this node");
        }
        Node otherNode = newPath.getOppositeNodeOf(this);
        if(hasNeighbour(otherNode)) {
            Log.w(getClass().getName(), this + " has already " + otherNode + " as neighbour");
        } else {
            _listPath.add(newPath);
        }
    }
    
    /**
     * Check if the current Node has this id
     * 
     * @param id the id which must be tested
     * @return True if this node has this id
     * @see #getID()
     */
    public boolean isNode(int id) {
        return this._id == id;
    }

    public boolean isNode(Node other) {
        return isNode(other.getID());
    }
    
    
    /**
     * Check if this node have this alias
     * 
     * @param alias the alias which must be tested
     * @return True if the node match
     * @see #getAlias()
     */
    public boolean haveAlias(String alias) {
        for(String a : _listAlias) {
            if(a.equalsIgnoreCase(alias))
                return true;
        }
        return false;
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
        return (float)getParentPlan().getAbsoluteX(this);
    }
    
    /**
     * Get Y coordinate
     * 
     * @return the Y coordinate
     */
    public float getY() {
        return (float)getParentPlan().getAbsoluteY(this);
    }

    public float getXOnPlan() {
        return _x;
    }

    public float getYOnPlan() {
        return _y;
    }
    
    /**
     * Get the name of the node
     * 
     * @return The name of the node
     * @see #isNode(int)
     */
    public Integer getID() {
        return _id;
    }
    
    /**
     * Get the list of the alias<br/>
     * <b>/!\</b> Java use reference... Clone before modification
     * 
     * @return An ArrayList of string that contains all alias
     * @see #isNode(int)
     */
    public ArrayList<String> getAlias() {
        return _listAlias;
    }

    public ArrayList<Wifi> getWifi() {
        return _listWifi;
    }

    protected void loadWifi() {
        _listWifi = SQLUtils.loadWifi(_id);
    }

    protected void loadPath() {
        SQLUtils.loadPath(getID(), this, getParentPlan());
    }


    /**
     * Add a wifi on the sensed wifi list
     *
     * @param wifi which is sensed on this node
     */
    public void addWifi(Wifi wifi) {
        _listWifi.add(wifi);
    }


    /**
     * Get the list of all BSS accessible at this node
     *
     * @return ArrayList with all BSS (String)
     */
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
            neighbours.add(path.getOppositeNodeOf(this));
        }
        return neighbours;
    }

    public boolean hasNeighbour(Node node) {
        for(final Node neighbour : getNeighbours())
            if(neighbour.isNode(node))
                return true;
        return false;
    }

    public double getDistanceFrom(Node neighbour) {
        if(!hasNeighbour(neighbour))
            throw new IllegalArgumentException("Provided node " + neighbour + "is not neighbour to " + this);
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
            if(path.getOppositeNodeOf(this).equals(dest))
                return path;
        }
        throw new NoPathException("No path between " + getID() + " and " + dest.getID());
    }

    @Override
    public String toString() {
        return getID().toString();
    }

    @Override
    public int compareTo(@NonNull Node node) {
        return getID() - node.getID();
    }
}
