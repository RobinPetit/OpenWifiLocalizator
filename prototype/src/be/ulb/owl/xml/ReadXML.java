/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ulb.owl.xml;

import java.io.File;
import java.io.IOException;
import java.util.List;
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
    
    
    /**
     * Load XML File
     * 
     * @param filename name of the file wich must be load
     */
    public static void loadXML(String filename) {
        loadXML(new File(filename));
    }
    
    /**
     * Load XML File
     * 
     * @param file file which must be load
     */
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
    
    private void loadXMLInfo() {
        List<Element> listBatiment = rootElem.getChildren("batiment");
        
        for(Element batiment : listBatiment) {
            // Ici
        }
        
//        On crée une List contenant tous les noeuds "etudiant" de l'Element racine
//        List listEtudiants = racine.getChildren("etudiant");
//
//        //On crée un Iterator sur notre liste
//        Iterator i = listEtudiants.iterator();
//        while(i.hasNext())
//        {
//           //On recrée l'Element courant à chaque tour de boucle afin de
//           //pouvoir utiliser les méthodes propres aux Element comme :
//           //sélectionner un nœud fils, modifier du texte, etc...
//           Element courant = (Element)i.next();
//           //On affiche le nom de l’élément courant
//           System.out.println(courant.getChild("nom").getText());
//        }
    }
    
    
}
