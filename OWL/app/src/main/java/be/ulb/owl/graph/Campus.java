package be.ulb.owl.graph;

import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.util.ArrayList;

import be.ulb.owl.utils.SQLUtils;

/**
 * Representation of Campus
 *
 * Created by Detobel36
 */

public class Campus extends Plan {

    private ArrayList<Plan> _allPlan = new ArrayList<Plan>();
    private final Integer _id;
    private final String _directoryImage;

    /**
     * Constructor<br />
     * <b>Only</b> call with by SQL (to have information)
     *
     * @param name          of the plan
     * @param id            in the database
     * @param directoryImage     path to the image
     * @param bgCoordX      relative x position of the upper left corner of the image
     * @param bgCoordY      relative y position of the upper left corner of the image
     * @param distance      number of pixel for on meter
     */
    public Campus(String name, int id, String directoryImage, float bgCoordX, float bgCoordY, float distance) {
        super(name, id, null, directoryImage, -1, -1, bgCoordX, bgCoordY, 0, distance);

        _id = id;
        _directoryImage = directoryImage;
    }


    /**
     * Get the path to the image
     *
     * @return the path to the image
     */
    public String getDirectoryImage() {return _directoryImage;}


    /**
     * Load all plan and then all Path
     */
    public void loadAllPlan() {
        _allPlan = SQLUtils.loadAllPlan(this, _id);
        for(Plan plan : _allPlan) {
            plan.loadAllPath();
        }
    }


    /**
    * Return all node of the Campus
    *
    * @return A list containing every node of the campus
    */
    @Override
    public ArrayList<Node> getAllNodes() {
        ArrayList<Node> allNodes = (ArrayList<Node>) _listNode.clone();

        for(Plan plan : _allPlan) {
            allNodes.addAll(plan.getAllNodes());
        }
        return allNodes;
    }

    /**
     * Return all plan of this campus
     *
     * @return all plan
     */
    public final ArrayList<Plan> getAllPlans() {
        return _allPlan;
    }


    /**
     * Get a specific plan or <b>create</b> if not exist
     *
     * @param name the name of the specific plan
     * @return The plan (or null if not found)
     */
    public Plan getPlan(String name) {
        return getPlan(name, true);
    }


    /**
     * Get a specific plan or <b>create</b> if not exist
     *
     * @param name the name of the specific plan
     * @param loadIfNotExist try to load if the plan is not found
     * @return The plan (or null if not found)
     */
    public Plan getPlan(String name, boolean loadIfNotExist) {
        for(Plan plan : _allPlan) {
            if(plan.isName(name)) {
                return plan;
            }
        }

        Plan resPlan = null;
        if(loadIfNotExist) {
            try {
                resPlan = SQLUtils.loadPlan(name);
                _allPlan.add(resPlan);
            } catch (SQLiteException exception) {
                Log.e(Graph.class.getName(), "Can not load the plan: " + name +
                        " (err: " + exception.getMessage()+")");
            }
        }

        return resPlan;
    }

    protected boolean haveId(int id) {
        return id == _id;
    }

    @Override
    public boolean isPlan() {return false;}

    @Override
    public double getAbsoluteX(float x) {
        return 0;
    }

    @Override
    public double getAbsoluteY(float x) {
        return 0;
    }

}
