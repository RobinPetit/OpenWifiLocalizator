/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package be.ulb.owl.graph;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

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
    MainActivity main = MainActivity.getInstance();
    private static final String IGNOREPLAN = "Example";
    private static ArrayList<Plan> _allPlan;

    private Scanner _scanner;

    // dedicated to the démonstration
    private int _offset;
    private ArrayList<Node> _demoMotions = new ArrayList<Node>();

    public Graph () {
        _allPlan = new ArrayList<Plan>();
        loadAllPlan();
        if (MainActivity.isDemo()) {
            _offset = 0;
        }

        if(_allPlan.isEmpty()) {
            Log.w(getClass().getName(), "No plan has been loaded");
        }

        _scanner = new Scanner();
    }

    /**
     *
     * @return A list containing every node of the graph
     */
    public ArrayList<Node> getAllNodes() {
        ArrayList<Node> allNodes = new ArrayList<>();
        for(Plan plan : _allPlan)
            allNodes.addAll(plan.getAllNodes());
        return allNodes;
    }

    public void findPath(String destination) throws NoPathException {
        ArrayList<Node> destinations = searchNode(destination);
        Node src = main.location();

        if(src != null) {
            double minHeuristic = Double.POSITIVE_INFINITY;
            Node closestDestination = null;
            for (Node node : destinations) {
                double currentHeuristic = ShortestPathEvaluator.heuristic(src, node);
                if (currentHeuristic < minHeuristic) {
                    closestDestination = node;
                    minHeuristic = currentHeuristic;
                }
            }
            main.setDestination(destination);
            main.draw(src);
            ArrayList<Path> p = bestPath(src, closestDestination);
            if(p.size() >= 2)
                refinePath(p, destination);
            Log.d(getClass().getName(), "Path between " + src.getName() + " and " + closestDestination.getName());
            for (Path path : p)
                Log.i(getClass().getName(), path.getNode().getName() + " - " + path.getOppositeNodeOf(path.getNode()).getName());
            main.drawPath(p);
        } else {
            Node dest = destinations.get(0);
            main.setCurrentPlan(dest.getParentPlan());
            Log.d(getClass().getName(), "Set current and draw");
            main.draw(dest);
        }
    }

    private void refinePath(ArrayList<Path> overallPath, String alias) {
        int firstOccurrenceOfDestination = overallPath.size() - 1;
        Node commonNode = overallPath.get(firstOccurrenceOfDestination).getIntersectionWith(overallPath.get(firstOccurrenceOfDestination-1));
        Log.d(getClass().getName(), overallPath.size() + " nodes were found, and after refinment:");
        while (overallPath.size() > 0 && commonNode.haveAlias(alias)) {
            firstOccurrenceOfDestination--;
            commonNode = overallPath.get(firstOccurrenceOfDestination).getOppositeNodeOf(commonNode);
            overallPath.remove(firstOccurrenceOfDestination+1);
        }
        Log.d(getClass().getName(), "only " + overallPath.size() + " are left");
    }

    /**
     * Implements a shortest path algorithm (A*) between two nodes
     * @param nodeFrom The node the user is located at
     * @param nodeTo The node the user wants to reach
     * @return An ordered list of nodes the user has to cross to reach the destination
     */
    public ArrayList<Path> bestPath(Node nodeFrom, Node nodeTo) throws NoPathException {
        Log.d(getClass().getName(), "Searching path between: " + nodeFrom.getName() + " and " + nodeTo.getName());
        ShortestPathEvaluator evaluator = new ShortestPathEvaluator(getAllNodes(), nodeFrom, nodeTo);
        return evaluator.find();
    }

    public void setPlan () {
        if (0 == _offset) {
            Plan currentPlan = getPlan("P.F");
            _demoMotions.add(currentPlan.getNode("64"));
            _demoMotions.add(currentPlan.getNode("63"));
            _demoMotions.add(currentPlan.getNode("9"));
            _demoMotions.add(currentPlan.getNode("13"));
            _demoMotions.add(currentPlan.getNode("20"));
            _demoMotions.add(currentPlan.getNode("21"));
            _demoMotions.add(currentPlan.getNode("27"));
            _demoMotions.add(currentPlan.getNode("29"));
            _demoMotions.add(currentPlan.getNode("31"));
            _demoMotions.add(currentPlan.getNode("32"));
            _demoMotions.add(currentPlan.getNode("34"));
            _demoMotions.add(currentPlan.getNode("35"));
            _demoMotions.add(currentPlan.getNode("23"));
            _demoMotions.add(currentPlan.getNode("16"));
            _demoMotions.add(currentPlan.getNode("42"));
        }
    }

    public Node whereAmI () {
        Node res = null;
        if (MainActivity.isDemo()) {
            setPlan();
            res = _demoMotions.get(_offset);
            _offset = (_offset+1)%_demoMotions.size();
        }
        else if(MainActivity.isDebug() && MainActivity.isTest()) {
            res = getAllNodes().get(new Random().nextInt(getAllNodes().size()));
        }
        else {
            ArrayList<Wifi> sensed = _scanner.scan();
            res = whereAmI(sensed);
        }
        return res;
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
            tmp.retainAll(sensedStr); // set-theoretical and operation
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
            Log.i(getClass().getName(), "You are not at ULB.");
            return null;
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
        _scanner.stopScanTask();
    }

    /**
     * Start the scan scheduller
     */
    public void startScanTask() {
        _scanner.startScanTask();
    }


    /**
     * Get a plan
     *
     * @param name of the plan
     * @return the Plan object
     */
    public Plan getPlanByName(String name) {
        for(Plan plan: _allPlan)
            if(plan.getName().equals(name))
                return plan;
        return null;
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
                Log.e(Graph.class.getName(), "Can not load the plan: " + name +
                        " (err: " + exception.getMessage()+")");
            }
        }

        return resPlan;
    }
}
