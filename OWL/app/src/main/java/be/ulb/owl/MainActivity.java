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
import android.graphics.Paint;
import android.graphics.Color;
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

    // android widgets
    private ImageView _imageView;
    private ImageView _imageDraw;
    private Bitmap _bitmap = null; // temp
    private Paint _paint = null; //temp
    private Canvas _canvas = null; // temp
    private MaterialSearchView _searchView = null;  // the widget with the searchbar and autocompletion

    // private attributes
    private Graph _graph = null;
    private Plan _currentPlan = null;
    private Node _currentPosition;
    private String _destinationName;  // null if none


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
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(_graph == null) {
            _graph = new Graph();
        }
        _graph.startScanTask();

        // Set default plan
        setCurrentPlan(Graph.getPlan("Solbosch"));
        this.setUpCanvas();

        Log.i(getClass().getName(), "Scanner.scan");
        localize();
        if(TEST) {
            setCurrentPlan(_graph.getPlanByName("P.F"));
            testWifi();
            testBestPath();
        }
    }


    private void setUpCanvas() {
        if(_imageView.getDrawable() != null) {
            Integer width = _imageView.getDrawable().getIntrinsicWidth();
            Integer height = _imageView.getDrawable().getIntrinsicHeight();
            _bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            _bitmap = _bitmap.copy(_bitmap.getConfig(), true);
            _canvas = new Canvas(_bitmap);
            _paint = new Paint();
            _paint.setColor(Color.RED);
            _paint.setStyle(Paint.Style.FILL_AND_STROKE);
            _paint.setAntiAlias(true);
            _imageDraw.setImageBitmap(_bitmap);
        } else {
            Log.w(getClass().getName(), "_imageView have no drawable");
        }
    }

    /**
     * Returns the campus plan of a given plan
     *
     * @param plan The plan to determine the campus of
     * @return The campus plan containing plan
     */
    public static Plan getRootParentOfPlan(Plan plan) {
        return plan.getName().charAt(0) == 'S' ? Graph.getPlan("Solbosch", false) : Graph.getPlan("Plaine", false);
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
            Log.i(getClass().getName(), "Testing best path between nodes " + src.getName() + " and " + dest.getName());
            try {
                ArrayList<Path> overallPath = _graph.bestPath(src, dest);
                String pathString = src.getName();
                Node current = src;
                int k = 0;
                while(!current.equals(dest)) {
                    current = overallPath.get(k++).getOppositNodeOf(current);
                    pathString += " --> " + current.getName();
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
        tmp.add(new Wifi("00:26:cb:a0:aa:c1", 72.0f, 71.0f, 71.2f));
        tmp.add(new Wifi("c8:b3:73:4b:01:c9", 85.0f, 83.0f, 84.2f));
        tmp.add(new Wifi("00:26:cb:4d:d9:41", 79.0f, 71.0f, 75.4f));
        tmp.add(new Wifi("00:0c:e6:00:cf:b3", 75.0f, 69.0f, 70.8f));
        tmp.add(new Wifi("00:0c:e6:00:d2:a4", 57.0f, 47.0f, 52.0f));
        tmp.add(new Wifi("00:0c:e6:00:d1:3c", 73.0f, 56.0f, 61.2f));
        tmp.add(new Wifi("00:0c:e6:00:d1:36", 82.0f, 81.0f, 81.2f));
        tmp.add(new Wifi("d0:57:4c:cb:4a:71", 77.0f, 77.0f, 77.0f));
        tmp.add(new Wifi("fe:52:8d:c6:63:5a", 86.0f, 86.0f, 86.0f));
        tmp.add(new Wifi("00:26:cb:a0:aa:c0", 71.0f, 71.0f, 71.0f));
        tmp.add(new Wifi("00:0c:e6:00:d1:2d", 79.0f, 78.0f, 78.8f));
        tmp.add(new Wifi("00:26:cb:4e:04:50", 65.0f, 63.0f, 64.5f));
        tmp.add(new Wifi("00:26:cb:4d:d9:40", 79.0f, 73.0f, 74.5f));
        tmp.add(new Wifi("00:0c:e6:00:d1:0c", 71.0f, 66.0f, 67.5f));
        tmp.add(new Wifi("00:26:cb:4e:04:51", 65.0f, 64.0f, 64.7f));
        tmp.add(new Wifi("c8:b3:73:4b:01:ca", 85.0f, 84.0f, 84.7f));
        tmp.add(new Wifi("30:b5:c2:df:fd:60", 88.0f, 81.0f, 84.3f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:72", 89.0f, 89.0f, 89.0f));
        tmp.add(new Wifi("00:26:cb:4e:07:f0", 79.0f, 79.0f, 79.0f));
        tmp.add(new Wifi("00:26:cb:4e:07:f1", 79.0f, 78.0f, 78.7f));
        tmp.add(new Wifi("00:37:b7:64:c3:66", 90.0f, 90.0f, 90.0f));

        Node position = _graph.whereAmI(tmp);
        draw(position);
        System.out.print("Test 1.\n res = ");
        System.out.println(position.getName());

        // ---- test ----

        tmp = new ArrayList<Wifi>();
        tmp.add(new Wifi("00:26:cb:a0:aa:c1", 69.0f, 65.0f, 66.8f));
        tmp.add(new Wifi("00:0c:e6:00:cf:b3", 85.0f, 79.0f, 81.6f));
        tmp.add(new Wifi("00:0c:e6:00:d2:a4", 64.0f, 51.0f, 58.6f));
        tmp.add(new Wifi("00:0c:e6:00:d1:3c", 82.0f, 69.0f, 76.8f));
        tmp.add(new Wifi("00:0c:e6:00:d1:0c", 64.0f, 64.0f, 64.0f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:70", 87.0f, 74.0f, 79.8f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:71", 89.0f, 72.0f, 82.0f));
        tmp.add(new Wifi("c8:b3:73:4b:01:c9", 92.0f, 92.0f, 92.0f));
        tmp.add(new Wifi("10:9a:dd:b5:2a:3f", 88.0f, 85.0f, 86.6f));
        tmp.add(new Wifi("00:26:cb:4d:d9:41", 79.0f, 75.0f, 76.6f));
        tmp.add(new Wifi("00:0c:e6:00:d1:2d", 86.0f, 85.0f, 85.2f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:72", 87.0f, 74.0f, 79.4f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:73", 80.0f, 80.0f, 80.0f));
        tmp.add(new Wifi("00:26:cb:4e:07:f0", 73.0f, 71.0f, 71.4f));
        tmp.add(new Wifi("00:26:cb:4e:04:50", 79.0f, 69.0f, 74.4f));
        tmp.add(new Wifi("00:26:cb:4e:07:f1", 81.0f, 71.0f, 77.8f));
        tmp.add(new Wifi("00:26:cb:4e:04:51", 79.0f, 69.0f, 73.8f));
        tmp.add(new Wifi("00:26:cb:4d:d9:40", 77.0f, 77.0f, 77.0f));
        tmp.add(new Wifi("00:26:cb:a0:aa:c0", 77.0f, 67.0f, 70.3f));

        position = _graph.whereAmI(tmp);
        draw(position);
        System.out.print("Test 2.\n res = ");
        System.out.println(position.getName());

        // ---- test ----

        tmp = new ArrayList<Wifi>();
        tmp.add(new Wifi("00:26:cb:4e:0e:e1", 53.0f, 33.0f, 37.8f));
        tmp.add(new Wifi("00:0c:e6:00:d2:a4", 82.0f, 82.0f, 82.0f));
        tmp.add(new Wifi("00:26:cb:a0:aa:c1", 85.0f, 81.0f, 83.2f));
        tmp.add(new Wifi("00:26:cb:4e:0e:e0", 35.0f, 32.0f, 33.2f));
        tmp.add(new Wifi("00:0c:e6:00:d1:0c", 88.0f, 88.0f, 88.0f));

        position = _graph.whereAmI(tmp);
        draw(position);
        System.out.print("Test 3.\n res = ");
        System.out.println(position.getName());

        // ---- test ----

        tmp = new ArrayList<Wifi>();
        tmp.add(new Wifi("00:26:cb:4e:0e:e1", 64.0f, 54.0f, 57.0f));
        tmp.add(new Wifi("00:26:cb:a0:aa:c1", 84.0f, 63.0f, 71.4f));
        tmp.add(new Wifi("00:26:cb:4e:0e:e0", 61.0f, 32.0f, 52.4f));
        tmp.add(new Wifi("00:0c:e6:00:d1:0c", 79.0f, 78.0f, 78.6f));
        tmp.add(new Wifi("00:0c:e6:00:d1:3c", 81.0f, 80.0f, 80.2f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:70", 88.0f, 88.0f, 88.0f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:72", 91.0f, 91.0f, 91.0f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:71", 91.0f, 91.0f, 91.0f));
        tmp.add(new Wifi("24:b6:57:8d:34:41", 81.0f, 79.0f, 80.6f));
        tmp.add(new Wifi("00:26:cb:4e:04:50", 69.0f, 69.0f, 69.0f));
        tmp.add(new Wifi("00:26:cb:4e:04:51", 69.0f, 66.0f, 67.8f));
        tmp.add(new Wifi("00:26:cb:a0:aa:c0", 75.0f, 62.0f, 71.2f));

        position = _graph.whereAmI(tmp);
        draw(position);
        System.out.print("Test 4.\n res = ");
        System.out.println(position.getName());

        // ---- test ----

        tmp = new ArrayList<Wifi>();
        tmp.add(new Wifi("00:26:cb:a0:aa:c1", 86.0f, 86.0f, 86.0f));
        tmp.add(new Wifi("00:0c:e6:00:d1:3c", 84.0f, 80.0f, 82.4f));
        tmp.add(new Wifi("00:26:cb:a0:aa:c0", 69.0f, 69.0f, 69.0f));
        tmp.add(new Wifi("00:26:cb:4e:0c:a1", 76.0f, 68.0f, 71.0f));
        tmp.add(new Wifi("00:26:cb:4e:0c:a0", 76.0f, 69.0f, 71.2f));
        tmp.add(new Wifi("10:9a:dd:b5:2a:3f", 90.0f, 88.0f, 88.4f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:70", 87.0f, 85.0f, 85.8f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:72", 89.0f, 87.0f, 87.8f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:71", 85.0f, 84.0f, 84.4f));
        tmp.add(new Wifi("00:0c:e6:00:d1:0c", 84.0f, 80.0f, 82.4f));
        tmp.add(new Wifi("00:0c:e6:00:d2:a4", 78.0f, 70.0f, 76.0f));
        tmp.add(new Wifi("00:26:cb:4e:0e:e0", 77.0f, 76.0f, 76.3f));
        tmp.add(new Wifi("00:26:cb:4e:0e:e1", 77.0f, 76.0f, 76.7f));
        tmp.add(new Wifi("00:0c:e6:00:d1:2d", 84.0f, 84.0f, 84.0f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:73", 86.0f, 86.0f, 86.0f));

        position = _graph.whereAmI(tmp);
        draw(position);
        System.out.print("Test 5.\n res = ");
        System.out.println(position.getName());

        // ---- test ----

        tmp = new ArrayList<Wifi>();
        tmp.add(new Wifi("00:26:cb:4e:0c:a1", 85.0f, 77.0f, 81.8f));
        tmp.add(new Wifi("00:26:cb:4e:0e:e1", 60.0f, 52.0f, 54.8f));
        tmp.add(new Wifi("00:0c:e6:00:d2:a4", 80.0f, 62.0f, 73.0f));
        tmp.add(new Wifi("00:0c:e6:00:cf:b3", 89.0f, 81.0f, 83.6f));
        tmp.add(new Wifi("00:0c:e6:00:d1:2d", 82.0f, 74.0f, 78.8f));
        tmp.add(new Wifi("00:26:cb:4e:0e:e0", 61.0f, 55.0f, 58.8f));
        tmp.add(new Wifi("30:b5:c2:df:fd:60", 87.0f, 84.0f, 86.0f));
        tmp.add(new Wifi("f8:e9:03:cb:00:a0", 87.0f, 87.0f, 87.0f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:74", 87.0f, 87.0f, 87.0f));
        tmp.add(new Wifi("d4:6d:50:f2:c7:73", 90.0f, 85.0f, 87.5f));

        position = _graph.whereAmI(tmp);
        draw(position);
        System.out.print("Test 6.\n res = ");
        System.out.println(position.getName());
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
        new DrawView(this, _canvas, getWidthShrinkageFactor(), getHeightShrinkageFactor()).draw(pathList);
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
        if(cleanBefore) {
            cleanCanvas();
        }
        new DrawView(this, _canvas, getWidthShrinkageFactor(), getHeightShrinkageFactor()).draw(node);
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
                //this.draw(current);
                if(_destinationName != null) {
                    try {
                        _graph.findPath(_destinationName);
                    } catch (NoPathException e) {
                        Log.e(getClass().getName(), "Error: should have found an alternative for a path between "
                                + _currentPosition.getName() + " and " + _destinationName);
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

}
