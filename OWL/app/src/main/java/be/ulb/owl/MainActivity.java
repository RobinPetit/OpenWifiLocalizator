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

import be.ulb.owl.demo.GraphDemo;
import be.ulb.owl.graph.Graph;
import be.ulb.owl.graph.NoPathException;
import be.ulb.owl.graph.Node;
import be.ulb.owl.graph.Path;
import be.ulb.owl.graph.Plan;
import be.ulb.owl.gui.DrawView;
import be.ulb.owl.gui.listener.ClickListenerChoseLocal;
import be.ulb.owl.gui.listener.ClickListenerLocalize;
import be.ulb.owl.gui.listener.ClickListenerSwitchButton;
import be.ulb.owl.gui.listener.QueryTextListener;
import be.ulb.owl.gui.listener.TouchListener;
import be.ulb.owl.test.GraphTest;
import be.ulb.owl.test.Test;
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

    private static final boolean DEBUG = true;          // view info message in log (maybe more after)
    private static final boolean TEST = DEBUG && false; // active to call test (active also DEBUG)
    private static final boolean DEMO = false;          // active to active

    private static final String[] NOT_SUGGESTED = {"Mystery"};

    // android widgets
    private ImageView _imageView;
    private ImageView _imageDraw;
    private Canvas _canvas = null;
    private MaterialSearchView _searchView = null;  // the widget with the searchbar and autocompletion

    // private attributes
    private Graph _graph = null;
    private Plan _currentPlan = null;
    private Node _currentPosition;
    private ArrayList<Node> _destinationNodes =  new ArrayList<Node>();
    private DrawView _drawer;



    //////////////////////////////////////////// EVENTS ////////////////////////////////////////////
    // Event called by Android

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
        Log.i(getClass().getName(), "Log loaded !");

        setContentView(R.layout.activity_main);

        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));

        _imageView = (ImageView)findViewById(R.id.plan);
        _imageDraw = (ImageView)findViewById(R.id.draw);
        _imageDraw.setOnTouchListener(new TouchListener());


        // Load sql
        SQLUtils.initSQLUtils(this);
        Log.i(getClass().getName(), "SQL Loaded !");

        // Load graph
        if(isDemo()) {
            _graph = new GraphDemo();

        } else if(isTest()) {
            _graph = new GraphTest();

        } else {
            _graph = new Graph();

        }
        Log.i(getClass().getName(), "Graph loaded !");

        _currentPlan = _graph.getDefaultCampus();


        // Init buttons listener
        initSwitchPlanButton();
        initChoseLocalButton();
        initLocalizeButton();
        Log.i(getClass().getName(), "Button loaded !");
    }


    @Override
    protected void onStart() {
        super.onStart();

        // Set default plan
        setCurrentPlan(_currentPlan);
        this.setUpCanvas();
        _drawer = new DrawView(this, _canvas, getWidthShrinkageFactor(), getHeightShrinkageFactor());

        if(TEST) {
            // TODO change plan... if we make automatic test ? :/
            setCurrentPlan(_graph.getPlanByName("P.F"));

            Test.testBestPath();
            Test.testWifi();
        }

        if (DEMO) {
            setCurrentPlan(_graph.getPlanByName("P.F"));
        }

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
        for(Node node: _graph.getAllNodes()) {
            _searchView.addSuggestions(node.getAlias());
        }

        for(String suggestion : NOT_SUGGESTED) {
            _searchView.removeSuggestion(suggestion);
        }

        return true;
    }


    /**
     * Method called when the `back` button is pressed
     */
    @Override
    public void onBackPressed() {
        // If back is pressed while the searchbar is being used, then close it and kep going
        if(_searchView != null && _searchView.isOpen()) {
            _searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }


    /**
     * Method called when any element of the application is selected
     *
     * @param item The selected item
     * @return True or default onOptionItemSelected
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



    ///////////////////////////////////// INIT ANDROID VIEW /////////////////////////////////////

    /**
     * Initialize the "switch plan" button<br />
     * To switch between the campus
     */
    private void initSwitchPlanButton() {
        Button changePlan = (Button)findViewById(R.id.changePlan);
        changePlan.setOnClickListener(new ClickListenerSwitchButton(this, _graph));
    }

    /**
     * Initialize the "chose local" button<br />
     * To choose a specific local in the current plan
     */
    private void initChoseLocalButton() {
        Button local = (Button)findViewById(R.id.local);
        local.setOnClickListener(new ClickListenerChoseLocal(this));
    }

    /**
     * Initialize the "localize" button<br />
     * To force the localization
     */
    private void initLocalizeButton() {
        Button localizeButton = (Button)findViewById(R.id.localizeButton);
        localizeButton.setOnClickListener(new ClickListenerLocalize(_graph));
    }



    /**
     * Set up the main screen (where we see the plan)
     */
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
     *
     * @param pathList An ArrayList of Path representing the nodes to pass by
     */
    public void drawPath(List<Path> pathList) {
        cleanCanvas();
        draw(_currentPosition);
        _drawer.draw(pathList);
    }


    /**
     * Removes anything being on the canvas
     */
    public void cleanCanvas() {
        if(_canvas != null) {
            _canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        }
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
        _drawer.draw(node);
        _imageDraw.invalidate();
        _imageView.invalidate();
    }



    ///////////////////////////////////// GETTER AND SETTER /////////////////////////////////////

    /**
     * Get the graph object
     *
     * @return Graph
     */
    public final Graph getGraph() {
        return _graph;
    }

    /**
     * Tell the application the word that we search AND find/draw the new path
     *
     * @param dest Name of the destination
     */
    public void setDestination(String dest) throws NoPathException {
        _destinationNodes = _graph.searchNode(dest);
        _graph.findPath();
    }

    /**
     * Get all node where the user will go
     *
     * @return an ArrayList with all nodes
     */
    public final ArrayList<Node> getDestinations() {
        return _destinationNodes;
    }


    /**
     * Get the name of the application
     *
     * @return String with the app name
     */
    public final String getAppName() {
        return getResources().getString(R.string.app_name);
    }


    /**
     * Get the current plan which is show
     *
     * @return the current plan
     */
    public final Plan getCurrentPlan() {
        return _currentPlan;
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
     * Get the image which is currently show
     *
     * @return the ImageView
     */
    public final ImageView getImageView() {
        return _imageView;
    }

    /**
     * Get the current location
     *
     * @return the Node where is currently the user
     */
    public final Node getCurrentLocation() {
        return _currentPosition;
    }


    /**
     * Change the current location/position Node
     *
     * @param newLocation the new position
     * @return True if the location havec change
     */
    public boolean setCurrentLocation(Node newLocation) {
        Node oldLocation = _currentPosition;
        _currentPosition = newLocation;
        return (oldLocation != newLocation);
    }



    /////////////////////////////////////////// STATIC ///////////////////////////////////////////

    /**
     * Get the main instance
     *
     * @return MainActivity instance
     */
    public static MainActivity getInstance() {
        return instance;
    }

    /**
     * Check if app is in DEBUG mode
     *
     * @return True if the app is in Debug mode
     */
    public static boolean isDebug() {
        return DEBUG;
    }

    /**
     * Check if app is in TEST mode
     *
     * @return True if we test application
     */
    public static boolean isTest() {
        return TEST;
    }

    /**
     * Check if the app is in DEMO test
     *
     * @return True if we make a demo of the application
     */
    public static boolean isDemo() { return DEMO; }


}
