package be.ulb.owl;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
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

import be.ulb.owl.gui.Zoom;

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

    private Graph graph = null;
    private Zoom zoom = new Zoom();
    private ImageView imageView;
    private Button changePlan;
    private Button local;


    /**
     * Call when the application is created the first time
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;

        setContentView(R.layout.activity_main);

        imageView = (ImageView)findViewById(R.id.plan);
        imageView.setOnTouchListener(this);
        changePlan = (Button) findViewById(R.id.changePlan);
        changePlan.setOnClickListener(this);
        local = (Button) findViewById(R.id.local);
        local.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(graph == null) {
            graph = new Graph();
        }
        // Create a plan for test
        System.out.println("Chargement du OF");
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
        System.out.println("text envoyé : "+text);
        return false;
    }

    /**
     * Event when text is change in search bar
     *
     * @param text string which is changed
     * @return ?
     */
    public boolean onQueryTextChange(String text){
        System.out.println("text modifié : "+text);
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
        zoom.start(v,event);
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
        final String[] items = {"plaine", "solbosch", "of"}; // TODO Remove test (of)

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Make your selection");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {

                String name = items[item];
                try {
                    // get input stream
                    InputStream ims = getAssets().open("IMGMap" + File.separator + name +".png");
                    // load image as Drawable
                    Drawable d = Drawable.createFromStream(ims, null);
                    // set image to ImageView
                    imageView.setImageDrawable(d);
                    ims .close();
                } catch(IOException ex) {
                    return;
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Search a local
     */
    private void searchLocal() {
        final String[] items = {"Forum A",
                "Forum B",
                "Forum C",
                "Forum D",
                "Forum E",
                "Forum F",
                "Forum G",
                "Forum H",
                "Pof 2058",
                "Pof 2064",
                "Pof 2066",
                "Pof 2070",
                "Pof 2072",
                "Pof 2076",
                "Pof 2078",
                "Pof 2080"
                };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Locaux");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                System.out.println("Sélection du local : "+items[item]);
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

}