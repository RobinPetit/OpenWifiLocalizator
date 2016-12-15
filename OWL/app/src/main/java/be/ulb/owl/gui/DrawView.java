package be.ulb.owl.gui;

/**
 * Created by hoornaert on 11.12.16.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.widget.ImageView;

import java.util.List;

import be.ulb.owl.graph.Node;
import be.ulb.owl.graph.Path;

public class DrawView extends ImageView {
    private Canvas _canvas;
    private Paint _paint = new Paint();
    private final float _wFactor;
    private final float _hFactor;

    public DrawView (Context context, Canvas canvas, float widthFactor, float heightFactor) {
        super(context);
        _wFactor = widthFactor;
        _hFactor = heightFactor;
        _canvas = canvas;
        _paint.setColor(Color.RED);
        _paint.setStyle(Paint.Style.FILL_AND_STROKE);
        _paint.setAntiAlias(true);
    }

    private Float getY (Float y) {
        return y/_hFactor;
    }

    private Float getX (Float x) {
        return x/_wFactor;
    }

    public void draw (Node node) {
        Float x = 50.0f; //this.getX(node.getX());
        Float y = 50.0f; //this.getY(node.getY());
        _canvas.drawCircle(x, y, 50, _paint);
        this.invalidate();
    }

    public void draw (Path path) {
        Node node = path.getNode();
        Float x1 = this.getX(node.getX());
        Float y1 = this.getY(node.getY());
        node = path.getOppositNodeOf(node);
        Float x2 = this.getX(node.getX());
        Float y2 = this.getY(node.getY());
        _canvas.drawLine(x1, y1, x2, y2, _paint);
        this.invalidate();
    }

    public void draw(List<Path> pathList) {
        for(Path path: pathList)
            draw(path);
    }
}
