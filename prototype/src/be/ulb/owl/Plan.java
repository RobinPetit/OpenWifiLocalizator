/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ulb.owl;

import be.ulb.owl.xml.XMLUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author Detobel36
 */
public class Plan {
    
    private static ArrayList<Plan> _allPlan = new ArrayList<Plan>();
    
    
    private final String _name;
    private ArrayList<Node> _listNode;

    /**
     * Create a plan <b>and</b> load XML file from this plan
     *
     * @param name of the plan
     */
    public Plan(String name) {
        this(name, true);
    }

    /**
     * Create a plan
     *
     * @param name name of the plan
     * @param loadPlan True if we must load XML file from this plan
     */
    public Plan(String name, boolean loadPlan) {
        _listNode = new ArrayList<Node>();

        _name = name;
        if(loadPlan) {
            loadXMLPlan();
        }
        
        _allPlan.add(this);
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
     * Get a node from this plan from his name <b>or</b> his alias
     * 
     * @param name of the node
     * @return The node or null if not found
     */
    public Node getNode(String name) {
        return getNode(name, true);
    }
    
    /**
     * Get a node from this plan
     * 
     * @param name of the node
     * @param checkList check also in alias list
     * @return The node or null if not found
     */
    public Node getNode(String name, boolean checkList) {
        for(Node node : _listNode) {
            if(node.isNode(name, checkList)) {
                return node;
            }
        }
        
        return null;
    }
    
    
    
    /**
     * Search all node with a specific name or alias in this plan
     * 
     * @param name the name (or alias) of this nodes
     * @return the list of all node (or empty list if not found)
     */
    public ArrayList<Node> searchNodeInPlan(String name) {
        ArrayList<Node> res = new ArrayList<Node>();
        
        for(Node node : _listNode) {
            if(node.isNode(name)) {
                res.add(node);
            }
        }
        
        return res;
    }
    
    
    
    /**
     * Load XML from this plan
     */
    private void loadXMLPlan() {
        try {
            // Init the parser
            XmlPullParser parser = XMLUtils.readXMLFile(_name);
            
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

                        }
                        
                        XMLUtils.removeSpace(parser);
                        
                    }
                    
                }
                
                
            }
            
        } catch (IOException | XmlPullParserException ex) {
            Logger.getLogger(Plan.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
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
                System.err.println("Erreur dans le fichier. Problème de "
                        + "chargement de la node:");
                XMLDebugParser(parser);
                XMLUtils.nextAndRemoveSpace(parser);
            }
            
            XMLUtils.removeSpace(parser);
            XMLDebugParser(parser);
        }
        parser.next(); // Next after the End_tag
    }
    
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

                    case "alias":
                        parser.next(); // START_TAG
                        if(parser.getEventType() == XmlPullParser.TEXT) {
                            listAlias.add(parser.getText());
                        } else {
                            System.err.println("Erreur avec le "
                                    + "fichier à paser ! pour le plan: " +
                                    _name);
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
            _listNode.add(new Node(this, x, y, pointId, listWifi));
        } else {
            System.err.println("Impossible de créer la node: " + pointId + 
                    " (il manque des informations)");
        }
        
    }
    
    
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
                int max = Integer.parseInt(parser.getAttributeValue(null, "max"));
                int min = Integer.parseInt(parser.getAttributeValue(null, "min"));
                int avg = Integer.parseInt(parser.getAttributeValue(null, "avg"));
                
                listWifi.add(new Wifi(bss, max, min, avg));
                System.out.println("Création d'un wifi: " + bss + 
                        " min: " + min + " max: "+ max + " avg: " + avg);
            }
            
            XMLUtils.nextAndRemoveSpace(parser);
        }
        parser.next(); // Next after the End_tag
        
        return listWifi;
    }
    
    
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
                System.out.println("External");
                
            } else {
                System.err.println("Erreur dans le fichier. Problème de "
                        + "chargement de l'edge:");
                XMLDebugParser(parser);
                XMLUtils.nextAndRemoveSpace(parser);
            }
            
            XMLUtils.removeSpace(parser);
            XMLDebugParser(parser);
        }
        parser.next(); // Next after the End_tag
        
    }
    
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
                
                Node nodeOne = getNode(begin, false);
                Node nodeTwo = null;
                
                /// IF external node
                if(typeEdge.equalsIgnoreCase("external")) {
                    String strPlan = parser.getAttributeValue(null, "plan");
                    Plan externalPlan = Plan.getPlan(strPlan);
                    if(externalPlan != null) {
                        nodeTwo = externalPlan.getNode(end, false);
                    }
                    
                } else {
                    nodeTwo = getNode(end, false);
                }
                
                if(nodeOne != null && nodeTwo != null) {
                    new Path(nodeOne, nodeTwo, weight, true);
                    System.out.println("Création d'un lien entre: " + nodeOne.getName() + 
                            " et " + nodeTwo.getName() + " (distance: " + weight + ")");
                    
                } else {
                    System.err.println("Impossible de faire un lien entre les " +
                            "points: " + begin + " et " + end + 
                            " (au moins un des deux noeuds n'a pas été trouvé)");
                }
                parser.next(); // Next 
                
            } else {
                System.err.println("Erreur dans le fichier. Problème de "
                        + "chargement d'une edge " + typeEdge + ": ");
                XMLDebugParser(parser);
            }
            
            XMLUtils.nextAndRemoveSpace(parser);
            
        }
        parser.next(); // Next after the End_tag
        
    }
    
    
    private static void XMLDebugParser(XmlPullParser parser) 
            throws XmlPullParserException {
        
        String type = ""+parser.getEventType();
        
        switch(parser.getEventType()) {
            
            case XmlPullParser.START_TAG:
                type = "START_TAG";
                break;
                
            case XmlPullParser.END_TAG:
                type = "END_TAG";
                break;
                
            case XmlPullParser.TEXT:
                type = "TEXT";
                break;
                
            case XmlPullParser.END_DOCUMENT:
                type = "END_DOCUMENT";
                break;
                
        }
        System.out.println(parser.getName() + " (type:" + type + 
                " text:" + parser.getText() + ")");
    }
    
    
    
    /////////////////////////// STATIC ///////////////////////////
    
    /**
     * Search all node with a specific name or alias
     * 
     * @param name the name (or alias) of this nodes
     * @return 
     */
    public static ArrayList<Node> searchNode(String name) {
        ArrayList<Node> listeNode = new ArrayList<Node>();
        for(Plan plan : _allPlan) {
            listeNode.addAll(plan.searchNodeInPlan(name));
        }
        return listeNode;
    }
    
    /**
     * Get a specific plan or <b>create</b> if not exist
     * 
     * @param name the name of the specific plan
     * @return The plan (or null if not found)
     */
    public static Plan getPlan(String name) {
        return getPlan(name, true);
    }
    
    /**
     * Get a specific plan or <b>create</b> if not exist
     * 
     * @param name the name of the specific plan
     * @param loadIfNotExist try to load if the plan is not found
     * @return The plan (or null if not found)
     */
    public static Plan getPlan(String name, boolean loadIfNotExist) {
        Plan resPlan = null;
        for(Plan plan : _allPlan) {
            if(plan.isName(name)) {
                return plan;
            }
        }
        
        if(resPlan == null && loadIfNotExist) {
            resPlan = new Plan(name);
        }
        
        return resPlan;
    }

}
