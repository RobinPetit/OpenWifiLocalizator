package be.ulb.owl.gui.listener;

import android.graphics.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import be.ulb.owl.MainActivity;
import be.ulb.owl.gui.Zoom;

/**
 * Created by Detobel36
 */

public class TouchListener implements View.OnTouchListener {

    private final MainActivity _main;
    private static Zoom _zoom = null;

    public TouchListener(MainActivity main) {
        _main = main;
        if(_zoom != null) {
            Log.e(getClass().getName(), "Zomm already initialized !");
        }
        _zoom = new Zoom(_main);
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if(view instanceof ImageView) {
            _zoom.start(motionEvent, (ImageView) view, _main.getImageView());
        } else {
            Log.w(getClass().getName(), "Listener on wrong view !");
        }

        return true;
    }

    public static void setNewCoordZoom(Matrix newMatrix, ImageView... allView) {
        _zoom.movePlan(newMatrix, allView);
    }


}
