/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package be.ulb.owl;

import java.util.ArrayList;

/**
 * Represent all Plan<br/>
 * Must be initialised only <b>once</b>
 * 
 * @author Detobel36
 */
public class Graph {
    
    private static ArrayList<Plan> _allPlan;
    private Scanner _scanner;
    
    
    public Graph () {
        _allPlan = new ArrayList<Plan>();
        loadAllPlan();
        
        _scanner = new Scanner();
    }
    
    
    
    public ArrayList<Path> bestPath(Node nodeFrom, Node nodeTo) {
        // TODO
        return new ArrayList<Path>();
    }
    
    
    public void whereAmI () {
        // @TODO Ajouter dans le xml des wifi -> plan un ensemble contenant les 
        // cas particulier ou un wifi ne serait présent que dans un seul plan.
    }
    
    
    
    
    
    
    /////////////////////////// STATIC ///////////////////////////
    
    /**
     * Load all plan in the default folder
     */
    private static void loadAllPlan() {
        // TODO get all plan in the folder
//        for(String plan : ) {
//            getPlan(plan);
//        }
    }
    
    
    /**
     * Search all node which contain a specific alias (no search in name)
     * 
     * @param name the name (alias) of this nodes
     * @return an ArrayList of Node
     */
    public static ArrayList<Node> searchNode(String name) {
        ArrayList<Node> listeNode = new ArrayList<Node>();
        for(Plan plan : _allPlan) {
            listeNode.addAll(plan.searchNode(name));
        }
        return listeNode;
    }
    
    
    /**
     * Get a specific plan or <b>create</b> if not exist
     * 
     * @param name the name of the specific plan
     * @return The plan (or null if not found)
     */
    public static Plan getPlan(String name) {
        return getPlan(name, true);
    }
    
    
    /**
     * Get a specific plan or <b>create</b> if not exist
     * 
     * @param name the name of the specific plan
     * @param loadIfNotExist try to load if the plan is not found
     * @return The plan (or null if not found)
     */
    public static Plan getPlan(String name, boolean loadIfNotExist) {
        Plan resPlan = null;
        for(Plan plan : _allPlan) {
            if(plan.isName(name)) {
                return plan;
            }
        }
        
        if(resPlan == null && loadIfNotExist) {
            resPlan = new Plan(name);
            _allPlan.add(resPlan);
        }
        
        return resPlan;
    }

    
    
}
