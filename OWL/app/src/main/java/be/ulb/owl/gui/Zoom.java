package be.ulb.owl.gui;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

public class Zoom {

    // Constants
    private static final float MAX_ZOOM = 2.5f;   // Maximum zomm
    private static final float MAX_DEZOOM = 0.8f; // Maximum dezoom
    private static final float MIN_SPACE = 5f;    // Minimum space between finger to detect move

    // Action type
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;


    // Matrix used to modify image
    private Matrix _matrix = new Matrix();
    private Matrix _savedMatrix = new Matrix();
    private Matrix _savedMatrix2 = new Matrix();

    // Variable for mouvement
    private int _mode = NONE;
    private PointF _start = new PointF();
    private PointF _mid = new PointF();
    private float _oldDist = 1f;



    // Calculate new positions on the screen

    /**
     * Space between two finger
     *
     * @param event all data of this event
     * @return the space (in float)
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Update the point center between you two fingers
     *
     * @param point which must be update
     * @param event all data of this event
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }


    //////////////////////// CALLED BY start EVENT ////////////////////////

    /**
     * Created and called by TouchListener. Instantiate when the user touch the screen for
     * the first time.
     *
     * @param event   all data about this event
     * @param allView all view who must be adapted
     */
    public void start(MotionEvent event, ImageView... allView) {
        if (allView == null || allView.length <= 0) {
            return;
        }

        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            // When we detect one finger
            case MotionEvent.ACTION_DOWN:
                initDragEvent(event);
                break;

            // If we stop the current mouvement
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                resetMode();
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                initZoomEvent(event);
                break;

            case MotionEvent.ACTION_MOVE:
                if (_mode == DRAG) {
                    actionMoveDrag(event, allView);

                } else if (_mode == ZOOM) {
                    actionMoveZoom(event);
                }

                break;

        }

        displayChange(allView);

    }

    /**
     * reset the mode (set to NONE)
     */
    private void resetMode() {
        _mode = NONE;
        Log.d(getClass().getName(), "mode=NONE");
    }

    /**
     * Switch in DRAG mode.  Init var
     *
     * @param event all informations about the event
     */
    private void initDragEvent(MotionEvent event) {
        Log.d(getClass().getName(), "mode=DRAG");
        _savedMatrix.set(_matrix);
        _start.set(event.getX(), event.getY());
        _mode = DRAG;
    }


    /**
     * Switch in ZOOM mode. Init var
     *
     * @param event all information about the event
     */
    private void initZoomEvent(MotionEvent event) {
        _oldDist = spacing(event);

        if (_oldDist > MIN_SPACE) { // Enought space
            _savedMatrix.set(_matrix);
            midPoint(_mid, event);
            _mode = ZOOM;
            Log.d(getClass().getName(), "mode=ZOOM");
        }
    }

    /**
     * Called when the user wants to move the plan
     *
     * @param event   action of the user
     * @param allView view which must move
     */
    private void actionMoveDrag(MotionEvent event, ImageView... allView) {
        if (allView[0].getDrawable() != null) {
            float newX = event.getX() - _start.x;
            float newY = event.getY() - _start.y;

            _matrix.set(_savedMatrix);
            _matrix.postTranslate(newX, newY);
        }
    }

    /**
     * Zoom started
     *
     * @param event variable with all data
     */
    private void actionMoveZoom(MotionEvent event) {
        float newDist = spacing(event);
        float scale = newDist / _oldDist;

        // If space is enought
        if (newDist > MIN_SPACE ) {
            _matrix.set(_savedMatrix);
            _matrix.postScale(scale, scale, _mid.x, _mid.y);
        }


    }

    /**
     * Apply the matrix
     *
     * @param allView which must be update
     */
    private void displayChange(ImageView... allView) {
        for (ImageView selectView : allView) {
            selectView.setScaleType(ImageView.ScaleType.MATRIX);
            selectView.setImageMatrix(_matrix);

            fixing(selectView); // Fix mouvement
            selectView.setImageMatrix(_savedMatrix2);

        }
    }

    /**
     * Fixe mouvement (carmera could not zoom too much or go outside)
     *
     * @param view the image view
     */
    private void fixing(ImageView view) {
        float[] value = new float[9];
        _matrix.getValues(value);

        float[] savedValue = new float[9];
        _savedMatrix2.getValues(savedValue);


        // Fixed width and height (all the time the same (it's the position of top left corner)
        int width = 10;  // view.getWidth();
        int height = 10; // view.getHeight();

        Drawable d = view.getDrawable();
        if (d == null) {
            return;
        }

        int imageWidth = d.getIntrinsicWidth();
        int imageHeight = d.getIntrinsicHeight();
        int scaleWidth = (int) (imageWidth * value[0]);
        int scaleHeight = (int) (imageHeight * value[4]);

        // don't let the image go outside
        if (value[2] > width - 1) {
            value[2] = width - 10;

        } else if(value[2] < -(scaleWidth-1)) {
            value[2]=-(scaleWidth-10);

        }

        if (value[5] > height - 1) {
            value[5] = height - 10;

        } else if (value[5] < -(scaleHeight-1-view.getHeight())) {
            value[5] = -(scaleHeight - 10 - view.getHeight());

        }

        boolean cancelZoom = false;
        // maximum zoom ratio: MAX
        if (value[0] > MAX_ZOOM || value[4] > MAX_ZOOM){
            value[0] = MAX_ZOOM;
            value[4] = MAX_ZOOM;
            cancelZoom = true;

        // Maximum dezoom ratio: MIN
        } else if(value[0] < MAX_DEZOOM || value[4] < MAX_DEZOOM) {
            value[0] = MAX_DEZOOM;
            value[4] = MAX_DEZOOM;
            cancelZoom = true;

        }

        if(cancelZoom) {
            value[2] = savedValue[2];
            value[5] = savedValue[5];
        }

        _matrix.setValues(value);
        _savedMatrix2.set(_matrix);
    }




    //////////////////// EXTERNAL CALL /////////////////////

    public void movePlan(Matrix newMatrix, ImageView... view) {
        _matrix.set(newMatrix);
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
