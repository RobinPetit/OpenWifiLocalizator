/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package be.ulb.owl.graph;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import be.ulb.owl.MainActivity;
import be.ulb.owl.Scanner;
import be.ulb.owl.Wifi;
import be.ulb.owl.graph.shortestpath.ShortestPathAStar;
import be.ulb.owl.graph.shortestpath.ShortestPathEvaluator;
import be.ulb.owl.task.LoadMapTask;
import be.ulb.owl.utils.SQLUtils;

/**
 * Represent all Plan<br/>
 * Must be initialised only <b>once</b>
 *
 * @author Detobel36
 */
public class Graph {

    MainActivity main = MainActivity.getInstance();
    private static final String IGNOREPLAN = "Example";
//    private static ArrayList<Plan> _allPlan;
    private static ArrayList<Campus> _allCampus;

    private Scanner _scanner;

    // dedicated to the démonstration
    private int _offset;
    private ArrayList<Node> _demoMotions = new ArrayList<Node>();

    /**
     * Constructor<br />
     * Load all campus and plan<br />
     * <br />
     * Init scanner
     */
    public Graph () {
//        _allPlan = new ArrayList<Plan>();
        _allCampus = new ArrayList<Campus>();
        loadAllPlan();

        if (MainActivity.isDemo()) {
            _offset = 0;
        }

        if(_allCampus.isEmpty()) {
            Log.w(getClass().getName(), "No campus has been loaded");
        }

        _scanner = new Scanner();
    }

    /**
     * Return all node of the graph
     *
     * @return A list containing every node of the graph
     */
    public ArrayList<Node> getAllNodes() {
        ArrayList<Node> allNodes = new ArrayList<>();
        for(Campus campus : _allCampus) {
            allNodes.addAll(campus.getAllNodes());
        }

        return allNodes;
    }


    public void findPath(String destination) throws NoPathException {
        ArrayList<Node> destinations = searchNode(destination);
        Node src = main.location();

        if(src != null) {
            double minHeuristic = Double.POSITIVE_INFINITY;
            Node closestDestination = null;
            for (Node node : destinations) {
                double currentHeuristic = ShortestPathAStar.heuristic(src, node);
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
            Log.d(getClass().getName(), "Path between " + src.getID() + " and " + closestDestination.getID());
            for (Path path : p) {
                Log.i(getClass().getName(), path.getNode().getID() + " - " + path.getOppositeNodeOf(path.getNode()).getID());
            }
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
        Log.d(getClass().getName(), "Searching path between: " + nodeFrom.getID() + " and " + nodeTo.getID());
        ShortestPathEvaluator evaluator = new ShortestPathAStar(getAllNodes(), nodeFrom, nodeTo);
        return evaluator.find();
    }

    public void setPlan () {
        if (0 == _offset) {
            Campus campus = getCampus("Solbosh");
            Plan currentPlan = campus.getPlan("P.F");
            _demoMotions.add(currentPlan.getNode(64));
            _demoMotions.add(currentPlan.getNode(63));
            _demoMotions.add(currentPlan.getNode(9));
            _demoMotions.add(currentPlan.getNode(13));
            _demoMotions.add(currentPlan.getNode(20));
            _demoMotions.add(currentPlan.getNode(21));
            _demoMotions.add(currentPlan.getNode(27));
            _demoMotions.add(currentPlan.getNode(29));
            _demoMotions.add(currentPlan.getNode(31));
            _demoMotions.add(currentPlan.getNode(32));
            _demoMotions.add(currentPlan.getNode(34));
            _demoMotions.add(currentPlan.getNode(35));
            _demoMotions.add(currentPlan.getNode(23));
            _demoMotions.add(currentPlan.getNode(16));
            _demoMotions.add(currentPlan.getNode(42));
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
            Log.d(getClass().getName(), "wifi: (" + sensed.size() + ") " + sensed.toString());
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

        for (Plan plan : getAllPlan()) {
            HashSet<String> tmp = plan.getListWifiBSS();
            tmp.retainAll(sensedStr); // set-theoretical and operation

            if (biggestSetSize == tmp.size()) {
                res.add(plan);

            } else if (biggestSetSize < tmp.size()) {
                res = new ArrayList<Plan>();
                res.add(plan);
                biggestSetSize = tmp.size();
            }

        }

        if (res.size() == 0) {
            Log.i(getClass().getName(), "You are not at ULB.");
            return null;
        }

        Node node = res.get(0).getNode(sensed); // @TODO manage case where two plans could contain the solution.
        if(node != null) {
            Log.d(getClass().getName(), "Node found: " + node.getID() + "(alias: " + node.getAlias() + ")");
        }
        return node;
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

        Log.i(getClass().getName(), "Scanner.scan");
        main.localize();
    }


    /**
     * Get a plan
     *
     * @param name of the plan
     * @return the Plan object
     */
    public Plan getPlanByName(String name) {
        for(Plan plan: getAllPlan())
            if(plan.getName().equals(name))
                return plan;
        return null;
    }


    /////////////////////////// STATIC ///////////////////////////

    /**
     * Load all plan in the default folder
     */
    private static void loadAllPlan() {
        _allCampus = SQLUtils.loadAllCampus();
        new LoadMapTask().execute(_allCampus);
        // TODO get all plan ?
//        _allPlan = SQLUtils.loadAllPlan();
    }


    /**
     * Search all node which contain a specific alias (no search in name)
     *
     * @param name the name (alias) of this nodes
     * @return an ArrayList of Node
     */
    public static ArrayList<Node> searchNode(String name) {
        ArrayList<Node> listeNode = new ArrayList<Node>();
        for(Plan plan : getAllPlan()) {
            listeNode.addAll(plan.searchNode(name));
        }
        return listeNode;
    }

    /**
     * Get a specific node
     *
     * @param nodeId the id of the node
     * @return the Node or null if not found
     */
    public static Node getNode(int nodeId) {
        for(Plan plan : getAllPlan()) {
            Node node = plan.getNode(nodeId);
            if(node != null) {
                return node;
            }
        }
        return null;
    }


    private static ArrayList<Plan> getAllPlan() {
        ArrayList<Plan> allPlan = new ArrayList<Plan>();
        for(Campus campus : _allCampus) {
            allPlan.addAll(campus.getAllPlans());
        }
        return allPlan;
    }


    /**
     * Get all campus of the graph
     *
     * @return all Campus
     */
    public static ArrayList<Campus> getAllCampus() {
        return _allCampus;
    }

    /**
     * Get a specific campus or <b>create</b> if not exist
     *
     * @param name the name of the specific campus
     * @return the campus (or null if not found)
     */
    public static Campus getCampus(String name) {
        return getCampus(name, true);
    }

    /**
     * Get a specific campus
     *
     * @param name of the campus
     * @param loadIfNotExist True to load if Campus not found
     * @return Campus or null if not found
     */
    public static Campus getCampus(String name, boolean loadIfNotExist) {
        for(Campus campus : _allCampus) {
            if(campus.isName(name)) {
                return campus;
            }
        }

        Campus resCampus = null;
        if(loadIfNotExist) {
            resCampus = SQLUtils.loadCampus(name);
        }

        return resCampus;
    }


}
