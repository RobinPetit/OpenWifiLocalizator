/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package be.ulb.owl;

import java.util.ArrayList;

/**
 *
 * @author Detobel36
 */
public class Graph {
    
    private Scanner _scanner;
    private ArrayList<Map> _maps;
    
    
    public Graph () {
        _scanner = new Scanner();
        _maps  = new ArrayList<Map>();
    }
    
    public ArrayList<Path> bestPath(Node nodeFrom, Node nodeTo) {
        // TODO
        return new ArrayList<Path>();
    }
    
    
    public void whereAmI () {
        // @TODO Ajouter dans le xml des wifi -> plan un ensemble contenant les 
        // cas particulier ou un wifi ne serait présent que dans un seul plan.
    }
    
}
