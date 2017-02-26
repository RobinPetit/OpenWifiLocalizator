package be.ulb.owl.gui.pathdrawing;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.List;
import java.util.Random;

import be.ulb.owl.MainActivity;
import be.ulb.owl.gui.FloatCouple;

/**
 * Created by robin on 15/02/17.
 */

public abstract class PathDrawer implements PathDrawing {

    private static int NODE_RADIUS = 3;
    private static int DEFAULT_PATH_COLOR = Color.BLUE;
    private static int PATH_SIZE = 3;

    protected Paint _pathColor = new Paint();
    protected Canvas _canvas;

    public PathDrawer(int pathColor, Canvas canvas) {
        _pathColor.setAntiAlias(true);
        _pathColor.setColor(pathColor);
        _pathColor.setStrokeWidth(PATH_SIZE);
        _canvas = canvas;
    }

    public PathDrawer(Canvas canvas) {
        this(PathDrawer.DEFAULT_PATH_COLOR, canvas);
    }

    @Override
    public void drawPath(List<FloatCouple> points) {
        if(MainActivity.isDebug()) {
            for(final FloatCouple point : points) {
                _canvas.drawCircle(point.getX(), point.getY(), NODE_RADIUS, _pathColor);
            }
        }
    }

    protected void setRandomColor() {
        Random r = new Random();
        _pathColor.setARGB(255, (int)(255 * r.nextFloat()), (int)(255 * r.nextFloat()), (int)(255 * r.nextFloat()));
    }
}
