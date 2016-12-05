package be.ulb.owl;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.View.OnClickListener;
import android.view.MotionEvent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import be.ulb.owl.gui.Zoom;
import be.ulb.owl.utils.LogUtils;

/**
 * Main file for Androit application<br/>
 * See this file before: https://developer.android.com/images/training/basics/basic-lifecycle-create.png<br/>
 * Doc: https://developer.android.com/training/basics/activity-lifecycle/starting.html
 *
 *
 * @author Nathan, Detobel36
 */
public class MainActivity extends AppCompatActivity implements OnTouchListener, OnClickListener,
        SearchView.OnQueryTextListener {

    private static MainActivity instance;
    private static boolean DEBUG = false;

    private Graph _graph = null;
    private Zoom _zoom = new Zoom();
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

        initLogSystem();
        Log.i("Main", "Test");
        setContentView(R.layout.activity_main);

        _imageView = (ImageView)findViewById(R.id.plan);
        _imageView.setOnTouchListener(this);
        _changePlan = (Button) findViewById(R.id.changePlan);
        _changePlan.setOnClickListener(this);
        _local = (Button) findViewById(R.id.local);
        _local.setOnClickListener(this);
    }


    private void initLogSystem() {
        if (LogUtils.isExternalStorageWritable() ) {
            File appDirectory = new File( Environment.getExternalStorageDirectory() + "/OWL" );
            File logDirectory = new File( appDirectory + "/log" );
            File logFile = new File( logDirectory, "logcat_" + System.currentTimeMillis() + ".txt" );

            // create app folder
            if ( !appDirectory.exists() ) {
                appDirectory.mkdir();
            }

            // create log folder
            if ( !logDirectory.exists() ) {
                logDirectory.mkdir();
            }

            // clear the previous logcat and then write the new one to the file
            try {
                Process process = Runtime.getRuntime().exec( "logcat -c");
                if(isDebug()) {
                    process = Runtime.getRuntime().exec( "logcat -f " + logFile + " *:I");
                } else {
                    process = Runtime.getRuntime().exec( "logcat -f " + logFile + " *:W");
                }

            } catch ( IOException e ) {
                e.printStackTrace();
            }

            LogUtils.clearLog();

        } /*else if (LogUtils.isExternalStorageReadable() ) {
            // only readable
        } else {
            // not accessible
        }*/
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(_graph == null) {
            _graph = new Graph();
        }
        // Create a plan for test
        Log.i(getClass().getName(), "Chargement du OF");
        Graph.getPlan("of");
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
        searchView.setOnQueryTextListener(this);

        return true;
    }

    /**
     * Event when we send a search request
     *
     * @param text string which is searched
     * @return ?
     */
    public boolean onQueryTextSubmit(String text) {
        Log.i(getClass().getName(), "text envoyé : "+text);
        return false;
    }

    /**
     * Event when text is change in search bar
     *
     * @param text string which is changed
     * @return ?
     */
    public boolean onQueryTextChange(String text){
        Log.d(getClass().getName(), "text modifié : "+text);
        return false;
    }

    /**
     * Event when we toutch the screen
     *
     * @param v ?
     * @param event information about the event
     * @return ?
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        _zoom.start(v,event);
        return true;
    }

    /**
     * Event when we clik on a view
     *
     * @param view the view
     */
    public void onClick(View view){
        switch (view.getId()){

            case R.id.changePlan:
                switchPlan();
                break;

            case R.id.local:
                searchLocal();
                break;

        }
    }

    /**
     * Switch between the two different global plans
     */
    private void switchPlan(){
        final String[] items = {"Plaine", "Solbosch", "P.F"}; // TODO Remove test (P.F)

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Make your selection");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {

                String name = items[item];
                Plan newPlan = Graph.getPlan(name);
                if(newPlan != null) {

                }
                try {
                    // get input stream
                    InputStream ims = getAssets().open("IMGMap" + File.separator + name +".png");
                    // load image as Drawable
                    Drawable d = Drawable.createFromStream(ims, null);
                    // set image to ImageView
                    _imageView.setImageDrawable(d);
                    ims .close();
                } catch(IOException ex) {
                    return;
                }

                _currentPlan = Graph.getPlan(name);

            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Search a local
     */
    private void searchLocal() {

        final String[] items;
        if(_currentPlan != null) {
            items = (String[]) _currentPlan.getAllAlias().toArray();
        } else {
            items = (String[]) Arrays.asList("Exemple1", "Exemple2").toArray();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Locaux");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                Log.i(getClass().getName(), "Sélection du local : "+items[item]);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
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