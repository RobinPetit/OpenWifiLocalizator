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
                debugParser(parser);
                
                if(parser.getEventType() == XmlPullParser.START_TAG &&
                        parser.getName().equalsIgnoreCase("plan")) {
                    
                    XMLUtils.nextAndRemoveSpace(parser);
                    
                    while(parser.getEventType() != XmlPullParser.END_TAG ||
                            !parser.getName().equalsIgnoreCase("plan")) {
                        
                        switch(parser.getName()) {

                            case "nodes":
                                loadNodes(parser);
                                break;
                                
                            case "edges":
                                loadEdges(parser);
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
    
    private void loadNodes(XmlPullParser parser) throws XmlPullParserException, IOException {
        // Next after "nodeS" balise
        XMLUtils.nextAndRemoveSpace(parser);
        
        while(parser.getEventType() != XmlPullParser.END_TAG || 
                !parser.getName().equalsIgnoreCase("nodes")) {
            
            if(parser.getEventType() == XmlPullParser.END_TAG && 
                parser.getName().equalsIgnoreCase("nodes")) {
                break;
                
            } else if(parser.getEventType() == XmlPullParser.START_TAG &&
                    parser.getName().equalsIgnoreCase("node")) {
                
                loadOneNode(parser);
                
            } else {
                System.err.println("Erreur dans le fichier. Problème de "
                        + "chargement de la node:");
                debugParser(parser);
                XMLUtils.nextAndRemoveSpace(parser);
            }
            
            XMLUtils.removeSpace(parser);
            debugParser(parser);
        }
        parser.next(); // Next after the End_tag
    }
    
    private void loadOneNode(XmlPullParser parser) throws XmlPullParserException, IOException {
        
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
                        listWifi = getListWifi(parser);
                        break;

                    default:
                        break;

                }
            }
            
            XMLUtils.nextAndRemoveSpace(parser);
        }
        parser.next(); // Remove End_tag
        
    }
    
    
    private ArrayList<Wifi> getListWifi(XmlPullParser parser) throws XmlPullParserException, IOException {
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
    
    
    private void loadEdges(XmlPullParser parser) throws XmlPullParserException, IOException {
        // Next after "edgesS" balise
        XMLUtils.nextAndRemoveSpace(parser);
        
        while(parser.getEventType() != XmlPullParser.END_TAG ||
                !parser.getName().equalsIgnoreCase("edges")) {
            
            
            if(parser.getEventType() == XmlPullParser.END_TAG && 
                parser.getName().equalsIgnoreCase("edges")) {
                break;
                
            } else if(parser.getEventType() == XmlPullParser.START_TAG &&
                    parser.getName().equalsIgnoreCase("internal")) {
                
                loadInternalEdge(parser);
            
            } else if(parser.getEventType() == XmlPullParser.START_TAG &&
                    parser.getName().equalsIgnoreCase("external")) {
                
                // TODO
//                loadExternalEdge(parser);
                
            } else {
                System.err.println("Erreur dans le fichier. Problème de "
                        + "chargement de l'edge:");
                debugParser(parser);
                XMLUtils.nextAndRemoveSpace(parser);
            }
            
            XMLUtils.removeSpace(parser);
            debugParser(parser);
        }
        parser.next(); // Next after the End_tag
        
    }
    
    private void loadInternalEdge(XmlPullParser parser) throws XmlPullParserException, IOException {
        // Next after "internal" balise
        XMLUtils.nextAndRemoveSpace(parser);
        
        while(parser.getEventType() != XmlPullParser.END_TAG ||
                !parser.getName().equalsIgnoreCase("internal")) {
            
            
            if(parser.getEventType() == XmlPullParser.END_TAG && 
                parser.getName().equalsIgnoreCase("internal")) {
                break;
                
            } else if(parser.getEventType() == XmlPullParser.START_TAG &&
                    parser.getName().equalsIgnoreCase("edge")) {
                
                // TODO
//                loadOneInternalEdge(parser);
                
            } else {
                System.err.println("Erreur dans le fichier. Problème de "
                        + "chargement d'une edge interne:");
                debugParser(parser);
                XMLUtils.nextAndRemoveSpace(parser);
            }
            
            XMLUtils.removeSpace(parser);
            debugParser(parser);
        }
        parser.next(); // Next after the End_tag
        
    }
    
    
    
    private static void debugParser(XmlPullParser parser) throws XmlPullParserException {
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
    
    
    
    
    
    
    
    
    
//    
//    /**
//     * Load XML from this plan
//     */
//    private void loadXMLPlan() {
//        try {
//            // Init the parser
//            XmlPullParser parser = XMLUtils.readXMLFile(_name);
//            
//            // While the
//            while(parser.next() != XmlPullParser.END_DOCUMENT) {
//                
//                // First section (normaly only one of this)
//                if(parser.getEventType() == XmlPullParser.START_TAG && 
//                        parser.getName().equalsIgnoreCase("plan")) {
//                    
//                    String xmlName = parser.getAttributeValue(null, "name");
//                    // TODO voir si c'est utile de check que cette valeur égal _name
//                    
//                    parser.next(); // pass the "TEXT" section
//                    
//                    // For all point
//                    while(parser.getEventType() != XmlPullParser.END_TAG ||
//                            !parser.getName().equalsIgnoreCase("plan")) {
//                        
//                        // Ignore if it's a space
//                        if(XMLUtils.isSpace(parser)) {
//                            parser.next();
//                            continue;
//                        }
//                        
//                        
//                        while(parser.getEventType() == XmlPullParser.START_TAG) {
//                            
//                            if(XMLUtils.isSpace(parser)) {
//                                parser.next();
//                                continue;
//                            }
//                            
//                            debugParser(parser);
//                            
//                            switch(parser.getName()) {
//                                case "nodes":
//                                    parser.next();
//                                    loadXMLNodes(parser);
//                                    break;
//                                    
//                                case "edges":
//                                    // TODO
//                                    loadXMLEdges(parser);
////                                    parser.next();
//                                    break;
//                                    
//                                default:
//                                    System.err.println("Obj inconnu (type: " +
//                                            parser.getEventType() + "): " + 
//                                            parser.getName() + " -> " + parser.getText());
//                                    parser.next();
//                                    break;
//                                    
//                            }
//                            
//                        }
//                        
//                        parser.next();
//                    }
//                    
//                } else {
//                    System.err.println("Pas un plan " + parser.getName() + " -> " + parser.getText());
//                    parser.next();
//                }
//                
//                
//            }
//            
//        } catch (IOException | XmlPullParserException ex) {
//            Logger.getLogger(Plan.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        
//    }
//    
//    private static void debugParser(XmlPullParser parser) throws XmlPullParserException {
//        String type = ""+parser.getEventType();
//        
//        switch(parser.getEventType()) {
//            
//            case XmlPullParser.START_TAG:
//                type = "START_TAG";
//                break;
//                
//            case XmlPullParser.END_TAG:
//                type = "END_TAG";
//                break;
//                
//            case XmlPullParser.TEXT:
//                type = "TEXT";
//                break;
//                
//            case XmlPullParser.END_DOCUMENT:
//                type = "END_DOCUMENT";
//                break;
//                
//        }
//        System.out.println(parser.getName() + " (type:" + type + 
//                " text:" + parser.getText() + ")");
//    }
//    
//    
//    private void loadXMLNodes(XmlPullParser parser) throws XmlPullParserException, IOException {
//        while(XMLUtils.isSpace(parser)) {
//            parser.next();
//        }
//        
//        while((parser.getEventType() == XmlPullParser.START_TAG &&
//                parser.getName().equalsIgnoreCase("node")) ||
//                XMLUtils.isSpace(parser)) {
//
//            while(XMLUtils.isSpace(parser)) {
//                parser.next();
//            }
//            _listNode.add(getNode(parser));
//            
//        }
//        System.out.println("End Load Node");
//        debugParser(parser);
//        
//    }
//    
//    private void loadXMLEdges(XmlPullParser parser) throws XmlPullParserException, IOException {
//        parser.next();
//    }
//    
//    
//    private Node getNode(XmlPullParser parser) 
//            throws XmlPullParserException, IOException {
//        
//        // Init default values
//        ArrayList<String> listAlias = new ArrayList<String>();
//        ArrayList<Wifi> listWifi = null;
//        float x = Integer.MIN_VALUE;
//        float y = Integer.MIN_VALUE;
//        String pointId = parser.getAttributeValue(null, "id");
//
//        // For all information of a point
//        while(parser.getEventType() == XmlPullParser.START_TAG ||
//                XMLUtils.isSpace(parser)) {
//
//            if(XMLUtils.isSpace(parser)) {
//                parser.next();
//                continue;
//            }
//
//            switch(parser.getName()) {
//
//                case "alias":
//                    parser.next();
//                    if(parser.getEventType() == XmlPullParser.TEXT) {
//                        listAlias.add(parser.getText());
//                    } else {
//                        System.err.println("Erreur avec le "
//                                + "fichier à paser ! pour le plan: " + 
//                                _name);
//                    }
//                    // Actualy we are on a TEXT type
//                    parser.next(); // We pass it and we are on a End
//                    break;
//
//                case "coord":
//                    x = Float.parseFloat(parser.getAttributeValue(null, "x"));
//                    y = Float.parseFloat(parser.getAttributeValue(null, "y"));
//                    parser.next();
//                    break;
//
//                case "listWifi":
//                    parser.next(); // TEXT null
//                    parser.next(); // Start
//                    listWifi = getListWifi(parser);
//                    break;
//
//                default:
//                    break;
//
//            }
//
//            parser.next();
//        }
//        
//        System.out.println("Création du point "+ pointId + " (" + x + 
//                ", " + y + ") alias: " + listAlias.toString());
//        return new Node(this, x, y, pointId, listWifi);
//    }
//    
//    
//    private ArrayList<Wifi> getListWifi(XmlPullParser parser) throws XmlPullParserException, IOException {
//        ArrayList<Wifi> listWifi = new ArrayList<Wifi>();
//        while((parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equalsIgnoreCase("wifi")) 
//                || XMLUtils.isSpace(parser)) {
//            
//            if(XMLUtils.isSpace(parser)) {
//                parser.next();
//                continue;
//            }
//            
//            String bss = parser.getAttributeValue(null, "BSS");
//            int max = Integer.parseInt(parser.getAttributeValue(null, "max"));
//            int min = Integer.parseInt(parser.getAttributeValue(null, "min"));
//            int avg = Integer.parseInt(parser.getAttributeValue(null, "avg"));
//            parser.next();
//            parser.next();
//            
//            System.out.println("Création d'un wifi: " + bss + " min: " + min + " max: "+ max + " avg: " + avg);
//            listWifi.add(new Wifi(bss, max, min, avg));
//        }
//        
//        return listWifi;
//    }
//    
//    
}
