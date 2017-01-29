package be.ulb.owl.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import be.ulb.owl.Wifi;
import be.ulb.owl.graph.Node;
import be.ulb.owl.graph.Path;
import be.ulb.owl.graph.Plan;
import be.ulb.owl.utils.sql.BuildingTable;
import be.ulb.owl.utils.sql.NodeTable;

/**
 * Tool to manage SQL
 *
 * Created by Detobel36
 */

public class SQLUtils extends SQLiteOpenHelper {

    private static SQLiteDatabase _db = null;


    // TODO ajouter la documenter
    /**
     * Constructor
     *
     * @param context
     * @param name
     * @param factory
     * @param version
     */
    public SQLUtils(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);

        if(_db == null) {
            throw new IllegalArgumentException("Une instance de SQLUtils existe déjà");
        }

        _db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // TODO
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // TODO
    }


    /////////////////////////////////////////// STATIC ///////////////////////////////////////////

    /**
     * Get the instance of SQLiteDatabase to make request
     *
     * @return the SQLitleDatabse instance
     */
    private static SQLiteDatabase getDatabase() {
        return _db;
    }

    /**
     * TODO faire la doc
     *
     * @param context
     */
    public static void initSQLUtils(Context context) {
        new SQLUtils(context, "OWL-DB.sql", null, 1);

    }

    /**
     * Load all plan contains in the databse
     *
     * @return an ArrayList with all plan
     */
    public static ArrayList<Plan> loadAllPlan() {
        ArrayList<Plan> res = new ArrayList<Plan>();

        Cursor cursor = getDatabase().query(BuildingTable.getName(),
                new String[] {
                        BuildingTable.NAME.toString(),
                        BuildingTable.ID.toString(),
                        BuildingTable.IMAGE_PATH.toString(),
                        BuildingTable.X_ON_PARENT.toString(),
                        BuildingTable.Y_ON_PARENT.toString(),
                        BuildingTable.BG_COORD_X.toString(),
                        BuildingTable.BG_COORD_Y.toString(),
                        BuildingTable.RELATIVE_ANGLE.toString(),
                        BuildingTable.PIXEL_PER_METER.toString()},
                "", null, null, null, null);

        int id;
        String planName;
        String pathImage;
        float xOnParent;
        float yOnParent;
        float bgCoordX;
        float bgCoordY;
        float relativeAngle;
        float distance;

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            while (cursor.isAfterLast() == false) {
                cursor.moveToFirst();

                planName = cursor.getString(BuildingTable.NAME.getIndex());
                id = cursor.getInt(BuildingTable.ID.getIndex());
                pathImage = cursor.getString(BuildingTable.IMAGE_PATH.getIndex());
                xOnParent = cursor.getFloat(BuildingTable.X_ON_PARENT.getIndex());
                yOnParent = cursor.getFloat(BuildingTable.Y_ON_PARENT.getIndex());
                bgCoordX = cursor.getFloat(BuildingTable.BG_COORD_X.getIndex());
                bgCoordY = cursor.getFloat(BuildingTable.BG_COORD_Y.getIndex());
                relativeAngle = cursor.getFloat(BuildingTable.RELATIVE_ANGLE.getIndex());
                distance = cursor.getFloat(BuildingTable.PIXEL_PER_METER.getIndex());

                res.add(new Plan(planName, id, pathImage, xOnParent, yOnParent, bgCoordX, bgCoordY,
                        relativeAngle, distance));

                cursor.moveToNext(); // next entry
            }
        }

        cursor.close(); // end of the request

        return res;


    }

    /**
     * Load all information about a plan
     *
     * @param planName the name of the plan
     * @return the created plan object
     */
    public static Plan loadPlan(String planName) throws SQLiteException {
        Cursor cursor = getDatabase().query(BuildingTable.getName(),
                new String[] {
                        BuildingTable.ID.toString(),
                        BuildingTable.IMAGE_PATH.toString(),
                        BuildingTable.X_ON_PARENT.toString(),
                        BuildingTable.Y_ON_PARENT.toString(),
                        BuildingTable.BG_COORD_X.toString(),
                        BuildingTable.BG_COORD_Y.toString(),
                        BuildingTable.RELATIVE_ANGLE.toString(),
                        BuildingTable.PIXEL_PER_METER.toString()},
                BuildingTable.NAME + " = " + planName, null, null, null, null);

        int id;
        String pathImage;
        float xOnParent;
        float yOnParent;
        float bgCoordX;
        float bgCoordY;
        float relativeAngle;
        float distance;

        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            id = cursor.getInt(BuildingTable.ID.getIndex());
            pathImage = cursor.getString(BuildingTable.IMAGE_PATH.getIndex());
            xOnParent = cursor.getFloat(BuildingTable.X_ON_PARENT.getIndex());
            yOnParent = cursor.getFloat(BuildingTable.Y_ON_PARENT.getIndex());
            bgCoordX = cursor.getFloat(BuildingTable.BG_COORD_X.getIndex());
            bgCoordY = cursor.getFloat(BuildingTable.BG_COORD_Y.getIndex());
            relativeAngle = cursor.getFloat(BuildingTable.RELATIVE_ANGLE.getIndex());
            distance = cursor.getFloat(BuildingTable.PIXEL_PER_METER.getIndex());

        } else {
            throw new SQLiteException("Il y a plusieurs batiments avec le nom: " + planName);
        }

        cursor.close(); // end of the request

        return new Plan(planName, id, pathImage, xOnParent, yOnParent, bgCoordX, bgCoordY, relativeAngle, distance);
    }

    /**
     * Load all node of a specific plan
     *
     * @param plan reference to the plan
     * @param planID numero du plan
     * @return an arraylist with all node created
     */
    public static ArrayList<Node> loadNodes(Plan plan, int planID) {
        ArrayList<Node> res = new ArrayList<Node>();

        Cursor cursor = getDatabase().query(NodeTable.getName(),
                new String[] {
                        NodeTable.ID.toString(),
                        NodeTable.X.toString(),
                        NodeTable.Y.toString(),
                        NodeTable.BUILDING_2_ID.toString()},
                NodeTable.BUILDING_ID + " = " + planID, null, null, null, null);

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            while (cursor.isAfterLast() == false) {
                float x = cursor.getInt(NodeTable.X.getIndex());
                float y = cursor.getInt(NodeTable.Y.getIndex());
                int id = cursor.getInt(NodeTable.ID.getIndex());

                res.add(new Node(plan, x, y, id));

                cursor.moveToNext();
            }
        }

        cursor.close(); // end of the request

        return res;
    }

    /**
     * Load all alias of a specific node
     *
     * @param nodeID the id of the node
     * @return ArrayList with all alias
     */
    public static ArrayList<String> loadAlias(int nodeID) {
        ArrayList<String> res = new ArrayList<String>();
        // TODO

        return res;
    }


    /**
     * Load all wifi of a specific node
     *
     * @param nodeID the id of the node
     * @return ArrayList with all wifi
     */
    public static ArrayList<Wifi> loadWifi(int nodeID) {
        ArrayList<Wifi> res = new ArrayList<Wifi>();
        // TODO
        // new wifi

        return res;
    }


    /**
     * Load all path on a specific node<br />
     * A path is create only if the two node exist !
     *
     * @param nodeID the id of the node
     * @return ArrayList with all path
     */
    public static ArrayList<Path> loadPath(int nodeID) {
        ArrayList<Path> res = new ArrayList<Path>();
        // TODO
        // new Path

        return res;
    }


}
