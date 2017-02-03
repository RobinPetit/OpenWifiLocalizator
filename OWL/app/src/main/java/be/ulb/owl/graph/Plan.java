/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ulb.owl.graph;

import android.graphics.drawable.Drawable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import be.ulb.owl.MainActivity;
import be.ulb.owl.Wifi;
import be.ulb.owl.utils.SQLUtils;

/**
 * A plan that contains Node, WifiList, ...
 *
 * @author Detobel36
 */
public class Plan {
    private final MainActivity main = MainActivity.getInstance();
    /**
     * represents the X-axis part of the relative coordinate of the upper left corner of the
     * background image in GraphMaker (the graph editor).
     */
    private float _bgCoordX;
    /**
     * represents the Y-axis part of the relative coordinate of the upper left corner of the
     * background image in GraphMaker (the graph editor).
     */
    private float _bgCoordY;

    private final String _name;
    private final Campus _parentPlan;
    private HashSet<String> _allAlias; // Cache
    private ArrayList<Node> _listNode;
    private HashSet<String> _allBssWifi;
    private InputStream _image;
//    private final String _pathImage;
    private final float _ppm;  // pixels per metre
    private final float _relativeAngle;
    private final float _xOnParent;
    private final float _yOnParent;


    /**
     * Constructor<br />
     * <b>Only</b> call with by SQL (to have information)
     *
     * @param name of the plan
     * @param id in the database
     * @param parentPlan the parent plan reference (campus plan)
     * @param pathImage path to the image
     * @param xOnParent x position on the parent plan
     * @param yOnParent y position on the parent plan
     * @param bgCoordX relative x position of the upper left corner of the image
     * @param bgCoordY relative y position of the upper left corner of the image
     * @param relativeAngle angle that the plan makes on the parent plan
     * @param distance number of pixel for on meter
     */
    public Plan(String name, int id, Campus parentPlan, String pathImage, float xOnParent, float yOnParent, float bgCoordX,
                float bgCoordY, float relativeAngle, float distance) {

        this._name = name;

        this._parentPlan = parentPlan;
        this._xOnParent = xOnParent;
        this._yOnParent = yOnParent;
        this._bgCoordX = bgCoordX;
        this._bgCoordY = bgCoordY;
        this._relativeAngle = relativeAngle;
        this._ppm = distance;
//        this._pathImage = pathImage;

        _listNode = SQLUtils.loadNodes(this, id);

        _allBssWifi = new HashSet<String>();
        _allAlias = new HashSet<String>();

        for(Node selectNode : _listNode) {
            _allBssWifi.addAll(selectNode.getListWifiBSS());
            _allAlias.addAll(selectNode.getAlias());
        }

    }


    /**
     * TODO add documentation @denis ?
     *
     * @param level
     * @return
     */
    private double getScore(Float level) {
        return Math.pow((-level+100)/50, 0.6);
    }


    /**
     * Get the node with a minimal difference between its avg dbm and the avg dbm of th sensed wifi
     *
     * @param wifis list of sensed wifis
     * @param nodes list of potential nearest nodes
     * @return the good node
     */
    private Node collisionManager(ArrayList<Wifi> wifis, ArrayList<Node> nodes) {
        ArrayList<String> wifisStr = new ArrayList<String>();
        for (Wifi wifi : wifis) {
            wifisStr.add(wifi.getBSS());
        }
        Node res;
        ArrayList<Double> scores = new ArrayList<Double>();
        for (int i = 0; i < nodes.size(); i++) { // for each node
            scores.add(0.0);
            ArrayList<Wifi> tmp = nodes.get(i).getWifi();
            double z = 0.0;
            for (Wifi wifi: tmp) {
                if (wifisStr.contains(wifi.getBSS())) { // has a Wifi with the same BSS
                    Integer offset = wifisStr.indexOf(wifi.getBSS());
                    z += Math.pow((wifis.get(offset)).getAvg()-wifi.getAvg(), 2)/(wifis.get(offset)).getVariance();
                }
            }
            scores.set(i, z);
        }
        res = nodes.get(scores.indexOf(Collections.min(scores)));
        return res;
    }


    /**
     * Load the image from the IMGMap folder
     *
     * @throws IOException error with file
     */
    private void loadImage() throws IOException {
        try {
            // TODO optimisation ? :/
            _image = main.getAssets().open("IMGMap" + File.separator + _name +".png");
        } catch (IOException e) {
            throw new IOException("Impossible to load the image of this plan (" + _name + ")");
        }
    }


    /**
     * @link _bgCoordX
     *
     * @return the relative x position of the upper left corner of the image
     */
    protected float getBgCoordX() {
        return _bgCoordX;
    }


    /**
     * @link _bgCoordY
     *
     * @return the relative y position of the upper left corner of the image
     */
    protected float getBgCoordY() {
        return _bgCoordY;
    }


    /**
     * Check if the current plan have this name
     *
     * @param name the specific name
     * @return True if this plan have this name
     */
    public boolean isName(String name) {
        return _name.equals(name);
    }


    /**
     * Get the id of the plan
     *
     * @return the name of this plan
     */
    public String getName() {
        return _name;
    }


    /**
     * Get the x coordinate of the plan on his parent plan
     *
     * @return the x coordinate
     */
    private double getXOnParent() {
        return _xOnParent;
    }


    /**
     * Get the y coordinate of the plan on his parent plan
     *
     * @return the Y coordinate
     */
    private double getYOnParent() {
        return _yOnParent;
    }


    /**
     * Check if a wifi signal (BSS) could be sensed in this plan
     *
     * @param bss the plan which must be test
     * @return True if we can capt it
     * @see #getListWifiBSS()
     */
    public boolean containsWifiBSS(String bss) {
        return _allBssWifi.contains(bss);
    }


    /**
     * Get the list of the BSS which could be sensed in this plan<br/>
     * <b>/!\</b> in Java list are reference (clone before modification)
     *
     * @return a list of String which contains all BSS
     * @see #containsWifiBSS(java.lang.String)
     */
    public HashSet<String> getListWifiBSS() {
        return _allBssWifi;
    }


    /**
     * Get the list of all node contain in this plan
     *
     * @return an ArrayList with all node from this plan
     */
    public ArrayList<Node> getAllNodes() {
        return _listNode;
    }


    /**
     * Get the list of all alias contains in this plan
     *
     * @return an HashSet with String which represent all alias
     */
    public HashSet<String> getAllAlias() {
        return _allAlias;
    }


    /**
     * Get the image of this map
     *
     * @return InputStream which represent the image or null if not found
     */
    public InputStream getImage() {
        return _image;
    }


    /**
     * Get the number of pixel for on meter
     *
     * @return the number of pixel
     */
    public float getPpm() {
        return _ppm;
    }


    /**
     *
     * @return The pseudo-absolute y coordinate of the origin of the plan
     */
    public double getAbsoluteX(float x) {
        double originX = getXOnParent() / _parentPlan.getPpm();
        return originX + Math.cos(Math.PI/180 * _relativeAngle)*x/getPpm();
    }


    /**
     *
     * @return The pseudo-absolute y coordinate of the origin of the plan
     */
    public double getAbsoluteY(float y) {
        double originY = getYOnParent() / _parentPlan.getPpm();
        return originY - Math.sin(Math.PI/180 * _relativeAngle)*y/getPpm();
    }


    /**
     * Get a node from this plan
     *
     * @param id of the node (the number of the node)
     * @return The node or null if not found
     */
    public Node getNode(int id) {
        for(Node node : _listNode) {
            if(node.isNode(id)) {
                return node;
            }
        }

        return null;
    }


    /**
     * Get a node from this plan through wifi (NB: change name of the attribute)
     *
     * @param wifis of wifi
     * @return The nearest Node based on the given array of Wifi
     */
    public Node getNode(ArrayList<Wifi> wifis) {
        ArrayList<String> wifisStr = new ArrayList<String>();
        for (Wifi wifi : wifis) {
            wifisStr.add(wifi.getBSS());
        }
        ArrayList<Node> res = new ArrayList<Node>();
        for (Node node : _listNode) {
            ArrayList<String> tmp = node.getListWifiBSS();
            tmp.retainAll(wifisStr);
            if (3 < tmp.size()){
                res.add(node);
            }
        }
        if (res.size() > 0) {
            return collisionManager(wifis, res);
        }
        return null;
    }


    /**
     * Search all node with an specific alias (no search in node name)
     *
     * @param name the name (or alias) of this nodes
     * @return the list of all node (or empty list if not found)
     */
    public ArrayList<Node> searchNode(String name) {
        ArrayList<Node> res = new ArrayList<Node>();

        for(Node node : _listNode) {
            if(node.haveAlias(name)) {
                res.add(node);
            }
        }

        return res;
    }


    /**
     * Get the image of this map
     *
     * @return Drawable which represent the image or null if not found
     */
    public Drawable getDrawableImage() {
        Drawable res = null;

        try {
            if(_image == null || _image.available() == 0) {
                loadImage();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(_image != null) {
            res = Drawable.createFromStream(_image, null);
        }

        return res;
    }


    /**
     * Get the distance between two nodes
     *
     * @param a first node
     * @param b second node
     * @return the distance between the two
     */
    public double getDistanceBetweenNodes(Node a, Node b) {
        double distance = -1;
        for(Node node : getAllNodes()) {
            if(node.isNode(a.getID())) {
                distance = node.getDistanceFrom(b);
            }
        }
        assert(distance > 0);
        return distance / _ppm;
    }

    static public double euclidianDistance(Node a, Node b) {
        assert(a.getParentPlan() == b.getParentPlan());
        double xOffset = a.getX() - b.getX();
        double yOffset = a.getY() - b.getY();
        return Math.sqrt(xOffset*xOffset + yOffset*yOffset);
    }
}
