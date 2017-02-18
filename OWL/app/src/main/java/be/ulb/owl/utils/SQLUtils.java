package be.ulb.owl.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import be.ulb.owl.graph.Campus;
import be.ulb.owl.graph.Graph;
import be.ulb.owl.graph.Node;
import be.ulb.owl.graph.Path;
import be.ulb.owl.graph.Plan;
import be.ulb.owl.scanner.Wifi;
import be.ulb.owl.utils.sql.AliasesLinkTable;
import be.ulb.owl.utils.sql.AliasesTable;
import be.ulb.owl.utils.sql.EdgeTable;
import be.ulb.owl.utils.sql.NodeTable;
import be.ulb.owl.utils.sql.PlanTable;
import be.ulb.owl.utils.sql.SpecialEdgeTable;
import be.ulb.owl.utils.sql.WifiTable;


/**
 * Tool to manage SQL
 *
 * @author Detobel36
 */

public class SQLUtils extends SQLiteOpenHelper {

    private static SQLiteDatabase _db = null;

    private static final String DB_NAME = "OWL-DB.db";
    private static String DB_PATH;

    private Context _context;


    /**
     * Create SQLUtils object to manage SQL
     *
     * @param context context to get default method (main)
     */
    public SQLUtils(Context context) {
        super(context, DB_NAME, null, 1);

        if(_db != null) {
            throw new IllegalArgumentException("SQLUtils have already been created");
        }
        this._context = context;

        // Location of the local database
        DB_PATH = context.getApplicationInfo().dataDir + "/databases";

        // create folder if not exist
        new File(DB_PATH).mkdir();

        if (!checkDatabase() || !checkDateDatabase()) {
            createDataBase();
        }

        openDatabase();

    }



    ////////////////////////// FUNCTION TO MOVE AND CHECK ASSETS DATABASE //////////////////////////

    /**
     * Check if database has already been copied
     *
     * @return True if database already exist
     */
    private boolean checkDatabase() {
        boolean checkDbExists = false;

        try {
            String myPath = getPathLocalDatabase();
            File dbFile = new File(myPath);
            checkDbExists  = dbFile.exists();

        } catch(SQLiteException e) {
            Log.w(getClass().getName(), "Database doesn't exist");
        }

        return checkDbExists ;
    }


    /**
     * Check if database on the phone is up to date
     *
     * @return True if database is up to date
     */
    private boolean checkDateDatabase() {
        boolean res = false;
        String[] assetDBStr = null;
        try {
            assetDBStr = _context.getAssets().list(DB_NAME);
        } catch (IOException e) {
            Log.e(getClass().getName(), DB_NAME + " not found in asset folder");
        }

        if(assetDBStr != null && assetDBStr.length == 1) {
            File assetDBFile = new File(assetDBStr[0]);
            File localDBFile = new File(getPathLocalDatabase());

            if(assetDBFile.exists() && localDBFile.exists() &&
                // if assetDB have been modified before or is equal
                // to the local database
                assetDBFile.lastModified() <= localDBFile.lastModified()) {

                res = true;
            }
        }

        return res;
    }


    /**
     * Create database if the don't exist
     */
    private void createDataBase() {
        //If the database does not exist, copy it from the assets.
        this.getReadableDatabase();
        this.close();

        copyDataBase();
        Log.i(getClass().getName(), "Database created");
    }


    /**
     * Copy the database from the assets folder to the local folder
     */
    private void copyDataBase() {
        //Open your local db as the input stream
        InputStream myinput = null;
        try {
            myinput = _context.getAssets().open(DB_NAME);
        } catch (IOException e) {
            Log.w(getClass().getName(), "Error open " + e.getMessage());
        }


        //Open the empty db as the output stream
        OutputStream myOutput = null;
        try {
            // Path to the just created empty db
            myOutput = new FileOutputStream(getPathLocalDatabase());
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
    private void openDatabase() {
        //Open the database
        _db = SQLiteDatabase.openDatabase(getPathLocalDatabase(), null,
                SQLiteDatabase.OPEN_READONLY);
    }


    /**
     * Path to the local database
     *
     * @return the path
     */
    private String getPathLocalDatabase() {
        return DB_PATH + DB_NAME;
    }



    ///////////////////////////////// EVENT SQL /////////////////////////////////

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.i(getClass().getName(), "The database is already created (in assets folder)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) { /* nothing */ }



    /////////////////////////////////////////// STATIC ///////////////////////////////////////////

    /**
     * Get the instance of SQLiteDatabase to send requests to
     *
     * @return the SQLitleDatabse instance
     */
    private static SQLiteDatabase getDatabase() {
        return _db;
    }


    /////////////// CAMPUS ///////////////

    /**
     * Load all campus but not linked map
     *
     * @return ArrayList with all campus
     */
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


    /////////////// PLANS ///////////////

    /**
     * Load all plan contains in the database from a specific campus
     *
     * @param campus reference to the parent campus
     * @param campusID the id of the parent campus
     * @return an ArrayList with all plan
     */
    public static ArrayList<Plan> loadAllPlan(Campus campus, int campusID) {
        Cursor cursor = getDatabase().query(PlanTable.getName(),
                new String[] {
                        PlanTable.NAME.getCol(),
                        PlanTable.ID.getCol(),
                        PlanTable.CAMPUS_ID.getCol(),
                        PlanTable.IMAGE_DIRECTORY.getCol(),
                        PlanTable.X_ON_PARENT.getCol(),
                        PlanTable.Y_ON_PARENT.getCol(),
                        PlanTable.BG_COORD_X.getCol(),
                        PlanTable.BG_COORD_Y.getCol(),
                        PlanTable.RELATIVE_ANGLE.getCol(),
                        PlanTable.PPM.getCol()},
                PlanTable.CAMPUS_ID.getCol() + " = ?", new String[]{""+campusID}, null, null, null);

        return loadAllPlan(cursor, campus);
    }

    /**
     * Get all plan which contains a specific BSS
     *
     * @param listBSS list of all linked bss
     * @return an ArrayList with all plan
     */
//    public static ArrayList<Plan> getPlanWithWifi(ArrayList<String> listBSS) {
//
//        String param = TextUtils.join("', '", listBSS);
//
//        String req = "SELECT DISTINCT " + PlanTable.getName() + ".*" +
//                        "FROM " + PlanTable.getName() + " " +
//                            "JOIN " + NodeTable.getName() + " " +
//                                "ON " + NodeTable.PLAN_ID.getFullCol() + " " +
//                                    "= " + PlanTable.ID.getFullCol() + " " +
//                            "JOIN " + WifiTable.getName() + " "+
//                                "ON " + WifiTable.NODE_ID.getFullCol() + " " +
//                                    "= " + NodeTable.ID.getFullCol() + " "+
//                        "WHERE " + WifiTable.BSS.getFullCol() + " IN (?)";
//
//        Cursor cursor = getDatabase().rawQuery(req, new String[]{"'"+param+"'"});
//
//        return loadAllPlan(cursor);
//    }


    /**
     * Get all plan which contains a specific BSS and load all wifi of specific nodes
     *
     * @param listBSS list of all linked bss
     * @return an ArrayList with all plan ID
     */
    public static ArrayList<Plan> getPlanWithWifi(ArrayList<String> listBSS) {

        String param = TextUtils.join("', '", listBSS);

        String req = "SELECT DISTINCT " + NodeTable.getName() + ".* " +
                "FROM " + NodeTable.getName() + " " +
                    "JOIN " + WifiTable.getName() + " "+
                        "ON " + WifiTable.NODE_ID.getFullCol() + " " +
                            "= " + NodeTable.ID.getFullCol() + " "+
                "WHERE " + WifiTable.BSS.getFullCol() + " IN (?)";

        Cursor cursor = getDatabase().rawQuery(req, new String[]{"'"+param+"'"});


        // TODO read data
        return loadAllPlan(cursor);
    }



    /**
     * Load all specific plan
     *
     * @param cursor cursor of sql request
     * @return
     */
    private static ArrayList<Plan> loadAllPlan(Cursor cursor) {
        return loadAllPlan(cursor, null);
    }

    /**
     * Load all specific plan
     *
     * @param cursor cursor of sql request
     * @param campus campus object in link with all plan (null if not define)
     * @return an ArrayList with all plan object
     */
    private static ArrayList<Plan> loadAllPlan(Cursor cursor, Campus campus) {
        ArrayList<Plan> res = new ArrayList<Plan>();

        int id;
        int campusID;
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
                campusID = getInt(cursor, PlanTable.CAMPUS_ID.getCol());
                directoryImage = getString(cursor, PlanTable.IMAGE_DIRECTORY.getCol());
                xOnParent = getFloat(cursor, PlanTable.X_ON_PARENT.getCol());
                yOnParent = getFloat(cursor, PlanTable.Y_ON_PARENT.getCol());
                bgCoordX = getFloat(cursor, PlanTable.BG_COORD_X.getCol());
                bgCoordY = getFloat(cursor, PlanTable.BG_COORD_Y.getCol());
                relativeAngle = getFloat(cursor, PlanTable.RELATIVE_ANGLE.getCol());
                distance = getFloat(cursor, PlanTable.PPM.getCol());

                if(campus == null) {
                    campus = Graph.getCampus(campusID);
                }

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
    @Nullable
    public static Plan loadPlan(String planName) throws SQLiteException {
        Cursor cursor = getDatabase().rawQuery("SELECT bSource.*, " +
                            "bCampus." + PlanTable.NAME.getCol() + " AS campusName " +
                        "FROM " + PlanTable.getName() + " bSource " +
                            "JOIN " + PlanTable.getName() + " bCampus " +
                                "ON bCampus." + PlanTable.ID.getCol() +
                                    " = bSource." + PlanTable.CAMPUS_ID.getCol() + " " +
                        "WHERE bSource." + PlanTable.NAME.getCol() + " = ?",
                new String[]{planName});

        return loadPlan(cursor);
    }

    /**
     * Load all informations about a plan
     *
     * @param IdPlan id of the plan
     * @return the created plan object
     */
    @Nullable
    public static Plan loadPlan(int IdPlan) {
        Cursor cursor = getDatabase().rawQuery("SELECT bSource.*, " +
                            "bCampus." + PlanTable.NAME.getCol() + " AS campusName " +
                        "FROM " + PlanTable.getName() + " bSource " +
                            "JOIN " + PlanTable.getName() + " bCampus " +
                                "ON bCampus." + PlanTable.ID.getCol() +
                                    " = bSource." + PlanTable.CAMPUS_ID.getCol() + " " +
                        "WHERE bSource." + PlanTable.ID.getCol() + " = ?",
                new String[]{"" + IdPlan});

        return loadPlan(cursor);
    }

    /**
     * Load all informations about  plan
     *
     * @param cursor result of sql request
     * @return the created plan object
     */
    @Nullable
    private static Plan loadPlan(Cursor cursor) {
        int id;
        String campusName;

        String directoryImage;
        String planName;
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
            planName = getString(cursor, PlanTable.NAME.getCol());
            xOnParent = getFloat(cursor, PlanTable.X_ON_PARENT.getCol());
            yOnParent = getFloat(cursor, PlanTable.Y_ON_PARENT.getCol());
            bgCoordX = getFloat(cursor, PlanTable.BG_COORD_X.getCol());
            bgCoordY = getFloat(cursor, PlanTable.BG_COORD_Y.getCol());
            relativeAngle = getFloat(cursor, PlanTable.RELATIVE_ANGLE.getCol());
            distance = getFloat(cursor, PlanTable.PPM.getCol());

        } else {
            throw new SQLiteException("Multiple building have the same name (or id)");
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
     * Get all plan ID
     *
     * @return an ArrayList with all id
     */
    public static ArrayList<Integer> getAllPlanID() {
        ArrayList<Integer> res = new ArrayList<Integer>();

        Cursor cursor = getDatabase().rawQuery("SELECT " + PlanTable.ID.getCol() + " " +
                "FROM " + PlanTable.getName(), null);

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                res.add(getInt(cursor, PlanTable.ID.getCol()));
                cursor.moveToNext();
            }
        }

        cursor.close();

        return res;
    }


    /////////////// NODES ///////////////

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


    /////////////// ALIAS ///////////////

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
        cursor.close();

        return res;
    }


    /////////////// WIFI ///////////////

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
        cursor.close();

        return res;
    }


    /**
     * Load specific wifi (based on bss list) and return all concerned plan
     *
     * @param listBSS the bss capt by network card
     * @return HashSet with the concerned plan
     */
    public static HashSet<Plan> loadSpecificWifi(ArrayList<String> listBSS) {
        HashSet<Plan> res = new HashSet<Plan>();

        String param = TextUtils.join("', '", listBSS);

        String reqStr = "SELECT DISTINCT " + WifiTable.getName() + ".* " + // TODO
                "FROM " + WifiTable.getName() + " wifi "+
                    "JOIN " + WifiTable.getName() + " joinWifi " +
                        "ON joinWifi." + WifiTable.NODE_ID.getCol() + " = " +
                                "wifi." + WifiTable.NODE_ID.getCol() + " " +
                "WHERE joinWifi." + WifiTable.BSS.getCol() + " IN (?)";

        Cursor cursor = getDatabase().rawQuery(reqStr, new String[]{"'"+param+"'"});

        HashMap<Integer, Node> cacheNode = new HashMap<Integer, Node>();

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            String bss;
            float avg;
            float variance;
            int nodeId;

            while (!cursor.isAfterLast()) {
                bss = getString(cursor, WifiTable.BSS.getCol());
                avg = getFloat(cursor, WifiTable.AVG.getCol());
                variance = getFloat(cursor, WifiTable.VARIANCE.getCol());
                nodeId = getInt(cursor, WifiTable.NODE_ID.getCol());

                Node node;
                if(cacheNode.containsKey(nodeId)) {
                    node = cacheNode.get(nodeId);
                } else {
                    node = Graph.getNode(nodeId);
                    cacheNode.put(nodeId, node);
                }
                node.addWifi(new Wifi(bss, avg, variance));
                res.add(node.getParentPlan());

                cursor.moveToNext();
            }

        }
        cursor.close();

        Log.d(SQLUtils.class.getName(), "Load wifi: " + res.size());

        return res;
    }




    /////////////// PATH ///////////////

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
                EdgeTable.NODE_1_ID.getCol() + " = ?", new String[]{""+nodeID}, null, null, null);


        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            int idOne;
            int idTwo;

            Node nodeOne;
            Node nodeTwo;

            while(!cursor.isAfterLast()) {
                idOne = getInt(cursor, EdgeTable.NODE_1_ID.getCol());
                idTwo = getInt(cursor, EdgeTable.NODE_2_ID.getCol());

                if(idOne != nodeID) { // We check that node one has the good id
                    // If not, create an error !
                    Log.e(SQLUtils.class.getName(), "The SQL response is not valide (Search edge with " +
                            "node: " + nodeID + " and return: " + idOne + " & " + idTwo + ")");
                    cursor.moveToNext();
                    continue; // next !
                }

//                Log.i(SQLUtils.class.getName(), "Edge: idTwo: " + idTwo + " -> " + nodeID);

                nodeOne = node;

                // Now we search the second id:
                nodeTwo = plan.getNode(idTwo);
                if(nodeTwo == null) { // If not found in the current plan
                    nodeTwo = Graph.getNode(idTwo); // Search in all plans
                }

                if(nodeTwo != null) { // If found :)
                    res.add(new Path(nodeOne, nodeTwo));
                } else {
                    Log.w(SQLUtils.class.getName(), "Paht not created " + nodeID + " & " + idTwo);
                }

                cursor.moveToNext();
            }

        }
        cursor.close();

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
        cursor.close();

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
