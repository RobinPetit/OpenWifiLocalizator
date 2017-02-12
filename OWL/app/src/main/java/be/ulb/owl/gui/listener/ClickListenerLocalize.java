package be.ulb.owl.gui.listener;

import android.view.View;

import be.ulb.owl.MainActivity;
import be.ulb.owl.graph.Graph;

/**
 * Created by Detobel36
 */

public class ClickListenerLocalize implements View.OnClickListener {

    private final Graph _graph;


    public ClickListenerLocalize(Graph graph) {
        _graph = graph;
    }


    @Override
    public void onClick(View view) {
        // TODO detobel36 refresh localisation !
//        _graph.localize();
    }

}
