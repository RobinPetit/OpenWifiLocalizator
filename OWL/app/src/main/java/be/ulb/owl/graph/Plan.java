/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ulb.owl.graph;

import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import be.ulb.owl.MainActivity;
import be.ulb.owl.scanner.Wifi;
import be.ulb.owl.utils.SQLUtils;

/**
 * A plan that contains Node, WifiList, ...
 *
 * @author Detobel36
 */
public class Plan {

    private static final MainActivity main = MainActivity.getInstance();


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
    private int _id;
    private HashSet<String> _allAlias; // Cache
    protected ArrayList<Node> _listNode;
    private HashSet<String> _allBssWifi;
    private InputStream _image;
    private final String _directoryImage;
    private final float _ppm;  // pixels per metre
    private final float _relativeAngle;
    private final float _xOnParent;
    private final float _yOnParent;
    private static final float VARIANCE_PSEUDO_COUNT = 0.1f;


    /**
     * Constructor<br />
     * <b>Only</b> call with by SQL (to have information)
     *
     * @param name of the plan
     * @param id in the database
     * @param parentPlan the parent plan reference (campus plan)
     * @param directoryImage path to the image (only the folder after IMGMap)
     * @param xOnParent x position on the parent plan
     * @param yOnParent y position on the parent plan
     * @param bgCoordX relative x position of the upper left corner of the image
     * @param bgCoordY relative y position of the upper left corner of the image
     * @param relativeAngle angle that the plan makes on the parent plan
     * @param pixelsPerMetre number of pixel for on meter
     */
    public Plan(String name, int id, Campus parentPlan, String directoryImage, float xOnParent,
                float yOnParent, float bgCoordX, float bgCoordY, float relativeAngle, float pixelsPerMetre) {
        if(pixelsPerMetre < 0) {
            throw new IllegalArgumentException("A plan's ppm can not be zero!");
        }
        this._name = name;
        this._id = id;

        this._parentPlan = parentPlan;
        this._xOnParent = xOnParent;
        this._yOnParent = yOnParent;
        this._bgCoordX = bgCoordX;
        this._bgCoordY = bgCoordY;
        this._relativeAngle = relativeAngle;
        this._ppm = pixelsPerMetre;
        this._directoryImage = directoryImage;
        this._listNode = new ArrayList<>();


        _listNode = SQLUtils.loadNodes(this, id);

        _allBssWifi = new HashSet<>();

        _allAlias = new HashSet<>();
        for(Node selectNode : _listNode) {
            _allAlias.addAll(selectNode.getAlias());
        }
    }

    /**
     * Get the node with a minimal difference between its avg dbm and the avg dbm of th sensed wifi
     *
     * @param wifis list of sensed wifis
     * @param nodes list of potential nearest nodes
     * @return the good node
     */
    private Node collisionManager(ArrayList<Wifi> wifis, ArrayList<Node> nodes) {
        ArrayList<String> wifisStr = new ArrayList<>();
        for (Wifi wifi : wifis) {
            wifisStr.add(wifi.getBSS());
        }

        Node res;
        ArrayList<Double> scores = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) { // for each node
            ArrayList<Wifi> tmp = nodes.get(i).getWifi();

            double z = 0.0; // 1 if complex
            for (Wifi wifi: tmp) {
                if (wifisStr.contains(wifi.getBSS())) { // has a Wifi with the same BSS
                    int offset = wifisStr.indexOf(wifi.getBSS());

                    float varianceDatabase = wifi.getVariance();
                    float avgDatabase = wifi.getAvg();
                    float avgSensed = (wifis.get(offset)).getAvg();

                    //// Calcul complex ////
//                    double term1 = (1+VARIANCE_PSEUDO_COUNT) / (Math.sqrt(2 * Math.PI * (varianceDatabase + VARIANCE_PSEUDO_COUNT)));
//                    double exposant = - ( (Math.pow(avgSensed - avgDatabase, 2)) + VARIANCE_PSEUDO_COUNT ) /
//                                        (2*(varianceDatabase + VARIANCE_PSEUDO_COUNT));
//                    double term2 = Math.pow(Math.E, exposant);
//
//                    z *= term1 * term2;


                    //// Calcul "simplified" ////
                    double term = ( (Math.pow(avgSensed - avgDatabase, 2)) + VARIANCE_PSEUDO_COUNT ) /
                                        (varianceDatabase + VARIANCE_PSEUDO_COUNT);
//                    double term = Math.pow(Math.E, exposant);

                    z += term;
                    Log.d(getClass().getName(), "Variance: " + (varianceDatabase + VARIANCE_PSEUDO_COUNT) +
                            " total: " + term + " z :  "+ z);
                }
            }
//            scores.add(Math.pow(z, 1/tmp.size()));
            scores.add(z);
        }
        Log.d(getClass().getName(), "Score node : " + nodes.toString());
        Log.d(getClass().getName(), "Score score : " + scores.toString());

        //// Calcul complex ////
//        res = nodes.get(scores.indexOf(Collections.max(scores)));

        //// Calcul "simplified" ////
        res = nodes.get(scores.indexOf(Collections.min(scores)));
        return res;
    }


    /**
     * Load the image from the IMGMap folder
     *
     * @throws IOException error with file
     */
    private void loadImage() throws IOException {
        String imagePath = "";
        try {
            imagePath = "IMGMap" + File.separator;
            if(!_directoryImage.equalsIgnoreCase("") && !_directoryImage.equalsIgnoreCase("./")) {
                imagePath += _directoryImage;
            }
            
            if(!(File.separator.equalsIgnoreCase(imagePath.substring(imagePath.length()-1)))) {
                imagePath += File.separator;
            }

            imagePath += _name + ".png";
            _image = main.getAssets().open(imagePath);
        } catch (IOException e) {
            throw new IOException("Impossible to load the image of this plan (" + _name +
                    " (path: " + imagePath + "))");
        }
    }

    /**
     * Awful method which aims to return the common elements of two arrays of Wifi
     *
     * @param set1 arrayList of Wifi
     * @param set2 arrayList of Wifi
     * @return an ArrayList which contains the common Wifi objects between the two arrayList given in param
     */
    private ArrayList<Wifi> common (ArrayList<Wifi> set1, ArrayList<Wifi> set2) {
        ArrayList<Wifi> res = new ArrayList<Wifi>();
        for (Wifi elem1:set1) {
            for (Wifi elem2:set2) {
                if (elem1.equals(elem2)) {
                    res.add(elem1);
                }
            }
        }
        return res;
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
     * Check if the current plan has this name
     *
     * @param name the specific name
     * @return True if this plan has this name
     */
    public boolean isName(String name) {
        return _name.equals(name);
    }


    /**
     * Get the name of the plan
     *
     * @return the name of this plan
     */
    public String getName() {
        return _name;
    }

    /**
     * Test if the current Plan have a specific id
     *
     * @param id the id which must be tested
     * @return True if it's the good id
     */
    public boolean isId(int id) {
        return this._id == id;
    }


    /**
     * Get the campus of the plan
     *
     * @return the campus of the plan
     */
    public Campus getCampus() {return _parentPlan;}

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
    public double getAbsoluteX(Node node) {
        double angle = Math.PI/180 * _relativeAngle;
        double originX = getXOnParent();
        if(_parentPlan != null) {
            originX /= _parentPlan.getPpm();
        }
        return originX + (Math.cos(angle)*node.getXOnPlan() + Math.sin(angle)*node.getYOnPlan())/getPpm();
    }


    /**
     *
     * @return The pseudo-absolute y coordinate of the origin of the plan
     */
    public double getAbsoluteY(Node node) {
        double angle = Math.PI/180 * _relativeAngle;
        double originY = getYOnParent();
        if(_parentPlan != null) {
            originY /= _parentPlan.getPpm();
        }
        return originY + (Math.cos(angle)*node.getYOnPlan() + Math.sin(angle)*node.getXOnPlan())/getPpm();
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
        ArrayList<Node> res = new ArrayList<Node>();
        for (Node node : _listNode) {
            ArrayList<Wifi> tmp = node.getWifi();
            tmp = this.common(tmp, wifis);
            if (2 < tmp.size()){
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


    public void loadWifi() {
        for(Node node : _listNode) {
            node.loadWifi();
            _allBssWifi.addAll(node.getListWifiBSS());
        }
    }

    protected void loadAllPath() {
        for(Node node : _listNode) {
            node.loadPath();
        }
    }


    /**
     * Get the image of this map
     *
     * @return Drawable which represents the image or null if not found
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
        assert distance > 0 || a.isNode(b);
        return distance / _ppm;
    }

    static public double euclidianDistance(Node a, Node b) {
        assert(a.getParentPlan().getCampus() == b.getParentPlan().getCampus());
        double xOffset = a.getX() - b.getX();
        double yOffset = a.getY() - b.getY();
        if(xOffset == 0 && yOffset == 0)
            Log.w("Plan", "for nodes " + a + " and " + b + ": distance == 0");
        return Math.sqrt(xOffset*xOffset + yOffset*yOffset);
    }

}
