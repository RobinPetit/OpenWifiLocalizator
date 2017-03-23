package be.ulb.owl.gui;

/**
 * Created by hoornaert on 11.12.16.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.List;

import be.ulb.owl.graph.Node;
import be.ulb.owl.graph.Path;
import be.ulb.owl.graph.Plan;
import be.ulb.owl.gui.pathdrawing.HermitianCubicSplinePathDrawer;
import be.ulb.owl.gui.pathdrawing.PathDrawer;

public class DrawView extends android.support.v7.widget.AppCompatImageView {
    private static final Integer NODE_RADIUS = 10;  // in pixels
    private static final Integer NODE_COLOR = Color.rgb(0xeb, 0x9c, 0x31);
    private static final Integer EDGE_COLOR = Color.BLUE;
    private static final Integer DEFAULT_COLOR = NODE_COLOR;

    private Canvas _canvas;
    private Paint _paint = new Paint();
    private PathDrawer _pathDrawer;
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
        // _pathDrawer = new LinePathDrawer(EDGE_COLOR, _canvas);
        _pathDrawer = new HermitianCubicSplinePathDrawer(EDGE_COLOR, canvas);
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

    public Float[] draw(Node node) {
        _paint.setColor(NODE_COLOR);
        Float x = projectOnX(node.getXOnPlan());
        Float y = projectOnY(node.getYOnPlan());
        _canvas.drawCircle(x, y, NODE_RADIUS, _paint);

        this.invalidate();

        return new Float[] {x, y};
    }

    public void draw(List<Path> pathList, Plan currentPlan) {
        ArrayList<FloatCouple> nodesList = new ArrayList<>();
        if(pathList.size() < 1)
            return;

        Node node = pathList.get(0).getNode();
        if(pathList.size() == 1) {
            nodesList.add(nodeToFloatCouple(node));
            nodesList.add(nodeToFloatCouple(pathList.get(0).getOppositeNodeOf(node)));

        } else {
            if (pathList.get(1).containsNode(node)) {
                node = pathList.get(0).getOppositeNodeOf(node);
            }

            Node lastNodeToDraw = null;
            for (Path path : pathList) {
                if(node.getParentPlan() == currentPlan) {
                    nodesList.add(nodeToFloatCouple(node));
                    lastNodeToDraw = node;
                }
                node = path.getOppositeNodeOf(node);
            }
            if(node.hasNeighbour(lastNodeToDraw)) {
                nodesList.add(nodeToFloatCouple(node));
            }
        }
        _pathDrawer.drawPath(nodesList);
    }

    public FloatCouple nodeToFloatCouple(Node node) {
        return new FloatCouple(projectOnX(node.getXOnPlan()), projectOnY(node.getYOnPlan()));
    }
}
