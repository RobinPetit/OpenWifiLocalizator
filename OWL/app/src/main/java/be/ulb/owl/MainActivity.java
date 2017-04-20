package be.ulb.owl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
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
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import be.ulb.owl.demo.GraphDemo;
import be.ulb.owl.graph.Campus;
import be.ulb.owl.graph.Graph;
import be.ulb.owl.graph.NoPathException;
import be.ulb.owl.graph.Node;
import be.ulb.owl.graph.Path;
import be.ulb.owl.graph.Plan;
import be.ulb.owl.gui.DrawView;
import be.ulb.owl.gui.LocalizeButton;
import be.ulb.owl.gui.SwitchButton;
import be.ulb.owl.gui.listener.ClickListenerChoseLocal;
import be.ulb.owl.gui.listener.QueryTextListener;
import be.ulb.owl.gui.listener.TouchListener;
import be.ulb.owl.scanner.Scanner;
import be.ulb.owl.task.SetSearchSuggestionsTask;
import be.ulb.owl.task.SetSearchSuggestionsTaskParam;
import be.ulb.owl.test.GraphTest;
import be.ulb.owl.utils.LogUtils;
import be.ulb.owl.utils.SQLUtils;
import br.com.mauker.materialsearchview.MaterialSearchView;

import static android.view.View.VISIBLE;

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
    private static MainActivity instance = null;

    private static final boolean DEBUG = true;     // view info message in log (maybe more after)
    private static final boolean TEST = false;      // active to call test (active also DEBUG)
    private static final boolean DEMO = false;      // active to active
    private static final String[] NOT_SUGGESTED = {"Mystery"};
    private static final String DEFAULT_SEARCH = "Campus";

    // android widgets
    private ImageView _imageView;
    private ImageView _imageDraw;
    private Canvas _canvas = null;
    private MaterialSearchView _searchView = null;  // the widget with the searchbar and autocompletion
    private SwitchButton _switchButton;

    private RelativeLayout _layout;

    // private attributes
    private Graph _graph = null;
    private Scanner _scanner = null;
    private Plan _currentPlan = null;
    private Node _currentPosition;
    private ArrayList<Node> _destinationNodes =  new ArrayList<Node>();
    private List<Path> _lastPathList; // Save the last path to the destination
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
        Log.d(getClass().getName(), "[DEBUG] OnCreate");
        super.onCreate(savedInstanceState);

        if(instance != null) {
            return;
        }

        instance = this;

        // Init log
        LogUtils.initLogSystem(this);
        Log.i(getClass().getName(), "Log loaded !");

        // Define view
        setContentView(R.layout.activity_main);
        // Define action bar
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));

        // Define image view and draw
        _imageView = (ImageView)findViewById(R.id.plan);
        _imageDraw = (ImageView)findViewById(R.id.draw);
        _imageDraw.setOnTouchListener(new TouchListener(this));
        _layout = (RelativeLayout)findViewById(R.id.frame_layout);

        try {
            // Load sql
            new SQLUtils(this);
            Log.i(getClass().getName(), "[V] SQL Loaded !");

            // Load graph
            if(isDemo()) {
                _graph = new GraphDemo(this);

            } else if(isTest()) {
                _graph = new GraphTest(this);

            } else {
                _graph = new Graph(this);

            }
            Log.i(getClass().getName(), "[V] Graph loaded !");

            // Begin wifi scan
            _scanner = new Scanner(this);
            Log.i(getClass().getName(), "[V] Scanner loaded !");

            // Set default campus
            setCurrentPlan(_graph.getDefaultCampus());

            // Init buttons listener
            initChoseLocalButton();
            _switchButton = new SwitchButton(this);
            new LocalizeButton(this, _scanner, _graph);
            Log.i(getClass().getName(), "[V] Button loaded !");

        } catch(IllegalArgumentException e) {
            Log.e(getClass().getName(), "SQLUtils has already been created once... " +
                    "So if shit gets wrong it's probably somewhere here <3");
        }
        this.setUpCanvas();
        _drawer = new DrawView(this, _canvas, getWidthShrinkageFactor(), getHeightShrinkageFactor());

        // Init suggestion to search bar
        _searchView = (MaterialSearchView)findViewById(R.id.search_view);
        setSearchSuggestions();
    }


    @Override
    protected void onStart() {
        Log.d(getClass().getName(), "[DEBUG] OnStart");
        super.onStart();

        // Force to see the current plan
        setCurrentPlan(_currentPlan);

        // Start scan
        _graph.setDisplayNotFound(true);
    }


    @Override
    public void onResume() {
        Log.d(getClass().getName(), "[DEBUG] onResume");
        super.onResume();

        Log.i(getClass().getName(), "Begin scanner task");
        _scanner.startScanTask(true);
    }

    @Override
    public void onPause() {
        Log.d(getClass().getName(), "[DEBUG] onPause");
        super.onPause();

        Log.i(getClass().getName(), "Stop scanner task");
        _scanner.stopScanTask();
    }

    /**
     * When we hide the application
     * @see #onDestroy() when application is destroy
     */
    @Override
    protected void onStop() {
        Log.d(getClass().getName(), "[DEBUG] OnStop");
        super.onStop();

        Log.i(getClass().getName(), "Reset wifi configuration");
        _scanner.resetWifiStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(getClass().getName(), "[DEBUG] onDestroy");
    }


    /**
     * Call when all other event are loaded.  This is call to create menu when the main screen is
     * allready visible
     *
     * @param menu other menu
     * @return True if all is ok
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        // QueryListener is used to detect when the user starts a query for a local
        _searchView.setOnQueryTextListener(new QueryTextListener(this));
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
                displayPlan();
            }
        });

        _searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hidePlan();
                _searchView.openSearch();
                _searchView.setQuery(DEFAULT_SEARCH, false);
            }
        });

        return true;
    }


    /**
     * Method called when the `back` button is pressed
     */
    @Override
    public void onBackPressed() {
        Log.d(getClass().getName(), "[DEBUG] Back Pressed");

        // If back is pressed while the searchbar is being used, then close it and kep going
        if(_searchView != null && _searchView.isOpen()) {
            _searchView.closeSearch();
            displayPlan();
        } else {
            Campus parentCampus = getCurrentPlan().getCampus();
            if(parentCampus != null) {
                setCurrentPlan(parentCampus);
            }
        }
    }

    @Override
    public void onRestart() {
        Log.d(getClass().getName(), "[DEBUG] onRestart");
        super.onRestart();
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
     * Initialize the "chose local" button<br />
     * To choose a specific local in the current plan
     */
    private void initChoseLocalButton() {
        Button local = (Button)findViewById(R.id.local);
        local.setOnClickListener(new ClickListenerChoseLocal(this));
    }


    /**
     * Set up the main screen (where we see the plan)
     */
    private void setUpCanvas() {
        if(_imageView.getDrawable() != null) {
            Integer height = _imageView.getDrawable().getIntrinsicWidth();
            Integer width = _imageView.getDrawable().getIntrinsicHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap = bitmap.copy(bitmap.getConfig(), true);
            _canvas = new Canvas(bitmap);
            _imageDraw.setImageBitmap(bitmap);
        } else {
            Log.w(getClass().getName(), "_imageView have no drawable");
        }
    }

    private float getWidthShrinkageFactor() {
        float res = 0;
        if(_imageView.getDrawable() != null) {
            float trueWidth = (float) ((BitmapDrawable) _imageView.getDrawable()).getBitmap().getWidth();
            float effectiveWidth = _imageView.getDrawable().getIntrinsicWidth();
            res = trueWidth / effectiveWidth;
        }
        return res;
    }

    private float getHeightShrinkageFactor() {
        float res = 0;
        if(_imageView.getDrawable() != null) {
            float trueHeight = (float) ((BitmapDrawable) _imageView.getDrawable()).getBitmap().getHeight();
            float effectiveHeight = _imageView.getDrawable().getIntrinsicHeight();
            res = trueHeight / effectiveHeight;
        }
        return res;
    }

    private void setSearchSuggestions() {
        _searchView.clearSuggestions();
        for(final Campus campus : _graph.getAllCampus()) {
            _searchView.addSuggestion("Campus " + campus.getName());
        }
        addSuggestions();
    }

    /**
     * Start a new task to add all suggestions to the searchbar
     */
    private void addSuggestions() {
        ArrayList<String> aliases = new ArrayList<>(_graph.getAllAlias());
        Collections.sort(aliases);
        SetSearchSuggestionsTaskParam param = new SetSearchSuggestionsTaskParam(_searchView, aliases);
        new SetSearchSuggestionsTask().execute(param);

        for (String suggestion : NOT_SUGGESTED) {
            _searchView.removeSuggestion(suggestion);
        }
    }

    /**
     * Draw the last path which have been calculate
     */
    private void drawPath() {
        if(_lastPathList != null) {
            drawPath(_lastPathList);
        }
    }


    /**
     * Draws the given path on the plan on the screen
     *
     * @param pathList An ArrayList of Path representing the nodes to pass by
     */
    public void drawPath(List<Path> pathList) {
        if(pathList == null)
        cleanCanvas();
        draw(_currentPosition);
        _lastPathList = pathList;
        _drawer.draw(pathList, getCurrentPlan());
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
     * Draw the "currentLocation" node (if not null) if we are
     * on the current plan node
     */
    public void refreshDraw() {
        Node currentLocation = getCurrentLocation();

        if(currentLocation != null) {

            if(!getDestinations().isEmpty()) {
                drawPath();

            } else {
                draw(currentLocation);
            }

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
        if(node.getParentPlan() == getCurrentPlan()) {
            if (cleanBefore) {
                cleanCanvas();
            }
            Float[] coord = _drawer.draw(node);
            _imageDraw.invalidate();
            _imageView.invalidate();

            moveToPoint(coord[0], coord[1]);
        }
    }

    /**
     * Scroll to center the good point
     *
     * @param x position of the node
     * @param y position of the node
     */
    private void moveToPoint(float x, float y) {
        int xMove = ((int) x) - _imageDraw.getWidth()/2;
        int yMove = ((int) y) - _imageDraw.getHeight()/2;

        Matrix matrixView = _imageDraw.getMatrix();
        matrixView.setTranslate(-xMove, -yMove);

        TouchListener.setNewCoordZoom(matrixView, _imageDraw, _imageView);
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
        // check first if user asks for a definite campus
        if(isCampus(dest)) {
            return;
        }
        // first look if such a destination can be found on plan
        _destinationNodes = _currentPlan.searchNode(dest);
        // if not
        if (_destinationNodes.isEmpty()) {
            // then look for it in the whole graph
            _destinationNodes = _graph.getAllNodesWithAlias(dest);
        }

        Log.i(getClass().getName(), "Set destination: " + dest +
                " (nbr node: " + _destinationNodes.size() + ")");
        if (!_destinationNodes.isEmpty()) {
            _graph.findPath();
        }
    }

    /**
     * Check if asked destination is a campus. If so, set the associated plan
     * @param dest the asked destination
     * @return true if a campus has been set as plan, and false otherwise
     */
    private boolean isCampus(String dest) {
        if(dest.contains("Campus")) {
            dest = dest.substring("Campus".length()+1);
            for (Campus campus : _graph.getAllCampus()) {
                if(dest.equals(campus.getName())) {
                    setCurrentPlan(campus);
                    return true;
                }
            }
        }
        return false;
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

            if(_switchButton != null) {
                _switchButton.updateSwitchButton(newCurrentPlan);
            }
            Log.d(getClass().getName(), "New plan: " + newCurrentPlan.getName());

            _currentPlan = newCurrentPlan;
            cleanCanvas();
            _imageView.setImageDrawable(_currentPlan.getDrawableImage());
            _imageView.setScaleType(ImageView.ScaleType.MATRIX);
            refreshDraw();

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
     * @return True if the location has changed
     */
    public boolean setCurrentLocation(Node newLocation) {
        Node oldLocation = _currentPosition;
        _currentPosition = newLocation;
        return (oldLocation != newLocation);
    }

    protected void displayPlan() {
        _layout.setVisibility(VISIBLE);
    }

    protected void hidePlan() {
        _layout.setVisibility(View.GONE);
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
     * Check if app is in TEST mode (and DEBUG mode on)
     *
     * @return True if we test application
     */
    public static boolean isTest() {
        return TEST && DEBUG;
    }

    /**
     * Check if the app is in DEMO test
     *
     * @return True if we make a demo of the application
     */
    public static boolean isDemo() { return DEMO; }


}
