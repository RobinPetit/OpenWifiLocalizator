package be.ulb.owl.gui;

import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.widget.ImageView;

import be.ulb.owl.MainActivity;

public class Zoom {

    private static final String TAG = "Touch";

    // Matrix used to modify image
    private Matrix save;
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    private MainActivity _main;
    // Screen dimentions
    private float height;
    private float width;
    private float heightN;
    private float widthN;
    // Action type
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;

    public Zoom(MainActivity main) {
        _main = main;
        Display display = _main.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;
    }

    // Calculate new positions on the screen
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    //////////////////////// CALLED BY start EVENT ////////////////////////
    /**
     * Called when user touch the screen
     */
    private void actionDown(MotionEvent event) {
        Log.d(TAG, "mode=DRAG");
        savedMatrix.set(matrix);
        start.set(event.getX(), event.getY());
        mode = DRAG;
    }

    private void actionPointerUp() {
        mode = NONE;
        Log.d(TAG, "mode=NONE");
    }

    /**
     * Called when the user wants to move the plan
     * @param event
     * @param allView
     */
    private void actionMoveDrag(MotionEvent event,ImageView... allView) {
        if(allView[0].getDrawable() != null) {
            float newX = event.getX() - start.x;
            float newY = event.getY() - start.y;

            // create the transformation in the matrix  of points
            matrix.set(savedMatrix);
            matrix.postTranslate(newX, newY);
            Log.i(getClass().getName(), "new: " + newX + " - " + newY);
        }
    }

    /**
     * Second finger detected
     * @param event
     */
    private void actionPointerDown(MotionEvent event) {
        oldDist = spacing(event);
        Log.d(TAG, "oldDist=" + oldDist);
        if (oldDist > 5f) {
            savedMatrix.set(matrix);
            midPoint(mid, event);
            mode = ZOOM;
            Log.d(TAG, "mode=ZOOM");
        }
    }

    /**
     * Zoom started
     * @param event
     */
    private void actionMoveZoom(MotionEvent event) {
        float newDist = spacing(event);
        Log.d(TAG, "newDist=" + newDist);
        save = new Matrix();
        if (newDist > 5f) {
            matrix.set(savedMatrix);
            save.set(matrix);
            float scale = newDist / oldDist;
            matrix.postScale(scale, scale, mid.x, mid.y);

            float[] values = new float[9];
            matrix.getValues(values);
            widthN = values[Matrix.MSCALE_X]*_main.getImageView().getWidth();
            heightN = values[Matrix.MSCALE_Y]*_main.getImageView().getHeight();
        }
        if (widthN < width/2 || heightN > height*2)
            matrix.set(save);
    }

    /**
     * Created and called by TouchListener. Instantiate when the user touch the screen for
     * the first time.
     * @param event
     * @param allView
     */
    public void start(MotionEvent event, ImageView... allView) {
        if(allView == null || allView.length <= 0) return;

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                actionDown(event);
                break;
            case MotionEvent.ACTION_UP:

            case MotionEvent.ACTION_POINTER_UP:
                actionPointerUp();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                actionPointerDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) actionMoveDrag(event,allView);
                else if (mode == ZOOM) actionMoveZoom(event);
                break;
        }
        displayChange(allView);
    }

    /**
     * Refresh the screen
     * @param allView
     */
    private void displayChange(ImageView... allView) {
        for(ImageView selectView : allView) {
            selectView.setScaleType(ImageView.ScaleType.MATRIX);
            selectView.setImageMatrix(matrix);
        }
    }


    public void movePlan(Matrix newMatrix, ImageView... view) {
        matrix.set(newMatrix);
        displayChange(view);
    }


    //////////////////////// DEBUG ////////////////////////

    /** Show an event in the LogCat view, for debugging */
    private void dumpEvent(MotionEvent event) {
        String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE","POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_").append(names[actionCode]);

        if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP) {
            sb.append("(pid ").append(action >> MotionEvent.ACTION_POINTER_INDEX_SHIFT);
            sb.append(")");
        }

        sb.append("[");
        for (int i = 0; i < event.getPointerCount(); i++) {
            sb.append("#").append(i);
            sb.append("(pid ").append(event.getPointerId(i));
            sb.append(")=").append((int) event.getX(i));
            sb.append(",").append((int) event.getY(i));
            if (i + 1 < event.getPointerCount())
                sb.append(";");
        }

        sb.append("]");
        Log.d("Touch Events ---------", sb.toString());
    }



}
