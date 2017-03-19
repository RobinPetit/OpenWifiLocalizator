package be.ulb.owl.demo;

import java.util.ArrayList;

import be.ulb.owl.MainActivity;
import be.ulb.owl.graph.Graph;
import be.ulb.owl.graph.Node;
import be.ulb.owl.graph.Plan;
import be.ulb.owl.scanner.Wifi;

/**
 * Make a demonstration (override some method)
 *
 * @author Detobel36
 */

public class GraphDemo extends Graph {

    public GraphDemo(MainActivity main) {
        super(main);
    }


    @Override
    protected Node whereAmI(ArrayList<Wifi> sensed, ArrayList<Plan> searchPlan) {
        Node res = null;
        if(MainActivity.isDemo()) {
            return getCampus("Solbosch").getNode(1);
        }

        return res;
    }

}
