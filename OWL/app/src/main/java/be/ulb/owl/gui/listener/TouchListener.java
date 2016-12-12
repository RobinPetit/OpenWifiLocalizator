package be.ulb.owl.gui.listener;

import android.view.MotionEvent;
import android.view.View;

import be.ulb.owl.MainActivity;
import be.ulb.owl.gui.Zoom;

/**
 * Created by Detobel36
 */

public class TouchListener implements View.OnTouchListener {

    private static final MainActivity main = MainActivity.getInstance();
    private final Zoom _zoom;

    public TouchListener() {
        _zoom = new Zoom();
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        _zoom.start(view, motionEvent);
        _zoom.start(main.getImageView(), motionEvent);

        return true;
    }


}
