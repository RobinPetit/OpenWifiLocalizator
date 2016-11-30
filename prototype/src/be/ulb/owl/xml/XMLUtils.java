/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ulb.owl.xml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;


/**
 * Example code... Need to be dev :P
 * 
 * @author Detobel36
 */
public class XMLUtils {
    
    private static final String ns = null;
    
    
    /**
     * Read XML File and return a XMLPullParser
     * 
     * @param name of the file who must be read
     * @return XmlPullParser
     */
    public static XmlPullParser readXMLFile(String name) {
        XmlPullParser parser = null;
        try {
            InputStream url = XMLUtils.class.getResourceAsStream("/"+name+".xml");
            Reader XMLFile = new BufferedReader(new InputStreamReader(url));
            //Reader XMLFile = new FileReader(name);
            XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
            parser = parserFactory.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(XMLFile);
            //parser.nextToken();
        } catch (XmlPullParserException ex) {
            Logger.getLogger(XMLUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return parser;
    }
    
    
    public static boolean isSpace(XmlPullParser parser) throws XmlPullParserException {
        return parser.getEventType() == XmlPullParser.TEXT && 
                (parser.getName() == null || parser.getName().equalsIgnoreCase(""));
    }
    
    public static void removeSpace(XmlPullParser parser) throws XmlPullParserException, IOException {
        while(isSpace(parser)) {
            parser.next();
        }
    }
    
    public static void nextAndRemoveSpace(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.next();
        removeSpace(parser);
    }
    
    
//    public List parse(Reader in) throws XmlPullParserException, IOException {
//        try {
//            XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
//            XmlPullParser parser = parserFactory.newPullParser();
//            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
//            parser.setInput(in);
//            parser.nextToken();
//            return readFeed(parser);
//        } finally {
//            in.close();
//        }
//    }
    
//    private List readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
//        List entries = new ArrayList();
//        
//        parser.require(XmlPullParser.START_TAG, ns, "feed");
//        while (parser.next() != XmlPullParser.END_TAG) {
//            if (parser.getEventType() != XmlPullParser.START_TAG) {
//                continue;
//            }
//            String name = parser.getName();
//            // Starts by looking for the entry tag
//            if (name.equals("entry")) {
//                // Read here
//                //entries.add(readEntry(parser));
//            } else {
//                skip(parser);
//            }
//        }
//        return entries;
//    }
//    
//    public static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
//        if (parser.getEventType() != XmlPullParser.START_TAG) {
//            throw new IllegalStateException();
//        }
//        int depth = 1;
//        while (depth != 0) {
//            switch (parser.next()) {
//                case XmlPullParser.END_TAG:
//                    depth--;
//                    break;
//                case XmlPullParser.START_TAG:
//                    depth++;
//                    break;
//            }
//        }
//    }
    
}
