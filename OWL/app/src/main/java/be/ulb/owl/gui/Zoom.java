package be.ulb.owl.gui;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

import be.ulb.owl.MainActivity;

public class Zoom {

    private static final String TAG = "Touch";

    // These matrices will be used to scale points of the image
    private Matrix save;
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    private MainActivity _main;
    private float width;
    private float widthN;

    // The 3 states (events) which the user is trying to perform
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    // these PointF objects are used to record the point(s) the user is touching
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;

    public Zoom(MainActivity main) {
        _main = main;
        DisplayMetrics metrics = new DisplayMetrics();
        _main.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        save = _main.getImageView().getImageMatrix();
        width = metrics.widthPixels;
    }


    /*
     * --------------------------------------------------------------------------
     * Method: spacing Parameters: MotionEvent Returns: float Description:
     * checks the spacing between the two fingers on touch
     * ----------------------------------------------------
     */

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /*
     * --------------------------------------------------------------------------
     * Method: midPoint Parameters: PointF object, MotionEvent Returns: void
     * Description: calculates the midpoint between the two fingers
     * ------------------------------------------------------------
     */

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    public void start(MotionEvent event, ImageView... allView) {

        if(allView == null || allView.length <= 0) {
            return;
        }


        // Handle touch events here...
//        dumpEvent(event);


        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            // first finger down only
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "mode=DRAG"); // write to LogCat
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                mode = DRAG;
                break;

            // first finger lifted
            case MotionEvent.ACTION_UP:

            // second finger lifted
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                Log.d(TAG, "mode=NONE");
                break;

            // first and second finger down
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                Log.d(TAG, "oldDist=" + oldDist);
                if (oldDist > 5f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                    Log.d(TAG, "mode=ZOOM");
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    if(allView[0].getDrawable() != null) {
                        float newX = event.getX() - start.x;
                        float newY = event.getY() - start.y;

                        // create the transformation in the matrix  of points
                        matrix.set(savedMatrix);
                        matrix.postTranslate(newX, newY);

                        Log.i(getClass().getName(), "new: " + newX + " - " + newY);
                    }

                } else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    Log.d(TAG, "newDist=" + newDist);
                    if (newDist > 5f) {
                        matrix.set(savedMatrix);
                        float scale = newDist / oldDist;

                        matrix.postScale(scale, scale, mid.x, mid.y);
                        float[] values = new float[9];
                        matrix.getValues(values);

                        widthN = values[Matrix.MSCALE_X]*_main.getImageView().getWidth();
                        if (widthN < width){
                            matrix.set(save);
                        }
                    }
                }
                break;
        }

        // display the transformation on screen
        displayChange(allView);
    }

    private void displayChange(ImageView... allView) {
        for(ImageView selectView : allView) {
            Log.d(TAG, "Mode = "+mode+" WidthN : "+widthN+" Width : "+width);
            if ((widthN >= width && mode == ZOOM) || mode == 1) {
                selectView.setScaleType(ImageView.ScaleType.MATRIX);
                selectView.setImageMatrix(matrix);
            }
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
