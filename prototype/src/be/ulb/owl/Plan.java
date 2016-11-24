/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ulb.owl;

import be.ulb.owl.xml.XMLUtils;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 *
 * @author Detobel36
 */
public class Plan {

    private final String _name;
    private ArrayList<Node> _listNode;

    /**
     * Create a map <b>and</b> load XML file from this map
     *
     * @param name of the map
     */
    public Plan(String name) {
        this(name, true);
    }

    /**
     * Create a map
     *
     * @param name name of the map
     * @param loadPlan True if we must load XML file from this map
     */
    public Plan(String name, boolean loadPlan) {
        _listNode = new ArrayList<Node>();

        _name = name;
        if(loadPlan) {
            loadXMLPlan();
        }
    }

    /**
     * Load XML from this map
     */
    private void loadXMLPlan() {
        try {
            FileReader XMLFile = new FileReader(_name);
            XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(XMLFile);
            parser.nextToken();

            ArrayList entries = new ArrayList();

            String ns = null;
            parser.require(XmlPullParser.START_TAG, ns, "feed");
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                // TODO
                // Starts by looking for the entry tag
                if (name.equals("entry")) {
                    // Read here
                    //entries.add(readEntry(parser));
                } else {
                    XMLUtils.skip(parser);
                }
            }

        } catch (IOException | XmlPullParserException ex) {
            Logger.getLogger(Plan.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


}
