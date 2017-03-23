/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package be.ulb.owl.graph;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import be.ulb.owl.MainActivity;
import be.ulb.owl.R;
import be.ulb.owl.event.ScanWifiUpdateEvent;
import be.ulb.owl.graph.shortestpath.ShortestPathDijkstra;
import be.ulb.owl.graph.shortestpath.ShortestPathEvaluator;
import be.ulb.owl.scanner.Scanner;
import be.ulb.owl.scanner.Wifi;
import be.ulb.owl.utils.DialogUtils;
import be.ulb.owl.utils.SQLUtils;

/**
 * Represent all Plan<br/>
 * Must be initialised only <b>once</b>
 *
 * @author Detobel36
 */
public class Graph implements ScanWifiUpdateEvent {

    private final MainActivity _main;

    private boolean _displayNotFound = false;

    private static ArrayList<Campus> _allCampus;
    private static final int SOLBOSCH_ID = 1;
    private static final int DEFAULT_PLAN_ID = SOLBOSCH_ID;


    /**
     * Constructor<br />
     * Load all campus and plan<br />
     * <br />
     * Init scanner
     */
    public Graph(MainActivity main) {
        _main = main;

        _allCampus = new ArrayList<>();
        loadAllPlan();

        if(_allCampus.isEmpty()) {
            Log.w(getClass().getName(), "No campus has been loaded");
        }

        Scanner.addEventUpdateWifi(this);
    }


    /**
     * Load all plan in the default folder
     */
    private void loadAllPlan() {
        _allCampus = SQLUtils.loadAllCampus();

        for(Campus campus : _allCampus) {
            campus.loadAllPlan();
            // load paths on the campus after each plan is loaded (in order to have functional external edges)
            campus.loadAllPath();
        }

        // TODO DEBUG load wifi
//        for(Campus campus : _allCampus) {
//            campus.loadWifi();
//        }

//        new LoadMapTask().execute(_allCampus);
        // TODO get all plan ?
//        _allPlan = SQLUtils.loadAllPlan();
    }


    /**
     * Awful method which aims to return the common elements of two arrays of Wifi
     *
     * @param set1 arrayList of Wifi
     * @param set2 arrayList of Wifi
     * @return an ArrayList which contains the common Wifi objects between the two arrayList given in param
     */
    private ArrayList<Wifi> common(ArrayList<Wifi> set1, ArrayList<Wifi> set2) {
        ArrayList<Wifi> res = new ArrayList<Wifi>();
        for (Wifi elem1:set1) {
            for (Wifi elem2:set2) {
                if (elem1.equals(elem2)) {
                    res.add(elem1);
                }
            }
        }
        return res;
    }

    // TODO DEBUG
    public void loadAllWifi() {
        for(Campus campus : _allCampus) {
            campus.loadWifi();

            for(Plan plan : campus.getAllPlans()) {
                plan.loadWifi();
            }

        }
    }


    ////////////////////////////////////// GETTER AND SETTER //////////////////////////////////////

    /**
     * Get all alias in this graph
     *
     * @return all alias name
     */
    public List<String> getAllAlias() {
        HashSet<String> allAlias = new HashSet<>();
        Log.d(getClass().getName(), "all plans of graph: " + getAllPlan());
        for(Plan plan : getAllPlan()) {
            allAlias.addAll(plan.getAllAlias());
        }

        return new ArrayList<String>(allAlias);
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


    /**
     * Search all node which contain a specific alias (no search in name)
     *
     * @param name the name (alias) of this nodes
     * @return an ArrayList of Node
     */
    public ArrayList<Node> getAllNodesWithAlias(String name) {
        ArrayList<Node> listeNode = new ArrayList<Node>();
        for(Plan plan : getAllPlan()) {
            listeNode.addAll(plan.searchNode(name));
        }
        return listeNode;
    }

    /**
     * Get a plan
     *
     * @param name of the plan
     * @return the Plan object
     */
    public Plan getPlanByName(String name) {
        for(Plan plan: getAllPlan()) {
            if (plan.getName().equals(name)) {
                return plan;
            }
        }

        return null;
    }


    /**
     * Get the default campus
     *
     * @return the default Campus
     */
    public Campus getDefaultCampus() {
        return getAllCampus().get(DEFAULT_PLAN_ID);
    }


    /**
     * Get all campus of the graph
     *
     * @return all Campus
     */
    public ArrayList<Campus> getAllCampus() {
        return _allCampus;
    }


    /**
     * Get a specific campus or <b>create</b> if not exist
     *
     * @param name the name of the specific campus
     * @return the campus (or null if not found)
     */
    public Campus getCampus(String name) {
        return getCampus(name, true);
    }


    /////////////////////////////////////////// LOCALIZE ///////////////////////////////////////////

    /**
     * Find the node where the user is
     *
     * @param sensed all detected around wifi
     * @return The node or null if not found
     */
    protected Node whereAmI(ArrayList<Wifi> sensed, ArrayList<Plan> searchPlan) {
        HashSet<String> sensedStr = Wifi.wifiListToBssList(sensed);

        ArrayList<Plan> res = new ArrayList<Plan>();
        int biggestSetSize = 0;

        for (Plan plan : searchPlan) {
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

        Node node = res.get(0).getNode(sensed); // TODO Denis manage case where two plans could contain the solution.
        if(node != null) {
            Log.d(getClass().getName(), "Node found: " + node.getID() + "(alias: " + node.getAlias() + ")");
        }
        return node;
    }


    /**
     * localize the user
     *
     * @param displayNotFound Boolean telling whether or not to signal if user is unable to localize
     * @param sensedWifi list of Wifi which have been capted
     * @param listPlan list of plan which match with sensed wifi
     */
    protected void localize(boolean displayNotFound, ArrayList<Wifi> sensedWifi, ArrayList<Plan> listPlan) {
        Node currentNode = whereAmI(sensedWifi, listPlan);

        boolean hasPositionChanged = _main.setCurrentLocation(currentNode);
        if(currentNode != null) {
            _main.setCurrentPlan(currentNode.getParentPlan());
            if(hasPositionChanged) {
                _main.cleanCanvas();
                displayNewPath(currentNode);
            }

        } else if (displayNotFound) {
            DialogUtils.infoBox(_main, R.string.not_found, R.string.not_in_ULB);
        }
    }

    private void displayNewPath(Node currentNode) {
        ArrayList<Node> destinationNodes = _main.getDestinations();
        if(!destinationNodes.isEmpty()) {
            try {
                findPath();
            } catch (NoPathException e) {
                Log.e(getClass().getName(), "Error: should have found an alternative for a " +
                        "path between " + currentNode.getID() + " and " +
                        destinationNodes.get(0).getAlias().toString());
            }

        } else {
            // _main.draw(currentNode);
        }
    }

    @Override
    public void scanWifiUpdateEvent(ArrayList<Wifi> listWifi, ArrayList<Plan> listPlan) {
        localize(_displayNotFound, listWifi, listPlan);
        _displayNotFound = false;
    }

    /**
     * Define if there is a message if the location is not found
     *
     * @param display True if we must display the message
     */
    public void setDisplayNotFound(boolean display) {
        _displayNotFound = display;
    }


    ///////////////////////////////////////////// PATH /////////////////////////////////////////////

    /**
     * Find path to the destination of the user will go (stock in main)
     *
     * @throws NoPathException
     */
    public void findPath() throws NoPathException {
        Node src = _main.getCurrentLocation();

        if(src != null) {
            Node closestDestination = getClosestDestination(src, _main.getDestinations());

            Log.d(getClass().getName(), "closest destination " + closestDestination);
            // Draw source point on screen
            _main.draw(src);

            ArrayList<Path> shortestPath = bestPath(src, closestDestination);
            if (shortestPath.size() >= 2) {
                refinePath(shortestPath, _main.getDestinations());
            }

            // Draw path on screen
            _main.drawPath(shortestPath);

        } else {
            Node dest = _main.getDestinations().get(0);
            _main.setCurrentPlan(dest.getParentPlan());
            Log.d(getClass().getName(), "Set current and draw");
//            _main.cleanCanvas();
//            _main.draw(dest);
        }

    }

    /**
     * Find the closest destination node from source in given destinations
     * @param src the node to minimize distance to
     * @param destinations list of candidate destinations
     * @return the element of `destinations` being the closest to `src`
     */
    private Node getClosestDestination(Node src, List<Node> destinations) {
        double minHeuristic = Double.POSITIVE_INFINITY;
        Node closestDestination = null;

        if(destinations.isEmpty()) {
            Log.e(getClass().getName(), "Unknown destination");
        } else {
            for (Node node : destinations) {
                double currentHeuristic = Plan.euclidianDistance(src, node);
                if (currentHeuristic < minHeuristic) {
                    closestDestination = node;
                    minHeuristic = currentHeuristic;
                }
            }
        }
        if(closestDestination == null) {
            throw new AssertionError("No closest destination");
        }
        return closestDestination;
    }


    /**
     * Rearrange the path from src to dest by stopping at the first node of the path having a label
     * mathcing the desired alias
     *
     * @param overallPath the path from src to dest
     * @param listDestination the list of nodes in the graph having the right alias
     */
    private void refinePath(ArrayList<Path> overallPath, ArrayList<Node> listDestination) {
        int lastOccurrenceOfDestination = overallPath.size() - 1;
        Path lastPath = overallPath.get(lastOccurrenceOfDestination);
        Node commonNode = lastPath.getIntersectionWith(overallPath.get(lastOccurrenceOfDestination-1));
        int initialPathSize = overallPath.size();
        Log.d(getClass().getName(), initialPathSize + " nodes were found, and after refinment:");
        // remind which is the last one which is removed in order to re-add it in the end
        lastPath = overallPath.get(overallPath.size()-1);
        while (overallPath.size() > 0 && listDestination.contains(commonNode)) {
            lastOccurrenceOfDestination--;
            commonNode = overallPath.get(lastOccurrenceOfDestination).getOppositeNodeOf(commonNode);
            overallPath.remove(lastOccurrenceOfDestination+1);
        }
        if(overallPath.size() < initialPathSize)
            overallPath.add(lastPath);
        Log.d(getClass().getName(), "only " + overallPath.size() + " are left: " + overallPath);
    }


    /**
     * Implements a shortest path algorithm (A*) between two nodes
     *
     * @param nodeFrom The node the user is located at
     * @param nodeTo The node the user wants to reach
     * @return An ordered list of nodes the user has to cross to reach the destination
     */
    public ArrayList<Path> bestPath(Node nodeFrom, Node nodeTo) throws NoPathException {
        Log.d(getClass().getName(), "Searching path between: " + nodeFrom + " and " + nodeTo);
        ShortestPathEvaluator evaluator = new ShortestPathDijkstra(getAllNodes(), nodeFrom, nodeTo);
        return evaluator.find();
    }



    /////////////////////////// STATIC ///////////////////////////



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
        ArrayList<Plan> allPlan = (ArrayList<Plan>) _allCampus.clone();
        for(Campus campus : _allCampus) {
            allPlan.addAll(campus.getAllPlans());
        }
        return allPlan;
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

    public static Campus getCampus(int id) {
        for(Campus campus : _allCampus) {
            if(campus.haveId(id)) {
                return campus;
            }
        }
        return null;
    }


}
