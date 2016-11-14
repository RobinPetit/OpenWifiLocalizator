/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ulb.owl;

import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author Detobel36
 */
public class Map {
    
    private final String _name;
    private ArrayList<Node> _listNode;
    
    /**
     * Create a map <b>and</b> load XML file from this map
     * 
     * @param name of the map
     */
    public Map(String name) {
        this(name, true);
    }
    
    /**
     * Create a map
     * 
     * @param name name of the map
     * @param loadMap True if we must load XML file from this map
     */
    public Map(String name, boolean loadMap) {
        _listNode = new ArrayList<Node>();
        
        _name = name;
        if(loadMap) {
            loadXMLMap();
        }
    }
    
    /**
     * Load XML from this map
     */
    private void loadXMLMap() {
        // new File("XML maps" + File.separator + _name);
        // @TODO
    }
    
    
}
