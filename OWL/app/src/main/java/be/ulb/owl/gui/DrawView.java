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
    private static final Integer DEFAULT_COLOR = NODE_COLOR;

    private Canvas _canvas;
    private Paint _paint = new Paint();
    private float _wFactor;
    private float _hFactor;

    public DrawView(Context context, Canvas canvas, float widthFactor, float heightFactor) {
        super(context);

        setXFactor(widthFactor);
        setYFactor(heightFactor);
        setCanvas(canvas);
        _paint.setStyle(Paint.Style.FILL_AND_STROKE);
        _paint.setAntiAlias(true);
        _paint.setColor(DEFAULT_COLOR);
    }

    private Float projectOnY(Float y) {
        return y/_hFactor;
    }

    private Float projectOnX(Float x) {
        return x/_wFactor;
    }

    public void setXFactor(Float factor) {
        _wFactor = factor;
    }

    public void setYFactor(Float factor) {
        _hFactor = factor;
    }

    public void setCanvas(Canvas canvas) {
        _canvas = canvas;
    }

    public void draw(Path path) {
        _paint.setColor(EDGE_COLOR);
        Node node = path.getNode();
        Float x1 = projectOnX(node.getXOnPlan());
        Float y1 = projectOnY(node.getYOnPlan());
        node = path.getOppositeNodeOf(node);
        Float x2 = projectOnX(node.getXOnPlan());
        Float y2 = projectOnY(node.getYOnPlan());
        _canvas.drawLine(x1, y1, x2, y2, _paint);
        this.invalidate();
    }

    public void draw(Node node) {
        _paint.setColor(NODE_COLOR);
        Float x = projectOnX(node.getXOnPlan());
        Float y = projectOnY(node.getYOnPlan());
        _canvas.drawCircle(x, y, NODE_RADIUS, _paint);
        this.invalidate();
    }

    public void draw(List<Path> pathList) {
        for(Path path: pathList)
            draw(path);
    }
}
