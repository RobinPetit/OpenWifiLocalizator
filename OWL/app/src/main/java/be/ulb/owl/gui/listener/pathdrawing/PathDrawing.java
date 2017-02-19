package be.ulb.owl.gui.listener.pathdrawing;

import java.util.List;

import be.ulb.owl.graph.Path;
import be.ulb.owl.gui.FloatCouple;

/**
 * Created by robin on 15/02/17.
 */

public interface PathDrawing {
    void drawPath(Float x1, Float y1, Float x3, Float y2);

    void drawPath(List<FloatCouple> paths);
}
