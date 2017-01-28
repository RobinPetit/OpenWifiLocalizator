package be.ulb.owl.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import be.ulb.owl.graph.Node;
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
     * Load all information about a plan
     *
     * @param planName the name of the plan
     * @return the created plan object
     */
    public static Plan loadPlan(String planName) throws SQLiteException {
        Cursor cursor = getDatabase().query(BuildingTable.getName(),
                new String[] {BuildingTable.IMAGE_PATH.toString(), BuildingTable.PIXEL_PER_METER.toString()},
                BuildingTable.NAME + " = " + planName, null, null, null, null);

        String pathImage;
        float xOnParent;
        float yOnParent;
        float bgCoordX;
        float bgCoordY;
        float relativeAngle;
        float distance;

        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            pathImage = cursor.getString(BuildingTable.IMAGE_PATH.getIndex());
            xOnParent = cursor.getFloat(BuildingTable.X_ON_PARENT.getIndex());
            yOnParent = cursor.getFloat(BuildingTable.Y_ON_PARENT.getIndex());
            bgCoordX = cursor.getFloat(BuildingTable.BG_COORD_x.getIndex());
            bgCoordY = cursor.getFloat(BuildingTable.BG_COORD_Y.getIndex());
            relativeAngle = cursor.getFloat(BuildingTable.RELATIVE_ANGLE.getIndex());
            distance = cursor.getFloat(BuildingTable.PIXEL_PER_METER.getIndex());

        } else {
            throw new SQLiteException("Il y a plusieurs batiment avec le nom: " + planName);
        }

        cursor.close(); // fin de la requête

        return new Plan(planName, pathImage, xOnParent, yOnParent, bgCoordX, bgCoordY, relativeAngle, distance);
    }

    /**
     * Load all node of a specific plan
     *
     * @param planID numero du plan
     * @return an arraylist with all node created
     */
    public static ArrayList<Node> loadPlanNodes(int planID) {
        ArrayList<Node> res = new ArrayList<Node>();

        Cursor cursor = getDatabase().query(NodeTable.getName(),
                new String[] {NodeTable.X.toString()}, NodeTable.BUILDING_ID + " = " + planID, null, null, null, null);

        return res;
    }


}
