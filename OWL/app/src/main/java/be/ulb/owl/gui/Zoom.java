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

    // Constants
    private static final double MAX_ZOOM = 2.5;   // Maximum zomm
    private static final double MAX_DEZOOM = 1.5; // Maximum dezoom
    private static final float MIN_SPACE = 5f;    // Minimum space between finger to detect move

    // Action type
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;


    // Matrix used to modify image
    private Matrix _matrix = new Matrix();
    private Matrix _savedMatrix = new Matrix();
    private Matrix _savedMatrixZoom = new Matrix();
    private Matrix _saveMatrixMove = new Matrix();

    // Screen dimensions
    private float _width;
    private float _zoomWidth;

    // Variable for mouvement
    private int _mode = NONE;
    private PointF _start = new PointF();
    private PointF _mid = new PointF();
    private float _oldDist = 1f;
    private MainActivity _main;


    /**
     * Constructor
     *
     * @param main instance of main
     */
    public Zoom(MainActivity main) {
        Display display = main.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        _width = size.x;
        _zoomWidth = _width;

        _main = main;
    }

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
     * @param event all data about this event
     * @param allView all view who must be adapted
     */
    public void start(MotionEvent event, ImageView... allView) {
        if(allView == null || allView.length <= 0) {
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
                    actionMoveDrag(event,allView);

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
     * @param event action of the user
     * @param allView view which must move
     */
    private void actionMoveDrag(MotionEvent event,ImageView... allView) {
        if(allView[0].getDrawable() != null) {
            float newX = event.getX() - _start.x;
            float newY = event.getY() - _start.y;

            // TODO currently not working
//            Matrix tmp = new Matrix();
//            tmp.set(_matrix);
//            tmp.postTranslate(newX, newY);
//
//            float[] values = new float[9];
//            tmp.getValues(values);
//            float valX = values[Matrix.MTRANS_X];
//            float valY = values[Matrix.MTRANS_Y];
//
//
//            if(valX >= 0 && newX >= 0) {
//                newX = (newX - valX);
//            }
//
//            if(valY >= 0 && newY >= 0) {
//                newY = (newY - valY);
//            }
//
//
//            if(Math.abs(newX) >= 0.01 || Math.abs(newY) >= 0.01) {
                _matrix.set(_savedMatrix);
                _matrix.postTranslate(newX, newY);
                _saveMatrixMove.set(_matrix);
//            } else {
//                _matrix.set(_saveMatrixMove);
//            }

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

        boolean currentlyZoom = scale > 1;
        boolean currentlyDezoom = scale < 1;

        // If space is enought AND we can zomm or dezoom
        if(newDist > MIN_SPACE &&
                (currentlyDezoom || _zoomWidth > _width /MAX_DEZOOM) &&
                (currentlyZoom || _zoomWidth < _width *MAX_ZOOM)) {

            _matrix.set(_savedMatrix);

            // Create temp _matrix to make calcul
            Matrix temp = new Matrix();
            temp.set(_matrix);
            temp.postScale(scale, scale, _mid.x, _mid.y);
            float newWidth = getWidthOfMatrix(temp);

            if(newWidth > _width /MAX_DEZOOM && newWidth < _width *MAX_ZOOM) {

                _matrix.postScale(scale, scale, _mid.x, _mid.y);
                _savedMatrixZoom.set(_matrix);
                _zoomWidth = newWidth;

            } else {  // Is not good, last zoom update :)
                _matrix.set(_savedMatrixZoom);
            }

        }



    }

    /**
     * Get the _width of a specific _matrix zoom
     *
     * @param matrix the tested _matrix
     * @return float with the size
     */
    private float getWidthOfMatrix(Matrix matrix) {
        float[] values = new float[9];
        matrix.getValues(values);
        return values[Matrix.MSCALE_X]* _main.getImageView().getWidth();
    }

    /**
     * Refresh the screen
     * @param allView
     */
    private void displayChange(ImageView... allView) {
        for(ImageView selectView : allView) {
            selectView.setScaleType(ImageView.ScaleType.MATRIX);
            selectView.setImageMatrix(_matrix);
        }
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
