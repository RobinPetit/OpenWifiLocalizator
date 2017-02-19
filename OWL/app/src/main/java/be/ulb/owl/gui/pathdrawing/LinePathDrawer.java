package be.ulb.owl.gui.pathdrawing;

import android.graphics.Canvas;

import java.util.List;

import be.ulb.owl.gui.FloatCouple;

/**
 * Created by robin on 15/02/17.
 */

public class LinePathDrawer extends PathDrawer {

    public LinePathDrawer(Canvas canvas) {
        super(canvas);
    }

    public LinePathDrawer(int pathColor, Canvas canvas) {
        super(pathColor, canvas);
    }

    protected void drawPath(Float x1, Float y1, Float x2, Float y2) {
        _canvas.drawLine(x1, y1, x2, y2, _pathColor);
    }

    @Override
    public void drawPath(List<FloatCouple> points) {
        FloatCouple previous = null;
        for(FloatCouple pos : points) {
            if(previous != null) {
                drawPath(previous.getX(), previous.getY(), pos.getX(), pos.getY());
            }
            previous = pos;

        }
    }
}
