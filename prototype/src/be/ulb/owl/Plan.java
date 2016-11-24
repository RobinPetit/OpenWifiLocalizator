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
                
                // First section (normaly only one of this)
                if(parser.getEventType() == XmlPullParser.START_TAG && 
                        parser.getName().equalsIgnoreCase("plan")) {
                    
                    String xmlName = parser.getAttributeValue(null, "name");
                    // TODO voir si c'est utile de check que cette valeur égal _name
                    
                    parser.next(); // pass the "TEXT" section
                    
                    // For all point
                    while(parser.getEventType() != XmlPullParser.END_TAG ||
                            !parser.getName().equalsIgnoreCase("plan")) {
                        
                        // Ignore if it's a space
                        if(XMLUtils.isSpace(parser)) {
                            parser.next();
                            continue;
                        }
                        
                        while(parser.getEventType() == XmlPullParser.START_TAG) {
                            
                            if(XMLUtils.isSpace(parser)) {
                                parser.next();
                                continue;
                            }
                            
                            switch(parser.getName()) {
                                case "point":
                                    _listNode.add(getNode(parser));
                                    break;
                                    
                                case "edge":
                                    // TODO
                                    parser.next();
                                    break;
                                    
                                default:
                                    System.err.println("Obj inconnu: " + 
                                            parser.getName() + " -> " + parser.getText());
                                    parser.next();
                                    break;
                                    
                            }
                            
                            
                        }
                        
                        parser.next();
                    }
                    
                } else {
                    System.err.println("Pas un plan " + parser.getName() + " -> " + parser.getText());
                    parser.next();
                }
                
                
            }
            
        } catch (IOException | XmlPullParserException ex) {
            Logger.getLogger(Plan.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    
    private Node getNode(XmlPullParser parser) 
            throws XmlPullParserException, IOException {
        
        // Init default values
        ArrayList<String> listAlias = new ArrayList<String>();
        ArrayList<Wifi> listWifi = null;
        float x = Integer.MIN_VALUE;
        float y = Integer.MIN_VALUE;
        String pointId = parser.getAttributeValue(null, "id");

        // For all information of a point
        while(parser.getEventType() == XmlPullParser.START_TAG ||
                XMLUtils.isSpace(parser)) {

            if(XMLUtils.isSpace(parser)) {
                parser.next();
                continue;
            }

            switch(parser.getName()) {

                case "alias":
                    parser.next();
                    if(parser.getEventType() == XmlPullParser.TEXT) {
                        listAlias.add(parser.getText());
                    } else {
                        System.err.println("Erreur avec le "
                                + "fichier à paser ! pour le plan: " + 
                                _name);
                    }
                    // Actualy we are on a TEXT type
                    parser.next(); // We pass it and we are on a End
                    break;

                case "coord":
                    x = Float.parseFloat(parser.getAttributeValue(null, "x"));
                    y = Float.parseFloat(parser.getAttributeValue(null, "y"));
                    parser.next();
                    break;

                case "listWifi":
                    parser.next(); // TEXT null
                    parser.next(); // Start
                    listWifi = getListWifi(parser);
                    break;

                default:
                    break;

            }

            parser.next();
        }
        
        System.out.println("Création du point "+ pointId + " (" + x + 
                ", " + y + ") alias: " + listAlias.toString());
        return new Node(this, x, y, pointId, listWifi);
    }
    
    
    private ArrayList<Wifi> getListWifi(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<Wifi> listWifi = new ArrayList<Wifi>();
        while((parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equalsIgnoreCase("wifi")) 
                || XMLUtils.isSpace(parser)) {
            
            if(XMLUtils.isSpace(parser)) {
                parser.next();
                continue;
            }
            
            String bss = parser.getAttributeValue(null, "BSS");
            int max = Integer.parseInt(parser.getAttributeValue(null, "max"));
            int min = Integer.parseInt(parser.getAttributeValue(null, "min"));
            int avg = Integer.parseInt(parser.getAttributeValue(null, "avg"));
            parser.next();
            parser.next();
            
            System.out.println("Création d'un wifi: " + bss + " min: " + min + " max: "+ max + " avg: " + avg);
            listWifi.add(new Wifi(bss, max, min, avg));
        }
        
        return listWifi;
    }
    
    
}
