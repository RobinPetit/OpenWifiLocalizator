package be.ulb.owl.gui;

/**
 * Created by hoornaert on 11.12.16.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.ImageView;

import be.ulb.owl.graph.Node;
import be.ulb.owl.graph.Path;

public class DrawView extends ImageView {

    private Bitmap _bitMap;
    private Canvas _canvas;
    private Paint _paint;

    public DrawView (Context context, Integer width, Integer height) {
        super(context);
        _bitMap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        _bitMap = _bitMap.copy(_bitMap.getConfig(), true);
        _canvas = new Canvas(_bitMap);
        _paint = new Paint();
        _paint.setColor(Color.RED);
        _paint.setStyle(Paint.Style.FILL_AND_STROKE);
        _paint.setAntiAlias(true);
        this.setImageBitmap(_bitMap);
    }

    private Float getY (Float y) {
        return y;
    }

    private Float getX (Float x) {
        return x;
    }

    public void draw (Node node) {
        Float x = 50.0f; //this.getX(node.getX());
        Float y = 50.0f; //this.getY(node.getY());
        _canvas.drawCircle(x, y, 50, _paint);
        this.invalidate();
    }

    public void Draw (Path path) {
        Node node = path.getNode();
        Float x1 = this.getX(node.getX());
        Float y1 = this.getY(node.getY());
        node = path.getOpositNode(node);
        Float x2 = this.getX(node.getX());
        Float y2 = this.getY(node.getY());
        _canvas.drawLine(x1, y1, x2, y2, _paint);
        this.invalidate();
    }
}
