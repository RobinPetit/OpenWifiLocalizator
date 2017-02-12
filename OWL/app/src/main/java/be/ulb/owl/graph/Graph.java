/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package be.ulb.owl.graph;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;

import be.ulb.owl.MainActivity;
import be.ulb.owl.R;
import be.ulb.owl.event.ScanWifiUpdateEvent;
import be.ulb.owl.graph.shortestpath.ShortestPathAStar;
import be.ulb.owl.graph.shortestpath.ShortestPathEvaluator;
import be.ulb.owl.scanner.Scanner;
import be.ulb.owl.scanner.Wifi;
import be.ulb.owl.task.LoadMapTask;
import be.ulb.owl.utils.DialogUtils;
import be.ulb.owl.utils.SQLUtils;

/**
 * Represent all Plan<br/>
 * Must be initialised only <b>once</b>
 *
 * @author Detobel36
 */
public class Graph implements ScanWifiUpdateEvent {

    private static final MainActivity main = MainActivity.getInstance();

    private static ArrayList<Campus> _allCampus;
    private static final String SOLBOSCH_PLAN = "Solbosch";


    /**
     * Constructor<br />
     * Load all campus and plan<br />
     * <br />
     * Init scanner
     */
    public Graph () {
        _allCampus = new ArrayList<Campus>();
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
        new LoadMapTask().execute(_allCampus);
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
    private ArrayList<Wifi> common (ArrayList<Wifi> set1, ArrayList<Wifi> set2) {
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

    ////////////////////////////////////// GETTER AND SETTER //////////////////////////////////////

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
    public ArrayList<Node> searchNode(String name) {
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
        // TODO change constant with magic number ? :P
        // return getAllCampus().get(0);
        return getCampus(SOLBOSCH_PLAN);
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
     * @param sensed all detected arround wifi
     * @return The node or null if not found
     */
    protected Node whereAmI(ArrayList<Wifi> sensed) {
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

        Node node = res.get(0).getNode(sensed); // TODO Denis manage case where two plans could contain the solution.
        if(node != null) {
            Log.d(getClass().getName(), "Node found: " + node.getID() + "(alias: " + node.getAlias() + ")");
        }
        return node;
    }


    /**
     * localizes the user
     *
     * @param displayNotFound Boolean telling whether or not to signal if user is unable to localize
     */
    public void localize(boolean displayNotFound, ArrayList<Wifi> sensedWifi) {
        Node current = whereAmI(sensedWifi);

        boolean haveChange = main.setCurrentLocation(current);

        if(current != null) {
            main.setCurrentPlan(current.getParentPlan());
            if(haveChange) {
                main.cleanCanvas();

                ArrayList<Node> distinationNodes = main.getDestinations();
                if(!distinationNodes.isEmpty()) {
                    try {
                        findPath();
                    } catch (NoPathException e) {
                        Log.e(getClass().getName(), "Error: should have found an alternative for a " +
                                "path between " + current.getID() + " and " +
                                distinationNodes.get(0).getAlias().toString());
                    }

                } else {
                    main.draw(current);
                }

            }

        } else if (displayNotFound) {
            DialogUtils.infoBox(main, R.string.not_found, R.string.not_in_ULB);
        }
    }


    @Override
    public void scanWifiUpdateEvent(ArrayList<Wifi> listWifi) {
        localize(false, listWifi);
    }



    ///////////////////////////////////////////// PATH /////////////////////////////////////////////

    /**
     * Find path to the destination of the user will go (stock in main)
     *
     * @throws NoPathException
     */
    public void findPath() throws NoPathException {
        Node src = main.getCurrentLocation();

        if(src != null) {
            double minHeuristic = Double.POSITIVE_INFINITY;
            Node closestDestination = null;
            ArrayList<Node> listDestination = main.getDestinations();

            for (Node node : listDestination) {
                double currentHeuristic = ShortestPathAStar.heuristic(src, node);
                if (currentHeuristic < minHeuristic) {
                    closestDestination = node;
                    minHeuristic = currentHeuristic;
                }
            }

            // Draw source point on screen
            main.draw(src);

            ArrayList<Path> p = bestPath(src, closestDestination);
            if (p.size() >= 2) {
                refinePath(p, listDestination);
            }

            Log.d(getClass().getName(), "Path between " + src.getID() + " and " + closestDestination.getID());
            for (Path path : p) {
                // Log path
                Log.i(getClass().getName(), path.getNode().getID() + " - " + path.getOppositeNodeOf(path.getNode()).getID());
            }

            // Draw path on screen
            main.drawPath(p);

        } else {
            Node dest = main.getDestinations().get(0);
            main.setCurrentPlan(dest.getParentPlan());
            Log.d(getClass().getName(), "Set current and draw");
            main.draw(dest);
        }

    }


    /**
     * TODO Robin possible d'ajouter de la doc ?
     *
     * @param overallPath
     * @param listDestination
     */
    private void refinePath(ArrayList<Path> overallPath, ArrayList<Node> listDestination) {
        int firstOccurrenceOfDestination = overallPath.size() - 1;
        Node commonNode = overallPath.get(firstOccurrenceOfDestination).getIntersectionWith(overallPath.get(firstOccurrenceOfDestination-1));
        Log.d(getClass().getName(), overallPath.size() + " nodes were found, and after refinment:");
        while (overallPath.size() > 0 && listDestination.contains(commonNode)) {
            firstOccurrenceOfDestination--;
            commonNode = overallPath.get(firstOccurrenceOfDestination).getOppositeNodeOf(commonNode);
            overallPath.remove(firstOccurrenceOfDestination+1);
        }
        Log.d(getClass().getName(), "only " + overallPath.size() + " are left");
    }


    /**
     * Implements a shortest path algorithm (A*) between two nodes
     *
     * @param nodeFrom The node the user is located at
     * @param nodeTo The node the user wants to reach
     * @return An ordered list of nodes the user has to cross to reach the destination
     */
    public ArrayList<Path> bestPath(Node nodeFrom, Node nodeTo) throws NoPathException {
        Log.d(getClass().getName(), "Searching path between: " + nodeFrom.getID() + " and " + nodeTo.getID());
        ShortestPathEvaluator evaluator = new ShortestPathAStar(getAllNodes(), nodeFrom, nodeTo);
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


}
