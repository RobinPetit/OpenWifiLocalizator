/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ulb.owl.utils;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import be.ulb.owl.MainActivity;


/**
 * Tool to manage XML
 * 
 * @author Detobel36
 */
public class XMLUtils {
    
    private static final String ns = null;
    
    
    /**
     * Read XML File and return a XMLPullParser
     * 
     * @param name of the file who must be read (with extention !)
     * @return XmlPullParser
     */
    public static XmlPullParser readXMLFile(String name) throws IOException, XmlPullParserException {
        XmlPullParser parser = null;
        InputStream url = MainActivity.getInstance().getAssets().open("XMLMap"+ File.separator + name + ".xml");
        // InputStream url = XMLUtils.class.getResourceAsStream("/"+name+".xml");
        Reader XMLFile = new BufferedReader(new InputStreamReader(url));
        //Reader XMLFile = new FileReader(name);
        XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
        parser = parserFactory.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(XMLFile);
        //parser.nextToken();

        return parser;
    }

    /**
     * Check if the current XMLPullParser (iterator) is a space or not
     *
     * @param parser the parser which must be tested
     * @return True if it is a space
     * @throws XmlPullParserException
     */
    public static boolean isSpace(XmlPullParser parser) throws XmlPullParserException {
        return parser.getEventType() == XmlPullParser.TEXT && 
                (parser.getName() == null || parser.getName().equalsIgnoreCase(""));
    }

    /**
     * Remove all space from the current iterator to the next data
     *
     * @param parser the parser which must be tested
     * @throws XmlPullParserException
     * @throws IOException
     */
    public static void removeSpace(XmlPullParser parser) 
            throws XmlPullParserException, IOException {
        
        while(isSpace(parser)) {
            parser.next();
        }
    }

    /**
     * Get the next real element of the parser.  Next element of the XMLPullParser and remove all
     * space after this next element
     *
     * @param parser the parser which must be tested
     * @throws XmlPullParserException
     * @throws IOException
     */
    public static void nextAndRemoveSpace(XmlPullParser parser) 
            throws XmlPullParserException, IOException {
        
        parser.next();
        removeSpace(parser);
    }
    
}
