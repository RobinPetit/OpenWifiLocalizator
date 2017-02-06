package be.ulb.owl.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
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
import be.ulb.owl.utils.sql.PlanTable;
import be.ulb.owl.utils.sql.EdgeTable;
import be.ulb.owl.utils.sql.NodeTable;
import be.ulb.owl.utils.sql.SpecialEdgeTable;
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


    // TODO add documentation
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
     * Check if database has already been copied
     *
     * @return True if database already exist
     */
    private boolean checkdatabase() {
        boolean checkDbExists = false;

        try {
            String myPath = DB_PATH + DB_NAME;
            File dbFile = new File(myPath);
            checkDbExists  = dbFile.exists();

        } catch(SQLiteException e) {
            Log.w(getClass().getName(), "Database doesn't exist");
        }

        return checkDbExists ;
    }

    private void createDataBase() {
        //If the database does not exist, copy it from the assets.
        this.getReadableDatabase();
        this.close();

        copyDataBase();
        Log.i(getClass().getName(), "Database created");

    }

    /**
     * Copy the database from the assets folder to
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
        OutputStream myOutput = null;
        try {
            myOutput = new FileOutputStream(outfilename);
        } catch (FileNotFoundException e) {
            Log.w(getClass().getName(), "Error not found " + e.getMessage());
        }

        // transfer byte from inputfile to outputfile
        byte[] buffer = new byte[1024];
        int length;

        try {
            while ((length = myinput.read(buffer))>0) {
                myOutput.write(buffer, 0, length);
            }
        } catch (IOException e) {
            Log.w(getClass().getName(), "Error copy " + e.getMessage());
        }

        //Close the streams

        try {
            myOutput.flush();
            myOutput.close();
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
        String myPath = DB_PATH + DB_NAME;
        _db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }




    ///////////////////////////////// EVENT SQL /////////////////////////////////

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.i(getClass().getName(), "The database is already created (in assets folder)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // @detobel36 Quel est le sens de cette phrase ? :/
        Log.i(getClass().getName(), "Database is directly updated when the file bd is override " +
                "(in assets folder)");
    }



    /////////////////////////////////////////// STATIC ///////////////////////////////////////////

    /**
     * Get the instance of SQLiteDatabase to send requests to
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

        Cursor cursor = getDatabase().query(PlanTable.getName(),
                new String[] {
                        PlanTable.NAME.getCol(),
                        PlanTable.ID.getCol(),
                        PlanTable.IMAGE_DIRECTORY.getCol(),
                        PlanTable.BG_COORD_X.getCol(),
                        PlanTable.BG_COORD_Y.getCol(),
                        PlanTable.PPM.getCol()},
                PlanTable.CAMPUS_ID.getCol() + " = 0", null, null, null, null);

        int id;
        String planName;
        String directoryImage;
        float bgCoordX;
        float bgCoordY;
        float distance;

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            while(!cursor.isAfterLast()) {

                id = getInt(cursor, PlanTable.ID.getCol());
                planName = getString(cursor, PlanTable.NAME.getCol());
                directoryImage = getString(cursor, PlanTable.IMAGE_DIRECTORY.getCol());

                bgCoordX = getFloat(cursor, PlanTable.BG_COORD_X.getCol());
                bgCoordY = getFloat(cursor, PlanTable.BG_COORD_Y.getCol());
                distance = getFloat(cursor, PlanTable.PPM.getCol());

                res.add(new Campus(planName, id, directoryImage, bgCoordX, bgCoordY, distance));

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

        Cursor cursor = getDatabase().query(PlanTable.getName(),
                new String[] {
                        PlanTable.NAME.getCol(),
                        PlanTable.ID.getCol(),
                        PlanTable.IMAGE_DIRECTORY.getCol(),
                        PlanTable.BG_COORD_X.getCol(),
                        PlanTable.BG_COORD_Y.getCol(),
                        PlanTable.PPM.getCol()},
                PlanTable.CAMPUS_ID.getCol() + " = 0 AND " +
                PlanTable.NAME.getCol() + " = ?", new String[]{name}, null, null, null);

        int id;
        String planName;
        String directoryImage;
        float bgCoordX;
        float bgCoordY;
        float distance;

        if(cursor.getCount() == 1) {
            cursor.moveToFirst();


            id = getInt(cursor, PlanTable.ID.getCol());
            planName = getString(cursor, PlanTable.NAME.getCol());
            directoryImage = getString(cursor, PlanTable.IMAGE_DIRECTORY.getCol());

            bgCoordX = getFloat(cursor, PlanTable.BG_COORD_X.getCol());
            bgCoordY = getFloat(cursor, PlanTable.BG_COORD_Y.getCol());
            distance = getFloat(cursor, PlanTable.PPM.getCol());

            res = new Campus(planName, id, directoryImage, bgCoordX, bgCoordY, distance);

        }

        return res;

    }


    /**
     * Load all plan contains in the database from a specific campus
     *
     * @param campus reference to the parent campus
     * @param campusID the id of the parent campus
     * @return an ArrayList with all plan
     */
    public static ArrayList<Plan> loadAllPlan(Campus campus, int campusID) {
        ArrayList<Plan> res = new ArrayList<Plan>();

        Cursor cursor = getDatabase().query(PlanTable.getName(),
                new String[] {
                        PlanTable.NAME.getCol(),
                        PlanTable.ID.getCol(),
                        PlanTable.IMAGE_DIRECTORY.getCol(),
                        PlanTable.X_ON_PARENT.getCol(),
                        PlanTable.Y_ON_PARENT.getCol(),
                        PlanTable.BG_COORD_X.getCol(),
                        PlanTable.BG_COORD_Y.getCol(),
                        PlanTable.RELATIVE_ANGLE.getCol(),
                        PlanTable.PPM.getCol()},
                PlanTable.CAMPUS_ID.getCol() + " = ?", new String[]{""+campusID}, null, null, null);

        int id;
        String planName;
        String directoryImage;
        float xOnParent;
        float yOnParent;
        float bgCoordX;
        float bgCoordY;
        float relativeAngle;
        float distance;

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                planName = getString(cursor, PlanTable.NAME.getCol());;
                id = getInt(cursor, PlanTable.ID.getCol());
                directoryImage = getString(cursor, PlanTable.IMAGE_DIRECTORY.getCol());
                xOnParent = getFloat(cursor, PlanTable.X_ON_PARENT.getCol());
                yOnParent = getFloat(cursor, PlanTable.Y_ON_PARENT.getCol());
                bgCoordX = getFloat(cursor, PlanTable.BG_COORD_X.getCol());
                bgCoordY = getFloat(cursor, PlanTable.BG_COORD_Y.getCol());
                relativeAngle = getFloat(cursor, PlanTable.RELATIVE_ANGLE.getCol());
                distance = getFloat(cursor, PlanTable.PPM.getCol());

                res.add(new Plan(planName, id, campus, directoryImage, xOnParent, yOnParent, bgCoordX,
                        bgCoordY, relativeAngle, distance));

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
                    "bCampus." + PlanTable.NAME.getCol() + " AS campusName " +
                "FROM " + PlanTable.getName() + " bSource " +
                    "JOIN " + PlanTable.getName() + " bCampus " +
                    "ON bCampus." + PlanTable.ID.getCol() + " = bSource." + PlanTable.CAMPUS_ID.getCol() +" " +
                "WHERE bSource." + PlanTable.NAME.getCol() + " = ?",
                new String[] {planName});


        int id;
        String campusName;

        String directoryImage;
        float xOnParent;
        float yOnParent;
        float bgCoordX;
        float bgCoordY;
        float relativeAngle;
        float distance;

        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            id = getInt(cursor, PlanTable.ID.getCol());
            campusName = getString(cursor, "campusName");

            directoryImage = getString(cursor, PlanTable.IMAGE_DIRECTORY.getCol());
            xOnParent = getFloat(cursor, PlanTable.X_ON_PARENT.getCol());
            yOnParent = getFloat(cursor, PlanTable.Y_ON_PARENT.getCol());
            bgCoordX = getFloat(cursor, PlanTable.BG_COORD_X.getCol());
            bgCoordY = getFloat(cursor, PlanTable.BG_COORD_Y.getCol());
            relativeAngle = getFloat(cursor, PlanTable.RELATIVE_ANGLE.getCol());
            distance = getFloat(cursor, PlanTable.PPM.getCol());

        } else {
            throw new SQLiteException("Multiple building have the same name: " + planName);
        }

        cursor.close(); // end of the request

        Campus campus = Graph.getCampus(campusName, false);
        if(campus == null) {
            Log.e(SQLUtils.class.getName(), "Campus " + campusName + " not found !");
            return null;
        }

        return new Plan(planName, id, campus, directoryImage, xOnParent, yOnParent, bgCoordX, bgCoordY,
                    relativeAngle, distance);
    }



    /**
     * Load all node of a specific plan
     *
     * @param plan reference to the plan
     * @param planID id number du plan
     * @return an arraylist with all created nodes
     */
    public static ArrayList<Node> loadNodes(Plan plan, int planID) {
        ArrayList<Node> res = new ArrayList<Node>();

        Cursor cursor = getDatabase().query(NodeTable.getName(),
                new String[] {
                        NodeTable.ID.getCol(),
                        NodeTable.X.getCol(),
                        NodeTable.Y.getCol()},
                NodeTable.PLAN_ID.getCol() + " = ?", new String[]{""+planID}, null, null, null);

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
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
     * Load all aliases of a specific node
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

            while(!cursor.isAfterLast()) {
                res.add(getString(cursor, AliasesTable.NAME.getCol()));

                cursor.moveToNext();
            }

        }

        return res;
    }


    /**
     * Load all wifis of a specific node
     *
     * @param nodeID the id of the node
     * @return ArrayList with all wifi
     */
    public static ArrayList<Wifi> loadWifi(int nodeID) {
        ArrayList<Wifi> res = new ArrayList<Wifi>();

        Cursor cursor = getDatabase().query(WifiTable.getName(),
                new String[] {
                        WifiTable.BSS.getCol(),
                        WifiTable.AVG.getCol(),
                        WifiTable.VARIANCE.getCol()},
                WifiTable.NODE_ID.getCol() + " = ?", new String[]{""+nodeID}, null, null, null);

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            String bss;
            float avg;
            float variance;

            while (!cursor.isAfterLast()) {
                bss = getString(cursor, WifiTable.BSS.getCol());
                avg = getFloat(cursor, WifiTable.AVG.getCol());
                variance = getFloat(cursor, WifiTable.VARIANCE.getCol());

                res.add(new Wifi(bss, avg, variance));

                cursor.moveToNext();
            }

        }

        return res;
    }


    /**
     * Load all paths on a specific node<br />
     * A path is created only if the two nodes exist !
     *
     * @param nodeID the id of the node
     * @param node which contains the path
     * @return ArrayList with all path
     */
    public static ArrayList<Path> loadPath(int nodeID, Node node) {
        return loadPath(nodeID, node, null);
    }

    /**
     * Load all paths on a specific node<br />
     * A path is created only if the two nodes exist !
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

            while(!cursor.isAfterLast()) {
                idOne = getInt(cursor, EdgeTable.NODE_1_ID.getCol());
                idTwo = getInt(cursor, EdgeTable.NODE_2_ID.getCol());

                Log.i(SQLUtils.class.getName(), "Chargement du node: " + idOne + " - " + idTwo);

                // All the time nodeOne will be the node in param
                // so if idTwo equals searched node, we switch the two ;)
                if(idTwo == nodeID) {
                    idTwo = idOne;

                } else if(idOne != nodeID) { // We check that node one has the good id
                    // If not, create an error !
                    Log.e(SQLUtils.class.getName(), "The SQL response is not valide (Search edge with " +
                            "node: " + nodeID + " and return: " + idOne + " & " + idTwo + ")");
                    cursor.moveToNext();
                    continue; // next !
                }

                nodeOne = node;

                Log.d(SQLUtils.class.getName(), "Info plan: " + plan.getName());

                // Now we search the second id:
                nodeTwo = plan.getNode(idTwo);
                if(nodeTwo == null) { // If not found in the current plan
                    nodeTwo = Graph.getNode(idTwo); // Search in all plans
                }

                if(nodeTwo != null) { // If found :)
                    res.add(new Path(nodeOne, nodeTwo));
                }

                cursor.moveToNext();
            }

        }

        res.addAll(loadSpecialPath(nodeID, node, plan));
        return res;
    }

    /**
     * @link loadPath
     */
    private static ArrayList<Path> loadSpecialPath(int nodeID, Node node, Plan plan) {
        ArrayList<Path> res = new ArrayList<Path>();

        Cursor cursor = getDatabase().query(SpecialEdgeTable.getName(),
                new String[] {
                        SpecialEdgeTable.NODE_1_ID.getCol(),
                        SpecialEdgeTable.NODE_2_ID.getCol(),
                        SpecialEdgeTable.WEIGHT.getCol()
                },
                EdgeTable.NODE_1_ID.getCol() + " = ?" +
                        " OR " + EdgeTable.NODE_2_ID.getCol() + " = ?", new String[]{""+nodeID,""+nodeID}, null, null, null);

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            int idOne;
            int idTwo;
            double weight;
            Node nodeOne;
            Node nodeTwo;

            while(!cursor.isAfterLast()) {
                idOne = getInt(cursor, SpecialEdgeTable.NODE_1_ID.getCol());
                idTwo = getInt(cursor, SpecialEdgeTable.NODE_2_ID.getCol());
                weight = getFloat(cursor, SpecialEdgeTable.WEIGHT.getCol());

                if(idTwo == nodeID)
                    idTwo = idOne;
                else if(idOne != nodeID) {
                    Log.e(SQLUtils.class.getName(), "The SQL response is not valid (Search special edge with " +
                            "node: " + nodeID + " and return: " + idOne + " & " + idTwo + ")");
                    cursor.moveToNext();
                    continue; // next !
                }
                nodeOne = node;
                nodeTwo = plan.getNode(idTwo);
                if(nodeTwo == null)
                    nodeTwo = Graph.getNode(idTwo);
                if(nodeTwo != null)
                    res.add(new Path(nodeOne, nodeTwo, weight));

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
