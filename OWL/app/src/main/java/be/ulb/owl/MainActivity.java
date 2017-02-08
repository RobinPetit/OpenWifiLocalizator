package be.ulb.owl;

import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import java.util.ArrayList;
import java.util.List;

import be.ulb.owl.graph.Graph;
import be.ulb.owl.graph.NoPathException;
import be.ulb.owl.graph.Node;
import be.ulb.owl.graph.Path;
import be.ulb.owl.graph.Plan;
import be.ulb.owl.gui.DrawView;
import be.ulb.owl.gui.listener.ClickListener;
import be.ulb.owl.gui.listener.QueryTextListener;
import be.ulb.owl.gui.listener.TouchListener;
import be.ulb.owl.utils.DialogUtils;
import be.ulb.owl.utils.LogUtils;
import be.ulb.owl.utils.SQLUtils;
import br.com.mauker.materialsearchview.MaterialSearchView;

/**
 * Main file for Android application<br/>
 * See this file before: https://developer.android.com/images/training/basics/basic-lifecycle-create.png<br/>
 * Doc: https://developer.android.com/training/basics/activity-lifecycle/starting.html
 *
 *
 * @author Nathan, Detobel36
 */
public class MainActivity extends AppCompatActivity  {

    // static attributes
    private static MainActivity instance;
    private static final boolean DEBUG = true; // view info message in log (maybe more after)
    private static final boolean TEST = false;   // active to call test
    private static final boolean DEMO = false; // active to active
    private static final String[] NOT_SUGGESTED = {"Mystery"};
    // TODO supprimé pour remplacer par une req sql
    private static final String SOLBOSCH_PLAN = "Solbosch";

    // android widgets
    private ImageView _imageView;
    private ImageView _imageDraw;
    private Canvas _canvas = null;
    private MaterialSearchView _searchView = null;  // the widget with the searchbar and autocompletion

    // private attributes
    private Graph _graph = null;
    private Plan _currentPlan = null;
    private Node _currentPosition;
    private String _destinationName;  // null if none
    private DrawView _drawer;


    /**
     * Call when the application is created the first time
     *
     * @param savedInstanceState ignored
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;

        LogUtils.initLogSystem();
        setContentView(R.layout.activity_main);

        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));

        _imageView = (ImageView)findViewById(R.id.plan);
        _imageDraw = (ImageView)findViewById(R.id.draw);
        _imageDraw.setOnTouchListener(new TouchListener());

        Button changePlan;
        Button local;
        Button localizeButton;

        // Define click listener
        ClickListener clickListener = new ClickListener();

        // init buttons
        changePlan = (Button)findViewById(R.id.changePlan);
        changePlan.setOnClickListener(clickListener);

        local = (Button)findViewById(R.id.local);
        local.setOnClickListener(clickListener);

        localizeButton = (Button)findViewById(R.id.localizeButton);
        localizeButton.setOnClickListener(clickListener);

        // Load sql
        SQLUtils.initSQLUtils(this);

    }

    @Override
    protected void onStart() {
        super.onStart();

        if(_graph == null) {
            _graph = new Graph();
        }

        Log.i(getClass().getName(), "Name: " + _graph.getAllCampus().get(0).getName());


        // Set default plan
        setCurrentPlan(Graph.getCampus(SOLBOSCH_PLAN));
        this.setUpCanvas();
        _drawer = new DrawView(this, _canvas, getWidthShrinkageFactor(), getHeightShrinkageFactor());

        if(TEST) {
            setCurrentPlan(_graph.getPlanByName("P.F"));
            testWifi();
            testBestPath();
        }

        if (DEMO) {
            setCurrentPlan(_graph.getPlanByName("P.F"));
        }
    }


    private void setUpCanvas() {
        if(_imageView.getDrawable() != null) {
            Integer height = _imageView.getDrawable().getIntrinsicWidth();
            Integer width = _imageView.getDrawable().getIntrinsicHeight();
            Bitmap bitmap;
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap = bitmap.copy(bitmap.getConfig(), true);
            _canvas = new Canvas(bitmap);
            _imageDraw.setImageBitmap(bitmap);
        } else {
            Log.w(getClass().getName(), "_imageView have no drawable");
        }
    }

    public final Graph getGraph() {
        return _graph;
    }

    /**
     * Tell the application what node has ben chosen to be reached
     *
     * @param dest Name of the destination
     */
    public void setDestination(String dest) {
        _destinationName = dest;
    }

    /**
     * When we hide the application
     * @see #onDestroy() when application is destroy
     */
    @Override
    protected void onStop() {
        super.onStop();
        _graph.hidden(); // (also) Reset wifi settings
    }

    private void testBestPath() {
        // 57 & 3
        int[] startingEnd = {21, 57};
        int[] arrivalEnd = {14, 3};
        assert(startingEnd.length == arrivalEnd.length);
        ArrayList<Node> allNodes = _graph.getAllNodes();
        for(int i = 0; i < startingEnd.length; ++i) {
            Node src = allNodes.get(startingEnd[i]);
            Node dest = allNodes.get(arrivalEnd[i]);
            Log.i(getClass().getName(), "Testing best path between nodes " + src.getID() + " and " + dest.getID());
            try {
                ArrayList<Path> overallPath = _graph.bestPath(src, dest);
                String pathString = ""+src.getID();
                Node current = src;
                int k = 0;
                while(!current.equals(dest)) {
                    current = overallPath.get(k++).getOppositeNodeOf(current);
                    pathString += " --> " + current.getID();
                }
                Log.i(getClass().getName(), "Found path is given by: " + pathString);
                if(i == 1)
                    drawPath(overallPath);
            } catch (NoPathException e) {
                Log.e(getClass().getName(), "No path has been found between nodes " + startingEnd[i]
                        + " and " + arrivalEnd[i] + " even though it was supposed to!");
            }
        }
    }

    private void testWifi() {
        // ---- test ----

        ArrayList<Wifi> tmp = new ArrayList<Wifi>();
        tmp.add(new Wifi("00:26:cb:a0:aa:c1", 71.2f, 0f));
        tmp.add(new Wifi("c8:b3:73:4b:01:c9", 84.2f, 0f));
        tmp.add(new Wifi("00:26:cb:4d:d9:41", 75.4f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:cf:b3", 70.8f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d2:a4", 52.0f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d1:3c", 61.2f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d1:36", 81.2f, 0f));
        tmp.add(new Wifi("d0:57:4c:cb:4a:71", 77.0f, 0f));
        tmp.add(new Wifi("fe:52:8d:c6:63:5a", 86.0f, 0f));
        tmp.add(new Wifi("00:26:cb:a0:aa:c0", 71.0f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d1:2d", 78.8f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:04:50", 64.5f, 0f));
        tmp.add(new Wifi("00:26:cb:4d:d9:40", 74.5f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d1:0c", 67.5f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:04:51", 64.7f, 0f));
        tmp.add(new Wifi("c8:b3:73:4b:01:ca", 84.7f, 0f));
        tmp.add(new Wifi("30:b5:c2:df:fd:60", 84.3f, 0f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:72", 89.0f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:07:f0", 79.0f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:07:f1", 78.7f, 0f));
        tmp.add(new Wifi("00:37:b7:64:c3:66", 90.0f, 0f));

        Node position = _graph.whereAmI(tmp);
        draw(position);
        System.out.print("Test 1.\n res = ");
        System.out.println(position.getID());

        // ---- test ----

        tmp = new ArrayList<Wifi>();
        tmp.add(new Wifi("00:26:cb:a0:aa:c1", 66.8f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:cf:b3", 81.6f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d2:a4", 58.6f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d1:3c", 76.8f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d1:0c", 64.0f, 0f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:70", 79.8f, 0f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:71", 82.0f, 0f));
        tmp.add(new Wifi("c8:b3:73:4b:01:c9", 92.0f, 0f));
        tmp.add(new Wifi("10:9a:dd:b5:2a:3f", 86.6f, 0f));
        tmp.add(new Wifi("00:26:cb:4d:d9:41", 76.6f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d1:2d", 85.2f, 0f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:72", 79.4f, 0f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:73", 80.0f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:07:f0", 71.4f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:04:50", 74.4f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:07:f1", 77.8f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:04:51", 73.8f, 0f));
        tmp.add(new Wifi("00:26:cb:4d:d9:40", 77.0f, 0f));
        tmp.add(new Wifi("00:26:cb:a0:aa:c0", 70.3f, 0f));

        position = _graph.whereAmI(tmp);
        draw(position);
        System.out.print("Test 2.\n res = ");
        System.out.println(position.getID());

        // ---- test ----

        tmp = new ArrayList<Wifi>();
        tmp.add(new Wifi("00:26:cb:4e:0e:e1", 37.8f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d2:a4", 82.0f, 0f));
        tmp.add(new Wifi("00:26:cb:a0:aa:c1", 83.2f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:0e:e0", 33.2f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d1:0c", 88.0f, 0f));

        position = _graph.whereAmI(tmp);
        draw(position);
        System.out.print("Test 3.\n res = ");
        System.out.println(position.getID());

        // ---- test ----

        tmp = new ArrayList<Wifi>();
        tmp.add(new Wifi("00:26:cb:4e:0e:e1", 57.0f, 0f));
        tmp.add(new Wifi("00:26:cb:a0:aa:c1", 71.4f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:0e:e0", 52.4f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d1:0c", 78.6f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d1:3c", 80.2f, 0f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:70", 88.0f, 0f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:72", 91.0f, 0f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:71", 91.0f, 0f));
        tmp.add(new Wifi("24:b6:57:8d:34:41", 80.6f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:04:50", 69.0f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:04:51", 67.8f, 0f));
        tmp.add(new Wifi("00:26:cb:a0:aa:c0", 71.2f, 0f));

        position = _graph.whereAmI(tmp);
        draw(position);
        System.out.print("Test 4.\n res = ");
        System.out.println(position.getID());

        // ---- test ----

        tmp = new ArrayList<Wifi>();
        tmp.add(new Wifi("00:26:cb:a0:aa:c1", 86.0f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d1:3c", 82.4f, 0f));
        tmp.add(new Wifi("00:26:cb:a0:aa:c0", 69.0f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:0c:a1", 71.0f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:0c:a0", 71.2f, 0f));
        tmp.add(new Wifi("10:9a:dd:b5:2a:3f", 88.4f, 0f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:70", 85.8f, 0f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:72", 87.8f, 0f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:71", 84.4f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d1:0c", 82.4f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d2:a4", 76.0f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:0e:e0", 76.3f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:0e:e1", 76.7f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d1:2d", 84.0f, 0f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:73", 86.0f, 0f));

        position = _graph.whereAmI(tmp);
        draw(position);
        System.out.print("Test 5.\n res = ");
        System.out.println(position.getID());

        // ---- test ----

        tmp = new ArrayList<Wifi>();
        tmp.add(new Wifi("00:26:cb:4e:0c:a1", 81.8f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:0e:e1", 54.8f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d2:a4", 73.0f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:cf:b3", 83.6f, 0f));
        tmp.add(new Wifi("00:0c:e6:00:d1:2d", 78.8f, 0f));
        tmp.add(new Wifi("00:26:cb:4e:0e:e0", 58.8f, 0f));
        tmp.add(new Wifi("30:b5:c2:df:fd:60", 86.0f, 0f));
        tmp.add(new Wifi("f8:e9:03:cb:00:a0", 87.0f, 0f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:74", 87.0f, 0f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:73", 87.5f, 0f));

        position = _graph.whereAmI(tmp);
        draw(position);
        System.out.print("Test 6.\n res = ");
        System.out.println(position.getID());
    }

    /**
     * TODO @NathanLiccardo quand est appellé cet event précisément ? :/
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        _searchView = (MaterialSearchView)findViewById(R.id.search_view);
        // QueryListener is used to detect when the user starts a query for a local
        _searchView.setOnQueryTextListener(new QueryTextListener());
        // OnItemClickListener is used to detect when the user selects a suggestion from the list
        _searchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * method called when the user selects a suggested local
             *
             * @param parent ignored
             * @param view ignored
             * @param position index of the selected item of the list
             * @param id ignored
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                _searchView.setQuery(_searchView.getSuggestionAtPosition(position), true);
            }
        });
        // give all of the nodes from the graph as available suggestions  (may bbe refined)
        for(Node node: _graph.getAllNodes())
            _searchView.addSuggestions(node.getAlias());
        for(String suggestion : NOT_SUGGESTED)
            _searchView.removeSuggestion(suggestion);

        return true;
    }

    /**
     * method called when the `back` button is pressed
     */
    @Override
    public void onBackPressed() {
        // if back is pressed while the searchbar is being used, then close it and kep going
        if(_searchView != null && _searchView.isOpen())
            _searchView.closeSearch();
        else
            super.onBackPressed();
    }

    /**
     * method called when any element of the application is selected
     * @param item The selected item
     * @return True
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // if the searchbar is clicked on, then open it (and allow suggestions)
        if(item.getItemId() == R.id.search_item) {
            _searchView.openSearch();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Get the name of the application
     *
     * @return String with the app name
     */
    public String getAppName() {
        return getResources().getString(R.string.app_name);
    }

    /**
     * Get the current plan which is show
     *
     * @return the current plan
     */
    public Plan getCurrentPlan() {
        return _currentPlan;
    }



    private float getWidthShrinkageFactor() {
        float trueWidth = (float)((BitmapDrawable)_imageView.getDrawable()).getBitmap().getWidth();
        float effectiveWidth = _imageView.getDrawable().getIntrinsicWidth();
        return trueWidth / effectiveWidth;
    }

    private float getHeightShrinkageFactor() {
        float trueHeight = (float)((BitmapDrawable)_imageView.getDrawable()).getBitmap().getHeight();
        float effectiveHeight = _imageView.getDrawable().getIntrinsicHeight();
        return trueHeight / effectiveHeight;
    }

    /**
     * Draws the given path on the plan on the screen
     * @param pathList An ArrayList of Path representing the nodes to pass by
     */
    public void drawPath(List<Path> pathList) {
        cleanCanvas();
        draw(_currentPosition);
        _drawer.draw(pathList);
    }

    /**
     * Change the current plan which is show on the user
     *
     * @param newCurrentPlan new Plan object
     */
    public void setCurrentPlan(Plan newCurrentPlan) {
        if(newCurrentPlan != null && (_currentPlan == null ||
                !newCurrentPlan.getName().equalsIgnoreCase(_currentPlan.getName())) ) {

            _currentPlan = newCurrentPlan;
            cleanCanvas();
            _imageView.setImageDrawable(_currentPlan.getDrawableImage());
            _imageView.setScaleType(ImageView.ScaleType.MATRIX);

        } else if(newCurrentPlan == null) {
            Log.w(this.getClass().getName(), "New plan is null");
        }
    }

    /**
     * Removes anything being on the canvas
     */
    private void cleanCanvas() {
        if(_canvas != null)
            _canvas.drawColor(0, PorterDuff.Mode.CLEAR);
    }


    /**
     * Draw a node on the current plan
     *
     * @param node the position of the point
     */
    public void draw(Node node) {
        draw(node, false);
    }

    /**
     * Draw a node on the current plan
     *
     * @param node the node which must be draw
     * @param cleanBefore True if the plan must be clean before
     */
    public void draw(Node node, boolean cleanBefore) {
        if(cleanBefore)
            cleanCanvas();
        _drawer.draw(node);
        _imageDraw.invalidate();
        _imageView.invalidate();
    }



    /**
     * Get the image which is currently show
     *
     * @return the ImageView
     */
    public ImageView getImageView() {
        return _imageView;
    }

    /**
     * localizes the user
     */
    public void localize() {
        localize(true);
    }

    /**
     * localizes the user
     * @param displayNotFound Boolean telling whether or not to signal if user is unable to localize
     */
    public void localize(boolean displayNotFound) {
        Node current = _graph.whereAmI();
        if(current != null) {
            setCurrentPlan(current.getParentPlan());
            if(_currentPosition != current) {
                _currentPosition = current;
                cleanCanvas();

                if(_destinationName != null) {
                    try {
                        _graph.findPath(_destinationName);
                    } catch (NoPathException e) {
                        Log.e(getClass().getName(), "Error: should have found an alternative for a path between "
                                + _currentPosition.getID() + " and " + _destinationName);
                    }
                } else
                    this.draw(current);
            }
        } else if (displayNotFound){
            _currentPosition = null;
            DialogUtils.infoBox(this, R.string.not_found, R.string.not_in_ULB);
        }
    }

    public final Node location() {
        return _currentPosition;
    }

    //////////////////////// STATIC ////////////////////////
    /**
     * Get the main instance
     *
     * @return MainActivity instance
     */
    public static MainActivity getInstance() {
        return instance;
    }

    public static boolean isDebug() {
        return DEBUG;
    }

    public static boolean isTest() {
        return TEST;
    }

    public static boolean isDemo() { return DEMO; }

}
