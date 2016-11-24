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
            XmlPullParser parser = XMLUtils.readXMLFile(_name);
            
            
//            while(parser.next() != XmlPullParser.END_DOCUMENT) {
//                System.out.println("\n------\nXML: EventType: " + parser.getEventType() + 
//                        " \nTagName: " + parser.getName() + 
//                        "\nText: " + parser.getText() + 
//                        "\nNbrAttr: " + parser.getAttributeCount());
//            }
            
            while(parser.next() != XmlPullParser.END_DOCUMENT) {
                
                if(parser.getEventType() == XmlPullParser.START_TAG && 
                        parser.getName().equalsIgnoreCase("plan")) {
                    
                    String xmlName = parser.getAttributeValue(null, "name");
                    // TODO voir si c'est utile de check que cette valeur
                    // égal _name
                    parser.next(); // pass the "TEXT" section
                    
                    // For all point
                    while(parser.getEventType() != XmlPullParser.END_TAG ||
                            !parser.getName().equalsIgnoreCase("plan")) {
                        
                        if(parser.getEventType() == XmlPullParser.TEXT && 
                                parser.getName() == null) {
                            parser.next();
                            continue;
                        }
                        
                        System.out.println("\n------\nXML: EventType: " + parser.getEventType() + 
                        " \nTagName: " + parser.getName() + 
                        "\nText: " + parser.getText() + 
                        "\nNbrAttr: " + parser.getAttributeCount());
                        
                        if(parser.getEventType() == XmlPullParser.START_TAG &&
                                parser.getName().equalsIgnoreCase("point")) {
                            
                            ArrayList<String> listAlias = new ArrayList<String>();
                            int x = Integer.MIN_VALUE;
                            int y = Integer.MIN_VALUE;
                            int pointId = Integer.parseInt(parser.getAttributeValue(null, "id"));
                      
                            while(parser.getEventType() == XmlPullParser.START_TAG ||
                                    parser.getName() == null) {
                                
                                
                                
                                if(parser.getName() == null) {
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
                                        parser.next(); // We pass the End type :)
                                        break;
                                    
                                    case "coord":
                                        x = Integer.parseInt(parser.getAttributeValue(null, "x"));
                                        y = Integer.parseInt(parser.getAttributeValue(null, "y"));
                                        parser.next();
                                        parser.next();
                                        break;
                                    
                                    case "listWifi":
                                        parser.next();
                                        break;
                                        
                                    default:
                                        parser.next();
                                        break;
                                        
                                }
                            }
                            
                            System.out.println("Création du point "+ pointId + " (" + x + 
                                    ", " + y + ") alias: " + listAlias.toString());
                            
     /* <alias>Super local</alias>
        <coord x="1" y="2" />
        <listWifi>
            <wifi BSS="00:26:cb:4e:0d:63" max="-68" min="-58" avg="-63" />
            <wifi BSS="00:26:cb:4e:0c:64" max="-77" min="-66" avg="-72" />
        </listWifi>*/
                            
                            
                        } else {
                            System.out.println("Pas un point " + parser.getName() + " -> " + parser.getText());
                            parser.next();
                        }
                        
                        
                    }
                    
                }
                
                
            }
            
            
            
//            
//            int nodeId = -1;
//            
//            int eventType = parser.getEventType();
//            while (eventType != XmlPullParser.END_DOCUMENT) {
//                String tagname = parser.getName();
//                switch (eventType) {
//                    case XmlPullParser.START_TAG:
//                        
//                        switch(tagname) {
//                            case "plan":
//                                String nom = parser.getAttributeValue(null, "name");
//
//                                // If the name of the plan is not the same
//                                if(!nom.equalsIgnoreCase(_name)) {
//                                    throw new IllegalArgumentException("XMLFile have not "
//                                            + "the same name that the current object");
//                                }
//                                break;
//                            
//                            case "point":
//                                String id = parser.getAttributeValue(null, "id");
//                                nodeId = Integer.parseInt(id);
//                                break;
//                            
//                        }
//                        break;
//                        
//                    case XmlPullParser.TEXT:
//                        text = parser.getText();
//                        break;
//                        
//                    case XmlPullParser.END_TAG:
//                        if (tagname.equalsIgnoreCase("employee")) {
//                            // add employee object to list
//                            employees.add(employee);
//                        } else if (tagname.equalsIgnoreCase("name")) {
//                            employee.setName(text);
//                        } else if (tagname.equalsIgnoreCase("id")) {
//                            employee.setId(Integer.parseInt(text));
//                        } else if (tagname.equalsIgnoreCase("department")) {
//                            employee.setDepartment(text);
//                        } else if (tagname.equalsIgnoreCase("email")) {
//                            employee.setEmail(text);
//                        } else if (tagname.equalsIgnoreCase("type")) {
//                            employee.setType(text);
//                        }
//                        break;
//                        
//                    default:
//                        break;
//                }
//                eventType = parser.next();
//            }
//            
//            
//            
//            
//            ArrayList entries = new ArrayList();
//            
//            String ns = null;
//            parser.require(XmlPullParser.START_TAG, ns, "feed");
//            while (parser.next() != XmlPullParser.END_TAG) {
//                if (parser.getEventType() != XmlPullParser.START_TAG) {
//                    continue;
//                }
//                String name = parser.getName();
//                // TODO
//                // Starts by looking for the entry tag
//                if (name.equals("entry")) {
//                    // Read here
//                    //entries.add(readEntry(parser));
//                } else {
//                    XMLUtils.skip(parser);
//                }
//            }
            
        } catch (IOException | XmlPullParserException ex) {
            Logger.getLogger(Plan.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        /*
        XmlPullParserFactory factory = null;
        XmlPullParser parser = null;
        try {
        factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        parser = factory.newPullParser();
        
        parser.setInput(is, null);
        
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
        String tagname = parser.getName();
        switch (eventType) {
        case XmlPullParser.START_TAG:
        if (tagname.equalsIgnoreCase("employee")) {
        // create a new instance of employee
        employee = new Employee();
        }
        break;
        
        case XmlPullParser.TEXT:
        text = parser.getText();
        break;
        
        case XmlPullParser.END_TAG:
        if (tagname.equalsIgnoreCase("employee")) {
        // add employee object to list
        employees.add(employee);
        } else if (tagname.equalsIgnoreCase("name")) {
        employee.setName(text);
        } else if (tagname.equalsIgnoreCase("id")) {
        employee.setId(Integer.parseInt(text));
        } else if (tagname.equalsIgnoreCase("department")) {
        employee.setDepartment(text);
        } else if (tagname.equalsIgnoreCase("email")) {
        employee.setEmail(text);
        } else if (tagname.equalsIgnoreCase("type")) {
        employee.setType(text);
        }
        break;
        
        default:
        break;
        }
        eventType = parser.next();
        }
        
        } catch (XmlPullParserException e) {
        e.printStackTrace();
        } catch (IOException e) {
        e.printStackTrace();
        }
        
        return employees;
        }
        */
        
        
    }
    
}
