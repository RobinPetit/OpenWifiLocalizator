package be.ulb.owl.gui;

/**
 * Created by hoornaert on 11.12.16.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.ImageView;

import java.util.List;

import be.ulb.owl.graph.Node;
import be.ulb.owl.graph.Path;

public class DrawView extends ImageView {
    private static final Integer NODE_RADIUS = 10;  // in pixels
    private static final Integer NODE_COLOR = Color.rgb(0xeb, 0x9c, 0x31);
    private static final Integer EDGE_COLOR = Color.BLUE;

    private Canvas _canvas;
    private Paint _paint = new Paint();
    private final float _wFactor;
    private final float _hFactor;

    public DrawView (Context context, Canvas canvas, float widthFactor, float heightFactor) {
        super(context);
        _wFactor = widthFactor;
        _hFactor = heightFactor;
        _canvas = canvas;
        _paint.setStyle(Paint.Style.FILL_AND_STROKE);
        _paint.setAntiAlias(true);
    }

    private Float getY (Float y) {
        return y/_hFactor;
    }

    private Float getX (Float x) {
        return x/_wFactor;
    }

    public void draw(Path path) {
        _paint.setColor(EDGE_COLOR);
        Node node = path.getNode();
        Float x1 = this.getX(node.getXOnPlan());
        Float y1 = this.getY(node.getYOnPlan());
        node = path.getOppositNodeOf(node);
        Float x2 = this.getX(node.getXOnPlan());
        Float y2 = this.getY(node.getYOnPlan());
        _canvas.drawLine(x1, y1, x2, y2, _paint);
        this.invalidate();
    }

    public void draw(Node node) {
        _paint.setColor(NODE_COLOR);
        Float x = getX(node.getXOnPlan());
        Float y = getY(node.getYOnPlan());
        _canvas.drawCircle(x, y, NODE_RADIUS, _paint);
        this.invalidate();
    }

    public void draw(List<Path> pathList) {
        for(Path path: pathList)
            draw(path);
    }
}
