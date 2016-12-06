package be.ulb.owl;

import android.graphics.drawable.Drawable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;

import be.ulb.owl.graph.Graph;
import be.ulb.owl.graph.Node;
import be.ulb.owl.graph.Plan;
import be.ulb.owl.gui.listener.ClickListener;
import be.ulb.owl.gui.listener.QueryTextListener;
import be.ulb.owl.gui.listener.TouchListener;
import be.ulb.owl.utils.LogUtils;

/**
 * Main file for Androit application<br/>
 * See this file before: https://developer.android.com/images/training/basics/basic-lifecycle-create.png<br/>
 * Doc: https://developer.android.com/training/basics/activity-lifecycle/starting.html
 *
 *
 * @author Nathan, Detobel36
 */
public class MainActivity extends AppCompatActivity  {

    private static MainActivity instance;
    private static boolean DEBUG = false;

    private Graph _graph = null;
    private ImageView _imageView;
    private Button _changePlan;
    private Button _local;

    private Plan _currentPlan = null;


    /**
     * Call when the application is created the first time
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;

        LogUtils.initLogSystem();
        Log.i("Main", "Test");
        setContentView(R.layout.activity_main);


        _imageView = (ImageView)findViewById(R.id.plan);
        _imageView.setOnTouchListener(new TouchListener());

        // Define clic listener
        ClickListener clicListener = new ClickListener();

        _changePlan = (Button) findViewById(R.id.changePlan);
        _changePlan.setOnClickListener(clicListener);

        _local = (Button) findViewById(R.id.local);
        _local.setOnClickListener(clicListener);
    }



    @Override
    protected void onStart() {
        super.onStart();

        if(_graph == null) {
            _graph = new Graph();
        }
        // Create a plan for test
        Log.i(getClass().getName(), "Chargement du P.F");
        Graph.getPlan("P.F");


//        testWifi();
    }


    private void testWifi() {
        // ---- test ----

        ArrayList<Wifi> tmp = new ArrayList<Wifi>();
        tmp.add(new Wifi("00:26:cb:4e:0c:a0", 82.0f, 78.0f, 80.0f));
        tmp.add(new Wifi("00:26:cb:4e:0c:a1", 81.0f, 79.0f, 80.4f));
        tmp.add(new Wifi("a4:ee:57:f1:96:6b", 74.0f, 67.0f, 69.2f));
        tmp.add(new Wifi("00:26:cb:4e:0e:70", 81.0f, 72.0f, 77.6f));
        tmp.add(new Wifi("10:9a:dd:b5:2a:3f", 78.0f, 75.0f, 76.6f));
        tmp.add(new Wifi("00:0c:e6:00:d1:0c", 91.0f, 81.0f, 88.6f));
        tmp.add(new Wifi("00:26:cb:4e:0e:71", 79.0f, 72.0f, 77.2f));

        Node position = _graph.whereAmI();
        System.out.print("The result should be F19. res = ");
        System.out.println(position.getName());

        // ---- test ----

        tmp = new ArrayList<Wifi>();
        tmp.add(new Wifi("00:26:cb:4e:0e:71", 61.0f, 58.0f, 59.6f));
        tmp.add(new Wifi("10:9a:dd:b5:2a:3f", 70.0f, 58.0f, 64.6f));
        tmp.add(new Wifi("00:26:cb:4e:0e:70", 61.0f, 55.0f, 57.4f));
        tmp.add(new Wifi("00:26:cb:4e:0c:a0", 82.0f, 72.0f, 74.2f));
        tmp.add(new Wifi("00:26:cb:4e:0c:a1", 83.0f, 72.0f, 75.2f));

        position = _graph.whereAmI();
        System.out.print("The result should be F29. res = ");
        System.out.println(position.getName());
    }


    /**
     *
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem searchItem = menu.findItem(R.id.Search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new QueryTextListener());

        return true;
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
     * @return the curent plan
     */
    public Plan getCurrentPlan() {
        return _currentPlan;
    }

    /**
     * Change the current plan which is show on the user
     *
     * @param newCurrentPlan new Plan object
     */
    public void setCurrentPlan(Plan newCurrentPlan) {
        if(newCurrentPlan != null) {
            _currentPlan = newCurrentPlan;
            _imageView.setImageDrawable(_currentPlan.getDrawableImage());
        } else {
            Log.w(this.getClass().getName(), "Le nouveau plan est null");
        }
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

}