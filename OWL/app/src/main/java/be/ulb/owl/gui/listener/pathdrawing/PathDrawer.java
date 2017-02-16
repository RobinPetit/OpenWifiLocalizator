package be.ulb.owl.gui.listener.pathdrawing;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by robin on 15/02/17.
 */

public abstract class PathDrawer implements PathDrawing {
    private static int DEFAULT_PATH_COLOR = Color.BLUE;
    protected Paint _pathColor = new Paint();
    protected Canvas _canvas;

    public PathDrawer(int pathColor, Canvas canvas) {
        _pathColor.setColor(pathColor);
        _canvas = canvas;
    }

    public PathDrawer(Canvas canvas) {
        this(PathDrawer.DEFAULT_PATH_COLOR, canvas);
    }
}
