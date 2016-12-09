/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ulb.owl.graph;

import android.graphics.drawable.Drawable;
import android.util.Log;

import be.ulb.owl.MainActivity;
import be.ulb.owl.Wifi;
import be.ulb.owl.utils.XMLUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * A plan that contains Node, WifiList, ...
 *
 * @author Detobel36
 */
public class Plan {

    private static final MainActivity main = MainActivity.getInstance();


    private final String _name;
    private ArrayList<String> _allAlias; // Cache
    private ArrayList<Node> _listNode;
    private ArrayList<String> _allBssWifi;
    private InputStream _image;


    /**
     * Create a plan <b>and</b> load XML file from this plan<br/>
     * <b>Only call by Graph object</b>
     *
     * @param name of the plan
     * @see Graph#getPlan(java.lang.String)
     */
    protected Plan(String name) throws IOException {
        this(name, true);
    }

    /**
     * Create a plan
     * <b>Only call by Graph object</b>
     *
     * @param name name of the plan
     * @param loadPlan True if we must load XML file from this plan
     * @see Graph#getPlan(java.lang.String, boolean)
     */
    protected Plan(String name, boolean loadPlan) throws IOException {
        _listNode = new ArrayList<Node>();
        _allBssWifi = new ArrayList<String>();
        _allAlias = null;

        _name = name;
        if(loadPlan) {
            boolean couldLoad = loadXMLPlan();
            if(!couldLoad) {
                throw new IOException("Impossible de charger ce plan " + _name + " (XML introuvable)");
            }
        }

        try {
            // TODO optimisation ? :/
            _image = main.getAssets().open("IMGMap" + File.separator + _name +".png");
        } catch (IOException e) {
            throw new IOException("Impossible de charger l'image de ce plan (" + _name + ")");
        }
    }

    private double getScore(Float level) {
        /*
        if (level > 0 && level < 40) {
            return 1.4f;
        }
        else if (level > 40 && level <= 60) {
            return 1.1f;
        }
        else if (level > 60 && level <= 80) {
            return 0.7f;
        }
        else if (level > 80 && level <= 85) {
            return 0.5f;
        }
        else if (level > 85 && level <= 100) {
            return 0.3f;
        }
        return 1.0f;
        */
        return Math.pow((-level+100)/50, 0.6);//Math.sqrt((-level+100)/50);
    }

    /**
     * Create a plan <b>and</b> load XML file from this plan
     *
     * @param wifis list of capted wifis
     * @param nodes list of potential nearest nodes
     * @return The node with a minimal diffrence between its avg dbm and the avg
     * dbm of the capted wifi
     */
    private Node collisionManager(ArrayList<Wifi> wifis, ArrayList<Node> nodes) {
        ArrayList<String> wifisStr = new ArrayList<String>();
        for (Wifi wifi : wifis) {
            wifisStr.add(wifi.getBSS());
        }
        Node res;
        ArrayList<Double> scores = new ArrayList<Double>();
        System.out.println("Possible node :");
        for (int i = 0; i < nodes.size(); i++) { // for each node
            System.out.print(nodes.get(i).getName()+" : ");
            scores.add(0.0);
            ArrayList<Wifi> tmp = nodes.get(i).getWifi();
            double totaldbm = 0.0;
            Integer commonWifi = 0;
            for (Wifi wifi: tmp) {
                if (wifisStr.contains(wifi.getBSS())) { // has a Wifi with the same BSS
                    Integer offset = wifisStr.indexOf(wifi.getBSS());
                    //System.out.print("+|"+(wifis.get(offset)).getAvg()+"-"+wifi.getAvg()+"|/"+getScore((wifis.get(offset).getAvg()+wifi.getAvg())/2));
                    totaldbm += Math.abs((wifis.get(offset)).getAvg()-wifi.getAvg())/getScore((wifis.get(offset).getAvg()+wifi.getAvg())/2);
                    commonWifi++;
                }
            }
            scores.set(i, totaldbm/(commonWifi*commonWifi));
            System.out.println(" = "+scores.get(i));
        }
        res = nodes.get(scores.indexOf(Collections.min(scores)));
        return res;
        /*
        ArrayList<String> wifisStr = new ArrayList<String>();
        for (Wifi wifi : wifis) {
            wifisStr.add(wifi.getBSS());
        }
        Node res;
        ArrayList<Float> scores = new ArrayList<Float>();
        System.out.println("Possible node :");
        for (int i = 0; i < nodes.size(); i++) { // for each node
            System.out.print(nodes.get(i).getName()+" : ");
            scores.add(0.0f);
            ArrayList<Wifi> tmp = nodes.get(i).getWifi();
            Float totaldbm = 0.0f;
            Integer commonWifi = 0;
            for (Wifi wifi: tmp) {
                if (wifisStr.contains(wifi.getBSS())) { // has a Wifi with the same BSS
                    Integer offset = wifisStr.indexOf(wifi.getBSS());
                    totaldbm += Math.abs((wifis.get(offset)).getAvg()-wifi.getAvg());
                    commonWifi++;
                }
            }
            scores.set(i, totaldbm*commonWifi);
            System.out.println(" = "+scores.get(i));
        }
        res = nodes.get(scores.indexOf(Collections.min(scores)));
        return res;
        */
    }

    private ArrayList<Wifi> and(ArrayList<Wifi> capted, ArrayList<Wifi> set) {
        ArrayList<Wifi> res = new ArrayList<Wifi>();
        ArrayList<String> tmp = new ArrayList<String>();
        for (Wifi wifi:set) {
            tmp.add(wifi.getBSS());
        }
        for (Wifi cap: capted) {
            String key = cap.getBSS();
            if (tmp.contains(key)) {
                Wifi correspondent = set.get(tmp.indexOf(key));
                if (cap.getAvg() < correspondent.getMax() && cap.getAvg() > correspondent.getMin() ) {
                    res.add(cap);
                }
            }
        }
        return res;
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
     * Get a node from this plan
     *
     * @param name of the node
     * @return The node or null if not found
     */
    public Node getNode(String name) {
        for(Node node : _listNode) {
            if(node.isNode(name)) {
                return node;
            }
        }

        return null;
    }


    /**
     * Get a node from this plan through wifi (NB: change name of the attribut)
     *
     * @param wifis of wifi
     * @return The nearest Node based on the given array of Wifi
     */
    public Node getNode(ArrayList<Wifi> wifis) {
        /*
        ArrayList<String> wifisStr = new ArrayList<String>();
        for (Wifi wifi : wifis) {
            wifisStr.add(wifi.getBSS());
        }
        ArrayList<Node> res = new ArrayList<Node>();
        int biggestSetSize = 0;
        for (Node node : _listNode) {
            ArrayList<String> tmp = node.getListWifiBSS();
            tmp.retainAll(wifisStr);
            if (biggestSetSize == tmp.size()){
                res.add(node);
            }
            else if (biggestSetSize < tmp.size()) {
                res = new ArrayList<Node>();
                res.add(node);
                biggestSetSize = tmp.size();
            }
        }
        if (res.size() > 1) {
            return collisionManager(wifis, res);
        }
        return res.get(0);
        */
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
        if (res.size() > 1) {
            return collisionManager(wifis, res);
        }
        else if (res.size() == 1) {
            return res.get(0);
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
     * Check if a wifi signal (BSS) could be capted in this plan
     *
     * @param bss the plan which must be test
     * @return True if we can capt it
     * @see #getListWifiBSS()
     */
    public boolean containsWifiBSS(String bss) {
        return _allBssWifi.contains(bss);
    }


    /**
     * Get the list of the BSS which could be capted in this plan<br/>
     * <b>/!\</b> in Java list are reference (clone before modification)
     *
     * @return a list of String which contains all BSS
     * @see #containsWifiBSS(java.lang.String)
     */
    public ArrayList<String> getListWifiBSS() {
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
     * @return an ArrayList with String which represent all alias
     */
    public ArrayList<String> getAllAlias() {
        if(_allAlias == null) {
            _allAlias = new ArrayList<String>();
            Log.d(this.getClass().getName(), "Load alias !");

            for(Node node : _listNode) {
                Log.d(this.getClass().getName(), "Alias: " + node.getName() + " (" + node.getAlias().toString() + ")");
                _allAlias.addAll(node.getAlias());
            }

        } else {
            Log.i(this.getClass().getName(), "Alias null :/");
        }

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
     * Get the image of this map
     *
     * @return Drawable which represent the image or null if not found
     */
    public Drawable getDrawableImage() {
        Drawable res = null;
        if(_image != null) {
            res = Drawable.createFromStream(_image, null);
        }
        return res;
    }



    ///////////////////////////// XML /////////////////////////////

    /**
     * Load XML from this plan
     *
     * @return True if plan could be load
     */
    private boolean loadXMLPlan() {
        try {
            // Init the parser
            XmlPullParser parser = XMLUtils.readXMLFile(_name);

            if(parser == null) {
                return false;
            }

            // While the
            while(parser.next() != XmlPullParser.END_DOCUMENT) {

                XMLUtils.removeSpace(parser);
                XMLDebugParser(parser);

                if(parser.getEventType() == XmlPullParser.START_TAG &&
                        parser.getName().equalsIgnoreCase("plan")) {

                    XMLUtils.nextAndRemoveSpace(parser);

                    while(parser.getEventType() != XmlPullParser.END_TAG ||
                            !parser.getName().equalsIgnoreCase("plan")) {

                        switch(parser.getName()) {

                            case "nodes":
                                XMLLoadNodes(parser);
                                break;

                            case "edges":
                                XMLLoadEdges(parser);
                                break;

                            case "background_image":
                            case "distance_unit":
                                XMLUtils.nextAndRemoveSpace(parser); // Skip START
                                XMLUtils.nextAndRemoveSpace(parser); // Skip END
                                break;

                            default:
                                Log.e(getClass().getName(), "Balise non prise en charge: "
                                        + parser.getName());
                                parser.next();
                                break;

                        }

                        XMLUtils.removeSpace(parser);

                    }

                }


            }

        } catch (IOException | XmlPullParserException ex) {
            Log.e(getClass().getName(), ex.getMessage());
            return false;
        }

        return true;
    }


    /**
     * Load all nodes of a XML File
     *
     * @param parser the XML File Iterator
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void XMLLoadNodes(XmlPullParser parser)
            throws XmlPullParserException, IOException {

        // Next after "nodeS" balise
        XMLUtils.nextAndRemoveSpace(parser);

        while(parser.getEventType() != XmlPullParser.END_TAG ||
                !parser.getName().equalsIgnoreCase("nodes")) {

            if(parser.getEventType() == XmlPullParser.END_TAG &&
                parser.getName().equalsIgnoreCase("nodes")) {
                break;

            } else if(parser.getEventType() == XmlPullParser.START_TAG &&
                    parser.getName().equalsIgnoreCase("node")) {

                XMLLoadOneNode(parser);

            } else {
                Log.e(getClass().getName(), "Erreur dans le fichier (" + this._name + "). " +
                        "Problème de chargement de la node: " + parser.getName());
                XMLDebugParser(parser);
                XMLUtils.nextAndRemoveSpace(parser);
            }

            XMLUtils.removeSpace(parser);
            XMLDebugParser(parser);
        }
        parser.next(); // Next after the End_tag
    }


    /**
     * Load One node from a XML File
     *
     * @param parser the XML File Iterator
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void XMLLoadOneNode(XmlPullParser parser)
            throws XmlPullParserException, IOException {

        // Init default values
        ArrayList<String> listAlias = new ArrayList<String>();
        ArrayList<Wifi> listWifi = null;
        float x = Integer.MIN_VALUE;
        float y = Integer.MIN_VALUE;
        String pointId = parser.getAttributeValue(null, "id");

        // Next after "node" balise
        XMLUtils.nextAndRemoveSpace(parser);

        while(parser.getEventType() != XmlPullParser.END_TAG ||
                !parser.getName().equalsIgnoreCase("node")) {

            if(parser.getEventType() == XmlPullParser.START_TAG) {
                switch(parser.getName()) {

                    // TODO alias changes !!
                    case "alias":
                        parser.next(); // START_TAG
                        if(parser.getEventType() == XmlPullParser.TEXT) {
                            listAlias.add(parser.getText());
                        } else {
                            Log.e(this.getClass().getName(), "Erreur avec le fichier à paser ! " +
                                    "Pour le plan: " + _name);
                        }
                        break;

                    case "coord":
                        x = Float.parseFloat(parser.getAttributeValue(null, "x"));
                        y = Float.parseFloat(parser.getAttributeValue(null, "y"));
                        parser.next();
                        break;

                    case "listWifi":
                        listWifi = XMLGetListWifi(parser);
                        break;

                    default:
                        break;

                }
            }

            XMLUtils.nextAndRemoveSpace(parser);
        }
        parser.next(); // Remove End_tag

        if(x != Integer.MIN_VALUE && y != Integer.MIN_VALUE && pointId != null) {
            // Create and add node
            _listNode.add(new Node(this, x, y, pointId, listWifi));

        } else {
            Log.e(getClass().getName(), "Impossible de créer la node: " + pointId + " (il manque " +
                    "des informations)");
        }

    }


    /**
     * Extract a list of Wifi from the XML File
     *
     * @param parser the XML File Iterator
     * @return an ArrayList of Wifi
     * @throws XmlPullParserException
     * @throws IOException
     */
    private ArrayList<Wifi> XMLGetListWifi(XmlPullParser parser)
            throws XmlPullParserException, IOException {

        ArrayList<Wifi> listWifi = new ArrayList<Wifi>();

        // Next after "listwifi" balise
        XMLUtils.nextAndRemoveSpace(parser);

        while(parser.getEventType() != XmlPullParser.END_TAG ||
                !parser.getName().equalsIgnoreCase("listWifi")) {

            if(parser.getEventType() == XmlPullParser.START_TAG &&
                    parser.getName().equalsIgnoreCase("wifi")) {

                String bss = parser.getAttributeValue(null, "BSS");
                float max = Float.parseFloat(parser.getAttributeValue(null, "max"));
                float min = Float.parseFloat(parser.getAttributeValue(null, "min"));
                float avg = Float.parseFloat(parser.getAttributeValue(null, "avg"));

                if(!bss.equals("")) {
                    listWifi.add(new Wifi(bss, max, min, avg));
                    _allBssWifi.add(bss);
                }

                Log.d(getClass().getName(), "Création d'un wifi: " + bss + " min: " + min +
                        " max: "+ max + " avg: " + avg);
            }

            XMLUtils.nextAndRemoveSpace(parser);
        }
        parser.next(); // Next after the End_tag

        return listWifi;
    }


    /**
     * Load all Edges
     *
     * @param parser the XML File Iterator
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void XMLLoadEdges(XmlPullParser parser)
            throws XmlPullParserException, IOException {

        // Next after "edgesS" balise
        XMLUtils.nextAndRemoveSpace(parser);

        while(parser.getEventType() != XmlPullParser.END_TAG ||
                !parser.getName().equalsIgnoreCase("edges")) {


            if(parser.getEventType() == XmlPullParser.END_TAG &&
                parser.getName().equalsIgnoreCase("edges")) {
                break;

            } else if(parser.getEventType() == XmlPullParser.START_TAG &&
                    parser.getName().equalsIgnoreCase("internal")) {

                XMLLoadSpecificEdges(parser, "internal");

            } else if(parser.getEventType() == XmlPullParser.START_TAG &&
                    parser.getName().equalsIgnoreCase("external")) {

                XMLLoadSpecificEdges(parser, "external");
                Log.d(getClass().getName(), "End External section");

            } else {
                Log.e(getClass().getName(), "Erreur dans le fichier. Problème de chargement de " +
                        "l'edge: " + parser.getName() + " (Plan: " + _name+ ")");
                XMLDebugParser(parser);
                XMLUtils.nextAndRemoveSpace(parser);
            }

            XMLUtils.removeSpace(parser);
            XMLDebugParser(parser);
        }
        parser.next(); // Next after the End_tag

    }


    /**
     * Load a specific Edge
     *
     * @param parser the XML File Iterator
     * @param typeEdge type of edge "internal" or "external"
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void XMLLoadSpecificEdges(XmlPullParser parser, String typeEdge)
            throws XmlPullParserException, IOException {

        if(!typeEdge.equalsIgnoreCase("internal") &&
                !typeEdge.equalsIgnoreCase("external")) {
            throw new IllegalArgumentException("Les liens: " + typeEdge +
                    " ne sont pas encore pris en compte");
        }

        // Next after "internal"/"external" balise
        XMLUtils.nextAndRemoveSpace(parser);

        while(parser.getEventType() != XmlPullParser.END_TAG ||
                !parser.getName().equalsIgnoreCase(typeEdge)) {


            if(parser.getEventType() == XmlPullParser.END_TAG &&
                parser.getName().equalsIgnoreCase(typeEdge)) {
                break;

            } else if(parser.getEventType() == XmlPullParser.START_TAG &&
                    parser.getName().equalsIgnoreCase("edge")) {

                // Init default values
                String begin = parser.getAttributeValue(null, "beg");
                String end = parser.getAttributeValue(null, "end");
                float weight = Float.parseFloat(parser.getAttributeValue(null, "weight"));

                Node nodeOne = getNode(begin);
                Node nodeTwo = null;

                /// IF external node
                if(typeEdge.equalsIgnoreCase("external")) {
                    String strPlan = parser.getAttributeValue(null, "plan");
                    Plan externalPlan = Graph.getPlan(strPlan);
                    if(externalPlan != null) {
                        nodeTwo = externalPlan.getNode(end);
                    }

                } else {
                    nodeTwo = getNode(end);
                }

                if(nodeOne != null && nodeTwo != null) {
                    new Path(nodeOne, nodeTwo, weight, true);
                    Log.d(getClass().getName(), "Création d'un lien entre: " + nodeOne.getName() +
                            " et " + nodeTwo.getName() + " (distance: " + weight + ")");

                } else {
                    Log.e(getClass().getName(), "Impossible de faire un lien entre les " +
                            "points: " + begin + " et " + end + " (au moins un des deux noeuds " +
                            "n'a pas été trouvé)");
                }
                parser.next(); // Next

            } else {
                Log.e(getClass().getName(), "Erreur dans le fichier (" + _name + "). Problème de "
                        + "chargement d'une edge " + typeEdge + ": " + parser.getName());
                XMLDebugParser(parser);
            }

            XMLUtils.nextAndRemoveSpace(parser);

        }
        parser.next(); // Next after the End_tag

    }


    /**
     * Print Debug message
     *
     * @param parser the XML File Iterator
     * @throws XmlPullParserException
     */
    private static void XMLDebugParser(XmlPullParser parser)
            throws XmlPullParserException {

        String type = XmlPullParser.TYPES[parser.getEventType()];
        Log.d(Plan.class.getName(), parser.getName() + " (type:" + type + " text:" +
                parser.getText() + ")");
    }



}
