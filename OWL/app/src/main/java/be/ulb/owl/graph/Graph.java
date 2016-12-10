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
import java.util.HashMap;

import be.ulb.owl.MainActivity;
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


        if(_allPlan.isEmpty()) {
            Log.w(getClass().getName(), "Aucun plan n'a été chargé");
        }

        _scanner = new Scanner();
    }

    /**
     *
     * @return A list containing every node of the graph
     */
    private ArrayList<Node> getAllNodes() {
        ArrayList<Node> allNodes = new ArrayList<>();
        for(Plan plan : _allPlan)
            allNodes.addAll(plan.getAllNodes());
        return allNodes;
    }

    /**
     * Implements a shortest path algorithm (A*) between two nodes
     * @param nodeFrom The node the user is located at
     * @param nodeTo The node the user wants to reach
     * @return An ordered list of nodes the user has to cross to reach the destination
     */
    public ArrayList<Path> bestPath(Node nodeFrom, Node nodeTo) {
        ShortestPathEvaluator evaluator = new ShortestPathEvaluator(getAllNodes(), nodeFrom, nodeTo);
        return evaluator.find();
    }

    public Node whereAmI () {
        ArrayList<Wifi> sensed = _scanner.scan();
        return whereAmI(sensed);
    }

    public Node whereAmI (ArrayList<Wifi> sensed) {
        ArrayList<String> sensedStr = new ArrayList<String>();
        for (Wifi wifi : sensed) {
            sensedStr.add(wifi.getBSS());
        }
        ArrayList<Plan> res = new ArrayList<Plan>();
        int biggestSetSize = 0;
        for (Plan plan : _allPlan) {
            ArrayList<String> tmp = plan.getListWifiBSS();
            tmp.retainAll(sensedStr); // set-theoric and operation
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
        return res.get(0).getNode(sensed);
    }


    /**
     * Call when application is hide
     *
     * @see MainActivity#onStop()
     */
    public void hidden() {
       _scanner.resetWifiStatus();
    }
        
    
    
    /////////////////////////// STATIC ///////////////////////////

    /**
     * Load all plan in the default folder
     */
    private static void loadAllPlan() {
        String[] strFile = null;
        try {
            strFile = MainActivity.getInstance().getAssets().list("XMLMap");
        } catch(IOException exception) {

        }

        if(strFile != null && strFile.length > 0) {

            for (String filePlan : strFile) {

                if(filePlan == null || filePlan.equalsIgnoreCase("")) {
                    continue;
                }

                String namePlan = filePlan.replaceFirst("[.][^.]+$", "");

                // Check if plan is not empty and not an ignored file
                if(namePlan != null && !namePlan.equalsIgnoreCase("") && !namePlan.contains(IGNOREPLAN)) {
                    getPlan(namePlan);
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
