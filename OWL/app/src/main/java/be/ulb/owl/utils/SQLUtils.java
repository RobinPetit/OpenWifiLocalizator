package be.ulb.owl.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import be.ulb.owl.Wifi;
import be.ulb.owl.graph.Campus;
import be.ulb.owl.graph.Graph;
import be.ulb.owl.graph.Node;
import be.ulb.owl.graph.Path;
import be.ulb.owl.graph.Plan;
import be.ulb.owl.utils.sql.AliasesLinkTable;
import be.ulb.owl.utils.sql.AliasesTable;
import be.ulb.owl.utils.sql.BuildingTable;
import be.ulb.owl.utils.sql.EdgeTable;
import be.ulb.owl.utils.sql.NodeTable;
import be.ulb.owl.utils.sql.WifiTable;

/**
 * Tool to manage SQL
 *
 * Created by Detobel36
 */

public class SQLUtils extends SQLiteOpenHelper {

    private static SQLiteDatabase _db = null;

    private static final String DB_NAME = "OWL-DB.db";
    private static String DB_PATH = "";

    private Context _context;


    // TODO ajouter la documenter
    /**
     * Constructor
     *
     * @param context
     */
    public SQLUtils(Context context) {
        super(context, DB_NAME, null, 1);

        if(_db != null) {
            throw new IllegalArgumentException("Une instance de SQLUtils existe déjà");
        }

        this._context = context;

        DB_PATH = context.getApplicationInfo().dataDir + "/databases";

        new File(DB_PATH).mkdir();
        Log.d(getClass().getName(), "DB_Path: " + DB_PATH);

        if (!checkdatabase()) {
            Log.i(getClass().getName(), "Database doesn't exist -> creation");
            createDataBase();
        }

        Log.d(getClass().getName(), "Open existing database");
        opendatabase();

    }

    ////////////////////////// FUNCTION TO MOVE AND CHECK ASSETS DATABASE //////////////////////////

    /**
     * Check if database have allready be moved
     *
     * @return True if database allready exist
     */
    private boolean checkdatabase() {
        boolean check_db_exists = false;

        try {
            String myPath = DB_PATH + DB_NAME;
            File dbFile = new File(myPath);
            check_db_exists = dbFile.exists();

        } catch(SQLiteException e) {
            Log.w(getClass().getName(), "Database doesn't exist");
        }

        return check_db_exists;
    }

    private void createDataBase() {
        //If the database does not exist, copy it from the assets.
        this.getReadableDatabase();
        this.close();

        copyDataBase();
        Log.i(getClass().getName(), "Database created");

    }

    /**
     * Copy the database frome the assets folder to
     *
     */
    private void copyDataBase() {
        //Open your local db as the input stream
        InputStream myinput = null;
        try {
            myinput = _context.getAssets().open(DB_NAME);
        } catch (IOException e) {
            Log.w(getClass().getName(), "Error open " + e.getMessage());
        }

        // Path to the just created empty db
        String outfilename = DB_PATH + DB_NAME;

        //Open the empty db as the output stream
        OutputStream myoutput = null;
        try {
            myoutput = new FileOutputStream(outfilename);
        } catch (FileNotFoundException e) {
            Log.w(getClass().getName(), "Error not found " + e.getMessage());
        }

        // transfer byte to inputfile to outputfile
        byte[] buffer = new byte[1024];
        int length;

        try {
            while ((length = myinput.read(buffer))>0) {
                myoutput.write(buffer, 0, length);
            }
        } catch (IOException e) {
            Log.w(getClass().getName(), "Error copy " + e.getMessage());
        }

        //Close the streams

        try {
            myoutput.flush();
            myoutput.close();
            myinput.close();
        } catch (IOException e) {
            Log.w(getClass().getName(), "Error close " + e.getMessage());
        }

    }

    /**
     * Open the database which was copied from assets
     */
    public void opendatabase() {
        //Open the database
        String mypath = DB_PATH + DB_NAME;
        _db = SQLiteDatabase.openDatabase(mypath, null, SQLiteDatabase.OPEN_READWRITE);
    }




    ///////////////////////////////// EVENT SQL /////////////////////////////////

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.i(getClass().getName(), "The database is allready created (in assets folder)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Log.i(getClass().getName(), "Database is directly updated when the file bd is override " +
                "(in assets folder)");
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
        new SQLUtils(context);

    }


    public static ArrayList<Campus> loadAllCampus() {
        ArrayList<Campus> res = new ArrayList<Campus>();

        Cursor cursor = getDatabase().query(BuildingTable.getName(),
                new String[] {
                        BuildingTable.NAME.getCol(),
                        BuildingTable.ID.getCol(),
                        BuildingTable.IMAGE_PATH.getCol(),
                        BuildingTable.BG_COORD_X.getCol(),
                        BuildingTable.BG_COORD_Y.getCol(),
                        BuildingTable.PPM.getCol()},
                BuildingTable.CAMPUS_ID.getCol() + " = 0", null, null, null, null);

        int id;
        String planName;
        String pathImage;
        float bgCoordX;
        float bgCoordY;
        float distance;

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            while(cursor.isAfterLast() == false) {

                id = getInt(cursor, BuildingTable.ID.getCol());
                planName = getString(cursor, BuildingTable.NAME.getCol());
                pathImage = getString(cursor, BuildingTable.IMAGE_PATH.getCol());

                bgCoordX = getFloat(cursor, BuildingTable.BG_COORD_X.getCol());
                bgCoordY = getFloat(cursor, BuildingTable.BG_COORD_Y.getCol());
                distance = getFloat(cursor, BuildingTable.PPM.getCol());

                res.add(new Campus(planName, id, pathImage, bgCoordX, bgCoordY, distance));

                cursor.moveToNext();
            }
        }

        return res;
    }

    /**
     * Load a specific campus
     *
     * @param name the name of the campus
     * @return the new campus or null if not found
     */
    public static Campus loadCampus(String name) {
        Campus res = null;

        Cursor cursor = getDatabase().query(BuildingTable.getName(),
                new String[] {
                        BuildingTable.NAME.getCol(),
                        BuildingTable.ID.getCol(),
                        BuildingTable.IMAGE_PATH.getCol(),
                        BuildingTable.BG_COORD_X.getCol(),
                        BuildingTable.BG_COORD_Y.getCol(),
                        BuildingTable.PPM.getCol()},
                BuildingTable.CAMPUS_ID.getCol() + " = 0 AND " +
                BuildingTable.NAME.getCol() + " = ?", new String[]{name}, null, null, null);

        int id;
        String planName;
        String pathImage;
        float bgCoordX;
        float bgCoordY;
        float distance;

        if(cursor.getCount() == 1) {
            cursor.moveToFirst();


            id = getInt(cursor, BuildingTable.ID.getCol());
            planName = getString(cursor, BuildingTable.NAME.getCol());
            pathImage = getString(cursor, BuildingTable.IMAGE_PATH.getCol());

            bgCoordX = getFloat(cursor, BuildingTable.BG_COORD_X.getCol());
            bgCoordY = getFloat(cursor, BuildingTable.BG_COORD_Y.getCol());
            distance = getFloat(cursor, BuildingTable.PPM.getCol());

            res = new Campus(planName, id, pathImage, bgCoordX, bgCoordY, distance);

        }

        return res;

    }


    /**
     * Load all plan contains in the databse from a specific campus
     *
     * @param campus refrence to the parent campus
     * @param campusID the id of the parent campus
     * @return an ArrayList with all plan
     */
    public static ArrayList<Plan> loadAllPlan(Campus campus, int campusID) {
        ArrayList<Plan> res = new ArrayList<Plan>();

        Cursor cursor = getDatabase().query(BuildingTable.getName(),
                new String[] {
                        BuildingTable.NAME.getCol(),
                        BuildingTable.ID.getCol(),
                        BuildingTable.IMAGE_PATH.getCol(),
                        BuildingTable.X_ON_PARENT.getCol(),
                        BuildingTable.Y_ON_PARENT.getCol(),
                        BuildingTable.BG_COORD_X.getCol(),
                        BuildingTable.BG_COORD_Y.getCol(),
                        BuildingTable.RELATIVE_ANGLE.getCol(),
                        BuildingTable.PPM.getCol()},
                BuildingTable.CAMPUS_ID.getCol() + " = ?", new String[]{""+campusID}, null, null, null);

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
                planName = getString(cursor, BuildingTable.NAME.getCol());;
                id = getInt(cursor, BuildingTable.ID.getCol());
                pathImage = getString(cursor, BuildingTable.IMAGE_PATH.getCol());
                xOnParent = getFloat(cursor, BuildingTable.X_ON_PARENT.getCol());
                yOnParent = getFloat(cursor, BuildingTable.Y_ON_PARENT.getCol());
                bgCoordX = getFloat(cursor, BuildingTable.BG_COORD_X.getCol());
                bgCoordY = getFloat(cursor, BuildingTable.BG_COORD_Y.getCol());
                relativeAngle = getFloat(cursor, BuildingTable.RELATIVE_ANGLE.getCol());
                distance = getFloat(cursor, BuildingTable.PPM.getCol());

                res.add(new Plan(planName, id, campus, pathImage, xOnParent, yOnParent, bgCoordX, bgCoordY,
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
        Cursor cursor = getDatabase().rawQuery("SELECT bSource.*, " +
                    "bCampus." + BuildingTable.NAME.getCol() + " AS campusName " +
                "FROM " + BuildingTable.getName() + " bSource " +
                    "JOIN " + BuildingTable.getName() + " bCampus " +
                    "ON bCampus." + BuildingTable.ID.getCol() + " = bSource." + BuildingTable.CAMPUS_ID.getCol() +" " +
                "WHERE bSource." + BuildingTable.NAME.getCol() + " = ?",
                new String[] {planName});


        int id;
        String campusName;

        String pathImage;
        float xOnParent;
        float yOnParent;
        float bgCoordX;
        float bgCoordY;
        float relativeAngle;
        float distance;

        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            id = getInt(cursor, BuildingTable.ID.getCol());
            campusName = getString(cursor, "campusName");

            pathImage = getString(cursor, BuildingTable.IMAGE_PATH.getCol());
            xOnParent = getFloat(cursor, BuildingTable.X_ON_PARENT.getCol());
            yOnParent = getFloat(cursor, BuildingTable.Y_ON_PARENT.getCol());
            bgCoordX = getFloat(cursor, BuildingTable.BG_COORD_X.getCol());
            bgCoordY = getFloat(cursor, BuildingTable.BG_COORD_Y.getCol());
            relativeAngle = getFloat(cursor, BuildingTable.RELATIVE_ANGLE.getCol());
            distance = getFloat(cursor, BuildingTable.PPM.getCol());

        } else {
            throw new SQLiteException("Multiple building have the same name: " + planName);
        }

        cursor.close(); // end of the request

        Campus campus = Graph.getCampus(campusName, false);
        if(campus == null) {
            Log.e(SQLUtils.class.getName(), "Campus " + campusName + " not found !");
            return null;
        }

        return new Plan(planName, id, campus, pathImage, xOnParent, yOnParent, bgCoordX, bgCoordY, relativeAngle, distance);
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
                        NodeTable.ID.getCol(),
                        NodeTable.X.getCol(),
                        NodeTable.Y.getCol()},
                NodeTable.BUILDING_ID.getCol() + " = ?", new String[]{""+planID}, null, null, null);

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            while (cursor.isAfterLast() == false) {
                float x = getInt(cursor, NodeTable.X.getCol());
                float y = getInt(cursor, NodeTable.Y.getCol());
                int id = getInt(cursor, NodeTable.ID.getCol());

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

        String reqStr = "SELECT " + AliasesTable.NAME.getFullCol() + " " +
                "FROM " + AliasesLinkTable.getName() + " " +
                    "JOIN " + AliasesTable.getName() + " " +
                    "ON " + AliasesLinkTable.ALIAS_ID.getFullCol() + " = " + AliasesTable.ID.getFullCol() + " " +
                "WHERE " + AliasesLinkTable.NODE_ID.getFullCol() + " = ?";

        Cursor cursor = getDatabase().rawQuery(reqStr, new String[]{""+nodeID});

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            while(cursor.isAfterLast() == false) {
                res.add(getString(cursor, AliasesTable.NAME.getCol()));

                cursor.moveToNext();
            }

        }

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

        Cursor cursor = getDatabase().query(WifiTable.getName(),
                new String[] {
                        WifiTable.BSS.getCol(),
                        WifiTable.MAX.getCol(),
                        WifiTable.MIN.getCol(),
                        WifiTable.AVG.getCol(),
                        WifiTable.VARIANCE.getCol()},
                WifiTable.NODE_ID.getCol() + " = ?", new String[]{""+nodeID}, null, null, null);

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            String bss;
            float max;
            float min;
            float avg;
            float variance;

            while (cursor.isAfterLast() == false) {
                bss = getString(cursor, WifiTable.BSS.getCol());
                max = getFloat(cursor, WifiTable.MAX.getCol());
                min = getFloat(cursor, WifiTable.MIN.getCol());
                avg = getFloat(cursor, WifiTable.AVG.getCol());
                variance = getFloat(cursor, WifiTable.VARIANCE.getCol());

                res.add(new Wifi(bss, max, min, avg, variance));

                cursor.moveToNext();
            }

        }

        return res;
    }


    /**
     * Load all path on a specific node<br />
     * A path is create only if the two node exist !
     *
     * @param nodeID the id of the node
     * @param node which contains the path
     * @return ArrayList with all path
     */
    public static ArrayList<Path> loadPath(int nodeID, Node node) {
        return loadPath(nodeID, node, null);
    }

    /**
     * Load all paht on a specific node<br />
     * A path is create only if the two node exist !
     *
     * @param nodeID the id of the node
     * @param node which contains the path
     * @param plan of the current node
     * @return ArrayList with all path
     */
    public static ArrayList<Path> loadPath(int nodeID, Node node, Plan plan) {

        ArrayList<Path> res = new ArrayList<Path>();

        Cursor cursor = getDatabase().query(EdgeTable.getName(),
                new String[] {
                    EdgeTable.NODE_1_ID.getCol(),
                    EdgeTable.NODE_2_ID.getCol()
                },
                EdgeTable.NODE_1_ID.getCol() + " = ?" +
                " OR " + EdgeTable.NODE_2_ID.getCol() + " = ?", new String[]{""+nodeID,""+nodeID}, null, null, null);


        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            int idOne;
            int idTwo;

            Node nodeOne;
            Node nodeTwo;

            while(cursor.isAfterLast() == false) {
                idOne = getInt(cursor, EdgeTable.NODE_1_ID.getCol());
                idTwo = getInt(cursor, EdgeTable.NODE_2_ID.getCol());

                // All the time nodeOne will be the node in param
                // so if idTwo egals searched node, we switch the two ;)
                if(idTwo == nodeID) {
                    idTwo = idOne;

                } else if(idOne != nodeID) { // We check that node one have the good id
                    // If not, create an error !
                    Log.e(SQLUtils.class.getName(), "The SQL response is not valide (Search edge with " +
                            "node: " + nodeID + " and return: " + idOne + " & " + idTwo + ")");
                    cursor.moveToNext();
                    continue; // next !
                }

                nodeOne = node;

                // Now we search the second id:
                nodeTwo = plan.getNode(idTwo);
                if(nodeTwo == null) { // If not found in the current plan
                    nodeTwo = Graph.getNode(idTwo); // Search in all plan
                }

                if(nodeTwo != null) { // If found :)
                    res.add(new Path(nodeOne, nodeTwo));
                }

                cursor.moveToNext();
            }

        }

        return res;
    }


    //////////////////////////////// Utils ////////////////////////////////

    /**
     * Get the int value
     *
     * @param cursor the bdd cursor
     * @param columnName the name of the column
     * @return the value
     */
    private static int getInt(Cursor cursor, String columnName) {
        return cursor.getInt(cursor.getColumnIndex(columnName));
    }

    /**
     * Get the String value
     *
     * @param cursor the bdd cursor
     * @param columnName the name of the column
     * @return the value
     */
    private static String getString(Cursor cursor, String columnName) {
        return cursor.getString(cursor.getColumnIndex(columnName));
    }

    /**
     * Get the Float value
     *
     * @param cursor the bdd cursor
     * @param columnName the name of the column
     * @return the value
     */
    private static Float getFloat(Cursor cursor, String columnName) {
        return cursor.getFloat(cursor.getColumnIndex(columnName));
    }

}
