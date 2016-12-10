package be.ulb.owl;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.BitmapFactory;
import android.widget.LinearLayout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import be.ulb.owl.graph.Graph;
import be.ulb.owl.graph.Node;
import be.ulb.owl.graph.Plan;
import be.ulb.owl.gui.listener.ClickListener;
import be.ulb.owl.gui.listener.QueryTextListener;
import be.ulb.owl.gui.listener.TouchListener;
import be.ulb.owl.utils.LogUtils;

/*
//Create a new image bitmap and attach a brand new canvas to it
Bitmap tempBitmap = Bitmap.createBitmap(myBitmap.getWidth(), myBitmap.getHeight(), Bitmap.Config.RGB_565);
Canvas tempCanvas = new Canvas(tempBitmap);

//Draw the image bitmap into the cavas
tempCanvas.drawBitmap(myBitmap, 0, 0, null);

//Draw everything else you want into the canvas, in this example a rectangle with rounded edges
tempCanvas.drawRoundRect(new RectF(x1,y1,x2,y2), 2, 2, myPaint);

//Attach the canvas to the ImageView
myImageView.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
 */


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
    private ImageView _imageDraw;
    private Bitmap _bitmap = null;
    private Paint _paint = null;
    private Button _changePlan;
    private Button _local;

    private Plan _currentPlan = null;


    private void setUpDrawArea () {
        int width = _imageView.getDrawable().getIntrinsicWidth();
        int height = _imageView.getDrawable().getIntrinsicHeight();
        System.out.println(width+"\t"+height);
        Bitmap bitMap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitMap = bitMap.copy(bitMap.getConfig(), true);
        Canvas canvas = new Canvas(bitMap);

        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);

        _imageDraw.setImageBitmap(bitMap);
        canvas.drawCircle(50, 50, 50, paint);
        _imageDraw.invalidate();
    }

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

        _imageDraw = (ImageView)findViewById(R.id.draw);
        _imageDraw.setOnTouchListener(new TouchListener());

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

        // Set default plan
        setCurrentPlan(Graph.getPlan("Solbosch"));

        Log.i(getClass().getName(), "Scanner.scan");
        Node current = _graph.whereAmI();
        if(current != null) {
            Log.d(getClass().getName(), "Noeud trouvé: " + current.getName());
            setCurrentPlan(current.getParentPlan());

        } else {
            Log.d(getClass().getName(), "Position introuvable");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Not found");
            builder.setMessage("You are not at ULB");
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            builder.create().show();

        }
        setUpDrawArea();
    //testWifi();
    }

    /**
     * When we hidde the application
     * @see #onDestroy() when application is destroy
     */
    @Override
    protected void onStop() {
        super.onStop();
        _graph.hidden();
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
        System.out.print("Test 6.\n res = ");
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
        if(newCurrentPlan != null && (_currentPlan == null ||
                !newCurrentPlan.getName().equalsIgnoreCase(_currentPlan.getName())) ) {

            _currentPlan = newCurrentPlan;
            _imageView.setImageDrawable(_currentPlan.getDrawableImage());
            _imageView.setScaleType(ImageView.ScaleType.MATRIX);

        } else if(newCurrentPlan == null) {
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