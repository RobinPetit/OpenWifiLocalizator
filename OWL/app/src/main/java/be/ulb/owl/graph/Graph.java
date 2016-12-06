/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package be.ulb.owl.graph;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import be.ulb.owl.Scanner;
import be.ulb.owl.Wifi;

/**
 * Represent all Plan<br/>
 * Must be initialised only <b>once</b>
 * 
 * @author Detobel36
 */
public class Graph {

    private static final String IGNOREPLAN = "Example";
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
    
    public Node whereAmI () {
        ArrayList<Wifi> capted = _scanner.scan();
        return whereAmI(capted);
    }
    
    public Node whereAmI (ArrayList<Wifi> capted) {
        ArrayList<String> captedStr = new ArrayList<String>();
        for (Wifi wifi : capted) {
            captedStr.add(wifi.getBSS());
        }
        ArrayList<Plan> res = new ArrayList<Plan>();
        int biggestSetSize = 0;
        for (Plan plan : _allPlan) {
            ArrayList<String> tmp = plan.getListWifiBSS();
            tmp.retainAll(capted); // set-theoric and operation
            if (biggestSetSize == tmp.size()) {
                res.add(plan);
            }
            else if (biggestSetSize < tmp.size()) {
                res = new ArrayList<Plan>();
                res.add(plan);
                biggestSetSize = tmp.size();
            }
        }
        if (res.size() == 0) {
            System.out.println("You are not at ULB.\nI should throw a propre exception but I'm too lazy...");
        }
        return res.get(0).getNode(capted);
    }
    
        
    
    
    /////////////////////////// STATIC ///////////////////////////
    
    /**
     * Load all plan in the default folder
     */
    private static void loadAllPlan() {
        File mapFolder = new File(Environment.getExternalStorageDirectory() + "/OWL/XMLMap");

        if(mapFolder != null && mapFolder.exists() && mapFolder.isDirectory()) {

            for (File plan : mapFolder.listFiles()) {
                // Check that the plan exist, is a file, have a name AND is not a test plan
                if(plan != null && plan.exists() && plan.isFile() && !plan.getName().equalsIgnoreCase("") &&
                        !plan.getName().contains(IGNOREPLAN)) {
                    getPlan(plan.getName());
                }
            }

        }

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
            try {
                resPlan = new Plan(name);
                _allPlan.add(resPlan);
            } catch (IOException exception) {
                Log.e(Graph.class.getName(), "Impossible de charger le plan: " + name +
                        " (e: " + exception.getMessage()+")");
            }
        }
        
        return resPlan;
    }

    
    
}
