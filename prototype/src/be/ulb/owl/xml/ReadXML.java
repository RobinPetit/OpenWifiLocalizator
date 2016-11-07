/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ulb.owl.xml;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;


/**
 *
 * @author Detobel36
 */
public class ReadXML {
    
    private static Element rootElem;
    
    
    public static void loadXML(String filename) {
        loadXML(new File(filename));
    }
    
    public static void loadXML(File file) {
        SAXBuilder sxb = new SAXBuilder();
        
        Document document = null;
        try {
            document = sxb.build(file);
        } catch (JDOMException | IOException ex) {
            Logger.getLogger(ReadXML.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(document != null) {
            rootElem = document.getRootElement();
        }
    }
    
    
    
    
}
